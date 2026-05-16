package com.ximun.gopropro.ble

/**
 * Génération de protocole BLE GoPro.
 *
 * - OPEN_GOPRO : Hero 9 et plus récents. Protocole OpenGoPro v2 :
 *   subscriptions async (0x52/0x53/0x62), Claim Control protobuf (0xF1),
 *   presets protobuf (0xF5), capabilities dynamiques.
 *
 * - LEGACY : Hero 5/6/7/8. Mêmes UUIDs BLE, mais pas de subscriptions
 *   async ni de protobuf. Commandes basiques (shutter 0x01, hilight 0x18,
 *   sleep 0x05, keep-alive 0x5B, set date 0x0D) fonctionnent. Settings et
 *   presets sont gérés différemment (réglages globaux via mode/sub-mode,
 *   pas de système de presets dynamiques) : on désactive les onglets
 *   Réglages/Presets dans l'UI.
 */
enum class CameraGeneration {
    UNKNOWN,
    LEGACY,
    OPEN_GOPRO;

    val isLegacy: Boolean get() = this == LEGACY
    val isOpenGoPro: Boolean get() = this == OPEN_GOPRO
}

/**
 * Détection de la génération à partir du nom BLE annoncé ou du modèle
 * retourné par CMD_GET_HARDWARE_INFO (0x3C).
 *
 * Conventions de nommage observées :
 *  - "GoPro 5", "Hero5 Black", "GoPro 6", "Hero7 Silver", "GoPro 8 Black" → LEGACY
 *  - "GoPro 9 Black", "GoPro 10 Black", "HERO11 Black", "HERO11 Black Mini",
 *    "HERO12 Black", "HERO13 Black" → OPEN_GOPRO
 *
 * Références :
 *  - https://github.com/KonradIT/gopro-ble-py
 *  - https://github.com/KonradIT/goprowifihack/blob/master/Bluetooth/bluetooth-api.md
 */
object CameraGenerationDetector {

    private val LEGACY_TOKENS = listOf("hero5", "hero6", "hero7", "hero8")
    private val LEGACY_NUMBER_TOKENS = listOf(" 5", " 6", " 7", " 8")
    private val MODERN_TOKENS = listOf(
        "hero9", "hero10", "hero11", "hero12", "hero13",
        " 9 ", " 10 ", " 11 ", " 12 ", " 13 "
    )

    fun detect(name: String?): CameraGeneration {
        if (name.isNullOrBlank()) return CameraGeneration.UNKNOWN
        val lower = name.lowercase().replace("-", "").replace("_", "")
        // Pad pour que les tokens " 5 " etc. matchent en début/fin de chaîne.
        val padded = " $lower "

        if (MODERN_TOKENS.any { padded.contains(it) }) return CameraGeneration.OPEN_GOPRO
        if (LEGACY_TOKENS.any { padded.contains(it) }) return CameraGeneration.LEGACY
        if (LEGACY_NUMBER_TOKENS.any { padded.contains(it) }) return CameraGeneration.LEGACY

        // Par défaut : OpenGoPro (sécurise les caméras récentes inconnues —
        // une caméra moderne avec un nom non reconnu reste fonctionnelle,
        // alors qu'une legacy mal détectée bloquerait sur les subscriptions).
        return CameraGeneration.OPEN_GOPRO
    }
}
