"""
Tests for the Insta360 HTTP OSC emulator.

Uses httpx.AsyncClient with the FastAPI app directly (no network socket).
Covers all standard OSC endpoints + key command handlers.
"""

import pytest
import pytest_asyncio
from httpx import AsyncClient, ASGITransport

from core.profile_loader import load_profile
from core.camera_state import CameraState
from wifi.insta360.http_server import OscHttpServer


# ------------------------------------------------------------------ #
#  Fixture                                                            #
# ------------------------------------------------------------------ #

@pytest.fixture
def ctx():
    profile = load_profile("insta360_x4")
    state = CameraState(profile)
    server = OscHttpServer(state, profile, port=8080)
    return server, state, profile


@pytest_asyncio.fixture
async def client(ctx):
    server, state, profile = ctx
    transport = ASGITransport(app=server._app)
    async with AsyncClient(transport=transport, base_url="http://testserver") as c:
        yield c, state, profile


# ------------------------------------------------------------------ #
#  /osc/info                                                          #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_osc_info_fields(client):
    c, state, profile = client
    r = await c.get("/osc/info")
    assert r.status_code == 200
    data = r.json()
    assert data["manufacturer"] == "Arashi Vision"
    assert data["model"] == profile.model
    assert data["serialNumber"] == profile.serial
    assert data["firmwareVersion"] == profile.firmware
    assert "/osc/commands/execute" in data["supportedApis"]


@pytest.mark.asyncio
async def test_osc_info_capture_status_idle(client):
    c, state, profile = client
    r = await c.get("/osc/info")
    assert r.json()["_captureStatus"] == "idle"


@pytest.mark.asyncio
async def test_osc_info_capture_status_video(client):
    c, state, profile = client
    import time
    state.is_recording = True
    state.recording_start_time = time.time()
    r = await c.get("/osc/info")
    assert r.json()["_captureStatus"] == "video"


# ------------------------------------------------------------------ #
#  /osc/state                                                         #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_osc_state_structure(client):
    c, state, profile = client
    r = await c.get("/osc/state")
    assert r.status_code == 200
    data = r.json()
    assert "fingerprint" in data
    assert "state" in data
    s = data["state"]
    assert "sessionId" in s
    assert "_captureStatus" in s
    assert "_batteryLevel" in s
    assert "_remainingSpace" in s


@pytest.mark.asyncio
async def test_osc_state_battery(client):
    c, state, profile = client
    state.battery_level = 75
    r = await c.get("/osc/state")
    assert r.json()["state"]["_batteryLevel"] == pytest.approx(0.75)


@pytest.mark.asyncio
async def test_osc_state_storage(client):
    c, state, profile = client
    r = await c.get("/osc/state")
    s = r.json()["state"]
    assert s["_remainingSpace"] == state.sd_remaining_kb * 1024
    assert s["_totalSpace"] == state.sd_capacity_kb * 1024


# ------------------------------------------------------------------ #
#  /osc/commands/execute — start/stop capture                         #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_start_capture(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.startCapture", "parameters": {}})
    assert r.status_code == 200
    assert r.json()["state"] == "done"
    assert state.is_recording is True


@pytest.mark.asyncio
async def test_start_capture_already_recording(client):
    c, state, profile = client
    import time
    state.is_recording = True
    state.recording_start_time = time.time()
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.startCapture", "parameters": {}})
    assert r.json()["state"] == "error"
    assert "Already recording" in r.json()["error"]["message"]


@pytest.mark.asyncio
async def test_stop_capture(client):
    c, state, profile = client
    import time
    state.is_recording = True
    state.recording_start_time = time.time()
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.stopCapture", "parameters": {}})
    assert r.status_code == 200
    assert r.json()["state"] == "done"
    assert state.is_recording is False
    assert "fileUri" in r.json()["results"]


