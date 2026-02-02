# 📋 Plan de Route - GoPro Studio Pro Native

## 🟢 PHASE 1 : Fondations & Connexion (COMPLÉTÉ)
- [x] Configuration du SDK Nordic BLE & Protobuf Android.
- [x] Implémentation du Scan & Connexion automatique.
- [x] Gestion des permissions Bluetooth (Android 12/13+).
- [x] Interface de connexion minimaliste et réactive.

## 🟢 PHASE 2 : Communication & Parser (COMPLÉTÉ)
- [x] Implémentation du `GoProPacketHandler` (Fragmentation 5-bit/13-bit).
- [x] Création du `GoProStatusParser` pour les réponses TLV Sync & Async.
- [x] Support des commandes HERO 9/10/11/12 (Correction des IDs 0x53, 0x52, 0x13).
- [x] Système de "Keep Alive" (GetHardwareInfo) toutes les 4s.

## 🟢 PHASE 3 : Contrôles de Capture (COMPLÉTÉ)
- [x] **Bouton Unique (Toggle)** : Logique optimiste Start/Stop.
- [x] **Hilight Moment** : Commande 0x18 sans paramètre (uniquement pendant l'enregistrement).
- [x] **Timer Automatique** : Décompte descendant avec envoi automatique du STOP à 0s.
- [x] **Interaction Timer** : Modification de la durée via boutons +/- en temps réel.

## 🟡 PHASE 4 : Architecture Multi-Onglets (EN COURS)
- [x] **DashboardLayout** : Structure Scaffold avec 4 onglets (Contrôle, Réglages, Presets, Status).
- [x] **Onglet Réglages (4.1 & 4.2)** :
    - [x] Interface Style "Navy Card" & "Teal Accent".
    - [x] Récupération dynamique des Valeurs et Capacités (Query 0x12/0x32).
    - [x] Boîte de dialogue de sélection des paramètres (Protune complet).
- [x] **Stabilité & Sync** : Correction Parser TLV (Crash #122), Keep Alive 0x5B, Subs 0x52/0x53.
- [ ] **Onglet Presets (4.3)** :
    - [x] Lister les presets de la caméra (Protobuf Feature 0xF5).
    - [x] Permettre le chargement rapide d'un preset.
- [ ] **Onglet Status (4.4)** :
    - [x] Affichage détaillé (Espace restant exact, Température, Version Firmware).

## ⚪ PHASE 5 : Refonte Esthétique & Polissage
- [ ] **Aesthetics Luxe** : Dégradés HSL, Glassmorphism sur la NavBar, Ombres portées.
- [ ] **Micro-animations** : Pulsation du bouton REC, transitions entre onglets.
- [ ] **Typographie** : Utilisation de polices modernes (Inter ou Outfit).
- [ ] **SEO & Metadata** : Optimisation finale.
