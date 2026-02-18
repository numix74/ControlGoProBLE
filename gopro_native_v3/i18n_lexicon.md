# Lexique i18n — gopro_native_v3

> Fichier de référence pour l'implémentation des `strings.xml` localisés.
> Langues cibles : **fr** (défaut), **en**, **es**, **eu** (Basque), **ca** (Catalan), **de**
> Mise à jour : 2026-02-19

---

## Convention de nommage des clés

- `nav_*` → navigation (tabs, rail)
- `dashboard_*` → écran Controle
- `connection_*` → écran de connexion
- `status_*` → écran Status
- `settings_*` → écran Réglages
- `presets_*` → écran Presets
- `common_*` → chaînes partagées entre plusieurs écrans

---

## Navigation

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `nav_tab_control` | Controle | Control | Control | Kontrol | Control | Steuerung |
| `nav_tab_settings` | Réglages | Settings | Ajustes | Ezarpenak | Configuració | Einstellungen |
| `nav_tab_presets` | Presets | Presets | Presets | Presets | Presets | Presets |
| `nav_tab_status` | Status | Status | Estado | Egoera | Estat | Status |

---

## Dashboard (écran Controle)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `dashboard_title` | STUDIO PRO | STUDIO PRO | STUDIO PRO | STUDIO PRO | STUDIO PRO | STUDIO PRO |
| `dashboard_subtitle` | LIAISON DIRECTE | DIRECT LINK | ENLACE DIRECTO | ZUZENEKO KONEXIOA | CONNEXIÓ DIRECTA | DIREKTVERBINDUNG |
| `dashboard_btn_sleep` | Veille | Sleep | Espera | Logerazpena | Espera | Schlafmodus |
| `dashboard_btn_disconnect` | Déconnexion | Disconnect | Desconectar | Deskonektatu | Desconnectar | Trennen |
| `dashboard_stat_battery` | BATTERIE | BATTERY | BATERÍA | BATERIA | BATERIA | AKKU |
| `dashboard_stat_storage` | STOCKAGE | STORAGE | ALMACENAMIENTO | BILTEGIRATZEA | EMMAGATZEMATGE | SPEICHER |
| `dashboard_timer_countdown` | REBOURS | COUNTDOWN | CUENTA ATRÁS | ATZERANTZ ZENBAKETA | COMPTE ENRERE | COUNTDOWN |
| `dashboard_timer_duration` | DURÉE | DURATION | DURACIÓN | IRAUPENA | DURADA | DAUER |
| `dashboard_btn_start_capture` | START CAPTURE | START CAPTURE | INICIAR CAPTURA | GRABAKETA HASI | INICIAR CAPTURA | AUFNAHME STARTEN |
| `dashboard_btn_stop_capture` | STOP CAPTURE | STOP CAPTURE | DETENER CAPTURA | GRABAKETA GELDITU | ATURAR CAPTURA | AUFNAHME STOPPEN |
| `dashboard_btn_hilight` | Hilight | Hilight | Hilight | Hilight | Hilight | Hilight |

> ⚠️ `Hilight` est un terme propriétaire GoPro — ne pas traduire.

---

