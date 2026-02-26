# FAQ — Questions fréquentes

---

## Connexion

**L'app ne trouve pas ma GoPro**
- Vérifie que le Bluetooth de la GoPro est activé _(Réglages GoPro → Connexions → Bluetooth)_
- Assure-toi que le Bluetooth du téléphone est actif
- Rapproche-toi de la caméra (moins de 10 m)
- Redémarre la GoPro et relance le scan

**La connexion s'établit puis se coupe immédiatement**
- C'est souvent un problème de Bluetooth instable. Redémarre les deux appareils et réessaie.
- Vérifie que tu n'as pas d'autre app qui utilise le Bluetooth en même temps (GoPro Quik, etc.)

**"Connexion en cours…" tourne pendant très longtemps**
- L'app scan jusqu'à 30 secondes. Si rien n'est trouvé, elle retourne en état "prêt".
- Vérifie les permissions Bluetooth et Localisation dans Paramètres → Apps → AirBubble.

**La caméra se déconnecte toute seule**
- Ta GoPro a une option **extinction automatique** (8s, 30s, 1 min…). Désactive-la ou augmente la durée : Réglages → Préférences → Extinction auto → Jamais.
- AirBubble envoie un keep-alive toutes les 3 secondes pour éviter ça, mais si la GoPro est configurée à 8s, ça peut quand même couper.

**L'app se reconnecte même après que j'ai éteint la caméra volontairement**
- C'est un comportement normal si tu utilises le bouton Power de la GoPro. AirBubble détecte ce cas depuis la version actuelle et **ne tente plus de reconnexion**.
- Si la reconnexion se déclenche quand même, c'est un bug — ouvre une issue GitHub.

---

## Enregistrement

**Le bouton START CAPTURE ne répond pas**
- Vérifie que la caméra est bien connectée (pastille verte dans la bulle)
- La caméra est peut-être occupée (chargement d'un preset…) — attends 2 secondes

**L'enregistrement démarre mais la caméra ne filme pas**
- Vérifie que la carte SD est insérée et non pleine
- Vérifie le preset actif (onglet Presets) — certains presets ne démarrent pas en vidéo

**Le minuteur ne se déclenche pas**
- Le mode minuteur doit être **activé** dans le Dashboard (icône horloge)
- La valeur doit être supérieure à 0 (régle avec les boutons +/-)

---

## GPS & GPX

**Le fichier GPX contient des waypoints "(no GPS)"**
- Le GPS n'avait pas encore acquis un fix au moment de l'événement. Ce n'est pas une erreur — les données horodatées sont quand même enregistrées.
- Solution : attends 30–60 secondes après avoir ouvert AirBubble avant de commencer à enregistrer.

**Je ne trouve pas les fichiers GPX**
- Ouvre le gestionnaire de fichiers du téléphone → Stockage interne → Documents → GoProPro → GPX
- Sur certains téléphones Samsung, cherche dans "Mes Fichiers"

**Le GPX s'ouvre dans le mauvais logiciel**
- Assure-toi d'avoir un lecteur GPX installé (Google Earth, GaiaGPS…)
- Sur PC, copie le fichier via USB et ouvre-le avec Viking ou GPXSee

**Aucun fichier GPX n'a été créé**
- AirBubble supprime automatiquement les fichiers vides (session sans enregistrement ni HiLight)
- Vérifie que la permission Localisation est accordée à AirBubble

---

## Bulle flottante

**La bulle ne s'affiche pas**
- Vérifie que la permission "Afficher par-dessus d'autres applications" est accordée : Paramètres → Apps → AirBubble → Afficher par-dessus

**La bulle disparaît quand je reçois un appel**
- C'est un comportement Android normal. La bulle reprend sa position après l'appel.

**Je n'arrive pas à fermer la bulle**
- Glisse la bulle lentement vers le bas — la zone rouge apparaît. Lâche la bulle dans la zone.
- Ou : rouvre AirBubble (double-tap sur la bulle) → Réglages → désactive la Bulle Flottante.

---

## Caméras compatibles

**Ma GoPro HERO 8 fonctionne-t-elle ?**
- La HERO 8 utilise une version antérieure du protocole BLE GoPro. Elle peut fonctionner partiellement mais n'est pas officiellement supportée.

**Ma GoPro MAX / HERO 13 fonctionne-t-elle ?**
- Ces modèles utilisent le même protocole Open GoPro BLE et devraient fonctionner. Ils ne sont pas encore officiellement testés — retours bienvenus !

**Mon Insta360 fonctionne-t-il ?**
- Pas encore. Le support Insta360 X3 est prévu dans une future version.

---

## Autre

**L'app consomme beaucoup de batterie**
- Le Bluetooth BLE est très économe. La consommation principale vient du **GPS continu** et de la bulle flottante (service en premier plan). En vol longue distance, tu peux couper le GPS depuis les paramètres Android si le GPX n'est pas prioritaire.

**Je veux changer la langue de l'app**
- Réglages (onglet bas) → section Langue → sélectionne ta langue

**L'app est-elle open source ?**
- Le code source sera publié sur GitHub. Consulte le dépôt pour l'état actuel.
