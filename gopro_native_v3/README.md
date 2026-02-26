# AirBubble — GoPro Remote Control

> **[FR]** Télécommande GoPro via Bluetooth pour les sports outdoor · **[EN]** Wireless GoPro remote for outdoor sports

[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-blue)](#license)

---

## 📸 Screenshots

<p align="center">
  <img src="assets/screenshots/01_connection.jpg" width="180" alt="Connection screen"/>
  <img src="assets/screenshots/02_dashboard_ready.jpg" width="180" alt="Dashboard — ready"/>
  <img src="assets/screenshots/03_dashboard_recording.jpg" width="180" alt="Dashboard — recording + countdown"/>
  <img src="assets/screenshots/04_settings.jpg" width="180" alt="Settings"/>
</p>
<p align="center">
  <img src="assets/screenshots/05_presets.jpg" width="180" alt="Presets"/>
  <img src="assets/screenshots/06_status.jpg" width="180" alt="System Status"/>
  <img src="assets/screenshots/07_bubble.jpg" width="180" alt="Floating bubble overlay"/>
</p>

> **Left to right:** Connection · Dashboard ready · Dashboard recording · Settings · Presets · System Status · Floating Bubble

---

## 🇬🇧 English

### What is AirBubble?

AirBubble is a native Android app that controls **GoPro HERO cameras** (HERO 9/10/11/12) wirelessly via **Bluetooth Low Energy (BLE)** — no Wi-Fi, no cable, no pairing screen.

It was designed for **outdoor athletes** (paragliders, mountain bikers, kayakers, skiers) who need to start/stop recording, tag moments, and check camera status **without stopping or looking at a screen**.

The **floating bubble** is AirBubble's signature feature: it stays visible over any app or screen, so you can control your camera while using maps, flight instruments, or anything else.

### Key Features

| Feature | Description |
|---------|-------------|
| 🔵 **BLE Connection** | Connects instantly to any GoPro HERO 9/10/11/12 |
| ▶️ **Record Control** | Start/stop recording with one tap |
| ⏱️ **Countdown Timer** | Auto-stop after a set duration (5s–5min) |
| ⭐ **HiLight Tag** | Mark moments during recording |
| 🔋 **Live Status** | Battery, storage, temperature, preset in real time |
| ⚙️ **Camera Settings** | Change resolution, FPS, lens, stabilisation directly |
| 🎬 **Preset Loader** | Switch between your saved camera presets |
| 📍 **GPS Tracking** | Export GPX file with timestamped waypoints at every HiLight/Record event |
| 💬 **Floating Bubble** | Stays on top of all apps — tap to tag, long-press to record |
| 🌍 **6 Languages** | French, English, Spanish, Basque, Catalan, German |
| 📱 **Portrait & Landscape** | Adaptive layout for any phone orientation |
| 🔄 **Auto-Reconnect** | Reconnects automatically on signal loss |

### Compatible Devices

**GoPro cameras:**
- HERO 9 Black
- HERO 10 Black
- HERO 11 Black / Mini
- HERO 12 Black

> Requires a GoPro camera with **Open GoPro BLE** support.
> Other models (HERO 13, MAX) may work but are not officially tested.

**Android phones:** Android 8.0 (API 26) and above. Tested on Android 12/13/14.

### Download

> _APK release and Play Store listing coming soon._

### Permissions Required

| Permission | Why |
|-----------|-----|
| `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` | Connect to the GoPro camera |
| `ACCESS_FINE_LOCATION` | Required by Android for BLE scanning (not used for tracking) |
| `SYSTEM_ALERT_WINDOW` | Floating bubble overlay |
| `FOREGROUND_SERVICE` | Keeps the bubble alive when the app is in background |
| `POST_NOTIFICATIONS` | Foreground service notification (Android 13+) |

> GPS tracking (GPX export) uses a **separate location permission** requested only when the bubble is active.

### Quick Start

1. Turn on your GoPro and make sure Bluetooth is enabled on your phone
2. Open **AirBubble** and tap **CONNECT**
3. The app scans and connects automatically (no pairing needed)
4. Use the **Dashboard** to record, tag, or change settings
5. Enable the **Floating Bubble** in Settings → it will stay visible over other apps

### GPS & GPX Export

When AirBubble is connected to your camera, it automatically tracks your GPS position and creates a `.gpx` file with waypoints at key moments:

- `REC_START #N` — when you start recording
- `HILIGHT #N` — when you tag a moment
- `REC_STOP #N` — when you stop (only if no HiLight was tagged in that clip)

Files are saved to `Documents/GoProPro/GPX/` and can be opened in Google Earth, GaiaGPS, XCTrack, or any GPX viewer.

---

## 🇫🇷 Français

### C'est quoi AirBubble ?

AirBubble est une application Android native qui contrôle les **caméras GoPro HERO** (HERO 9/10/11/12) sans fil via **Bluetooth Low Energy (BLE)** — sans Wi-Fi, sans câble, sans écran d'appairage.

Conçue pour les **sportifs outdoor** (parapentistes, vttistes, kayakistes, skieurs) qui veulent lancer/arrêter un enregistrement, taguer des moments et vérifier l'état de la caméra **sans s'arrêter ni regarder un écran**.

La **bulle flottante** est la fonctionnalité signature d'AirBubble : elle reste visible par-dessus toutes les applications, pour contrôler la caméra tout en utilisant une carte, un instrument de vol ou n'importe quoi d'autre.

### Fonctionnalités principales

| Fonctionnalité | Description |
|----------------|-------------|
| 🔵 **Connexion BLE** | Connexion instantanée à tout GoPro HERO 9/10/11/12 |
| ▶️ **Contrôle enregistrement** | Démarrer/arrêter en un tap |
| ⏱️ **Minuteur** | Arrêt automatique après une durée définie (5s–5min) |
| ⭐ **Tag HiLight** | Marquer les moments forts pendant l'enregistrement |
| 🔋 **Statut en direct** | Batterie, stockage, température, preset en temps réel |
| ⚙️ **Réglages caméra** | Changer résolution, FPS, objectif, stabilisation directement |
| 🎬 **Presets** | Basculer entre tes presets enregistrés dans la caméra |
| 📍 **Suivi GPS** | Export GPX avec waypoints horodatés à chaque HiLight/Enregistrement |
| 💬 **Bulle flottante** | Visible par-dessus toutes les apps — tap pour taguer, appui long pour enregistrer |
| 🌍 **6 langues** | Français, Anglais, Espagnol, Basque, Catalan, Allemand |
| 📱 **Portrait & Paysage** | Layout adaptatif selon l'orientation |
| 🔄 **Reconnexion auto** | Se reconnecte automatiquement en cas de perte de signal |

### Caméras compatibles

- GoPro HERO 9 Black
- GoPro HERO 10 Black
- GoPro HERO 11 Black / Mini
- GoPro HERO 12 Black

> Nécessite une GoPro compatible **Open GoPro BLE**. Les autres modèles (HERO 13, MAX) peuvent fonctionner sans avoir été officiellement testés.

### Export GPS & GPX

Dès la connexion à la caméra, AirBubble suit ta position GPS et crée un fichier `.gpx` avec des waypoints aux moments clés :

- `REC_START #N` — au démarrage de l'enregistrement
- `HILIGHT #N` — à chaque tag HiLight
- `REC_STOP #N` — à l'arrêt (uniquement si aucun HiLight dans le clip)

Les fichiers sont enregistrés dans `Documents/GoProPro/GPX/` et s'ouvrent dans Google Earth, GaiaGPS, XCTrack ou n'importe quel logiciel GPX.

---

## 🛠️ Build from Source

### Requirements

- Android Studio Hedgehog (2023.1) or newer
- JDK 17
- Android SDK 35

### Steps

```bash
git clone https://github.com/YOUR_USERNAME/airbubble.git
cd airbubble/gopro_native_v3

# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Release build (requires keystore)
./gradlew assembleRelease
```

### Key Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| `no.nordicsemi.android:ble` | 2.11.0 | BLE connection & management |
| `androidx.compose:compose-bom` | 2024.05.00 | Jetpack Compose UI |
| `androidx.compose.material3:material3` | BOM | Material Design 3 |
| `com.google.protobuf:protobuf-javalite` | 3.25.3 | GoPro preset protocol |
| `com.google.android.gms:play-services-location` | 21.3.0 | Fused GPS |
| `kotlinx-coroutines-android` | 1.8.0 | Async operations |

### Architecture

```
MVVM + StateFlow

MainActivity
├── GoProConnectionManager     ← BLE scan, connect, commands, keep-alive
│   ├── GoProBleManager        ← Nordic BLE stack, packet fragmentation
│   ├── GoProStatusParser      ← TLV response parser, Protobuf decoder
│   └── GpsTracker             ← FusedLocationProvider, GPX session
│       └── GpxWriter          ← Real-time GPX file writer (MediaStore)
├── GoProViewModel             ← CameraUiState (StateFlow), timer logic
└── UI (Compose)
    ├── ConnectionScreen       ← BLE scan animation
    ├── DashboardLayout        ← Adaptive layout (portrait/landscape)
    │   ├── DashboardScreen    ← Record, HiLight, timer, stats
    │   ├── SettingsScreen     ← Camera settings dropdowns
    │   ├── PresetsScreen      ← Preset groups
    │   └── StatusScreen       ← Battery, storage, system info
    └── bubble/
        ├── FloatingBubbleService  ← Overlay foreground service
        └── BubbleController       ← Bubble lifecycle & callbacks
```

---

## 🗺️ Roadmap

- [x] BLE connection & reconnect
- [x] Record / HiLight / Sleep / Reboot
- [x] Live camera settings & presets
- [x] Countdown timer
- [x] Floating bubble overlay
- [x] GPS tracking + GPX export
- [x] 6 languages (fr, en, es, eu, ca, de)
- [x] Portrait + Landscape layouts
- [ ] Camera horizon calibration (IMU / GPMF)
- [ ] Auto-trigger (accelerometer takeoff / GPS landing)
- [ ] Insta360 X3 support
- [ ] Play Store release

---

## 🤝 Contributing

Contributions welcome! Please open an issue first to discuss what you'd like to change.

```bash
# Create a feature branch
git checkout -b feat/your-feature

# Commit convention
git commit -m "feat: description"   # new feature
git commit -m "fix: description"    # bug fix
git commit -m "refactor: ..."       # code change without feature/fix
```

---

## 📄 License

MIT License — see [LICENSE](LICENSE) file.

---

## 🙏 Credits

Built with [Open GoPro BLE API](https://gopro.github.io/OpenGoPro/) · [Nordic Semiconductor BLE library](https://github.com/NordicSemiconductor/Android-BLE-Library)
