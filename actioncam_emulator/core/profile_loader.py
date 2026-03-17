"""Loads camera profiles from YAML files."""

from pathlib import Path
from typing import Any

import yaml
from pydantic import BaseModel, Field


class BleConfig(BaseModel):
    service_uuid: str = ""
    device_name: str = ""
    ap_ssid: str = ""
    ap_password: str = ""
    company_id: int = 0
    characteristics: dict[str, str] = Field(default_factory=dict)


class WifiConfig(BaseModel):
    ssid: str = ""
    password: str = ""
    camera_ip: str = "192.168.42.1"
    http_port: int = 80


class InitialState(BaseModel):
    battery_level: int = 85
    battery_bars: int = 3
    sd_remaining_kb: int = 60_000_000
    sd_capacity_kb: int = 128_000_000
    sd_status: int = 0
    recording: bool = False
    overheating: bool = False
    system_ready: bool = True
    active_preset: int = 0
    busy: bool = False
    photos_remaining: int = 9999
    photos_total: int = 0
    videos_count: int = 0
    video_remaining_sec: int = 36000
    is_charging: bool = False


class CaptureModeConfig(BaseModel):
    id: int
    name: str
    is_video: bool = False
    is_photo: bool = False
    is_live: bool = False


class CaptureConfig(BaseModel):
    new_capture_flow: bool = True
    modes: list[CaptureModeConfig] = Field(default_factory=list)


class SettingDef(BaseModel):
    default: int = 0
    capabilities: list[int] = Field(default_factory=list)


class PresetDef(BaseModel):
    id: int
    title: str = ""
    title_id: int = 0
    mode: int = 0
    icon: int = 0
    settings: dict[int, int] = Field(default_factory=dict)


class PresetGroupDef(BaseModel):
    id: int
    presets: list[PresetDef] = Field(default_factory=list)


class CameraProfile(BaseModel):
    brand: str
    model: str
    model_id: int = 0
    serial: str = "C0000000000000"
    firmware: str = "1.0.0"
    board_type: str = "0x00"
    schema_version: int = 2

    ble: BleConfig = Field(default_factory=BleConfig)
    wifi: WifiConfig = Field(default_factory=WifiConfig)
    capture: CaptureConfig = Field(default_factory=CaptureConfig)
    initial_state: InitialState = Field(default_factory=InitialState)
    settings: dict[int, SettingDef] = Field(default_factory=dict)
    preset_groups: list[PresetGroupDef] = Field(default_factory=list)

    # Raw extra data from YAML not covered by the model
    extra: dict[str, Any] = Field(default_factory=dict)


def _parse_settings(raw: dict) -> dict[int, SettingDef]:
    result = {}
    for key, val in raw.items():
        setting_id = int(key)
        if isinstance(val, dict):
            result[setting_id] = SettingDef(**val)
        else:
            result[setting_id] = SettingDef(default=int(val))
    return result


def _parse_capture(raw: dict) -> CaptureConfig:
    modes = [CaptureModeConfig(**m) for m in raw.get("modes", [])]
    return CaptureConfig(
        new_capture_flow=raw.get("new_capture_flow", True),
        modes=modes,
    )


def _parse_preset_groups(raw: list) -> list[PresetGroupDef]:
    groups = []
    for group_data in raw:
        presets = []
        for p in group_data.get("presets", []):
            settings = {int(k): int(v) for k, v in p.get("settings", {}).items()}
            presets.append(PresetDef(
                id=p["id"],
                title=p.get("title", ""),
                title_id=p.get("title_id", 0),
                mode=p.get("mode", 0),
                icon=p.get("icon", 0),
                settings=settings,
            ))
        groups.append(PresetGroupDef(id=group_data["id"], presets=presets))
    return groups


def load_profile(name: str) -> CameraProfile:
    """Load a camera profile by name (without .yaml extension)."""
    profiles_dir = Path(__file__).parent.parent / "config" / "profiles"
    path = profiles_dir / f"{name}.yaml"

    if not path.exists():
        raise FileNotFoundError(f"Profile not found: {path}")

    with open(path, "r", encoding="utf-8") as f:
        raw = yaml.safe_load(f)

    ble_raw = raw.pop("ble", {})
    wifi_raw = raw.pop("wifi", {})
    capture_raw = raw.pop("capture", {})
    initial_state_raw = raw.pop("initial_state", {})
    settings_raw = raw.pop("settings", {})
    presets_raw = raw.pop("presets", {})

    ble = BleConfig(**ble_raw) if ble_raw else BleConfig()
    wifi = WifiConfig(**wifi_raw) if wifi_raw else WifiConfig()
    capture = _parse_capture(capture_raw) if capture_raw else CaptureConfig()
    initial_state = InitialState(**initial_state_raw) if initial_state_raw else InitialState()
    settings = _parse_settings(settings_raw) if settings_raw else {}
    preset_groups = _parse_preset_groups(
        presets_raw.get("groups", [])
    ) if presets_raw else []

    known_keys = {"brand", "model", "model_id", "serial", "firmware",
                  "board_type", "schema_version"}
    extra = {k: v for k, v in raw.items() if k not in known_keys}
    profile_data = {k: v for k, v in raw.items() if k in known_keys}

    return CameraProfile(
        **profile_data,
        ble=ble,
        wifi=wifi,
        capture=capture,
        initial_state=initial_state,
        settings=settings,
        preset_groups=preset_groups,
        extra=extra,
    )
