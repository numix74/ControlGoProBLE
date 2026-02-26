# Stratégie réseaux sociaux — AirBubble

## Objectif

Toucher les **sportifs outdoor** qui utilisent une GoPro et cherchent une solution de contrôle à distance simple, sans Wi-Fi, avec export GPS.
Cibles prioritaires : parapentistes, vttistes, kayakistes, skieurs, surfeurs.

---

## Phase 1 — Lancement (J-7 à J0 : juste avant la release Play Store)

### Reddit (gratuit, très efficace pour les niches outdoor)

**Subreddits cibles :**

| Subreddit | Membres | Angle |
|-----------|---------|-------|
| r/gopro | ~250k | Audience principale, tech |
| r/paragliding | ~80k | Cas d'usage parfait (vol libre) |
| r/MTB | ~600k | Vélo de montagne |
| r/kayaking | ~90k | Kayak, eau vive |
| r/skiing | ~400k | Ski, snowboard |
| r/AndroidApps | ~150k | Découverte app Android |

**Template post r/gopro (EN) :**
```
Title: I built an Android app to control my GoPro via BLE — no Wi-Fi needed

Body:
Hey r/gopro! I've been paragliding for years and got tired of stopping to
grab my phone every time I wanted to start/stop recording. So I built AirBubble.

It connects to your GoPro HERO 9/10/11/12 via Bluetooth directly — no Wi-Fi,
no Quik, no pairing screen. You get:
- Start/stop recording from a floating bubble that overlays any app
- Single tap to HiLight during recording
- Live battery/storage/temperature status
- Change resolution, FPS, lens without touching the camera
- Automatic GPX export with waypoints at each HiLight/recording event

[Screenshot 1 — dashboard] [Screenshot 2 — floating bubble] [Screenshot 3 — GPX in Google Earth]

APK available here: [link] (Play Store coming soon)
Source code: [GitHub link]

Would love feedback from HERO 12 users especially — I've mainly tested on HERO 11.
```

**Template post r/paragliding (EN) :**
```
Title: AirBubble — control your GoPro in flight with a floating bubble + auto GPX export

Body:
Fellow pilots — built an app specifically for hands-free GoPro control in the air.

The floating bubble stays on top of XCTrack/XCSoar, so you can:
- Tap once to HiLight a thermal or landing
- Long press to start/stop recording
- Double-tap to open the full app

After landing, you get a .gpx file with all your HiLight positions timestamped
(lat/lon/altitude/speed/T+ since record start). Open it in Google Earth or
import to your flight log.

No Wi-Fi required. Connects via BLE in ~2 seconds.

[Link] — Tested on HERO 11 Mini, feedback for other models welcome!
```

**Règles importantes pour Reddit :**
- Ne pas spammer plusieurs subreddits le même jour
- Réponds à chaque commentaire — l'engagement booste la visibilité
- Attends au moins 72h entre les posts sur différents subreddits
- Pas de lien direct en premier commentaire sur certains subs — lis les rules

---

### Facebook (groupes spécialisés)

**Groupes cibles (FR) :**
- "Parapente France" (~50k membres)
- "GoPro France" (~30k)
- "VTT France" (~100k)
- Groupes locaux de clubs de parapente

**Template post FR :**
```
J'ai développé une app Android pour contrôler ma GoPro en vol sans Wi-Fi.

AirBubble se connecte directement à la GoPro via Bluetooth. Une bulle flottante
reste visible par-dessus n'importe quelle app (XCTrack, cartes…). En vol :
• Tap simple → HiLight
• Appui long → start/stop enregistrement
• Après l'atterrissage → fichier GPX avec tous les waypoints

Testé sur HERO 11 Mini. Dispo en APK pour l'instant, Play Store en préparation.

[Lien APK] — Retours bienvenus !
```

---

### Instagram

**Compte à créer :** `@airbubble.app` (ou `@airbubble_app`)

**Contenu pilier (répétable) :**

| Type | Fréquence | Exemple |
|------|-----------|---------|
| Screen recording bulle en vol | 1/semaine | Vidéo de l'app avec paysage en fond |
| Terrain (toi en parapente / VTT) | 1/semaine | Selfie ou séquence avec la bulle visible |
| Feature highlight | 1/semaine | Carrousel : "5 choses qu'AirBubble fait que GoPro Quik ne fait pas" |
| GPX visualisation | 1/2 semaines | Screenshot Google Earth d'un vol avec waypoints |
| Behind the scenes / dev | 1/mois | Code, tests, prototypes |

**Hashtags FR :** `#parapente #volibre #gopro #androidapp #outdoor #vtt #kayak #airbubble`
**Hashtags EN :** `#paragliding #gopro #androidapp #outdoor #mtb #goprohero #airbubble #freeflight`

**Format vidéo idéal :** Reels verticaux 9:16, 15–30 secondes, avec texte incrustés expliquant les features.

---

### YouTube

**Chaîne à créer :** `AirBubble`

**Vidéos prioritaires :**

| # | Titre | Durée | Description |
|---|-------|-------|-------------|
| 1 | "AirBubble — Full Demo in 60 seconds" | ~60s | Screen recording + terrain : connexion → bulle → vol → GPX |
| 2 | "How to connect AirBubble to your GoPro" | ~3min | Tutoriel getting started |
| 3 | "GPS GPX export explained" | ~4min | Démo GPX dans Google Earth/XCTrack |
| 4 | "AirBubble in flight — paragliding demo" | ~5min | Vraie vidéo terrain en vol |
| 5 | "AirBubble vs GoPro Quik — what's different?" | ~5min | Comparaison features |

**Shorts (< 60s) :**
- "One tap HiLight in flight"
- "Change GoPro settings without touching it"
- "GPX export after a flight — 30 seconds"

---

## Phase 2 — Post-lancement Play Store

### ASO (App Store Optimisation)

**Titre Play Store :** `AirBubble — GoPro BLE Remote`

**Description courte :** `Control your GoPro wirelessly. Floating bubble. GPS export. No Wi-Fi needed.`

**Mots-clés (EN) :** `gopro remote, gopro bluetooth, gopro controller, gopro hilight, gpx export, action camera remote, floating bubble, gopro hero`

**Mots-clés (FR) :** `télécommande gopro, bluetooth gopro, bulle flottante, export gpx, parapente`

**Screenshots Play Store (à préparer) :**
1. Connection screen — "Connect to your GoPro in seconds"
2. Dashboard — "Full control at your fingertips"
3. Floating bubble — "Works over any app"
4. Settings — "Change settings without touching the camera"
5. GPX in Google Earth — "Every HiLight. Every takeoff. On the map."

---

## Métriques à suivre

| Métrique | Outil | Objectif 3 mois |
|----------|-------|-----------------|
| Installs Play Store | Play Console | 500+ |
| Reddit upvotes / comments | Reddit | 1 post viral (50+ upvotes) |
| GitHub stars | GitHub | 50+ |
| Followers Instagram | Instagram | 200+ |
| YouTube vues (vidéo #1) | YouTube Studio | 1000+ |

---

## Timing recommandé

```
Semaine -2 : Préparer screenshots + vidéo démo 60s
Semaine -1 : Post Reddit r/gopro + r/AndroidApps (APK beta)
Jour 0 (release Play Store) : Post Reddit r/paragliding + Facebook groupes FR
Semaine +1 : YouTube tutoriel getting started
Semaine +2 : Reddit r/MTB + r/kayaking
Semaine +4 : Premier Reel Instagram terrain
```