## Connexion

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `connection_title` | GOPRO STUDIO | GOPRO STUDIO | GOPRO STUDIO | GOPRO STUDIO | GOPRO STUDIO | GOPRO STUDIO |
| `connection_subtitle` | BLE INTERFACE PRO | BLE INTERFACE PRO | BLE INTERFACE PRO | BLE INTERFACE PRO | BLE INTERFACE PRO | BLE INTERFACE PRO |
| `connection_status_label` | STATUS CAMÉRA | CAMERA STATUS | ESTADO CÁMARA | KAMERAREN EGOERA | ESTAT CÀMERA | KAMERASTATUS |
| `connection_status_bt_off` | BT DÉSACTIVÉ | BT DISABLED | BT DESACTIVADO | BT DESGAITUTA | BT DESACTIVAT | BT DEAKTIVIERT |
| `connection_status_ready` | PRÊT | READY | LISTO | PREST | LLEST | BEREIT |
| `connection_status_init` | INITIALISATION… | INITIALIZING… | INICIALIZANDO… | HASIERATZEN… | INICIALITZANT… | WIRD INITIALISIERT… |
| `connection_msg_bt_off` | Le Bluetooth est désactivé. Activez-le dans les paramètres de votre téléphone. | Bluetooth is disabled. Enable it in your phone settings. | El Bluetooth está desactivado. Actívalo en los ajustes de tu teléfono. | Bluetooth-a desgaituta dago. Gaitu ezazu zure telefonoaren ezarpenetan. | El Bluetooth està desactivat. Activa\'l a la configuració del teu telèfon. | Bluetooth ist deaktiviert. Aktiviere es in den Einstellungen deines Telefons. |
| `connection_msg_bt_ready` | Le service Bluetooth est prêt. Vous pouvez lancer la connexion. | Bluetooth service is ready. You can start the connection. | El servicio Bluetooth está listo. Puedes iniciar la conexión. | Bluetooth zerbitzua prest dago. Konexioa has dezakezu. | El servei Bluetooth està llest. Pots iniciar la connexió. | Der Bluetooth-Dienst ist bereit. Du kannst die Verbindung herstellen. |
| `connection_msg_bt_init` | Veuillez patienter pendant l\'initialisation du service Bluetooth… | Please wait while the Bluetooth service initializes… | Por favor, espera mientras se inicializa el servicio Bluetooth… | Mesedez itxaron Bluetooth zerbitzua hasieratu arte… | Si us plau, espereu mentre s\'inicialitza el servei Bluetooth… | Bitte warten, während der Bluetooth-Dienst initialisiert wird… |
| `connection_btn_scan` | SCAN… | SCANNING… | BUSCANDO… | BILATZEN… | CERCANT… | SUCHE… |
| `connection_btn_connect` | CONNECTER | CONNECT | CONECTAR | KONEKTATU | CONNECTAR | VERBINDEN |
| `connection_footer` | GOPRO CONTROLLER V4.2 | GOPRO CONTROLLER V4.2 | GOPRO CONTROLLER V4.2 | GOPRO CONTROLLER V4.2 | GOPRO CONTROLLER V4.2 | GOPRO CONTROLLER V4.2 |

> ⚠️ `connection_footer` : version string, ne pas traduire (garder identique).
> ⚠️ `connection_title`, `connection_subtitle` : noms techniques, ne pas traduire.

---