@pytest.mark.asyncio
async def test_stop_capture_not_recording(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.stopCapture", "parameters": {}})
    assert r.json()["state"] == "error"
    assert "Not recording" in r.json()["error"]["message"]


@pytest.mark.asyncio
async def test_capture_round_trip(client):
    """Start then stop — video counter increments."""
    c, state, profile = client
    assert state.videos_count == 0
    await c.post("/osc/commands/execute",
                 json={"name": "camera.startCapture", "parameters": {}})
    await c.post("/osc/commands/execute",
                 json={"name": "camera.stopCapture", "parameters": {}})
    assert state.videos_count == 1
    assert state.is_recording is False


# ------------------------------------------------------------------ #
#  /osc/commands/execute — take picture                               #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_take_picture(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.takePicture", "parameters": {}})
    assert r.status_code == 200
    data = r.json()
    assert data["state"] == "done"
    assert "fileUri" in data["results"]
    assert ".insp" in data["results"]["fileUri"]
    assert state.photos_total == 1


@pytest.mark.asyncio
async def test_take_picture_counter(client):
    c, state, profile = client
    for _ in range(3):
        await c.post("/osc/commands/execute",
                     json={"name": "camera.takePicture", "parameters": {}})
    assert state.photos_total == 3
    assert state.photos_remaining == 9999 - 3


# ------------------------------------------------------------------ #
#  /osc/commands/execute — getOptions                                 #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_get_options_known(client):
    c, state, profile = client
    opts = ["captureMode", "batteryLevel", "_model", "_serialNumber"]
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.getOptions",
                           "parameters": {"optionNames": opts}})
    assert r.status_code == 200
    data = r.json()
    assert data["state"] == "done"
    options = data["results"]["options"]
    assert options["captureMode"] == "video"
    assert options["batteryLevel"] == pytest.approx(state.battery_level / 100)
    assert options["_model"] == profile.model
    assert options["_serialNumber"] == profile.serial


@pytest.mark.asyncio
async def test_get_options_unknown_returns_none(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.getOptions",
                           "parameters": {"optionNames": ["_unknownOption42"]}})
    assert r.json()["results"]["options"]["_unknownOption42"] is None


@pytest.mark.asyncio
async def test_get_options_empty_list(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.getOptions",
                           "parameters": {"optionNames": []}})
    assert r.json()["state"] == "done"
    assert r.json()["results"]["options"] == {}


# ------------------------------------------------------------------ #
#  /osc/commands/execute — setOptions                                 #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_set_options(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.setOptions",
                           "parameters": {"options": {"captureMode": "image"}}})
    assert r.json()["state"] == "done"
    assert state.settings[1] == 1  # 1 = image in our mapping


# ------------------------------------------------------------------ #
#  /osc/commands/execute — session                                    #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_start_session(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.startSession", "parameters": {}})
    assert r.status_code == 200
    data = r.json()
    assert data["state"] == "done"
    assert "sessionId" in data["results"]
    assert data["results"]["timeout"] == 180


@pytest.mark.asyncio
async def test_close_session(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.closeSession", "parameters": {}})
    assert r.json()["state"] == "done"


# ------------------------------------------------------------------ #
#  /osc/commands/execute — misc                                       #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_unknown_command_returns_success(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360FetchSupportConfig",
                           "parameters": {}})
    assert r.status_code == 200
    assert r.json()["state"] == "done"


@pytest.mark.asyncio
async def test_disabled_command(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.updateFirmware", "parameters": {}})
    assert r.json()["state"] == "error"
    assert r.json()["error"]["code"] == "disabledCommand"


@pytest.mark.asyncio
async def test_invalid_json_body(client):
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     content=b"not json",
                     headers={"content-type": "application/json"})
    assert r.status_code == 400


# ------------------------------------------------------------------ #
#  /osc/commands/status                                               #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_command_status(client):
    c, state, profile = client
    r = await c.get("/osc/commands/status", params={"id": "abc123"})
    assert r.status_code == 200
    assert r.json()["state"] == "done"


