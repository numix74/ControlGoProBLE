# FAQ — Frequently Asked Questions

---

## Connection

**The app can't find my GoPro**
- Make sure Bluetooth is enabled on the GoPro _(GoPro Settings → Connections → Bluetooth)_
- Make sure your phone's Bluetooth is on
- Stay within 10 metres of the camera
- Restart the GoPro and try scanning again

**Connection drops immediately after connecting**
- Often caused by unstable Bluetooth. Restart both devices and try again.
- Check that no other app is using Bluetooth simultaneously (GoPro Quik, etc.)

**"Connecting…" spins for a long time**
- AirBubble scans for up to 30 seconds. If nothing is found, it returns to "ready" state.
- Check that Bluetooth and Location permissions are granted: Settings → Apps → AirBubble.

**Camera disconnects on its own**
- Your GoPro has an **auto power off** setting (8s, 30s, 1 min…). Disable it or increase the duration: GoPro Settings → Preferences → Auto Power Off → Never.
- AirBubble sends a keep-alive every 3 seconds but cannot override a very short auto-off.

**App reconnects even after I deliberately turned the camera off**
- AirBubble detects intentional camera shutdowns (GATT code 0x13) and **does not attempt reconnection** in this case.
- If reconnection still triggers, please open a GitHub issue.

---

## Recording

**START CAPTURE button doesn't respond**
- Check the camera is connected (green dot in the bubble)
- The camera may be busy (loading a preset…) — wait 2 seconds and try again

**Recording starts but the camera doesn't film**
- Check that the SD card is inserted and not full
- Check the active preset (Presets tab) — some presets don't start in video mode

**The countdown timer doesn't trigger**
- Timer mode must be **enabled** in the Dashboard (clock icon)
- The value must be greater than 0 (use +/- buttons to set it)

---

## GPS & GPX

**The GPX file has "(no GPS)" waypoints**
- GPS hadn't acquired a fix yet at the time of the event. This is not an error — timestamp data is still recorded.
- Fix: wait 30–60 seconds after opening AirBubble before you start recording.

**I can't find the GPX files**
- Open your phone's file manager → Internal storage → Documents → GoProPro → GPX
- On Samsung devices, look in "My Files"

**The GPX opens in the wrong app**
- Make sure you have a GPX viewer installed (Google Earth, GaiaGPS…)
- On PC, copy the file via USB and open with Viking or GPXSee

**No GPX file was created**
- AirBubble automatically deletes empty files (sessions with no recording or HiLight)
- Check that Location permission is granted to AirBubble

---

## Floating Bubble

**The bubble doesn't appear**
- Check that "Display over other apps" permission is granted: Settings → Apps → AirBubble → Display over other apps

**The bubble disappears when I receive a call**
- This is normal Android behaviour. The bubble resumes its position after the call ends.

**I can't close the bubble**
- Drag the bubble slowly downward — the red close zone appears. Release inside the zone.
- Or: reopen AirBubble (double-tap the bubble) → Settings → disable Floating Bubble.

---

## Compatible cameras

**Does my GoPro HERO 8 work?**
- The HERO 8 uses an older BLE protocol. It may work partially but is not officially supported.

**Does my GoPro MAX / HERO 13 work?**
- These models use the same Open GoPro BLE protocol and should work. They haven't been officially tested yet — feedback welcome!

**Does my Insta360 work?**
- Not yet. Insta360 X3 support is planned for a future release.

---

## Other

**The app drains my battery**
- BLE is very energy-efficient. The main consumption comes from **continuous GPS** and the foreground service for the floating bubble. On long flights, you can disable GPS in Android settings if GPX export isn't a priority.

**How do I change the app language?**
- Settings tab → Language section → select your language

**Is the app open source?**
- The source code will be published on GitHub. Check the repository for current status.