## Status

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `status_title` | SYSTEM STATUS | SYSTEM STATUS | ESTADO DEL SISTEMA | SISTEMAREN EGOERA | ESTAT DEL SISTEMA | SYSTEMSTATUS |
| `status_subtitle` | DIAGNOSTIC TEMPS RÉEL | REAL-TIME DIAGNOSTIC | DIAGNÓSTICO EN TIEMPO REAL | DENBORA ERREALEKO DIAGNOSTIKOA | DIAGNÒSTIC EN TEMPS REAL | ECHTZEIT-DIAGNOSE |
| `status_battery_section` | ALIMENTATION | POWER | ALIMENTACIÓN | ELIKADURA | ALIMENTACIÓ | STROMVERSORGUNG |
| `status_battery_charging` | EN CHARGE | CHARGING | CARGANDO | KARGATZEN | CARREGANT | WIRD GELADEN |
| `status_battery_critical` | CRITIQUE | CRITICAL | CRÍTICO | KRITIKOA | CRÍTIC | KRITISCH |
| `status_battery_nominal` | NOMINAL | NOMINAL | NORMAL | NORMALA | NOMINAL | NORMAL |
| `status_battery_ac` | SECTEUR | AC POWER | RED ELÉCTRICA | KORRONTEA | CORRENT ELÈCTRIC | NETZSTROM |
| `status_battery_internal` | INTERNE | INTERNAL | INTERNA | BATERIKOA | INTERNA | INTERN |
| `status_storage_section` | STATISTIQUES STOCKAGE | STORAGE STATS | ESTADÍSTICAS DE ALMACENAMIENTO | BILTEGIRATZEKO ESTATISTIKAK | ESTADÍSTIQUES D\'EMMAGATZEMATGE | SPEICHERSTATISTIKEN |
| `status_storage_remaining` | RESTANT | REMAINING | RESTANTE | GERATZEN | RESTANT | VERBLEIBEND |
| `status_storage_full_pct` | %d%% PLEIN | %d%% FULL | %d%% LLENO | %d%% BETETA | %d%% PLE | %d%% VOLL |
| `status_storage_card_label` | CARTE %s | CARD %s | TARJETA %s | TXARTELA %s | TARGETA %s | KARTE %s |
| `status_storage_sd_type` | SD CARD (V30) | SD CARD (V30) | SD CARD (V30) | SD TXARTELA (V30) | SD CARD (V30) | SD-KARTE (V30) |
| `status_info_system_state` | État Système | System State | Estado del Sistema | Sistemaren Egoera | Estat del Sistema | Systemzustand |
| `status_info_recording` | ENREGISTREMENT | RECORDING | GRABANDO | GRABATZEN | GRAVANT | AUFNAHME |
| `status_info_ready` | PRÊT | READY | LISTO | PREST | LLEST | BEREIT |
| `status_info_temperature` | Température | Temperature | Temperatura | Tenperatura | Temperatura | Temperatur |
| `status_info_overheat` | SURCHAUFFE | OVERHEATING | SOBRECALENTAMIENTO | BEROEGI | SOBREESCALFAMENT | ÜBERHITZUNG |
| `status_info_danger` | DANGER | DANGER | PELIGRO | ARRISKUA | PERILL | GEFAHR |
| `status_info_power` | Alimentation | Power | Alimentación | Elikadura | Alimentació | Stromversorgung |
| `status_info_photos` | Photos Restantes | Remaining Photos | Fotos Restantes | Argazki Geratzen | Fotos Restants | Verbleibende Fotos |
| `status_info_videos` | Vidéos sur Carte | Videos on Card | Vídeos en Tarjeta | Txarteleko Bideoak | Vídeos a la Targeta | Videos auf Karte |
| `status_info_sd_capacity` | Capacité SD | SD Capacity | Capacidad SD | SD Ahalmena | Capacitat SD | SD-Kapazität |
| `status_info_time_remaining` | Temps Restant | Remaining Time | Tiempo Restante | Denbora Geratzen | Temps Restant | Verbleibende Zeit |
| `status_info_active_preset` | Preset Actif | Active Preset | Preset Activo | Preset Aktiboa | Preset Actiu | Aktives Preset |
| `status_info_firmware` | Firmware | Firmware | Firmware | Firmware | Firmware | Firmware |
| `status_info_serial` | N° Série | Serial No. | N° Serie | Serie Zk. | N° Sèrie | Seriennr. |

---

## Réglages

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `settings_title` | RÉGLAGES | SETTINGS | AJUSTES | EZARPENAK | CONFIGURACIÓ | EINSTELLUNGEN |
| `settings_subtitle` | PARAMÈTRES DE LA CAMÉRA | CAMERA PARAMETERS | PARÁMETROS DE CÁMARA | KAMERAREN PARAMETROAK | PARÀMETRES DE LA CÀMERA | KAMERAPARAMETER |
| `settings_section_mode` | MODE ACTIF | ACTIVE MODE | MODO ACTIVO | MODU AKTIBOA | MODE ACTIU | AKTIVER MODUS |
| `settings_section_system` | PARAMÈTRES SYSTÈME | SYSTEM SETTINGS | PARÁMETROS DEL SISTEMA | SISTEMAREN PARAMETROAK | PARÀMETRES DEL SISTEMA | SYSTEMPARAMETER |
| `settings_dark_mode_label` | Mode Clair | Light Mode | Modo Claro | Modu Argia | Mode Clar | Heller Modus |
| `settings_light_mode_label` | Mode Sombre | Dark Mode | Modo Oscuro | Modu Iluna | Mode Fosc | Dunkler Modus |
| `settings_bubble_label` | Bulle Flottante | Floating Bubble | Burbuja Flotante | Burbuila Flotatzailea | Bombolla Flotant | Schwebende Blase |
| `settings_sync_clock_label` | Sync Horloge | Sync Clock | Sincronizar Hora | Ordua Sinkronizatu | Sincronitzar Rellotge | Uhr synchronisieren |
| `settings_sync_clock_action` | Synchroniser | Synchronize | Sincronizar | Sinkronizatu | Sincronitzar | Synchronisieren |
| `settings_sync_clock_done` | ✓ Synchronisé | ✓ Synchronized | ✓ Sincronizado | ✓ Sinkronizatuta | ✓ Sincronitzat | ✓ Synchronisiert |
| `settings_reboot_label` | Redémarrer | Restart | Reiniciar | Berrabiarazi | Reiniciar | Neustart |
| `settings_reboot_action` | Redémarrer | Restart | Reiniciar | Berrabiarazi | Reiniciar | Neustart |
| `settings_reboot_in_progress` | Redémarrage… | Restarting… | Reiniciando… | Berrabiarazten… | Reiniciant… | Neustart läuft… |