# ------------------------------------------------------------------ #
#  Catch-all 404                                                      #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_unknown_path_404(client):
    c, state, profile = client
    r = await c.get("/unknown/path")
    assert r.status_code == 404


@pytest.mark.asyncio
async def test_state_reflects_in_info_after_record(client):
    """Cross-check: after startCapture, /osc/info shows 'video'."""
    c, state, profile = client
    await c.post("/osc/commands/execute",
                 json={"name": "camera.startCapture", "parameters": {}})
    info = await c.get("/osc/info")
    assert info.json()["_captureStatus"] == "video"
    st = await c.get("/osc/state")
    assert st.json()["state"]["_captureStatus"] == "video"


# ------------------------------------------------------------------ #
#  Insta360 proprietary commands — capture mode                       #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_support_config_structure(client):
    """_insta360FetchSupportConfig returns modes list + new_capture_flow flag."""
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360FetchSupportConfig",
                           "parameters": {}})
    assert r.status_code == 200
    data = r.json()
    assert data["state"] == "done"
    results = data["results"]
    assert "supportNewCaptureControlFlow" in results
    assert isinstance(results["supportNewCaptureControlFlow"], bool)
    assert "supportCaptureModes" in results
    assert isinstance(results["supportCaptureModes"], list)
    assert len(results["supportCaptureModes"]) > 0


@pytest.mark.asyncio
async def test_support_config_x4_new_flow(client):
    """X4 profile should report new_capture_flow = True."""
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360FetchSupportConfig",
                           "parameters": {}})
    # X4 fixture → new_capture_flow is True
    assert r.json()["results"]["supportNewCaptureControlFlow"] is True


@pytest.mark.asyncio
async def test_support_config_x3_old_flow():
    """X3 profile should report new_capture_flow = False."""
    from core.profile_loader import load_profile
    from core.camera_state import CameraState
    from wifi.insta360.http_server import OscHttpServer
    from httpx import AsyncClient, ASGITransport

    profile = load_profile("insta360_x3")
    state = CameraState(profile)
    server = OscHttpServer(state, profile, port=8080)
    transport = ASGITransport(app=server._app)
    async with AsyncClient(transport=transport, base_url="http://testserver") as c:
        r = await c.post("/osc/commands/execute",
                         json={"name": "camera._insta360FetchSupportConfig",
                               "parameters": {}})
        assert r.json()["results"]["supportNewCaptureControlFlow"] is False


@pytest.mark.asyncio
async def test_support_config_modes_have_required_fields(client):
    """Each mode in supportCaptureModes has id, name, isVideoMode, isPhotoMode."""
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360FetchSupportConfig",
                           "parameters": {}})
    for mode in r.json()["results"]["supportCaptureModes"]:
        assert "mode" in mode
        assert "name" in mode
        assert "isVideoMode" in mode
        assert "isPhotoMode" in mode
        assert "isLiveMode" in mode


@pytest.mark.asyncio
async def test_set_capture_mode_valid(client):
    """_insta360SetCaptureMode with a valid mode id updates state."""
    c, state, profile = client
    valid_id = profile.capture.modes[1].id
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360SetCaptureMode",
                           "parameters": {"captureMode": valid_id}})
    assert r.json()["state"] == "done"
    assert state.current_capture_mode_id == valid_id


@pytest.mark.asyncio
async def test_set_capture_mode_invalid(client):
    """_insta360SetCaptureMode with unknown mode id returns error."""
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360SetCaptureMode",
                           "parameters": {"captureMode": 9999}})
    assert r.json()["state"] == "error"
    assert r.json()["error"]["code"] == "invalidParameterValue"


@pytest.mark.asyncio
async def test_get_capture_mode_default(client):
    """_insta360GetCaptureMode returns the initial mode."""
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360GetCaptureMode",
                           "parameters": {}})
    assert r.json()["state"] == "done"
    results = r.json()["results"]
    assert results["captureMode"] == profile.capture.modes[0].id
    assert results["name"] == profile.capture.modes[0].name


