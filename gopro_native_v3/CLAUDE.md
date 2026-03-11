# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Native Android app (Kotlin) for controlling GoPro cameras (HERO 9/10/11/12) via Bluetooth Low Energy (BLE). Uses Jetpack Compose for UI and the GoPro Open API BLE protocol.

- **Package:** `com.ximun.gopropro` (applicationId: `com.ximun.gopropro.v2`)
- **Min SDK:** 26 (Android 8.0) / **Target SDK:** 35 (Android 15)
- **Java Target:** 17

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Clean build
./gradlew clean

# Run lint
./gradlew lint
```

No tests currently exist in the project.

## Architecture

**Pattern:** MVVM with reactive state via `StateFlow<CameraUiState>`

```
app/src/main/kotlin/com/ximun/gopropro/
├── MainActivity.kt              # Entry point, permissions, navigation host
├── GoProSettingsMappings.kt     # Label maps for all GoPro settings/values
├── viewmodel/
│   └── GoProViewModel.kt       # Central state holder (CameraUiState), timer logic, formatting
├── ble/
│   ├── GoProBleManager.kt      # Nordic BLE manager: scan, connect, send commands, keep-alive
│   ├── GoProConstants.kt       # UUIDs, command bytes, status/setting/query IDs
│   ├── GoProPacketHandler.kt   # Packet fragmentation/defragmentation (short/long headers, continuations)
│   └── GoProStatusParser.kt    # TLV response parsing, protobuf preset decoding
└── ui/
    ├── ConnectionScreen.kt      # BLE scan & connect UI with animated ping
    ├── DashboardScreen.kt       # Main control: record, hilight, timer, device controls
    ├── SettingsScreen.kt        # Video/system settings with capability-driven dropdowns
    ├── PresetsScreen.kt         # Preset groups list with load-on-click
    ├── StatusScreen.kt          # Battery, storage, system info display
    ├── Common.kt                # Shared UI components
    └── theme/Theme.kt           # Dark theme only (Material3)
```

## Key Technical Details

### BLE Protocol
- **GoPro Service UUID:** `0000fea6-0000-1000-8000-00805f9b34fb`
- 3 characteristic pairs (Command, Settings, Query) each with write + notify
- Packet fragmentation: short header (<32 bytes), long header (>=32 bytes), continuation packets with 4-bit cycling counter
- MTU negotiated to 512 bytes; default packet size 244 bytes
- Keep-alive sent every 3 seconds
- Responses parsed as TLV (Tag-Length-Value)

### Connection Flow
1. Scan for GoPro service UUID → connect on first match (3 retries)
2. Enable MTU 512 + notifications on all response characteristics
3. Query hardware info (up to 10 retries)
4. Claim camera control via protobuf command
5. Register for status/settings/capabilities async updates
6. Start keep-alive loop

### Protobuf
- Schema in `app/src/main/proto/gopro.proto`
- Used for preset notifications and camera control claims
- Generated with `protobuf-javalite:3.25.3`

### Key Dependencies
- **BLE:** `no.nordicsemi.android:ble:2.11.0` + `ble-ktx`
- **UI:** Compose BOM 2024.05.00, Material3, Icons Extended
- **Async:** kotlinx-coroutines-android 1.8.0
- **Lifecycle:** 2.8.7 (viewmodel-compose, runtime-compose)

## Constants Reference

All GoPro command bytes, status IDs, setting IDs, and query IDs are defined in `GoProConstants.kt`. Setting value-to-label mappings are in `GoProSettingsMappings.kt`. When adding new settings support, both files must be updated together.

## Build Configuration

- Gradle wrapper: 9.3.1
- AGP: 8.7.3, Kotlin: 2.1.0
- ProGuard enabled in release builds
- Multidex enabled
- Configuration cache enabled

## Bugs connus

### APD (Auto Power Down) laissé à "Jamais" après déconnexion
**Symptôme** : Quand l'app se déconnecte (perte signal ou intentionnel), la caméra garde le
setting 59 à 0 (Jamais) même si l'utilisateur avait défini une autre durée (ex: 5 min).
La caméra ne s'éteindra plus jamais automatiquement jusqu'à ce que l'utilisateur la reconfigure.

**Cause** : L'app force `59=0` en premier plan pour maintenir la connexion BLE (HERO11 Mini).
`restoreAutoOff()` est appelé uniquement dans `setAppForeground(false)` (onPause).
Si la déconnexion survient alors que l'app est encore au premier plan (perte de signal,
ou déconnexion intentionnelle via bouton), `restoreAutoOff()` n'est jamais appelé.

**Fichiers concernés** : `GoProConnectionManager.kt` — `disconnect()`, `onConnectionStatusChanged`

**Fix appliqué (Fix B — SharedPreferences)** — session 2026-03-12 :
- `disableAutoOff()` : lit prefs si `savedAutoOffValue == null`, écrit dans prefs
- `updateSetting(59, v)` : persiste dans prefs
- `disconnect()` et `sendSleep()` : appellent `restoreAutoOff()` avant de couper
- `restoreAutoOff()` : fallback prefs si `savedAutoOffValue == null`

**Contexte doc officielle** (https://gopro.github.io/OpenGoPro/ble/features/control) :
Le timer APD est reset par "sets a setting". HERO11 Mini quirk : ignore les writes même-valeur →
seul `59=0` (désactivation du timer) est fiable. Keep Alive seul (toutes les 3s) ne suffit pas.
