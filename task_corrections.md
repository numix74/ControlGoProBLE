# Tasklist de Correction & Optimisation V3.1 (Studio Pro)

Objectif : Finaliser l'interface "Studio Pro" en corrigeant les bugs d'interaction, en stabilisant le cycle du timer et en validant le design visuel final selon les captures d'écran.

---

## 📋 Corrections Prioritaires

### 1. Stabilisation du Système de Capture
- [ ] **Tâche 1.1 : Unification du Bouton Start/Stop**
  - **Action** : S'assurer que le bouton unique bascule parfaitement entre l'état "START" (Rouge) et "STOP" (Gris/AppCard) en fonction de l'état `isRecording` ET `isCountdownActive`.
  - **Validation** : Le clic sur STOP doit pouvoir annuler un retardateur ou arrêter un film en cours.

### 2. Dynamique du Chronographe
- [ ] **Tâche 2.1 : Synchronisation Durée / Rebours**
  - **Action** : Lier la variable `displayTime` du ViewModel pour qu'elle passe de "15s" (Rebours) à "00:01" (Durée) sans latence au moment précis du shutter.
  - **Validation** : Le texte d'en-tête doit changer de "REBOURS" à "DURÉE" au switch.
- [ ] **Tâche 2.2 : Réglage du Timer (UX)**
  - **Action** : Fixer les boutons +/- sous l'icône chronomètre pour permettre de modifier la valeur `initialTimerValue` (incrément/décrément de 5s).
  - **Validation** : La valeur doit être visible et impacter immédiatement le temps affiché en mode "Timer On".

### 3. Interface & Design "Studio Pro"
- [ ] **Tâche 3.1 : Bouton Hilight Premium**
  - **Action** : Appliquer la couleur `#CA8A04` et ajouter les icônes `AutoAwesome` (étoiles) pour correspondre à la charte graphique.
  - **Validation** : Le bouton doit être "grisé" (alpha 0.5) si aucun enregistrement n'est lancé.
- [ ] **Tâche 3.2 : Nettoyage & Allègement UI**
  - **Action** : Supprimer les anciens éléments (Header compact, espacements excessifs) pour un look plus "Studio".
  - **Validation** : Comparaison finale avec la capture d'écran `uploaded_media_0`.

### 4. Robustesse Bluetooth
- [ ] **Tâche 4.1 : Validation des Callbacks Caméra**
  - **Action** : Vérifier que `MainActivity` met bien à jour le ViewModel lors de la réception des octets de statut (ID 10 pour Rec).
  - **Validation** : L'interface doit se mettre à jour même si l'enregistrement est lancé directement sur la GoPro.

---
*Note : Cette tasklist remplace la phase 4 précédente pour se concentrer sur le polissage final.*