@pytest.mark.asyncio
async def test_get_capture_mode_after_set(client):
    """After setCaptureMode, getCaptureMode reflects new state."""
    c, state, profile = client
    target = profile.capture.modes[-1]
    await c.post("/osc/commands/execute",
                 json={"name": "camera._insta360SetCaptureMode",
                       "parameters": {"captureMode": target.id}})
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360GetCaptureMode",
                           "parameters": {}})
    assert r.json()["results"]["captureMode"] == target.id
    assert r.json()["results"]["name"] == target.name


@pytest.mark.asyncio
async def test_get_options_capture_mode_dynamic(client):
    """getOptions _captureMode reflects current state after mode change."""
    c, state, profile = client
    new_mode_id = profile.capture.modes[2].id
    state.current_capture_mode_id = new_mode_id
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.getOptions",
                           "parameters": {"optionNames": ["_captureMode", "_captureModes"]}})
    options = r.json()["results"]["options"]
    assert options["_captureMode"] == new_mode_id
    assert new_mode_id in options["_captureModes"]


@pytest.mark.asyncio
async def test_set_options_capture_mode_proprietary(client):
    """setOptions with _captureMode (int) updates state."""
    c, state, profile = client
    new_mode_id = profile.capture.modes[1].id
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera.setOptions",
                           "parameters": {"options": {"_captureMode": new_mode_id}}})
    assert r.json()["state"] == "done"
    assert state.current_capture_mode_id == new_mode_id


# ------------------------------------------------------------------ #
#  Insta360 proprietary commands — battery / storage                  #
# ------------------------------------------------------------------ #

@pytest.mark.asyncio
async def test_battery_status(client):
    """_insta360BatteryStatus returns battery level and charging state."""
    c, state, profile = client
    state.battery_level = 42
    state.is_charging = True
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360BatteryStatus",
                           "parameters": {}})
    assert r.json()["state"] == "done"
    results = r.json()["results"]
    assert results["batteryLevel"] == 42
    assert results["isCharging"] is True
    assert results["batteryState"] == "charging"


@pytest.mark.asyncio
async def test_storage_status(client):
    """_insta360StorageStatus returns space and remaining info."""
    c, state, profile = client
    r = await c.post("/osc/commands/execute",
                     json={"name": "camera._insta360StorageStatus",
                           "parameters": {}})
    assert r.json()["state"] == "done"
    results = r.json()["results"]
    assert results["totalSpace"] == state.sd_capacity_kb * 1024
    assert results["freeSpace"] == state.sd_remaining_kb * 1024
    assert "remainingVideoSeconds" in results


# ------------------------------------------------------------------ #
#  X3 profile loading                                                 #
# ------------------------------------------------------------------ #

def test_x3_profile_loads():
    """X3 profile loads correctly with capture config."""
    from core.profile_loader import load_profile
    profile = load_profile("insta360_x3")
    assert profile.brand == "insta360"
    assert profile.model == "X3"
    assert profile.capture.new_capture_flow is False
    assert len(profile.capture.modes) >= 4
    # Premier mode : RECORD_NORMAL (noms SDK depuis Insta360SettingsMappings.kt)
    assert profile.capture.modes[0].name == "RECORD_NORMAL"
    assert profile.capture.modes[0].is_video is True


def test_x3_camera_state_init():
    """CameraState initialised from X3 profile has correct defaults."""
    from core.profile_loader import load_profile
    from core.camera_state import CameraState
    profile = load_profile("insta360_x3")
    state = CameraState(profile)
    assert state.new_capture_flow is False
    assert state.current_capture_mode_id == profile.capture.modes[0].id


def test_x4_camera_state_init(ctx):
    """CameraState initialised from X4 profile has correct defaults."""
    _, state, profile = ctx
    assert state.new_capture_flow is True
    assert state.current_capture_mode_id == profile.capture.modes[0].id
