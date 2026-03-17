"""
ActionCam Emulator — Multi-brand action camera emulator for testing.
Emulates GoPro, Insta360, DJI cameras via BLE + WiFi/HTTP.
"""

import argparse
import asyncio
import sys
from pathlib import Path

from core.profile_loader import load_profile
from core.camera_state import CameraState


def parse_args():
    parser = argparse.ArgumentParser(
        description="ActionCam Emulator — Simulate action cameras for app testing"
    )
    parser.add_argument(
        "profile",
        nargs="?",
        default="gopro_hero12",
        help="Camera profile name (without .yaml extension). Default: gopro_hero12",
    )
    parser.add_argument(
        "--list", "-l",
        action="store_true",
        help="List available camera profiles",
    )
    parser.add_argument(
        "--no-tui",
        action="store_true",
        help="Disable terminal UI dashboard",
    )
    parser.add_argument(
        "--no-ble",
        action="store_true",
        help="Disable BLE server (HTTP only, useful on Windows where BLE advertising is blocked)",
    )
    return parser.parse_args()


def list_profiles():
    profiles_dir = Path(__file__).parent / "config" / "profiles"
    profiles = sorted(profiles_dir.glob("*.yaml"))
    if not profiles:
        print("No profiles found in config/profiles/")
        return
    print("Available camera profiles:")
    for p in profiles:
        print(f"  - {p.stem}")


async def run_emulator(profile_name: str, use_tui: bool = True, no_ble: bool = False):
    profile = load_profile(profile_name)
    state = CameraState(profile)

    print(f"Loaded profile: {profile.brand} {profile.model}")
    print(f"  Serial: {profile.serial}")
    print(f"  Firmware: {profile.firmware}")
    print(f"  Brand: {profile.brand}")

    if profile.brand == "gopro":
        from ble.ble_server import BleServer
        from ble.gopro.gatt_services import GoProGattHandler

        handler = GoProGattHandler(state, profile)
        server = BleServer(profile, handler)

        if use_tui:
            from ui.dashboard import Dashboard
            dashboard = Dashboard(state, profile)
            await asyncio.gather(
                server.start(),
                dashboard.run(),
            )
        else:
            print("BLE server starting... Press Ctrl+C to stop.")
            await server.start()

    elif profile.brand == "insta360":
        from wifi.insta360.http_server import OscHttpServer

        http_server = OscHttpServer(state, profile, port=8080)

        print(f"  WiFi SSID  : {profile.wifi.ssid}")
        print(f"  Camera IP  : {profile.wifi.camera_ip}:{profile.wifi.http_port}")
        print(f"  HTTP OSC   : http://0.0.0.0:8080/osc/info")
        print(f"  ADB tip    : adb reverse tcp:80 tcp:8080")

        tasks = [http_server.start()]

        if not no_ble:
            from ble.ble_server import BleServer
            from ble.insta360.gatt_services import Insta360GattHandler
            handler = Insta360GattHandler(state, profile)
            ble_server = BleServer(profile, handler)
            tasks.insert(0, ble_server.start())
        else:
            print("[BLE] BLE disabled (--no-ble). HTTP only.")

        if use_tui:
            from ui.dashboard import Dashboard
            dashboard = Dashboard(state, profile)
            tasks.append(dashboard.run())

        mode = "HTTP only" if no_ble else "BLE + HTTP"
        print(f"{mode} server starting... Press Ctrl+C to stop.")
        await asyncio.gather(*tasks)
    else:
        print(f"Unknown brand: {profile.brand}")
        sys.exit(1)


def main():
    args = parse_args()

    if args.list:
        list_profiles()
        return

    try:
        asyncio.run(run_emulator(args.profile, use_tui=not args.no_tui, no_ble=args.no_ble))
    except KeyboardInterrupt:
        print("\nEmulator stopped.")


if __name__ == "__main__":
    main()