---

## Presets

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `presets_title` | PRESETS | PRESETS | PRESETS | PRESETS | PRESETS | PRESETS |
| `presets_subtitle` | MODES RAPIDES | QUICK MODES | MODOS RÁPIDOS | MODU AZKARRAK | MODES RÀPIDS | SCHNELLMODI |
| `presets_item_fallback` | Preset %d | Preset %d | Preset %d | Preset %d | Preset %d | Preset %d |

---

## Notes d'implémentation

### Fichiers à créer
```
app/src/main/res/
  values/strings.xml          ← fr (langue par défaut de l'app)
  values-en/strings.xml       ← en
  values-es/strings.xml       ← es
  values-eu/strings.xml       ← eu (Basque)
  values-ca/strings.xml       ← ca (Catalan)
  values-de/strings.xml       ← de
```

### Usage dans Compose
```kotlin
// Avant
Text("RÉGLAGES")

// Après
Text(stringResource(R.string.settings_title))
```

### Chaînes avec variables
```kotlin
// %d%% PLEIN  →  stringResource(R.string.status_storage_full_pct, storagePercent)
// CARTE %s    →  stringResource(R.string.status_storage_card_label, sdStatusLabel)
// Preset %d   →  stringResource(R.string.presets_item_fallback, index)
```

### Chaînes à NE PAS traduire
- `Hilight` (terme propriétaire GoPro)
- `BLE INTERFACE PRO` (technique)
- `GOPRO CONTROLLER V4.2` (version)
- `Firmware` (terme universel)
- `Presets` (terme universel)
- Noms de presets GoPro (Standard Video, Night Lapse, etc.) → gérés par `GoProPresetMappings`

### Chaînes dynamiques (hors scope strings.xml)
Les labels de settings et leurs options (`GoProSettingsMappings.kt`) sont hors scope de cette phase.
Ils seront traités dans une phase ultérieure via un mécanisme Context-aware.

---

---

## GoProSettingsMappings — Noms des Settings (`getSettingName`)

> Ces chaînes sont retournées par `GoProSettingsMappings.getSettingName(settingId)`.
> Implémentation future : passer un `Context` ou utiliser une `Map<Int, StringRes>`.

