# DAY J — Guide terrain : calibrage émulateur avec X3 réelle

> Pas d'internet requis. Tout est préparé. Suis les étapes dans l'ordre.

---

## AVANT DE PARTIR (à faire chez toi)

- [ ] APK airbuble installé sur le téléphone (`adb install -r airbuble-debug.apk`)
- [ ] Android en **mode développeur** activé (Options développeur → Débogage USB ON)
- [ ] **Bluetooth HCI snoop log** activé : Options développeur → Activer le journal HCI Bluetooth
- [ ] Câble USB + PC (pour `adb`)
- [ ] Ce dossier sur ton PC : `actioncam_emulator/`

---

## ÉTAPE 1 — Préparer la capture logcat

Brancher le téléphone en USB, puis dans un terminal :

```bash
# Vider l'ancien logcat
adb logcat -c

# Lancer la capture en arrière-plan (dans un nouveau terminal)
adb logcat -s Insta360Debug > x3_capture.log
```

Laisser ce terminal ouvert pendant toute la session.

---

## ÉTAPE 2 — Connecter la X3 à airbuble

1. Allumer la X3
2. Lancer airbuble sur le téléphone
3. Appuyer **Scan** → sélectionner la X3 dans la liste
4. Attendre que le Dashboard s'affiche (câble BLE → WiFi → connecté)

**Ce qui se passe automatiquement :**
- `[INFO]` : firmware, type caméra, n° série → loggé
- `[CONFIG]` + `[MODE]` : liste des modes → loggés
- `[SETTING]` : settings par mode → loggés
- `[BATTERY]` + `[STORAGE]` : état initial → loggés

---

## ÉTAPE 3 — Explorer tous les modes

Dans l'onglet **Réglages** de airbuble :

1. Passer en mode **Vidéo normale** → attendre 2s
2. Passer en mode **Timelapse** → attendre 2s
3. Passer en mode **Photo** → attendre 2s
4. Passer en mode **Vidéo HDR** → attendre 2s
5. Passer en mode **Bullet Time** → attendre 2s
6. Parcourir tous les modes disponibles

> Chaque changement de mode déclenche un nouveau dump `[SETTING]` dans le log.

---

## ÉTAPE 4 — Tester les fonctions principales

1. **Enregistrer une vidéo** (10 sec) → vérifier le timer dans l'app
2. **Prendre une photo**
3. **Vérifier l'onglet Statut** : batterie %, stockage Go, firmware affiché

---

## ÉTAPE 5 — Récupérer les logs

```bash
# Arrêter la capture logcat (Ctrl+C dans le terminal de capture)

# Récupérer le HCI snoop log (BLE)
adb bugreport bugreport.zip
# Extraire de bugreport.zip : FS/data/misc/bluetooth/logs/btsnoop_hci.log
# Ouvrir dans Wireshark, filtrer : btatt
# Chercher les 3 notifications TLV (type 0x01 SSID, 0x02 pwd, 0x03 IP)
```

---

## ÉTAPE 6 — Analyser (de retour chez toi)

```bash
cd actioncam_emulator

# Comparaison offline (émulateur éteint — suffit pour calibrer le YAML)
python compare_emulator.py x3_capture.log

# Avec émulateur actif (comparaison complète)
python main.py insta360_x3 --no-tui &
python compare_emulator.py x3_capture.log --emulator-url http://localhost:8080

# Le rapport compare_report.txt liste tous les écarts à corriger
```

---

## ÉTAPE 7 — Corriger le profil

Ouvrir `config/profiles/insta360_x3.yaml` et corriger selon `compare_report.txt` :

| Ce que le rapport dit | Ce qu'il faut changer dans le YAML |
|---|---|
| `firmware → réel=X profil=Y` | `firmware: X` |
| `cameraType → réel=X` | `model: X` (et `serial:`, `wifi.ssid:`) |
| `MODE MANQUANT : "FOO"` | Ajouter le mode dans `capture.modes` |
| `MODE EN TROP : "BAR"` | Supprimer le mode de `capture.modes` |
| `sd_remaining_kb` | Mettre à jour `initial_state.sd_remaining_kb` |

---

## ÉTAPE 8 — Valider

```bash
# Tests régression
python -m pytest tests/ -v

# Doit afficher : 73 passed (ou plus si nouveaux tests ajoutés)
```

Si tout est vert : **émulateur calibré pour X3** ✓

> Pour un futur test avec X4 ou X5 : même procédure, adapter le nom du profil.
> Profils disponibles : `insta360_x3.yaml`, `insta360_x4.yaml`, `insta360_x5.yaml`

---

## Notes BLE (optionnel, pour calibrage fin)

Dans Wireshark avec `btsnoop_hci.log` :

- Filtrer : `btatt`
- Chercher les notifications vers la char `0x3377`
- 3 paquets consécutifs = credentials WiFi :
  - `[0x01, len, SSID]`
  - `[0x02, len, password]`
  - `[0x03, len, IP]`

Comparer avec ce que l'émulateur envoie dans `ble/insta360/gatt_services.py`.
Si format différent → corriger `_send_wifi_credentials()`.

---

## Checklist rapide terrain

```
[ ] APK installé + HCI snoop activé
[ ] adb logcat -s Insta360Debug > x3_capture.log  (lancé)
[ ] X3 connectée, Dashboard visible
[ ] Tous les modes parcourus
[ ] Vidéo enregistrée, photo prise
[ ] adb bugreport bugreport.zip  (récupéré)
[ ] x3_capture.log  (récupéré)
```
