# GPS & Export GPX

AirBubble enregistre ta **position GPS en temps réel** pendant toute la session et génère automatiquement un fichier **GPX** avec des waypoints précis à chaque moment important.

---

## Comment ça marche

Dès la connexion à la GoPro, AirBubble :
1. Démarre le suivi GPS (via le service de localisation Google)
2. Ouvre un fichier GPX horodaté dans `Documents/GoProPro/GPX/`
3. Ajoute un waypoint à chaque événement GoPro
4. Ferme et finalise le fichier à la déconnexion

---

## Les waypoints enregistrés

| Type | Quand | Nom dans le GPX |
|------|-------|----------------|
| **REC_START** | Dès que l'enregistrement commence | `REC_START #1`, `#2`… |
| **HILIGHT** | Quand tu appuies sur HiLight | `HILIGHT #1`, `#2`… |
| **REC_STOP** | À l'arrêt de l'enregistrement _(seulement si aucun HILIGHT dans ce clip)_ | `REC_STOP #1`… |

> **Logique REC_STOP :** Si tu as posé un HiLight pendant le clip, le REC_STOP n'est pas ajouté — le HiLight suffit pour retrouver le moment. Ça évite les doublons inutiles.

---

## Données par waypoint

Chaque waypoint contient :
- **Coordonnées GPS** (latitude, longitude, altitude)
- **Heure absolue** (UTC, format ISO 8601)
- **T+** temps depuis le début de l'enregistrement _(ex : T+02:34)_
- **Vitesse** en km/h _(si disponible)_
- **Précision GPS** en mètres _(ex : acc: 4m)_

**Exemple de description dans le GPX :**
```
HILIGHT #2
2024-08-15 14:23:05 | T+02:34 | 48 km/h | acc: 6m
```

---

## Sans fix GPS

Si le GPS n'a pas encore acquis un fix au moment de l'événement (début de session, zone couverte…), le waypoint est quand même enregistré avec `lat=0/lon=0` et le nom se termine par `(no GPS)` :
```
HILIGHT #1 (no GPS)
```

Les autres données (horodatage, T+) sont toujours présentes.

---

## Où trouver les fichiers GPX

Les fichiers sont enregistrés dans le stockage interne du téléphone :

```
Documents/
└── GoProPro/
    └── GPX/
        ├── gopro_20240815_142305.gpx
        ├── gopro_20240816_091042.gpx
        └── ...
```

**Nommage :** `gopro_YYYYMMDD_HHMMSS.gpx` (heure de connexion à la caméra)

> Les fichiers **sans aucun waypoint** (session sans enregistrement ni HiLight) sont **automatiquement supprimés** pour éviter l'encombrement.

---

## Ouvrir les fichiers GPX

| Application | Plateforme | Usage |
|-------------|-----------|-------|
| **Google Earth** | Web / Android / PC | Visualisation 3D, survol |
| **GaiaGPS** | Android / iOS | Outdoor, traces topographiques |
| **XCTrack** | Android | Parapente, vario, traces vol |
| **Viking** | PC (Linux/Win) | Analyse détaillée GPS |
| **GPXSee** | PC (Win/Mac/Linux) | Lecteur GPX simple |
| **Garmin Connect** | Web | Import sur montres Garmin |
| **Strava** | Android / Web | Sport, segments |

---

## Permissions GPS

AirBubble demande la permission **Localisation (précise)** pour le suivi GPS. Cette permission est nécessaire pour :
- Enregistrer les coordonnées GPS des waypoints
- (Sur Android ≤ 11) Scanner le Bluetooth

> La localisation est **uniquement utilisée pour le GPX** — AirBubble ne transmet aucune donnée de localisation à des serveurs externes.

---

## Conseils

**Parapente :** Lance AirBubble avant de quitter le décollage — le GPS aura le temps d'acquérir un fix avant le premier enregistrement.

**Précision :** En plein air avec ciel dégagé, la précision GPS est généralement de 3–10 mètres. Sous les arbres ou dans une vallée encaissée, elle peut dépasser 20 m.

**Batterie :** Le GPS continu consomme de la batterie. En vol longue distance, active le mode économie d'énergie du téléphone si nécessaire.