| Clé | settingId | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|---|
| `setting_name_resolution` | 2 | Résolution | Resolution | Resolución | Bereizmena | Resolució | Auflösung |
| `setting_name_fps` | 3 | FPS | FPS | FPS | FPS | FPS | FPS |
| `setting_name_timelapse_rate` | 5 | Intervalle Timelapse | Timelapse Interval | Intervalo Timelapse | Timelapse Tartea | Interval Timelapse | Timelapse-Intervall |
| `setting_name_photo_timelapse_rate` | 30 | Intervalle Photo Timelapse | Photo Timelapse Interval | Intervalo Foto Timelapse | Argazki Timelapse Tartea | Interval Foto Timelapse | Foto-Timelapse-Intervall |
| `setting_name_night_lapse_rate` | 32 | Intervalle Nuit Accéléré | Night Lapse Interval | Intervalo Night Lapse | Night Lapse Tartea | Interval Night Lapse | Night-Lapse-Intervall |
| `setting_name_auto_power_down` | 59 | Extinction auto | Auto Power Off | Apagado auto | Auto Itzaltzea | Apagada auto | Auto-Ausschalten |
| `setting_name_gps` | 83 | GPS | GPS | GPS | GPS | GPS | GPS |
| `setting_name_lcd_brightness` | 88 | Luminosité LCD | LCD Brightness | Brillo LCD | LCD Argitasuna | Brillantor LCD | LCD-Helligkeit |
| `setting_name_led` | 91 | LEDs | LEDs | LEDs | LEDs | LEDs | LEDs |
| `setting_name_aspect_ratio` | 108 | Ratio d'aspect | Aspect Ratio | Relación de aspecto | Aspektu Erlazioa | Relació d'aspecte | Seitenverhältnis |
| `setting_name_timewarp_speed` | 111 | Vitesse TimeWarp | TimeWarp Speed | Velocidad TimeWarp | TimeWarp Abiadura | Velocitat TimeWarp | TimeWarp-Geschwindigkeit |
| `setting_name_lens` | 121 | Objectif | Lens | Objetivo | Objektiboa | Objectiu | Objektiv |
| `setting_name_photo_lens` | 122 | Objectif Photo | Photo Lens | Objetivo Foto | Argazki Objektiboa | Objectiu Foto | Foto-Objektiv |
| `setting_name_timelapse_lens` | 123 | Objectif Timelapse | Timelapse Lens | Objetivo Timelapse | Timelapse Objektiboa | Objectiu Timelapse | Timelapse-Objektiv |
| `setting_name_photo_output` | 125 | Sortie Photo | Photo Output | Salida Foto | Argazki Irteera | Sortida Foto | Fotoausgabe |
| `setting_name_media_format` | 128 | Format Média | Media Format | Formato Multimedia | Multimedia Formatua | Format Multimèdia | Medienformat |
| `setting_name_anti_flicker` | 134 | Anti-Flicker | Anti-Flicker | Anti-Flicker | Anti-Flicker | Anti-Flicker | Anti-Flicker |
| `setting_name_hypersmooth` | 135 | HyperSmooth | HyperSmooth | HyperSmooth | HyperSmooth | HyperSmooth | HyperSmooth |
| `setting_name_horizon_leveling` | 150 | Maintien de l'horizon | Horizon Leveling | Nivelación de Horizonte | Horizonte Egonkortzea | Anivellament d'Horitzó | Horizont-Stabilisierung |
| `setting_name_horizon_photo` | 151 | Horizon (Photo) | Horizon (Photo) | Horizonte (Foto) | Horizontea (Argazkia) | Horitzó (Foto) | Horizont (Foto) |
| `setting_name_hindsight` | 167 | Hindsight | Hindsight | Hindsight | Hindsight | Hindsight | Hindsight |
| `setting_name_photo_interval` | 171 | Intervalle Photo | Photo Interval | Intervalo Foto | Argazki Tartea | Interval Foto | Foto-Intervall |
| `setting_name_interval_duration` | 172 | Durée Intervalle | Interval Duration | Duración Intervalo | Tarte Iraupena | Durada Interval | Intervalldauer |
| `setting_name_perf_mode` | 173 | Mode Performance | Performance Mode | Modo Rendimiento | Errendimendu Modua | Mode Rendiment | Leistungsmodus |
| `setting_name_control_mode` | 175 | Mode de contrôle | Control Mode | Modo de control | Kontrol Modua | Mode de control | Steuerungsmodus |
| `setting_name_easy_mode_speed` | 176 | Vitesse Easy Mode | Easy Mode Speed | Velocidad Easy Mode | Easy Mode Abiadura | Velocitat Easy Mode | Easy-Mode-Geschwindigkeit |
| `setting_name_night_photo` | 177 | Photo Nuit | Night Photo | Foto Nocturna | Gau Argazkia | Foto Nocturna | Nachtfoto |
| `setting_name_wifi_band` | 178 | Bande WiFi | WiFi Band | Banda WiFi | WiFi Banda | Banda WiFi | WLAN-Band |
| `setting_name_star_trails` | 179 | Longueur Filés | Trail Length | Longitud de Rastros | Arrastoen Luzera | Longitud de Rastres | Schleierlänge |
| `setting_name_system_video_mode` | 180 | Mode Vidéo Système | System Video Mode | Modo Vídeo Sistema | Sistema Bideo Modua | Mode Vídeo Sistema | System-Videomodus |
| `setting_name_bit_rate` | 182 | Débit | Bit Rate | Tasa de bits | Bit Tasa | Taxa de bits | Bitrate |
| `setting_name_bit_depth` | 183 | Profondeur de bits | Bit Depth | Profundidad de bits | Bit Sakontasuna | Profunditat de bits | Bittiefe |
| `setting_name_video_profile` | 184 | Profil vidéo | Video Profile | Perfil de vídeo | Bideo Profila | Perfil de vídeo | Videoprofil |
| `setting_name_easy_video_mode` | 186 | Mode Vidéo Easy | Easy Video Mode | Modo Vídeo Easy | Easy Bideo Modua | Mode Vídeo Easy | Easy-Videomodus |
| `setting_name_lapse_mode` | 187 | Mode Timelapse | Timelapse Mode | Modo Timelapse | Timelapse Modua | Mode Timelapse | Timelapse-Modus |
| `setting_name_max_lens_mod` | 189 | Mod Objectif Max | Max Lens Mod | Mod Objetivo Max | Max Objektibo Moda | Mod Objectiu Max | Max-Objektiv-Mod |
| `setting_name_max_lens_mod_active` | 190 | Mod Objectif Max (actif) | Max Lens Mod (active) | Mod Objetivo Max (activo) | Max Objektibo Moda (aktibo) | Mod Objectiu Max (actiu) | Max-Objektiv-Mod (aktiv) |
| `setting_name_easy_night_photo` | 191 | Photo Nuit Easy | Easy Night Photo | Foto Nocturna Easy | Easy Gau Argazkia | Foto Nocturna Easy | Easy-Nachtfoto |
| `setting_name_multi_shot_ratio` | 192 | Ratio Multi-Shot | Multi-Shot Ratio | Relación Multi-Shot | Multi-Shot Erlazioa | Relació Multi-Shot | Multi-Shot-Verhältnis |
| `setting_name_framing` | 193 | Cadrage | Framing | Encuadre | Enkoadraketa | Enquadrament | Bildausschnitt |
| `setting_name_camera_mode` | 194 | Mode Caméra | Camera Mode | Modo Cámara | Kamera Modua | Mode Càmera | Kameramodus |
| `setting_name_beep_volume` | 216 | Volume Bip | Beep Volume | Volumen Pitido | Bip Bolumena | Volum Bip | Signalton-Lautstärke |
| `setting_name_screen_saver` | 219 | Économiseur d'écran | Screen Saver | Salvapantallas | Pantaila Babeslea | Estalvi de pantalla | Bildschirmschoner |
| `setting_name_language` | 223 | Langue | Language | Idioma | Hizkuntza | Idioma | Sprache |
| `setting_name_photo_mode` | 227 | Mode Photo | Photo Mode | Modo Foto | Argazki Modua | Mode Foto | Fotomodus |
| `setting_name_video_framing` | 232 | Cadrage Vidéo | Video Framing | Encuadre Vídeo | Bideo Enkoadraketa | Enquadrament Vídeo | Video-Bildausschnitt |
| `setting_name_multi_shot_framing` | 233 | Cadrage Multi-Shot | Multi-Shot Framing | Encuadre Multi-Shot | Multi-Shot Enkoadraketa | Enquadrament Multi-Shot | Multi-Shot-Bildausschnitt |
| `setting_name_frame_rate` | 234 | Fréquence d'images | Frame Rate | Frecuencia de imagen | Irudi Maiztasuna | Freqüència d'imatges | Bildrate |

