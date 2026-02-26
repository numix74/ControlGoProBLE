# Floating Bubble

The floating bubble is AirBubble's signature feature. It stays on top of all other apps — maps, flight instruments, GPS, messages — so you can control your camera **without switching back to AirBubble**.

---

## Enable the bubble

1. Open AirBubble and connect to your GoPro
2. Go to **Settings** → enable **Floating Bubble**
3. Allow "Display over other apps" if Android asks
4. The bubble appears in the top-left corner of your screen

> You can now press Home or switch to any other app — the bubble stays visible.

---

## Visual states

| Border colour | State |
|--------------|-------|
| 🔘 Grey | Disconnected from camera |
| 🔵 Cyan | Connected, ready |
| 🔴 Red | **Recording in progress** |
| 🔴 Red + timer | Recording with countdown active |

The indicator dot on the left side:
- 🟢 Green = connected
- 🔴 Blinking red = recording

---

## Gestures

| Gesture | Action |
|---------|--------|
| **Single tap** _(while recording)_ | Add a **HiLight** to the video |
| **Single tap** _(connected, not recording)_ | No action |
| **Double tap** | Open AirBubble in the foreground |
| **Long press** _(connected)_ | Start / stop recording |
| **Long press** _(disconnected)_ | Attempt manual reconnect |
| **Drag downward** | Reveal the close zone |

---

## Close the bubble

To close the bubble:
1. **Drag it down** toward the bottom of the screen
2. A red "× CLOSE" zone appears at the bottom
3. Release the bubble **inside the zone** to close it

Or from the app: **Settings → Floating Bubble → disable**.

---

## Bubble and the countdown timer

If you have the **timer mode** active in the Dashboard:
- The bubble shows a **countdown** in `RECORDING_TIMER` state (e.g. `00:45`)
- Recording stops automatically when the countdown reaches `00:00`
- The bubble returns to the "connected" state (cyan)

→ [Set up the timer](Settings-Presets-EN#timer)

---

## Practical tips

**Paragliding:** Start recording before your run-up → enable the bubble → slide your phone into your pocket. A single tap adds a HiLight in-flight.

**MTB / Skiing:** Long-press on the bubble = start/stop recording without digging into the app.

**Kayaking:** Double-tap to reopen the app works even with gloves on.

---

## Required permission

The floating bubble requires the **"Display over other apps"** permission (`SYSTEM_ALERT_WINDOW`). This is a special Android permission — Android will ask you to grant it explicitly in Settings.

This permission is **only** used to display the bubble — never to read content from other apps.
