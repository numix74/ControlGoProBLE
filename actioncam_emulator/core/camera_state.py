"""Central camera state machine — brand-agnostic."""

import time
from typing import Any

from core.event_bus import EventBus
from core.profile_loader import CameraProfile


class CameraState:
    """Holds all mutable camera state, driven by a profile."""

    def __init__(self, profile: CameraProfile):
        self.profile = profile
        self.event_bus = EventBus()

        init = profile.initial_state

        # Dynamic state
        self.is_recording: bool = init.recording
        self.recording_start_time: float = 0.0
        self.battery_level: int = init.battery_level
        self.battery_bars: int = init.battery_bars
        self.is_charging: bool = init.is_charging
        self.sd_remaining_kb: int = init.sd_remaining_kb
        self.sd_capacity_kb: int = init.sd_capacity_kb
        self.sd_status: int = init.sd_status
        self.is_overheating: bool = init.overheating
        self.is_system_ready: bool = init.system_ready
        self.is_busy: bool = init.busy
        self.photos_remaining: int = init.photos_remaining
        self.photos_total: int = init.photos_total
        self.videos_count: int = init.videos_count
        self.video_remaining_sec: int = init.video_remaining_sec
        self.active_preset_id: int = init.active_preset
        self.hilight_count: int = 0

        # Insta360 capture mode state
        self.current_capture_mode_id: int = (
            profile.capture.modes[0].id if profile.capture.modes else 0
        )
        self.new_capture_flow: bool = profile.capture.new_capture_flow

        # Settings: setting_id -> current value
        self.settings: dict[int, int] = {}
        for sid, sdef in profile.settings.items():
            self.settings[sid] = sdef.default

        # Capabilities: setting_id -> list of valid values
        self.capabilities: dict[int, list[int]] = {}
        for sid, sdef in profile.settings.items():
            self.capabilities[sid] = list(sdef.capabilities)

        # Registered subscriber IDs for async notifications
        self.registered_status_ids: set[int] = set()
        self.registered_setting_ids: set[int] = set()
        self.registered_capability_ids: set[int] = set()

        # Notification callback (set by BLE server)
        self._notify_callback: Any = None

        # Last command received (for dashboard display)
        self.last_command: str = ""
        self.last_command_time: float = 0.0

    def set_notify_callback(self, callback):
        self._notify_callback = callback

    async def toggle_recording(self) -> bool:
        """Toggle recording state. Returns new state."""
        self.is_recording = not self.is_recording
        if self.is_recording:
            self.recording_start_time = time.time()
        else:
            self.recording_start_time = 0.0
        await self.event_bus.emit("state_changed", status_id=10)
        return self.is_recording

    async def set_setting(self, setting_id: int, value: int) -> bool:
        """Set a setting value. Returns True if valid."""
        if setting_id in self.capabilities:
            if value not in self.capabilities[setting_id]:
                return False
        self.settings[setting_id] = value
        await self.event_bus.emit("setting_changed", setting_id=setting_id)
        return True

    async def load_preset(self, preset_id: int) -> bool:
        """Load a preset by ID. Returns True if found."""
        for group in self.profile.preset_groups:
            for preset in group.presets:
                if preset.id == preset_id:
                    self.active_preset_id = preset_id
                    for sid, val in preset.settings.items():
                        self.settings[sid] = val
                    await self.event_bus.emit("preset_changed", preset_id=preset_id)
                    return True
        return False

    def get_recording_duration_ms(self) -> int:
        if not self.is_recording:
            return 0
        return int((time.time() - self.recording_start_time) * 1000)

    def get_status_value(self, status_id: int) -> int:
        """Get a status value by ID (GoPro status IDs)."""
        mapping = {
            2: self.battery_bars,
            6: int(self.is_overheating),
            8: int(self.is_busy),
            10: int(self.is_recording),
            13: self.get_recording_duration_ms(),
            33: self.sd_status,
            34: self.photos_remaining,
            35: self.video_remaining_sec,
            38: self.photos_total,
            39: self.videos_count,
            54: self.sd_remaining_kb,
            70: self.battery_level,
            82: int(self.is_system_ready),
            97: self.active_preset_id,
            117: self.sd_capacity_kb,
        }
        return mapping.get(status_id, 0)

    def log_command(self, description: str):
        self.last_command = description
        self.last_command_time = time.time()