---

## GoProSettingsMappings — Valeurs d'Options (translatable uniquement)

> ⚠️ **Ne pas traduire** les termes suivants (noms de marque GoPro / termes techniques universels) :
> `4K`, `2.7K`, `1080`, `720`, `FPS`, `Wide`, `Linear`, `SuperView`, `HyperView`, `Narrow`,
> `Max SuperView`, `HyperSmooth`, `TimeWarp`, `Hindsight`, `NTSC`, `PAL`, `HDR`, `Log`,
> `HLG HDR`, `Max Lens`, `SuperPhoto`, `Standard`, `Raw`, `Easy`, `Pro`, `Auto`,
> `Boost`, `High`, `Low`, toutes les valeurs numériques (ratios, Hz, fps, délais en secondes/minutes).

### Vocabulaire commun (réutilisé dans plusieurs settings)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_off` | Off | Off | Desactivado | Itzalita | Desactivat | Aus |
| `opt_on` | On | On | Activado | Aktibatuta | Activat | Ein |
| `opt_never` | Jamais | Never | Nunca | Inoiz ez | Mai | Nie |
| `opt_disabled` | Désactivé | Disabled | Desactivado | Desgaituta | Desactivat | Deaktiviert |
| `opt_enabled` | Activé | Enabled | Activado | Gaituta | Activat | Aktiviert |
| `opt_locked` | Verrouillé | Locked | Bloqueado | Blokeatuta | Bloquejat | Gesperrt |
| `opt_auto` | Auto | Auto | Auto | Auto | Auto | Auto |
| `opt_none` | Aucun | None | Ninguno | Bat ere ez | Cap | Keiner |
| `opt_low` | Faible | Low | Bajo | Txikia | Baix | Niedrig |
| `opt_medium` | Moyen | Medium | Medio | Ertaina | Mitjà | Mittel |
| `opt_high_vol` | Fort | High | Alto | Altua | Alt | Hoch |
| `opt_short` | Court | Short | Corto | Laburra | Curt | Kurz |
| `opt_long` | Long | Long | Largo | Luzea | Llarg | Lang |
| `opt_max` | Max | Max | Máx. | Max | Màx. | Max |

