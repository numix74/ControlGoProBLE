"""
BLE GATT peripheral server using the bless library.
Advertises camera services and routes writes to the GATT handler.
"""

import asyncio
import logging
from typing import Any

from bless import (
    BlessServer,
    BlessGATTCharacteristic,
    GATTCharacteristicProperties,
    GATTAttributePermissions,
)

from core.profile_loader import CameraProfile

logger = logging.getLogger(__name__)


class BleServer:
    def __init__(self, profile: CameraProfile, handler):
        self.profile = profile
        self.handler = handler
        self._server: BlessServer | None = None
        self._loop: asyncio.AbstractEventLoop | None = None
        self._stop_event = asyncio.Event()

    async def start(self):
        self._loop = asyncio.get_running_loop()
        device_name = self.profile.ble.device_name
        service_uuid = self.handler.get_service_uuid()

        print(f"[BLE] Advertising as '{device_name}'")
        print(f"[BLE] Service UUID: {service_uuid}")

        self._server = BlessServer(name=device_name, loop=self._loop)
        self._server.read_request_func = self._on_read
        self._server.write_request_func = self._on_write

        await self._server.add_new_service(service_uuid)

        # Build a deduped map uuid → combined properties.
        # Insta360 uses the same UUID for write and notify; GoPro uses distinct UUIDs.
        write_set  = set(u.lower() for u in self.handler.get_write_uuids())
        notify_set = set(u.lower() for u in self.handler.get_notify_uuids())
        all_uuids  = write_set | notify_set

        for char_uuid in all_uuids:
            is_write  = char_uuid in write_set
            is_notify = char_uuid in notify_set

            props = GATTCharacteristicProperties(0)
            perms = GATTAttributePermissions.readable
            if is_write:
                props |= (GATTCharacteristicProperties.write |
                          GATTCharacteristicProperties.write_without_response)
                perms |= GATTAttributePermissions.writeable
            if is_notify:
                props |= (GATTCharacteristicProperties.read |
                          GATTCharacteristicProperties.notify)

            # Use handler-supplied initial value if available (e.g. Insta360 SSID on notify char)
            init_val = bytearray(b"")
            if hasattr(self.handler, "get_characteristic_value"):
                init_val = bytearray(self.handler.get_characteristic_value(char_uuid))

            await self._server.add_new_characteristic(
                service_uuid, char_uuid, props, init_val, perms
            )

        self.handler.set_notification_sender(self._send_notification)
        await self._server.start()
        print("[BLE] Advertising started. Ctrl+C to stop.")

        try:
            await self._stop_event.wait()
        finally:
            await self._server.stop()
            print("[BLE] Server stopped.")

    async def stop(self):
        self._stop_event.set()

    def _on_read(self, characteristic: BlessGATTCharacteristic, **kwargs) -> bytearray:
        return characteristic.value or bytearray(b"")

    def _on_write(self, characteristic: BlessGATTCharacteristic, value: Any, **kwargs):
        char_uuid = str(characteristic.uuid)
        data = bytes(value) if value else b""
        logger.debug(f"[BLE] Write {char_uuid}: {data.hex()}")
        if self._loop and not self._loop.is_closed():
            fut = asyncio.run_coroutine_threadsafe(
                self.handler.handle_write(char_uuid, data),
                self._loop,
            )
            def _log_exc(f):
                exc = f.exception()
                if exc:
                    logger.error(f"[BLE] handle_write error on {char_uuid}: {exc!r}")
            fut.add_done_callback(_log_exc)

    async def _send_notification(self, char_uuid: str, data: bytes):
        if not self._server:
            return
        char = self._server.get_characteristic(char_uuid)
        if char is None:
            logger.warning(f"[BLE] Char not found: {char_uuid}")
            return
        char.value = bytearray(data)
        self._server.update_value(self.handler.get_service_uuid(), char_uuid)
        logger.debug(f"[BLE] Notified {char_uuid}: {data.hex()}")
