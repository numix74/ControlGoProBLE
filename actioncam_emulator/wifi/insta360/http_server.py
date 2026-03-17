"""
Insta360 HTTP OSC emulator — FastAPI server on port 8080.

Protocol: Open Spherical Camera (OSC) API, standard + Insta360 extensions.
Reference: https://developers.google.com/streetview/open-spherical-camera/reference

Usage with ADB reverse (Android phone as PC's WiFi router):
    adb reverse tcp:80 tcp:8080
    → Android HTTP calls to 127.0.0.1:80 are forwarded to this server on port 8080.

All incoming requests are logged at INFO level — essential for discovering
what the Insta360 SDK actually calls (black-box reverse engineering by observation).
"""

import asyncio
import logging
import time
import uuid
from typing import Any

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from core.camera_state import CameraState
from core.profile_loader import CameraProfile

logger = logging.getLogger(__name__)


class OscHttpServer:
    """
    FastAPI server implementing the OSC API for Insta360 emulation.

    Shared state with the BLE server via CameraState — changes made
    over HTTP (start/stop capture) are reflected in the TUI dashboard.
    """

    def __init__(self, state: CameraState, profile: CameraProfile, port: int = 8080):
        self.state = state
        self.profile = profile
        self.port = port
        self._session_id = f"SID_{uuid.uuid4().hex[:8].upper()}"
        self._fingerprint = f"FP_{int(time.time())}"
        self._app = self._build_app()

    # ------------------------------------------------------------------ #
    #  FastAPI app                                                         #
    # ------------------------------------------------------------------ #

    def _build_app(self) -> FastAPI:
        app = FastAPI(title="Insta360 OSC Emulator", docs_url=None, redoc_url=None)

        # --- logging middleware ---
        @app.middleware("http")
        async def log_all(request: Request, call_next):
            body = await request.body()
            method = request.method
            path = request.url.path
            if body:
                logger.info("[OSC] %s %s  body=%s", method, path,
                            body.decode("utf-8", errors="replace"))
            else:
                logger.info("[OSC] %s %s", method, path)
            response = await call_next(request)
            logger.debug("[OSC] → %d", response.status_code)
            return response

        # --- standard OSC endpoints ---
        @app.get("/osc/info")
        async def osc_info():
            return self._make_info()

        @app.get("/osc/state")
        async def osc_state():
            return self._make_state()

        @app.post("/osc/commands/execute")
        async def osc_execute(request: Request):
            try:
                payload = await request.json()
            except Exception:
                return JSONResponse({"error": "invalid_json"}, status_code=400)
            return await self._dispatch(payload)

        @app.get("/osc/commands/status")
        async def osc_status(id: str = ""):
            # Long-running commands — we always complete immediately
            return {"id": id, "name": "camera._unknown", "state": "done"}

        # --- catch-all: log unknown paths (SDK discovery) ---
        @app.api_route("/{path:path}", methods=["GET", "POST", "PUT", "DELETE"])
        async def catch_all(path: str, request: Request):
            body = b""
            try:
                body = await request.body()
            except Exception:
                pass
            logger.warning("[OSC] UNHANDLED %s /%s  body=%r",
                           request.method, path, body)
            return JSONResponse(
                {"error": "notFound", "message": f"/{path} not implemented"},
                status_code=404,
            )

        return app

    # ------------------------------------------------------------------ #
    #  OSC state builders                                                  #
    # ------------------------------------------------------------------ #

    def _make_info(self) -> dict:
        s, p = self.state, self.profile
        return {
            "manufacturer": "Arashi Vision",
            "model": p.model,
            "serialNumber": p.serial,
            "firmwareVersion": p.firmware,
            "supportedApis": [
                "/osc/info",
                "/osc/state",
                "/osc/commands/execute",
                "/osc/commands/status",
            ],
            "_captureStatus": "video" if s.is_recording else "idle",
            "_batteryLevel": round(s.battery_level / 100, 2),
            "_wifiPassword": p.wifi.password,
        }

    def _make_state(self) -> dict:
        s, p = self.state, self.profile
        record_secs = s.get_recording_duration_ms() // 1000 if s.is_recording else 0
        return {
            "fingerprint": self._fingerprint,
            "state": {
                "sessionId": self._session_id,
                "_captureStatus": "video" if s.is_recording else "idle",
                "_batteryLevel": round(s.battery_level / 100, 2),
                "_batteryState": "charging" if s.is_charging else "notCharging",
                "_recordTime": record_secs,
                "_remainingVideoSeconds": s.video_remaining_sec,
                "_remainingSpace": s.sd_remaining_kb * 1024,
                "_totalSpace": s.sd_capacity_kb * 1024,
                "_storageUri": f"http://{p.wifi.camera_ip}/DCIM",
                "_wifiPassword": p.wifi.password,
            },
        }

    # ------------------------------------------------------------------ #
    #  OSC command dispatcher                                              #
    # ------------------------------------------------------------------ #

    async def _dispatch(self, payload: dict) -> dict:
        name = payload.get("name", "")
        params = payload.get("parameters", {})
        logger.info("[OSC] execute  name=%s  params=%s", name, params)
        self.state.log_command(f"OSC: {name}")

        handlers = {
            "camera.takePicture":                    self._cmd_take_picture,
            "camera.startCapture":                   self._cmd_start_capture,
            "camera.stopCapture":                    self._cmd_stop_capture,
            "camera.getOptions":                     self._cmd_get_options,
            "camera.setOptions":                     self._cmd_set_options,
            "camera.startSession":                   self._cmd_start_session,
            "camera.closeSession":                   self._cmd_close_session,
            "camera.listFiles":                      self._cmd_list_files,
            "camera._getLivePreview":                self._cmd_live_preview,
            "camera.updateFirmware":                 self._cmd_disabled,
            # Insta360 proprietary commands (names speculative — verified by SDK logs)
            "camera._insta360FetchSupportConfig":    self._cmd_support_config,
            "camera._insta360SetCaptureMode":        self._cmd_set_capture_mode,
            "camera._insta360GetCaptureMode":        self._cmd_get_capture_mode,
            "camera._insta360BatteryStatus":         self._cmd_battery_status,
            "camera._insta360StorageStatus":         self._cmd_storage_status,
        }
        handler = handlers.get(name, self._cmd_unknown)
        return await handler(name, params)

    # ------------------------------------------------------------------ #
    #  Individual command handlers                                         #
    # ------------------------------------------------------------------ #

    async def _cmd_take_picture(self, name, params) -> dict:
        self.state.photos_total += 1
        self.state.photos_remaining = max(0, self.state.photos_remaining - 1)
        fid = f"IMG_{self.state.photos_total:04d}.insp"
        logger.info("[OSC] Photo taken — %s", fid)
        return {
            "name": name,
            "state": "done",
            "results": {
                "fileUri": f"http://{self.profile.wifi.camera_ip}/DCIM/100INSTA/{fid}",
                "_fileId": fid,
            },
        }

    async def _cmd_start_capture(self, name, params) -> dict:
        if self.state.is_recording:
            return {"name": name, "state": "error",
                    "error": {"code": "invalidParameterValue",
                              "message": "Already recording"}}
        self.state.is_recording = True
        self.state.recording_start_time = time.time()
        await self.state.event_bus.emit("state_changed", status_id=10)
        logger.info("[OSC] Recording STARTED")
        return {"name": name, "state": "done"}

    async def _cmd_stop_capture(self, name, params) -> dict:
        if not self.state.is_recording:
            return {"name": name, "state": "error",
                    "error": {"code": "invalidParameterValue",
                              "message": "Not recording"}}
        self.state.is_recording = False
        self.state.recording_start_time = 0.0
        self.state.videos_count += 1
        await self.state.event_bus.emit("state_changed", status_id=10)
        fid = f"VID_{self.state.videos_count:04d}.insv"
        logger.info("[OSC] Recording STOPPED — %s", fid)
        return {
            "name": name,
            "state": "done",
            "results": {
                "fileUri": f"http://{self.profile.wifi.camera_ip}/DCIM/100INSTA/{fid}",
            },
        }

    async def _cmd_get_options(self, name, params) -> dict:
        option_names = params.get("optionNames", [])
        options = {opt: self._option_value(opt) for opt in option_names}
        logger.info("[OSC] getOptions: %s", option_names)
        return {"name": name, "state": "done", "results": {"options": options}}

    def _option_value(self, opt: str) -> Any:
        """Return a sensible emulated value for any OSC option name."""
        s, p = self.state, self.profile
        _map = {
            # Standard OSC options
            "captureMode": "video",
            "captureModeSupport": ["video", "image", "interval"],
            "exposureProgram": 2,
            "exposureProgramSupport": [1, 2, 4, 9],
            "iso": 100,
            "isoSupport": [100, 200, 400, 800, 1600, 3200],
            "shutterSpeed": 1 / 120,
            "shutterSpeedSupport": [1 / 8000, 1 / 4000, 1 / 2000, 1 / 1000,
                                     1 / 500, 1 / 250, 1 / 120, 1 / 60, 1 / 30],
            "whiteBalance": "auto",
            "whiteBalanceSupport": ["auto", "daylight", "shade", "cloudy",
                                    "incandescent", "fluorescent"],
            "exposureCompensation": 0.0,
            "exposureCompensationSupport": [-2.0, -1.7, -1.3, -1.0, -0.7, -0.3,
                                             0.0, 0.3, 0.7, 1.0, 1.3, 1.7, 2.0],
            "fileFormat": {"type": "jpeg", "width": 11264, "height": 5632},
            "fileFormatSupport": [
                {"type": "jpeg", "width": 11264, "height": 5632},
                {"type": "jpeg", "width": 7680, "height": 3840},
            ],
            "dateTimeZone": time.strftime("%Y:%m:%d %H:%M:%S+00:00"),
            "gpsInfo": {"lat": 0.0, "lng": 0.0},
            "sleepDelay": 60,
            "sleepDelaySupport": [0, 60, 180, 600],
            "offDelay": 600,
            # Storage / battery
            "totalSpace": s.sd_capacity_kb * 1024,
            "remainingSpace": s.sd_remaining_kb * 1024,
            "remainingPictures": s.photos_remaining,
            "batteryLevel": round(s.battery_level / 100, 2),
            # Insta360-specific extensions
            "_captureStatus": "video" if s.is_recording else "idle",
            "_batteryState": "charging" if s.is_charging else "notCharging",
            "_wifiPassword": p.wifi.password,
            "_manufacturer": "Arashi Vision",
            "_model": p.model,
            "_serialNumber": p.serial,
            "_firmwareVersion": p.firmware,
            "_boardType": p.board_type,
            # Insta360 capture-mode details (dynamic from state)
            "_captureMode": self.state.current_capture_mode_id,
            "_captureModes": [m.id for m in self.profile.capture.modes],
            "_videoResolution": 167,  # 7680×3840@30fps (see proto.json)
            "_videoFps": 30,
            "_videoEncode": 0,        # 0 = H264
            "_photoResolution": 306,  # 11264×5632@30fps
            "_supportConfig": True,
        }
        return _map.get(opt)  # None = unknown option — SDK will handle it

    async def _cmd_set_options(self, name, params) -> dict:
        options = params.get("options", {})
        logger.info("[OSC] setOptions: %s", options)
        # Standard OSC captureMode (string)
        if "captureMode" in options:
            mode_str = options["captureMode"]
            mode_map = {"video": 0, "image": 1, "interval": 2}
            sid = mode_map.get(mode_str, 0)
            self.state.settings[1] = sid
        # Insta360 proprietary: _captureMode (int mode id)
        if "_captureMode" in options:
            mode_id = options["_captureMode"]
            valid_ids = {m.id for m in self.profile.capture.modes}
            if mode_id in valid_ids:
                self.state.current_capture_mode_id = mode_id
                logger.info("[OSC] setOptions _captureMode → %d", mode_id)
        return {"name": name, "state": "done"}

    async def _cmd_start_session(self, name, params) -> dict:
        return {
            "name": name,
            "state": "done",
            "results": {"sessionId": self._session_id, "timeout": 180},
        }

    async def _cmd_close_session(self, name, params) -> dict:
        return {"name": name, "state": "done"}

    async def _cmd_list_files(self, name, params) -> dict:
        return {
            "name": name,
            "state": "done",
            "results": {
                "entries": [],
                "totalEntries": 0,
            },
        }

    async def _cmd_live_preview(self, name, params) -> dict:
        p = self.profile
        return {
            "name": name,
            "state": "done",
            "results": {
                "previewFormat": {"width": 1920, "height": 960, "framerate": 30},
                "_previewUrl": f"rtsp://{p.wifi.camera_ip}:554/live",
            },
        }

    async def _cmd_support_config(self, name, params) -> dict:
        """
        Response to camera._insta360FetchSupportConfig (name speculative).

        The SDK uses this to populate:
          - instaCameraManager.supportCaptureMode  (list of CaptureMode)
          - instaCameraManager.supportConfig       (supportNewCaptureControlFlow flag)
          - getSupportRecordResolutionList() etc.

        Format is reverse-engineered / best-guess — verify with real device logs.
        NOTE: if the SDK ignores these results, check the WARNING logs for the real
        command name called and adjust the handlers dict above.
        """
        p = self.profile
        modes = [
            {
                "mode": m.id,
                "name": m.name,
                "isVideoMode": m.is_video,
                "isPhotoMode": m.is_photo,
                "isLiveMode": m.is_live,
            }
            for m in p.capture.modes
        ]
        return {
            "name": name,
            "state": "done",
            "results": {
                "supportNewCaptureControlFlow": p.capture.new_capture_flow,
                "supportCaptureModes": modes,
                "currentCaptureMode": self.state.current_capture_mode_id,
            },
        }

    async def _cmd_set_capture_mode(self, name, params) -> dict:
        """camera._insta360SetCaptureMode — switch active capture mode."""
        mode_id = params.get("captureMode", params.get("mode", -1))
        valid_ids = {m.id for m in self.profile.capture.modes}
        if mode_id not in valid_ids:
            return {
                "name": name, "state": "error",
                "error": {"code": "invalidParameterValue",
                          "message": f"Unknown capture mode: {mode_id}"},
            }
        self.state.current_capture_mode_id = mode_id
        logger.info("[OSC] capture mode → %d", mode_id)
        return {"name": name, "state": "done"}

    async def _cmd_get_capture_mode(self, name, params) -> dict:
        """camera._insta360GetCaptureMode — return current capture mode."""
        mode = next(
            (m for m in self.profile.capture.modes
             if m.id == self.state.current_capture_mode_id),
            None,
        )
        return {
            "name": name,
            "state": "done",
            "results": {
                "captureMode": self.state.current_capture_mode_id,
                "name": mode.name if mode else "",
                "isVideoMode": mode.is_video if mode else False,
                "isPhotoMode": mode.is_photo if mode else False,
            },
        }

    async def _cmd_battery_status(self, name, params) -> dict:
        """camera._insta360BatteryStatus — return battery state."""
        s = self.state
        return {
            "name": name,
            "state": "done",
            "results": {
                "batteryLevel": s.battery_level,
                "batteryLevelPercent": round(s.battery_level / 100, 2),
                "isCharging": s.is_charging,
                "batteryState": "charging" if s.is_charging else "notCharging",
            },
        }

    async def _cmd_storage_status(self, name, params) -> dict:
        """camera._insta360StorageStatus — return storage state."""
        s = self.state
        return {
            "name": name,
            "state": "done",
            "results": {
                "totalSpace": s.sd_capacity_kb * 1024,
                "freeSpace": s.sd_remaining_kb * 1024,
                "remainingVideoSeconds": s.video_remaining_sec,
                "remainingPictures": s.photos_remaining,
            },
        }

    async def _cmd_disabled(self, name, params) -> dict:
        return {
            "name": name,
            "state": "error",
            "error": {"code": "disabledCommand", "message": "Not supported by emulator"},
        }

    async def _cmd_unknown(self, name, params) -> dict:
        logger.warning("[OSC] UNKNOWN command %r params=%s — returning generic success", name, params)
        return {"name": name, "state": "done", "results": {}}

    # ------------------------------------------------------------------ #
    #  Run                                                                 #
    # ------------------------------------------------------------------ #

    async def start(self, host: str = "0.0.0.0", port: int = None):
        port = port or self.port
        print(f"[OSC] HTTP server: http://0.0.0.0:{port}/osc/info")
        print(f"[OSC] ADB tip   : adb reverse tcp:80 tcp:{port}")
        config = uvicorn.Config(
            app=self._app,
            host=host,
            port=port,
            log_level="warning",   # suppress uvicorn own logs; we log everything ourselves
            access_log=False,
        )
        server = uvicorn.Server(config)
        await server.serve()