### Auto Power Down & Screen Saver (durées)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_duration_8sec` | 8 Sec | 8 Sec | 8 Seg | 8 seg | 8 Seg | 8 Sek |
| `opt_duration_30sec` | 30 Sec | 30 Sec | 30 Seg | 30 seg | 30 Seg | 30 Sek |
| `opt_duration_1min` | 1 Min | 1 Min | 1 Min | 1 min | 1 Min | 1 Min |
| `opt_duration_2min` | 2 Min | 2 Min | 2 Min | 2 min | 2 Min | 2 Min |
| `opt_duration_3min` | 3 Min | 3 Min | 3 Min | 3 min | 3 Min | 3 Min |
| `opt_duration_5min` | 5 Min | 5 Min | 5 Min | 5 min | 5 Min | 5 Min |
| `opt_duration_15min` | 15 Min | 15 Min | 15 Min | 15 min | 15 Min | 15 Min |
| `opt_duration_30min` | 30 Min | 30 Min | 30 Min | 30 min | 30 Min | 30 Min |
| `opt_duration_1h` | 1 Heure | 1 Hour | 1 Hora | 1 ordu | 1 Hora | 1 Std |
| `opt_duration_2h` | 2 Heures | 2 Hours | 2 Horas | 2 ordu | 2 Hores | 2 Std |
| `opt_duration_3h` | 3 Heures | 3 Hours | 3 Horas | 3 ordu | 3 Hores | 3 Std |

### Media Format (setting 128)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_timelapse_video` | Vidéo Accéléré | Timelapse Video | Vídeo Timelapse | Timelapse Bideoa | Vídeo Timelapse | Zeitraffer-Video |
| `opt_timelapse_photo` | Photo Accéléré | Timelapse Photo | Foto Timelapse | Timelapse Argazkia | Foto Timelapse | Zeitraffer-Foto |
| `opt_night_lapse_photo` | Photo Nuit Accéléré | Night Lapse Photo | Foto Night Lapse | Night Lapse Argazkia | Foto Night Lapse | Nacht-Lapse-Foto |
| `opt_night_lapse_video` | Vidéo Nuit Accéléré | Night Lapse Video | Vídeo Night Lapse | Night Lapse Bideoa | Vídeo Night Lapse | Nacht-Lapse-Video |

