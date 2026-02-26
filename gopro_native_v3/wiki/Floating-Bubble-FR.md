# La bulle flottante

La bulle flottante est la fonctionnalité signature d'**AirBubble**. Elle reste visible par-dessus toutes les applications — carte, instruments de vol, GPS, messages — et te permet de contrôler ta caméra **sans jamais revenir dans l'app**.

---

## Activer la bulle

1. Ouvre AirBubble et connecte-toi à ta GoPro
2. Va dans **Réglages** → active **Bulle Flottante**
3. Autorise "Afficher par-dessus d'autres apps" si Android le demande
4. La bulle apparaît en haut à gauche de l'écran

> Tu peux maintenant appuyer sur le bouton Home ou passer à n'importe quelle autre app — la bulle reste là.

---

## Les états visuels

| Couleur de la bordure | État |
|----------------------|------|
| 🔘 Gris | Déconnecté de la caméra |
| 🔵 Cyan | Connecté, prêt |
| 🔴 Rouge | **Enregistrement en cours** |
| 🔴 Rouge + chrono | Enregistrement avec minuteur actif |

La pastille à gauche de la bulle indique :
- 🟢 Vert : connecté
- 🔴 Rouge clignotant : en train d'enregistrer

---

## Les gestes

| Geste | Action |
|-------|--------|
| **Tap simple** _(en enregistrement)_ | Ajoute un **HiLight** sur la vidéo |
| **Tap simple** _(connecté, pas en record)_ | Aucune action |
| **Double tap** | Ouvre l'app AirBubble au premier plan |
| **Appui long** _(connecté)_ | Démarre / arrête l'enregistrement |
| **Appui long** _(déconnecté)_ | Tente une reconnexion manuelle |
| **Glisser vers le bas** | Fait apparaître la zone de fermeture |

---

## Fermer la bulle

Pour fermer la bulle :
1. **Glisse-la vers le bas** de l'écran
2. Une zone rouge "× CLOSE" apparaît en bas
3. Lâche la bulle **dans cette zone** pour la fermer

Ou depuis l'app : **Réglages → Bulle Flottante → désactiver**.

---

## La bulle et le minuteur

Si tu as activé le **mode minuteur** dans le Dashboard :
- La bulle affiche un **chrono dégressif** en mode `RECORDING_TIMER` (ex : `00:45`)
- L'enregistrement s'arrête automatiquement quand le chrono atteint `00:00`
- La bulle repasse en état "connecté" (cyan)

→ [Configurer le minuteur](Settings-Presets-FR#minuteur)

---

## Conseils pratiques

**Pour le parapente :**
Lance l'enregistrement avant de courir au décollage → active la bulle → glisse ton téléphone dans la poche. Un tap suffit pour HiLight en vol.

**Pour le VTT / ski :**
Appui long sur la bulle = start/stop record sans avoir à chercher le bouton dans l'app.

**Pour le kayak :**
Le double-tap pour rouvrir l'app fonctionne même avec des gants.

---

## Permissions nécessaires

La bulle flottante nécessite la permission **"Afficher par-dessus d'autres applications"** (`SYSTEM_ALERT_WINDOW`). C'est une permission spéciale sur Android — Android te demandera de l'autoriser explicitement dans les paramètres.

Cette permission est **uniquement** utilisée pour afficher la bulle — jamais pour lire le contenu d'autres apps.
