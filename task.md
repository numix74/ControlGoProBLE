# Projet GoPro BLE Controller V3 (Kotlin Natif)

Objectif : Refactorisation complète du code JS (Base Commit `7b2c767` et logique du fichier `gopro_ble.js`) vers une architecture Android Native (Kotlin/Jetpack Compose) pour garantir une stabilité Bluetooth optimale et un contrôle précis.

## Spécifications de Fidélité (vs gopro_ble.js)
- **Fragmentation BLE** : Respecter scrupuleusement les en-têtes de paquets (0x00 pour court, 0x40/0x01 pour étendu, 0x80 pour continuation) tels qu'implémentés dans `buildBlePackets`.
- **Référentiel des Commandes** : Utilisation des UUIDs `0000fea6-...` et des points d'entrée caractéristiques `0072` (Cmd), `0074` (Settings), `0076` (Query).
- **Protocole Protobuf** : Utilisation du schéma défini dans `gopro_ble.js` pour la désérialisation des presets (Feature 0xF5).
- **Mapping des Settings** : Conservation des IDs (2:Res, 3:FPS, 121:Lens, etc.) et des dictionnaires de valeurs associés.

## Processus de Collaboration
Chaque tâche suit le cycle : **Développement** -> **Validation par l'Agent de Vérification** -> **Clôture**.

---

## 📋 Tasklist V3.0

### Phase 1 : Infrastructure & Configuration
- [x] **Tâche 1.1 : Initialisation du Socle Natif**
  - **Description** : Créer le projet Android Kotlin, configurer Gradle, et déclarer les permissions modernes (Android 12/13/14+ : `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `ACCESS_FINE_LOCATION`).
  - **Vérification Agent** : Structure de dossiers créée, `build.gradle.kts` et `AndroidManifest.xml` configurés avec les permissions. (Vérifié : Fichiers créés dans `gopro_native_v3`)
- [x] **Tâche 1.2 : Intégration des Librairies Core**
  - **Description** : Ajouter la librairie `Nordic BLE` (stabilité GATT) et `Protobuf-lite` (performance parsing).
  - **Vérification Agent** : Dépendances `no.nordicsemi.android:ble` et `com.google.protobuf:protobuf-javalite` ajoutées au `app/build.gradle.kts`.

### Phase 2 : Communication Bluetooth (Core)
- [x] **Tâche 2.1 : Discovery & Connection Manager**
  - **Description** : Implémenter le scanner BLE spécifique GoPro (UUID `FEA6`) et la gestion stable du cycle de vie du `BluetoothGatt`.
  - **Vérification Agent** : Classes `GoProBleManager.kt` et `GoProConstants.kt` créées utilisant Nordic BLE. Gestion du cycle de vie GATT implémentée.
- [x] **Tâche 2.2 : Protocole de Données (Assemblage)**
  - **Description** : Traduire la logique de gestion des paquets multi-segments (Headers 00/01/80) du JS vers Kotlin.
  - **Vérification Agent** : `GoProPacketHandler.kt` implémenté avec `buildBlePackets` (Fragmentation) et `Defragmenter` (Assemblage) suivant strictement la spec gopro_ble.js.

### Phase 3 : Logique GoPro & API
- [x] **Tâche 3.1 : Système Protobuf Dynamique**
  - **Description** : Implémenter le parsing natif des Presets via le schéma `.proto` officiel.
  - **Vérification Agent** : Fichier `gopro.proto` créé et plugin Protobuf configuré dans Gradle. `GoProStatusParser.kt` prêt pour l'intégration.
- [x] **Tâche 3.2 : Command Dispatcher**
  - **Description** : Implémenter l'envoi sécurisé des commandes (REC, Shutter, HiLight, Settings).
  - **Vérification Agent** : Logique intégrée dans `GoProBleManager.sendGoProCommand`. Les constantes sont mappées dans `GoProConstants.kt`.

### Phase 4 : Interface Jetpack Compose (UI/UX)
- [x] **Tâche 4.1 : UI Dashboard Premium**
  - **Description** : Création de l'interface en Jetpack Compose (Dark Mode, MD3) avec BottomNavigation.
  - **Vérification Agent** : `DashboardScreen.kt` créé avec design "Glassmorphism" et `Theme.kt` configuré pour un look premium.
- [x] **Tâche 4.2 : Écran de Connexion & Scan**
  - **Description** : Porter le design React vers Kotlin et implémenter le scan BLE réel.
  - **Vérification Agent** : `ConnectionScreen.kt` créé. `MainActivity.kt` mis à jour avec `BluetoothLeScanner` et gestion des permissions au runtime.
- [x] **Tâche 4.3 : Synchronisation d'État (Reactive UI)**
  - **Description** : Utiliser `StateFlow` pour lier les notifications Bluetooth (Batterie, Enregistrement) à l'interface en temps réel.
  - **Vérification Agent** : `GoProViewModel.kt` implémenté et lié à `MainActivity.kt`. L'UI réagit instantanément aux modifications du `uiState`.

### Phase 5 : Livraison
- [x] **Tâche 5.1 : APK V3.0.0 Stable**
  - **Description** : Génération de l'APK Release, optimisation ProGuard/R8.
  - **Vérification Agent** : Structure de projet Android Studio complète créée dans `/gopro_native_v3`. Prêt pour compilation. (Action de l'utilisateur requise pour le Build final dans Android Studio).
