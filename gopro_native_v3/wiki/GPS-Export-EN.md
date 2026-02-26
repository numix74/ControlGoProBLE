# GPS & GPX Export

AirBubble tracks your **GPS position in real time** throughout the session and automatically generates a **GPX file** with precise timestamped waypoints at every key moment.

---

## How it works

As soon as you connect to the GoPro, AirBubble:
1. Starts GPS tracking (via Google's Fused Location Provider)
2. Opens a timestamped GPX file in `Documents/GoProPro/GPX/`
3. Adds a waypoint at each GoPro event
4. Closes and finalises the file when you disconnect

---

## Waypoints recorded

| Type | When | Name in the GPX |
|------|------|----------------|
| **REC_START** | When recording begins | `REC_START #1`, `#2`… |
| **HILIGHT** | When you press HiLight | `HILIGHT #1`, `#2`… |
| **REC_STOP** | When recording stops _(only if no HILIGHT was tagged in that clip)_ | `REC_STOP #1`… |

> **REC_STOP logic:** If you tagged a HiLight during the clip, the REC_STOP waypoint is skipped — the HiLight already marks the interesting moment. This avoids redundant waypoints.

---

## Data per waypoint

Each waypoint contains:
- **GPS coordinates** (latitude, longitude, altitude)
- **Absolute timestamp** (UTC, ISO 8601 format)
- **T+** elapsed time since recording started _(e.g. T+02:34)_
- **Speed** in km/h _(if available)_
- **GPS accuracy** in metres _(e.g. acc: 4m)_

**Example description in the GPX:**
```
HILIGHT #2
2024-08-15 14:23:05 | T+02:34 | 48 km/h | acc: 6m
```

---

## Without GPS fix

If GPS hasn't acquired a fix yet at the time of the event (start of session, covered area…), the waypoint is still recorded with `lat=0/lon=0` and the name ends with `(no GPS)`:
```
HILIGHT #1 (no GPS)
```

All other data (timestamp, T+) is still present.

---

## Where to find the GPX files

Files are saved in the phone's internal storage:

```
Documents/
└── GoProPro/
    └── GPX/
        ├── gopro_20240815_142305.gpx
        ├── gopro_20240816_091042.gpx
        └── ...
```

**File naming:** `gopro_YYYYMMDD_HHMMSS.gpx` (timestamp of camera connection)

> Files **with no waypoints** (session with no recording or HiLight) are **automatically deleted** to keep storage clean.

---

## Opening GPX files

| App | Platform | Best for |
|-----|----------|---------|
| **Google Earth** | Web / Android / PC | 3D visualisation, flythrough |
| **GaiaGPS** | Android / iOS | Outdoor, topo maps |
| **XCTrack** | Android | Paragliding, vario, flight tracks |
| **Viking** | PC (Linux/Win) | Detailed GPS analysis |
| **GPXSee** | PC (Win/Mac/Linux) | Simple GPX viewer |
| **Garmin Connect** | Web | Import to Garmin watches |
| **Strava** | Android / Web | Sport segments |

---

## Permissions

AirBubble requests the **Precise Location** permission for GPS tracking. This is needed to:
- Record GPS coordinates in waypoints
- (Android ≤ 11) Scan for Bluetooth devices

> Location data is **only used for the GPX file** — AirBubble does not send any location data to external servers.

---

## Tips

**Paragliding:** Open AirBubble before leaving the launch — GPS will have time to acquire a fix before your first recording.

**Accuracy:** In open sky, GPS accuracy is typically 3–10 metres. Under tree cover or in enclosed valleys it may exceed 20 m.

**Battery:** Continuous GPS uses battery. On long-distance flights, enable battery saver mode if needed.
