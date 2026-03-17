# ActionCam Emulator — Notes Claude

## Stack & versions critiques

```
Python  3.11
bless   0.2.4   ← PINNÉ, ne pas updater
bleak   0.22.3  ← PINNÉ, ne pas updater
```

**Seule combinaison compatible sur Windows 11 :**
- `bless >= 0.2.5` → requiert `pysetupdi` (indisponible sur PyPI)
- `bleak >= 0.23` → retire `bleak.backends.winrt.service` utilisé par bless
- `bless 0.2.4 + bleak 0.22.3` = seule paire qui fonctionne

## Patch bless (appliqué manuellement, à réappliquer après pip install)

**Fichier :** `site-packages/bless/backends/winrt/characteristic.py`, ligne ~89

Problème : `BleakGATTCharacteristicWinRT.__init__()` requiert `max_write_without_response_size`
depuis bleak 0.21, mais bless 0.2.4 ne le passe pas → `TypeError` au démarrage.

Fix appliqué dans `init()` :
```python
# Avant (original bless 0.2.4) :
super(BlessGATTCharacteristic, self).__init__(obj=gatt_char)

# Après (patch) :
super(BlessGATTCharacteristic, self).__init__(
    obj=gatt_char,
    max_write_without_response_size=lambda: 512,
)
```

## Comportement BLE sur Windows (WinRT)

### Nom du device
`BlessServer(name="GoPro 4567")` **n'a aucun effet** sur le nom diffusé.
Le nom BLE visible par Android est le **nom Bluetooth système du PC** (défini dans Paramètres Windows).

Pour changer le nom provisoirement (PowerShell **admin**) :
```powershell
New-ItemProperty -Path 'HKLM:\SYSTEM\CurrentControlSet\Services\BTHPORT\Parameters' `
  -Name 'LocalDeviceName' -Value 'GoPro 4567' -PropertyType String -Force
Restart-Service bthserv
```
Effet annulé au prochain reboot.

### Write callbacks
- WinRT déclenche `WriteRequested` pour les deux types : Write Request ET Write Command (sans réponse).
- Le callback bless utilise `asyncio.new_event_loop().run_until_complete(get_request_async())` — fonctionne mais crée un nouveau loop à chaque write → ne pas l'utiliser dans des contextes haute fréquence.
- Les exceptions dans `handle_write` sont loggées via `fut.add_done_callback(_log_exc)` dans `BleServer._on_write`.

### Fragmentation GATT
Android envoie les commandes dans des BLE packets GoPro-framés.
**nRF Connect doit envoyer les bytes en une seule écriture BYTE ARRAY**, jamais séparément.

Exemples (commandes framed) :
| Commande         | Bytes BYTE ARRAY |
|------------------|------------------|
| HW Info          | `013C`           |
| Keep Alive       | `015B`           |
| Shutter ON       | `03010101`       |
| Shutter OFF      | `03010100`       |

### Reset du stack BLE entre sessions
Après plusieurs arrêts/redémarrages de l'émulateur, le stack WinRT peut rester bloqué
sur `add_new_service` (hang infini). Fix : **toggle Bluetooth off/on** sur Windows.
Aussi : supprimer le bond Android (Paramètres → Bluetooth → Oublier l'appareil)
pour éviter le cache GATT Android.

### GATT cache Android
Android met en cache les services GATT par adresse MAC. Si l'émulateur redémarre
(adresse BLE aléatoire change), nRF Connect peut montrer 0 characteristics.
Fix : dans nRF Connect → ⋮ → **Refresh device** ou **Discover services**.

## Architecture protocoles

### GoPro BLE
- Service : `0000fea6-...`
- 3 paires write/notify : command (`72`/`73`), settings (`74`/`75`), query (`76`/`77`)
- Framing : header 1 octet (payload ≤ 31 bytes) ou 2 octets (> 31 bytes)
- TLV responses : `[cmd][status][field_count][len][data]...`

### Insta360 BLE (SDK 1.9.11, DEX reverse-engineered)
- L'app lit le SSID WiFi depuis l'**advertisement BLE** (BleScanProto, company_id `0x0B4B`)
  **sans connexion GATT**. Connexion GATT = credentials only.
- Service : `00003366-...`, Char unique : `00003377-...` (write + notify)
- SSID format : `<Model>.<Serial>.OSC` ex: `X4.IS4A1234567890.OSC`
- Contrôle principal : HTTP OSC sur `192.168.42.1:80`

## Phase 3 — HTTP OSC (Insta360)

`wifi/insta360/http_server.py` — FastAPI OSC server.

Démarré automatiquement sur **port 8080** quand `profile.brand == "insta360"`.
Test avec ADB reverse (Android fournit le WiFi au PC via hotspot) :
```bash
adb reverse tcp:80 tcp:8080    # map Android 127.0.0.1:80 → PC:8080
python main.py insta360_x4 --no-tui
```

Endpoints :
| Méthode | Chemin | Rôle |
|---|---|---|
| GET | `/osc/info` | Infos caméra (modèle, firmware, état) |
| GET | `/osc/state` | État complet (batterie, stockage, recording) |
| POST | `/osc/commands/execute` | Exécuter une commande OSC |
| GET | `/osc/commands/status` | Statut commande (toujours "done" dans l'émulateur) |

Commandes OSC implémentées :
`camera.startCapture` / `camera.stopCapture` / `camera.takePicture` /
`camera.getOptions` / `camera.setOptions` / `camera.startSession` /
`camera.closeSession` / `camera.listFiles` / `camera._getLivePreview`

**Commandes inconnues** → log `WARNING [OSC] UNKNOWN command` + `{"state":"done"}`.
Cela permet de découvrir les appels SDK par simple observation des logs.

## Tests

```bash
python -m pytest tests/ -v   # 73 tests, ~1.3s
```

Fichiers de tests :
- `tests/test_packet_codec.py` — framing BLE GoPro (27 tests)
- `tests/test_handlers.py` — handlers GoPro command/settings/query (15 tests)
- `tests/test_insta360.py` — protocole Insta360 BLE (21 tests)
- `tests/test_osc_http.py` — HTTP OSC FastAPI via httpx ASGI (25 tests)