### Video Performance Mode (setting 173)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_perf_max` | Perf. Max | Max Performance | Rend. Máx. | Errendimendu Max | Rend. Màx. | Max-Leistung |
| `opt_perf_extended_battery` | Batterie étendue | Extended Battery | Batería extendida | Bateria Hedatua | Bateria estesa | Erweiterter Akku |
| `opt_perf_tripod` | Trépied / Stationnaire | Tripod / Stationary | Trípode / Estacionario | Tripodea / Geldirik | Trípode / Estacionari | Stativ / Stationär |

### System Video Mode (setting 180)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_quality_max` | Qualité max | Max Quality | Calidad máx. | Kalitate Max | Qualitat màx. | Max-Qualität |
| `opt_quality_standard` | Qualité standard | Standard Quality | Calidad estándar | Kalitate Estandarra | Qualitat estàndard | Standardqualität |
| `opt_quality_basic` | Qualité basique | Basic Quality | Calidad básica | Oinarrizko Kalitatea | Qualitat bàsica | Grundqualität |
| `opt_battery_extended` | Batterie étendue | Extended Battery | Batería extendida | Bateria Hedatua | Bateria estesa | Erweiterter Akku |
| `opt_battery_long` | Batterie longue | Long Battery | Batería larga | Bateria Luzea | Bateria llarga | Langer Akku |

### Easy Video Mode (setting 186)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_video_standard` | Vidéo standard | Standard Video | Vídeo estándar | Bideo Estandarra | Vídeo estàndard | Standard-Video |
| `opt_video_hdr` | Vidéo HDR | HDR Video | Vídeo HDR | HDR Bideoa | Vídeo HDR | HDR-Video |

### Lapse Mode (setting 187)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_star_trails` | Filés d'étoiles | Star Trails | Trazos de Estrellas | Izar Arrastoak | Rastres d'Estrelles | Sternenspuren |
| `opt_vehicle_lights` | Feux de véhicules | Vehicle Lights | Luces de Vehículos | Ibilgailu Argiak | Llums de Vehicles | Fahrzeuglichter |
| `opt_max_star_trails` | Max Filés d'étoiles | Max Star Trails | Max Trazos Estrellas | Max Izar Arrastoak | Max Rastres Estrelles | Max-Sternenspuren |
| `opt_max_vehicle_lights` | Max Feux de véhicules | Max Vehicle Lights | Max Luces Vehículos | Max Ibilgailu Argiak | Max Llums Vehicles | Max-Fahrzeuglichter |

### Max Lens Mod (setting 189)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_standard_lens` | Objectif standard | Standard Lens | Objetivo estándar | Objektibo Estandarra | Objectiu estàndard | Standard-Objektiv |
| `opt_auto_detect` | Détection auto | Auto Detect | Detección auto | Auto Detekzioa | Detecció auto | Auto-Erkennung |

### Easy Night Photo (setting 191) & Photo Mode (setting 227)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_night_photo` | Photo Nuit | Night Photo | Foto Nocturna | Gau Argazkia | Foto Nocturna | Nachtfoto |
| `opt_burst` | Rafale | Burst | Ráfaga | Erradioa | Ràfega | Serienaufnahme |

### Beep Volume (setting 216)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_vol_low` | Faible | Low | Bajo | Txikia | Baix | Niedrig |
| `opt_vol_medium` | Moyen | Medium | Medio | Ertaina | Mitjà | Mittel |
| `opt_vol_high` | Fort | High | Alto | Altua | Alt | Hoch |

### Star Trails Length (setting 179)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_trail_short` | Court | Short | Corto | Laburra | Curt | Kurz |
| `opt_trail_long` | Long | Long | Largo | Luzea | Llarg | Lang |

### Camera Mode (setting 194)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_single_lens` | Objectif simple | Single Lens | Objetivo único | Objektibo Bakarra | Objectiu únic | Einzelobjektiv |

### Video Horizon Leveling (settings 150, 151)

| Clé | fr | en | es | eu | ca | de |
|---|---|---|---|---|---|---|
| `opt_horizon_locked` | Verrouillé | Locked | Bloqueado | Blokeatuta | Bloquejat | Gesperrt |
