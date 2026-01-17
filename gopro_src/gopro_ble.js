// --- Constantes basées sur la documentation Open GoPro ---
// UUID du Service principal GoPro BLE (Control & Query)
const GOPRO_SERVICE_UUID = "0000fea6-0000-1000-8000-00805f9b34fb"; // FEA6
const CMD_SET_SHUTTER = 0x01;
const CMD_HILIGHT = 0x18;
const GATT_RETRY_DELAY = 100; // Délai en ms avant de réessayer l'envoi

// --- NOUVEAU: Drapeau pour logs détaillés ---
const VERBOSE_LOGGING = false; // Mettre à true pour voir tous les logs commentés


 // --- Définitions Protobuf --- (Basées sur la documentation fournie) ---
 // Mettre ceci au début du fichier JS ou dans une section dédiée

    const protoDefinitions = `
    syntax = "proto2"; // Basé sur la doc indiquant v2
    package OpenGoPro; // Nom de package arbitraire pour l'organisation

    // --- ÉNUMÉRATIONS ---
    enum EnumRegisterPresetStatus {
      REGISTER_PRESET_STATUS_PRESET = 1;
      REGISTER_PRESET_STATUS_PRESET_GROUP_ARRAY = 2;
    }

    enum EnumResultGeneric {
         RESULT_UNKNOWN = 0;
         RESULT_SUCCESS = 1;
         RESULT_ILL_FORMED = 2;
         RESULT_NOT_SUPPORTED = 3;
         RESULT_ARGUMENT_OUT_OF_BOUNDS = 4;
         RESULT_ARGUMENT_INVALID = 5;
         RESULT_RESOURCE_NOT_AVAILABLE = 6;
    }

    // Ajouter TOUS les Enums nécessaires tirés de la doc
    // (EnumFlatMode, EnumPresetGroup, EnumPresetTitle, EnumPresetIcon, etc.)
    // Exemple pour quelques-uns :
     enum EnumFlatMode {
         FLAT_MODE_UNKNOWN = -1;
         FLAT_MODE_PLAYBACK = 4;
         FLAT_MODE_SETUP = 5;
         FLAT_MODE_VIDEO = 12;
         FLAT_MODE_TIME_LAPSE_VIDEO = 13;
         FLAT_MODE_LOOPING = 15;
         FLAT_MODE_PHOTO_SINGLE = 16;
         FLAT_MODE_PHOTO = 17;
         FLAT_MODE_PHOTO_NIGHT = 18;
         FLAT_MODE_PHOTO_BURST = 19;
         FLAT_MODE_TIME_LAPSE_PHOTO = 20;
         FLAT_MODE_NIGHT_LAPSE_PHOTO = 21;
         FLAT_MODE_BROADCAST_RECORD = 22; // ADDED: Manquait dans JS original vs protobuf.md
         FLAT_MODE_BROADCAST_BROADCAST = 23; // ADDED: Manquait dans JS original vs protobuf.md
         FLAT_MODE_TIME_WARP_VIDEO = 24;
         FLAT_MODE_LIVE_BURST = 25;
         FLAT_MODE_NIGHT_LAPSE_VIDEO = 26;
         FLAT_MODE_SLOMO = 27;
         FLAT_MODE_IDLE = 28;
         FLAT_MODE_VIDEO_STAR_TRAIL = 29;
         FLAT_MODE_VIDEO_LIGHT_PAINTING = 30;
         FLAT_MODE_VIDEO_LIGHT_TRAIL = 31;
         FLAT_MODE_VIDEO_BURST_SLOMO =32;
         // ... ajouter les autres si besoin ...
     }

     enum EnumPresetGroup {
        PRESET_GROUP_ID_VIDEO = 1000;
        PRESET_GROUP_ID_PHOTO = 1001;
        PRESET_GROUP_ID_TIMELAPSE = 1002;
     }

     enum EnumPresetGroupIcon {
        PRESET_GROUP_VIDEO_ICON_ID            = 0;
        PRESET_GROUP_PHOTO_ICON_ID            = 1;
        PRESET_GROUP_TIMELAPSE_ICON_ID        = 2;
        PRESET_GROUP_LONG_BAT_VIDEO_ICON_ID   = 3;
        PRESET_GROUP_ENDURANCE_VIDEO_ICON_ID  = 4;
        PRESET_GROUP_MAX_VIDEO_ICON_ID        = 5;
        PRESET_GROUP_MAX_PHOTO_ICON_ID        = 6;
        PRESET_GROUP_MAX_TIMELAPSE_ICON_ID    = 7;
        PRESET_GROUP_ND_MOD_VIDEO_ICON_ID     = 8;
        PRESET_GROUP_ND_MOD_PHOTO_ICON_ID     = 9;
        PRESET_GROUP_ND_MOD_TIMELAPSE_ICON_ID = 10;
     }

     enum EnumPresetIcon {
        PRESET_ICON_VIDEO = 0;
        PRESET_ICON_ACTIVITY = 1;
        PRESET_ICON_CINEMATIC = 2;
        PRESET_ICON_PHOTO = 3;
        PRESET_ICON_LIVE_BURST  = 4;	
        PRESET_ICON_BURST	= 5;	
        PRESET_ICON_PHOTO_NIGHT	= 6;	
        PRESET_ICON_TIMEWARP	= 7;	
        PRESET_ICON_TIMELAPSE	= 8;	
        PRESET_ICON_NIGHTLAPSE	= 9;	
        PRESET_ICON_SNAIL	= 10;	
        PRESET_ICON_VIDEO_2	= 11;	
        PRESET_ICON_PHOTO_2	= 13;	
        PRESET_ICON_PANORAMA	= 14;	
        PRESET_ICON_BURST_2	= 15;	
        PRESET_ICON_TIMEWARP_2	= 16;	
        PRESET_ICON_TIMELAPSE_2	= 17;	
        PRESET_ICON_CUSTOM	= 18;	
        PRESET_ICON_AIR	= 19;	
        PRESET_ICON_BIKE	= 20;	
        PRESET_ICON_EPIC	= 21;	
        PRESET_ICON_INDOOR	= 22;	
        PRESET_ICON_MOTOR	= 23;	
        PRESET_ICON_MOUNTED	= 24;	
        PRESET_ICON_OUTDOOR	= 25;	
        PRESET_ICON_POV	= 26;	
        PRESET_ICON_SELFIE	= 27;	
        PRESET_ICON_SKATE	= 28;	
        PRESET_ICON_SNOW	= 29;	
        PRESET_ICON_TRAIL	= 30;	
        PRESET_ICON_TRAVEL	= 31;	
        PRESET_ICON_WATER	= 32;	
        PRESET_ICON_LOOPING	= 33;	
        PRESET_ICON_STARS	= 34;	
        PRESET_ICON_ACTION	= 35;	
        PRESET_ICON_FOLLOW_CAM	= 36;	
        PRESET_ICON_SURF	= 37;	
        PRESET_ICON_CITY	= 38;	
        PRESET_ICON_SHAKY	= 39;	
        PRESET_ICON_CHESTY	= 40;	
        PRESET_ICON_HELMET	= 41;	
        PRESET_ICON_BITE	= 42;	
        PRESET_ICON_CUSTOM_CINEMATIC	= 43;	
        PRESET_ICON_VLOG	= 44;	
        PRESET_ICON_FPV	= 45;	
        PRESET_ICON_HDR	= 46;	
        PRESET_ICON_LANDSCAPE	= 47;	
        PRESET_ICON_LOG	= 48;	
        PRESET_ICON_CUSTOM_SLOMO	= 49;	
        PRESET_ICON_TRIPOD	= 50;	
        PRESET_ICON_MAX_VIDEO	= 55;	
        PRESET_ICON_MAX_PHOTO	= 56;	
        PRESET_ICON_MAX_TIMEWARP	= 57;	
        PRESET_ICON_BASIC	= 58;	
        PRESET_ICON_ULTRA_SLO_MO	= 59;	
        PRESET_ICON_STANDARD_ENDURANCE	= 60;	
        PRESET_ICON_ACTIVITY_ENDURANCE	= 61;	
        PRESET_ICON_CINEMATIC_ENDURANCE	= 62;	
        PRESET_ICON_SLOMO_ENDURANCE	= 63;	
        PRESET_ICON_STATIONARY_1	= 64;	
        PRESET_ICON_STATIONARY_2	= 65;	
        PRESET_ICON_STATIONARY_3	= 66;	
        PRESET_ICON_STATIONARY_4	= 67;	
        PRESET_ICON_SIMPLE_SUPER_PHOTO	= 70;	
        PRESET_ICON_SIMPLE_NIGHT_PHOTO	= 71;	
        PRESET_ICON_HIGHEST_QUALITY_VIDEO	= 73;	
        PRESET_ICON_STANDARD_QUALITY_VIDEO	= 74;	
        PRESET_ICON_BASIC_QUALITY_VIDEO	= 75;	
        PRESET_ICON_STAR_TRAIL	= 76;	
        PRESET_ICON_LIGHT_PAINTING	= 77;	
        PRESET_ICON_LIGHT_TRAIL	= 78;	
        PRESET_ICON_FULL_FRAME	= 79;	
        PRESET_ICON_EASY_MAX_VIDEO	= 80;	
        PRESET_ICON_EASY_MAX_PHOTO	= 81;	
        PRESET_ICON_EASY_MAX_TIMEWARP	= 82;	
        PRESET_ICON_EASY_MAX_STAR_TRAIL	= 83;	
        PRESET_ICON_EASY_MAX_LIGHT_PAINTING	= 84;	
        PRESET_ICON_EASY_MAX_LIGHT_TRAIL	= 85;	
        PRESET_ICON_MAX_STAR_TRAIL	= 89;	
        PRESET_ICON_MAX_LIGHT_PAINTING	= 90;	
        PRESET_ICON_MAX_LIGHT_TRAIL	= 91;	
        PRESET_ICON_EASY_STANDARD_PROFILE	= 100;	
        PRESET_ICON_EASY_HDR_PROFILE	= 101;	
        PRESET_ICON_BURST_SLOMO	= 102;
        PRESET_ICON_TIMELAPSE_PHOTO = 1000;
        PRESET_ICON_NIGHTLAPSE_PHOTO = 1001;
     }

     enum EnumPresetTitle {
        PRESET_TITLE_ACTIVITY = 0;
        PRESET_TITLE_STANDARD = 1;
        PRESET_TITLE_CINEMATIC = 2;
        PRESET_TITLE_PHOTO = 3;
        PRESET_TITLE_LIVE_BURST = 4;
        PRESET_TITLE_BURST = 5;
        PRESET_TITLE_NIGHT = 6;
        PRESET_TITLE_TIME_WARP = 7;
        PRESET_TITLE_TIME_LAPSE = 8;
        PRESET_TITLE_NIGHT_LAPSE = 9;
        PRESET_TITLE_VIDEO = 10;
        PRESET_TITLE_SLOMO = 11;
        PRESET_TITLE_PHOTO_2 = 13; // Note: ID 12 semble manquant dans la doc ?
        PRESET_TITLE_PANORAMA = 14;
        PRESET_TITLE_BURST_2 = 15; // Manquant dans la doc ?
        PRESET_TITLE_TIME_WARP_2 = 16;
        PRESET_TITLE_TIMELAPSE_2 = 17; // Manquant ?
        PRESET_TITLE_CUSTOM = 18;
        PRESET_TITLE_AIR = 19;
        PRESET_TITLE_BIKE = 20;
        PRESET_TITLE_EPIC = 21;
        PRESET_TITLE_INDOOR = 22;
        PRESET_TITLE_MOTOR = 23;
        PRESET_TITLE_MOUNTED = 24;
        PRESET_TITLE_OUTDOOR = 25;
        PRESET_TITLE_POV = 26;
        PRESET_TITLE_SELFIE = 27;
        PRESET_TITLE_SKATE = 28;
        PRESET_TITLE_SNOW = 29;
        PRESET_TITLE_TRAIL = 30;
        PRESET_TITLE_TRAVEL = 31;
        PRESET_TITLE_WATER = 32;
        PRESET_TITLE_LOOPING = 33;
        PRESET_TITLE_STARS = 34;
        PRESET_TITLE_ACTION = 35;
        PRESET_TITLE_FOLLOW_CAM = 36;
        PRESET_TITLE_SURF = 37;
        PRESET_TITLE_CITY = 38;
        PRESET_TITLE_SHAKY = 39;
        PRESET_TITLE_CHESTY = 40;
        PRESET_TITLE_HELMET = 41;
        PRESET_TITLE_BITE = 42;
        PRESET_TITLE_CUSTOM_CINEMATIC = 43;
        PRESET_TITLE_VLOG = 44;
        PRESET_TITLE_FPV = 45;
        PRESET_TITLE_HDR = 46;
        PRESET_TITLE_LANDSCAPE = 47;
        PRESET_TITLE_LOG = 48;
        PRESET_TITLE_CUSTOM_SLOMO = 49;
        PRESET_TITLE_TRIPOD = 50;
        PRESET_TITLE_BASIC = 58;
        PRESET_TITLE_ULTRA_SLO_MO = 59;
        PRESET_TITLE_STANDARD_ENDURANCE = 60;
        PRESET_TITLE_ACTIVITY_ENDURANCE = 61;
        PRESET_TITLE_CINEMATIC_ENDURANCE = 62;
        PRESET_TITLE_SLOMO_ENDURANCE = 63;
        PRESET_TITLE_STATIONARY_1 = 64;
        PRESET_TITLE_STATIONARY_2 = 65;
        PRESET_TITLE_STATIONARY_3 = 66;
        PRESET_TITLE_STATIONARY_4 = 67;
        PRESET_TITLE_SIMPLE_VIDEO = 68;
        PRESET_TITLE_SIMPLE_TIME_WARP = 69;
        PRESET_TITLE_SIMPLE_SUPER_PHOTO = 70;
        PRESET_TITLE_SIMPLE_NIGHT_PHOTO = 71;
        PRESET_TITLE_SIMPLE_VIDEO_ENDURANCE = 72;
        PRESET_TITLE_HIGHEST_QUALITY = 73; // Semble être un doublon de 93 ?
        PRESET_TITLE_EXTENDED_BATTERY = 74;
        PRESET_TITLE_LONGEST_BATTERY = 75;
        PRESET_TITLE_STAR_TRAIL = 76;
        PRESET_TITLE_LIGHT_PAINTING = 77;
        PRESET_TITLE_LIGHT_TRAIL = 78;
        PRESET_TITLE_FULL_FRAME = 79;
        PRESET_TITLE_MAX_VIDEO	= 80;
        PRESET_TITLE_MAX_TIMEWARP	= 81;	
        PRESET_TITLE_STANDARD_QUALITY_VIDEO = 82;
        PRESET_TITLE_BASIC_QUALITY_VIDEO = 83;
        PRESET_TITLE_HIGHEST_QUALITY_VIDEO = 93;
        PRESET_TITLE_USER_DEFINED_CUSTOM_NAME = 94;
        PRESET_TITLE_EASY_STANDARD_PROFILE = 99;
        PRESET_TITLE_EASY_HDR_PROFILE = 100;
        PRESET_TITLE_BURST_SLOMO = 106;
        // *** AJOUT DES VALEURS MANQUANTES DE LA DOC ***
        PRESET_TITLE_4_3_VIDEO = 125;
        PRESET_TITLE_16_9_VIDEO = 126;
        PRESET_TITLE_16_9_SLOMO = 127;
        // *** FIN DES AJOUTS ***
    }


    // --- MESSAGES ---

    message RequestGetPresetStatus {
      repeated EnumRegisterPresetStatus register_preset_status = 1;
      repeated EnumRegisterPresetStatus unregister_preset_status = 2;
      optional bool use_constant_setting_ids = 3 [default = false]; // Important pour la cohérence
    }

    // Message représentant une valeur de setting dans un preset
    message PresetSetting {
      required int32 id = 1;      // Setting ID
      required int32 value = 2;   // Setting value
      optional bool is_caption = 3;
    }

    // Message représentant un preset individuel
    message Preset {
      required int32 id = 1;                    // Preset ID
      optional EnumFlatMode mode = 2;
      optional EnumPresetTitle title_id = 3;
      optional int32 title_number = 4;
      optional bool user_defined = 5;
      optional EnumPresetIcon icon = 6;
      repeated PresetSetting setting_array = 7;
      optional bool is_modified = 8;
      optional bool is_fixed = 9;
      optional string custom_name = 10;
    }

    // Message représentant un groupe de presets
    message PresetGroup {
      required EnumPresetGroup id = 1;
      repeated Preset preset_array = 2;
      optional bool can_add_preset = 3;
      optional EnumPresetGroupIcon icon = 4;
      repeated EnumFlatMode mode_array = 5;
    }

    // Message principal pour la réponse/notification
    message NotifyPresetStatus {
      repeated PresetGroup preset_group_array = 1;
      // On peut ajouter result ici si la réponse initiale le contient, mais la doc ne le montre pas explicitement
      // optional EnumResultGeneric result = ?;
    }

    // Ajouter d'autres messages si besoin (ResponseGeneric, RequestCustomPresetUpdate...)
    message ResponseGeneric {
        optional EnumResultGeneric result = 1;
    }

    message RequestCustomPresetUpdate {
        optional EnumPresetTitle title_id = 1;
        optional string custom_name = 2;
        optional EnumPresetIcon icon_id = 3;
    }

    `; // Fin de la chaîne protoDefinitions


// IDs des Requêtes Query (TLV)
const QRY_GET_STATUS_VALUES = 0x13;
const QRY_REGISTER_STATUS_UPDATES = 0x53;
const QRY_UNREGISTER_STATUS_UPDATES = 0x73;
const QRY_GET_SETTING_CAPABILITIES = 0x32;
const QRY_REGISTER_CAPABILITIES_UPDATES = 0x62;
const QRY_UNREGISTER_CAPABILITIES_UPDATES = 0x82;
const QRY_GET_SETTING_VALUES = 0x12;          // <-- Nouveau: Pour obtenir valeur initiale des settings
const QRY_REGISTER_SETTING_UPDATES = 0x52;    // <-- Nouveau: Pour s'enregistrer aux MAJ settings
// const QRY_UNREGISTER_SETTING_UPDATES = 0x72;  // <-- Nouveau: Pour se désinscrire des MAJ settings
const QRY_UNREGISTER_SETTING_VALUE_UPDATES = 0x72;
  // <-- Nouveau


// IDs des Réponses/Notifications Query (TLV)
const RSP_GET_STATUS_VALUES = 0x13;
const RSP_GET_SETTING_CAPABILITIES = 0x32;
const RSP_GET_SETTING_VALUES = 0x12;          // <-- Nouveau
const RSP_REGISTER_STATUS_UPDATES = 0x53;     // <-- Réponse initiale à 0x53
const RSP_REGISTER_CAPABILITIES_UPDATES = 0x62; // <-- Réponse initiale à 0x62
const RSP_REGISTER_SETTING_UPDATES = 0x52;    // <-- Réponse initiale à 0x52
const ASYNC_STATUS_NOTIFICATION = 0x93;       // <-- Notification de statut
const ASYNC_CAPABILITIES_NOTIFICATION = 0xA2; // <-- Notification de capacité
const ASYNC_SETTING_NOTIFICATION = 0x92;      // <-- Notification de valeur Setting



// IDs des Statuts que nous voulons
const STATUS_ID_BATTERY_PERCENT = 70;
const STATUS_ID_BATTERY_BARS = 2;
const STATUS_ID_ENCODING = 10;
const STATUS_ID_SD_REMAINING = 54;
const STATUS_ID_SD_CAPACITY = 117;
const STATUS_ID_VIDEO_REMAINING = 35;
const STATUS_ID_COLD = 85;
const STATUS_ID_OVERHEATING = 6;
const STATUS_ID_BUSY = 8;
const STATUS_ID_PRESET_ACTIVE = 97;

// IDs des Settings (TLV)
const SETTING_ID_VIDEO_RESOLUTION = 2;
const SETTING_ID_FPS = 3;               // <-- Nouveau
const SETTING_ID_VIDEO_TIMELAPSE_RATE = 5;
const SETTING_ID_VIDEO_LENS = 121;       // <-- Nouveau
const SETTING_ID_GPS = 83;              // <-- Nouveau
const SETTING_ID_HYPERSMOOTH = 135;     // <-- Nouveau
const SETTING_ID_TIMEWARP_SPEED = 111;
// Ajouter d'autres IDs de settings ici au besoin

// Liste des IDs des Settings dont on veut les capacités initiales
const requestedCapabilityIds = [
    SETTING_ID_VIDEO_RESOLUTION,
    SETTING_ID_FPS, 
    SETTING_ID_VIDEO_TIMELAPSE_RATE, // <-- AJOUTER
    SETTING_ID_VIDEO_LENS,           // <-- AJOUTER
    SETTING_ID_GPS,                 // <-- Ajouté
    SETTING_ID_HYPERSMOOTH,          // <-- Ajouté
    SETTING_ID_TIMEWARP_SPEED
    // Ajouter d'autres IDs de settings ici si besoin (ex: FPS ID 3, Lens ID 121)
];

// Liste des IDs de Settings dont on veut la valeur
const requestedSettingIds = [
    SETTING_ID_VIDEO_RESOLUTION,
    SETTING_ID_FPS,                 // <-- Nouveau
    SETTING_ID_VIDEO_TIMELAPSE_RATE,
    SETTING_ID_VIDEO_LENS,
    SETTING_ID_GPS,                 // <-- Ajouté
    SETTING_ID_HYPERSMOOTH,          // <-- Ajouté
    SETTING_ID_TIMEWARP_SPEED        // <-- AJOUTÉ
    // Ajouter d'autres IDs de settings ici si besoin
];

// Liste des IDs à demander
const requestedStatusIds = [
    STATUS_ID_BATTERY_PERCENT,
    STATUS_ID_BATTERY_BARS,
    STATUS_ID_ENCODING,
    STATUS_ID_SD_REMAINING,
    STATUS_ID_SD_CAPACITY,
    STATUS_ID_VIDEO_REMAINING,
    STATUS_ID_COLD,
    STATUS_ID_OVERHEATING,
    STATUS_ID_BUSY,
    STATUS_ID_PRESET_ACTIVE
];

// UUID des Caractéristiques (format court, sera résolu en format long si nécessaire)
// Note: GP-XXXX signifie b5f9XXXX-aa8d-11e3-9046-0002a5d5c51b
const COMMAND_CHAR_UUID = "b5f90072-aa8d-11e3-9046-0002a5d5c51b"; // GP-0072 (Command Write)
const COMMAND_RSP_CHAR_UUID = "b5f90073-aa8d-11e3-9046-0002a5d5c51b"; // GP-0073 (Command Response Notify)
const SETTINGS_CHAR_UUID = "b5f90074-aa8d-11e3-9046-0002a5d5c51b"; // GP-0074 (Settings Write)
const SETTINGS_RSP_CHAR_UUID = "b5f90075-aa8d-11e3-9046-0002a5d5c51b"; // GP-0075 (Settings Response Notify)
const QUERY_CHAR_UUID = "b5f90076-aa8d-11e3-9046-0002a5d5c51b"; // GP-0076 (Query Write)
const QUERY_RSP_CHAR_UUID = "b5f90077-aa8d-11e3-9046-0002a5d5c51b"; // GP-0077 (Query Response Notify)

// IDs des Commandes (TLV)
const CMD_GET_HARDWARE_INFO = 0x3C;
const CMD_KEEP_ALIVE = 0x5B; // Sera utilisé plus tard
const CMD_LOAD_PRESET = 0x40; // <-- AJOUTÉ pour clarté

// --- Variables Globales ---
let goproDevice = null;
let goproServer = null;
let goproService = null;
let commandCharacteristic = null;
let commandResponseCharacteristic = null;
let settingsCharacteristic = null; // <-- Ajouter
let settingsResponseCharacteristic = null;
let isCameraReady = false;
let incomingMessage = {
    buffer: new Uint8Array(0), // Pour accumuler les données
    expectedLength: 0,         // Longueur totale attendue du message
    receivedLength: 0,         // Longueur actuellement reçue
    isAssembling: false        // Indicateur si un message est en cours d'assemblage
};
let queryCharacteristic = null;
let queryResponseCharacteristic = null;
let initialStatusRequested = false; // Drapeau pour savoir si on a demandé l'état initial

let recordingStartTime = null; // Nouveau : timestamp début enregistrement réel
let countdownIntervalId = null; // Nouveau : ID pour le setInterval du compte à rebours
let stopTimerTimeoutId = null;
let isCameraBusy = false; // Pour suivre l'état Busy (Status 8)// Nouveau : ID pour le setTimeout d'arrêt
let currentEncodingStatus = 0; // 0 = Off, 1 = On
let isGattOperationInProgress = false;
let isDisconnecting = false; // Drapeau pour gérer la déconnexion
let settingCapabilities = {};
let currentSettingValues = {}; // <-- Nouvelle variable pour les valeurs actuelles { settingId: value }
let currentSettings = {}; // <-- Nouveau: Pour stocker la valeur actuelle des settings
let initialSettingsRequested = false; // <-- Nouveau: Drapeau pour Get Setting Values

// Variables pour les éléments UI (déclarées mais non initialisées)
let connectButton = null;
let connectionStatusDiv = null;
let deviceInfoDiv = null;
let errorMessageDiv = null;
let controlsSection = null;
let statusSection = null;
let statusBatteryDiv = null;
let statusEncodingDiv = null;
// ... et ainsi de suite pour TOUS les éléments UI ...
let settingResolutionSelect = null;
let settingResolutionFeedback = null;
let settingFpsSelect = null;
let settingFpsFeedback = null;
let settingLensSelect = null;
let settingLensFeedback = null;
let settingGpsSelect = null;
let settingGpsFeedback = null;
let settingHypersmoothSelect = null;
let settingHypersmoothFeedback = null;

// Ajouter les références aux DIVs parentes pour les masquer/afficher
let settingItemResolutionDiv = null;
let settingItemFpsDiv = null;
let settingItemLensDiv = null;
let settingItemGpsDiv = null;
let settingItemHypersmoothDiv = null;
let presetsContainer = null; // <-- AJOUTÉ
let presetsSection = null;   // <-- AJOUTÉ
let presetsFeedback = null;  // <-- AJOUTÉ
let startRecordButton = null;
let stopRecordButton = null;
let shutterFeedbackDiv = null;
let hilightButton = null; 
let hilightFeedbackDiv = null;
let timerToggleButton = null;
let timerToggleHandle = null;
let timedDurationInput = null;
let toggleStatusButton = null;
let statusGrid = null;
let statusSdRemainingDiv = null;
let statusSdCapacityDiv = null;
let statusVideoRemainingDiv = null;
let statusTempDiv = null;
let statusBusyDiv = null;
let statusPresetDiv = null;


// Ajouter d'autres caractéristiques ici si nécessaire (Settings, Query)

let readinessCheckInterval = null;
let keepAliveInterval = null; // Sera utilisé plus tard
let isTimerEnabled = false; // <-- AJOUTÉ

// Ajouter d'autres types si besoin (ResponseGeneric, etc.)
let Root = null; // Racine Protobuf
let RequestGetPresetStatusType = null; // Renommé pour clarté
let NotifyPresetStatusType = null;   // Renommé pour clarté
let EnumRegisterPresetStatusValues = null; // Pour stocker les valeurs de l'enum
let EnumPresetGroupValues = null;
let EnumPresetTitleValues = null;
let EnumPresetIconValues = null;
let EnumPresetGroupIconValues = null;
let EnumFlatModeValues = null;
let ResponseGenericType = null; // <-- AJOUTÉ
let RequestCustomPresetUpdateType = null; // <-- AJOUTÉ
// ... Ajouter d'autres si nécessaire ...


// Variable globale pour stocker les presets reçus
let availablePresets = { // Structure exemple
    groups: [], // Sera rempli avec les objets PresetGroup décodés
    lastUpdateTime: 0 // Pour suivre les modifications si nécessaire
};
let presetInfoCache = {}; // { presetId: { name: "...", iconName: "..." } }



// --- Fonctions Utilitaires ---

/** Met à jour l'affichage HTML pour UN SEUL statut */
function updateSingleStatusDisplay(statusId, value) {
    let displayValue = 'N/A';
    let targetElement = null;

    switch (statusId) {
        case STATUS_ID_BATTERY_PERCENT:
            targetElement = statusBatteryDiv;
            // Vérifier si le statut des barres existe et indique la charge
            const batteryBarsStatus = currentSettings[STATUS_ID_BATTERY_BARS]; // Lire l'état des barres
            if (batteryBarsStatus === 4) { // 4 = Charging
                displayValue = `${value}% <span class="text-green-500">(Charge...)</span>`; // Ajouter indication
            } else {
                displayValue = `${value}%`; // Affichage normal
            }
            break;
        case STATUS_ID_BATTERY_BARS:
             // On met à jour l'état global, mais on ne change pas directement l'UI ici.
             // L'UI principale de la batterie est mise à jour par STATUS_ID_BATTERY_PERCENT.
             // On pourrait ajouter un log ou une petite icône séparée si besoin.
             if (VERBOSE_LOGGING) console.log(`   Statut Barres Batterie reçu: ${value}`);
             // Si on reçoit la notification de barres APRES celle du pourcentage,
             // on pourrait forcer une mise à jour de l'affichage du pourcentage :
             if (currentSettings.hasOwnProperty(STATUS_ID_BATTERY_PERCENT)) {
                 updateSingleStatusDisplay(STATUS_ID_BATTERY_PERCENT, currentSettings[STATUS_ID_BATTERY_PERCENT]);
             }
             return; // Ne pas essayer de mettre à jour targetElement/displayValue pour les barres elles-mêmes
        case STATUS_ID_ENCODING: targetElement = statusEncodingDiv; displayValue = value === 1 ? '<span class="text-red-600 font-bold">Oui</span>' : 'Non'; break;
        case STATUS_ID_SD_REMAINING: targetElement = statusSdRemainingDiv; displayValue = formatSize(Number(value)); break;
        case STATUS_ID_SD_CAPACITY: targetElement = statusSdCapacityDiv; displayValue = formatSize(Number(value)); break;
        case STATUS_ID_VIDEO_REMAINING: targetElement = statusVideoRemainingDiv; displayValue = formatDuration(value); break;
        case STATUS_ID_COLD:
        case STATUS_ID_OVERHEATING:
            targetElement = statusTempDiv;
            let isCold = (statusId === STATUS_ID_COLD && value === 1) || (currentSettings[STATUS_ID_COLD] === 1 && statusId !== STATUS_ID_COLD);
            let isHot = (statusId === STATUS_ID_OVERHEATING && value === 1) || (currentSettings[STATUS_ID_OVERHEATING] === 1 && statusId !== STATUS_ID_OVERHEATING);
            if (isCold) displayValue = '<span class="text-blue-600">Froid</span>';
            else if (isHot) displayValue = '<span class="text-red-600">Chaud</span>';
            else displayValue = 'OK';
            break;
        case STATUS_ID_BUSY: targetElement = statusBusyDiv; displayValue = value === 1 ? '<span class="text-yellow-600">Occupé</span>' : 'Prêt'; break;
        case STATUS_ID_PRESET_ACTIVE: 
            targetElement = statusPresetDiv; 
            // displayValue = `ID ${value}`; 
            // *** Logique modifiée : Utiliser le cache ***
            const activePresetIdValue = value; // Renommer pour clarté
            const cachedPresetInfo = presetInfoCache[activePresetIdValue]; // Chercher dans le cache
            // Afficher le nom du cache s'il existe, sinon l'ID
            displayValue = cachedPresetInfo ? `${cachedPresetInfo.name} (ID: ${activePresetIdValue})` : `ID ${activePresetIdValue}`;
            // *** Fin Logique modifiée ***
            break;
        default:
            // console.log(`Pas de mise à jour UI spécifique pour Status ID: ${statusId}`);
            return; // Sortir si pas d'élément défini
    }
    if (targetElement) {
        targetElement.innerHTML = displayValue;
    }
}

/**
 * Construit le(s) paquet(s) BLE pour un message Protobuf.
 * @param {number} featureId L'ID de fonctionnalité Protobuf (1 octet).
 * @param {number} actionId L'ID d'action Protobuf (1 octet).
 * @param {Uint8Array} serializedProtoData Les données Protobuf sérialisées.
 * @param {number} mtu La taille maximale d'un paquet BLE.
 * @returns {Array<ArrayBuffer>} Tableau de paquets BLE prêts à être envoyés.
 */
function buildProtobufBlePackets(featureId, actionId, serializedProtoData, mtu = 20) {
    // 1. Construire le payload BLE complet (avant en-tête BLE)
    const protoHeaderLength = 2; // Feature ID + Action ID
    const totalProtoPayloadLength = protoHeaderLength + serializedProtoData.length;
    const fullBlePayload = new Uint8Array(totalProtoPayloadLength);
    fullBlePayload[0] = featureId;
    fullBlePayload[1] = actionId;
    fullBlePayload.set(serializedProtoData, protoHeaderLength);

    if (VERBOSE_LOGGING) console.log(`[buildProtobufBlePackets] Construction pour Feature 0x${featureId.toString(16)}, Action 0x${actionId.toString(16)}, ProtoData ${serializedProtoData.length} bytes. Total BLE Payload: ${totalProtoPayloadLength} bytes.`);

    // 2. Utiliser une logique similaire à buildBlePackets pour segmenter fullBlePayload
    const packets = [];
    let bytesSent = 0;
    let packetCounter = 0;
    const totalLength = fullBlePayload.length; // Longueur utilisée pour l'en-tête BLE

    // --- Premier paquet ---
    let headerLength;
    let firstPacketPayloadSize;
    let firstPacketBuffer;

    if (totalLength <= mtu - 1) { // En-tête 5-bit
        headerLength = 1;
        const bleHeader = totalLength & 0b00011111;
        firstPacketBuffer = new Uint8Array(headerLength + totalLength);
        firstPacketBuffer[0] = bleHeader;
        firstPacketPayloadSize = totalLength;
        if (VERBOSE_LOGGING) console.log(`  -> Proto: En-tête départ 5-bit (0x${bleHeader.toString(16).padStart(2, '0')})`);
    } else if (totalLength <= 8191) { // En-tête 13-bit
        headerLength = 2;
        const headerByte0 = 0b01000000 | ((totalLength >> 8) & 0b00011111);
        const headerByte1 = totalLength & 0xFF;
        firstPacketBuffer = new Uint8Array(mtu);
        firstPacketBuffer[0] = headerByte0;
        firstPacketBuffer[1] = headerByte1;
        firstPacketPayloadSize = Math.min(totalLength, mtu - headerLength);
        if (VERBOSE_LOGGING) console.log(`  -> Proto: En-tête départ 13-bit (0x${headerByte0.toString(16).padStart(2, '0')}:${headerByte1.toString(16).padStart(2, '0')})`);
    } else {
        console.error(`[buildProtobufBlePackets] Payload Protobuf combiné trop long (${totalLength} bytes > 8191).`);
        return [];
    }

    firstPacketBuffer.set(fullBlePayload.slice(0, firstPacketPayloadSize), headerLength);
    packets.push(firstPacketBuffer.slice(0, headerLength + firstPacketPayloadSize).buffer);
    bytesSent += firstPacketPayloadSize;
    if (VERBOSE_LOGGING) console.log(`  Proto: Premier paquet (${headerLength + firstPacketPayloadSize} bytes). Envoyé: ${bytesSent}/${totalLength}`);

    // --- Paquets de continuation ---
    const continuationHeaderLength = 1;
    const continuationPayloadSize = mtu - continuationHeaderLength;

    while (bytesSent < totalLength) {
        const remainingBytes = totalLength - bytesSent;
        const currentPayloadSize = Math.min(remainingBytes, continuationPayloadSize);
        const continuationPacketBuffer = new Uint8Array(continuationHeaderLength + currentPayloadSize);
        const continuationHeader = 0b10000000 | (packetCounter & 0b00001111);
        continuationPacketBuffer[0] = continuationHeader;
        packetCounter = (packetCounter + 1) % 16;
        continuationPacketBuffer.set(fullBlePayload.slice(bytesSent, bytesSent + currentPayloadSize), continuationHeaderLength);
        packets.push(continuationPacketBuffer.buffer);
        bytesSent += currentPayloadSize;
        if (VERBOSE_LOGGING) console.log(`  Proto: Paquet continuation (${continuationHeaderLength + currentPayloadSize} bytes, Header: 0x${continuationHeader.toString(16).padStart(2, '0')}). Envoyé: ${bytesSent}/${totalLength}`);
    }
     console.log(`[buildProtobufBlePackets] Terminé. ${packets.length} paquet(s) généré(s).`);

    return packets;
}

/**
 * Construit le(s) paquet(s) BLE à envoyer, en ajoutant l'en-tête approprié
 * et en gérant la segmentation si nécessaire.
 * Note: Pour l'envoi, on utilise les en-têtes 5-bit ou 13-bit.
 * @param {Uint8Array} tlvPayload Le payload TLV complet (ID + [Len+Val...])
 * @param {number} mtu La taille maximale d'un paquet BLE (généralement 20 pour GATT standard).
 * @returns {Array<ArrayBuffer>} Un tableau contenant un ou plusieurs ArrayBuffer(s) prêts à être envoyés.
 */
function buildBlePackets(tlvPayload, mtu = 20) {
    const packets = [];
    const totalLength = tlvPayload.length;
    let bytesSent = 0;
    let packetCounter = 0;

    if (VERBOSE_LOGGING) console.log(`[buildBlePackets] Construction paquets pour payload TLV de ${totalLength} bytes (MTU: ${mtu})`);

    // --- Premier paquet (avec en-tête de départ) ---
    let headerLength;
    let firstPacketPayloadSize;
    let firstPacketBuffer;

    // Choisir l'en-tête basé sur la LONGUEUR TOTALE du message TLV
    if (totalLength <= mtu - 1) { // Tient dans un paquet 5-bit ? (Header=1 byte)
        headerLength = 1;
        const bleHeader = totalLength & 0b00011111; // Start(0) General(00) Length(5 bits)
        firstPacketBuffer = new Uint8Array(headerLength + totalLength);
        firstPacketBuffer[0] = bleHeader;
        firstPacketPayloadSize = totalLength; // Prend tout le payload
        if (VERBOSE_LOGGING) console.log(`  -> En-tête départ: General 5-bit (0x${bleHeader.toString(16).padStart(2, '0')})`);
    } else if (totalLength <= 8191) { // Utiliser en-tête 13-bit ? (Header=2 bytes)
        headerLength = 2;
        // Header: 010LLLLL LLLLLLLL
        const headerByte0 = 0b01000000 | ((totalLength >> 8) & 0b00011111); // Start(0) Type(01) 5 MSB length
        const headerByte1 = totalLength & 0xFF;                           // 8 LSB length
        firstPacketBuffer = new Uint8Array(mtu); // Taille max du premier paquet = MTU
        firstPacketBuffer[0] = headerByte0;
        firstPacketBuffer[1] = headerByte1;
        // Calculer combien de payload TLV tient dans ce premier paquet
        firstPacketPayloadSize = Math.min(totalLength, mtu - headerLength);
        if (VERBOSE_LOGGING) console.log(`  -> En-tête départ: Extended 13-bit (0x${headerByte0.toString(16).padStart(2, '0')}:${headerByte1.toString(16).padStart(2, '0')})`);
    } else {
        console.error(`[buildBlePackets] Payload TLV trop long (${totalLength} bytes > 8191) pour être envoyé.`);
        return []; // Retourner un tableau vide en cas d'erreur
    }

    // Copier la première partie du payload TLV dans le premier paquet
    firstPacketBuffer.set(tlvPayload.slice(0, firstPacketPayloadSize), headerLength);

    // Ajuster la taille réelle du buffer si plus petit que MTU (cas 5-bit)
    packets.push(firstPacketBuffer.slice(0, headerLength + firstPacketPayloadSize).buffer);
    bytesSent += firstPacketPayloadSize;
    if (VERBOSE_LOGGING) console.log(`  Premier paquet (${headerLength + firstPacketPayloadSize} bytes) ajouté. Payload TLV envoyé: ${bytesSent}/${totalLength}`);

    // --- Paquets de continuation (si nécessaire) ---
    const continuationHeaderLength = 1; // Header = 1 byte (1RRRCCCC)
    const continuationPayloadSize = mtu - continuationHeaderLength; // Taille max du payload par paquet suite

    while (bytesSent < totalLength) {
        const remainingBytes = totalLength - bytesSent;
        const currentPayloadSize = Math.min(remainingBytes, continuationPayloadSize);
        const continuationPacketBuffer = new Uint8Array(continuationHeaderLength + currentPayloadSize);

        // Construire l'en-tête de continuation
        const continuationHeader = 0b10000000 | (packetCounter & 0b00001111); // Continuation(1) Reserved(000) Counter(4 bits)
        continuationPacketBuffer[0] = continuationHeader;
        packetCounter = (packetCounter + 1) % 16; // Incrémenter le compteur (modulo 16)

        // Copier la partie suivante du payload TLV
        continuationPacketBuffer.set(tlvPayload.slice(bytesSent, bytesSent + currentPayloadSize), continuationHeaderLength);

        packets.push(continuationPacketBuffer.buffer);
        bytesSent += currentPayloadSize;
        if (VERBOSE_LOGGING) console.log(`  Paquet continuation (${continuationHeaderLength + currentPayloadSize} bytes, Header: 0x${continuationHeader.toString(16).padStart(2, '0')}) ajouté. Payload TLV envoyé: ${bytesSent}/${totalLength}`);
    }

    console.log(`[buildBlePackets] Terminé. ${packets.length} paquet(s) généré(s).`);
    return packets;
}

// Nouvelle fonction pour mettre à jour UNIQUEMENT l'état actif des boutons presets
function markActivePresetButton() {
    const activePresetId = currentSettings[STATUS_ID_PRESET_ACTIVE];
    console.log(`[markActivePresetButton] Tentative de marquage du preset actif ID: ${activePresetId}`);

    const presetButtons = document.querySelectorAll('#presets-container .preset-button');
    if (!presetButtons || presetButtons.length === 0) {
        // console.warn("[markActivePresetButton] Aucun bouton de preset trouvé dans l'UI pour marquer l'état actif.");
        return; // Rien à faire si les boutons ne sont pas (encore) là
    }

    let foundActive = false;
    presetButtons.forEach(button => {
        const buttonPresetId = parseInt(button.dataset.presetId, 10);
        if (!isNaN(buttonPresetId) && buttonPresetId === activePresetId) {
            if (!button.classList.contains('active-preset')) {
                if (VERBOSE_LOGGING) console.log(`   -> Marquage du bouton pour ID ${buttonPresetId} comme actif.`);
                button.classList.add('active-preset', 'bg-blue-500', 'text-white', 'border-blue-700');
                button.classList.remove('bg-gray-200', 'hover:bg-gray-300');
            }
             foundActive = true;
        } else {
            if (button.classList.contains('active-preset')) {
                if (VERBOSE_LOGGING) console.log(`   -> Retrait du marquage actif du bouton pour ID ${buttonPresetId}.`);
                button.classList.remove('active-preset', 'bg-blue-500', 'text-white', 'border-blue-700');
                button.classList.add('bg-gray-200', 'hover:bg-gray-300');
            }
        }
    });

    if (activePresetId !== undefined && !foundActive) {
         // console.warn(`[markActivePresetButton] ID Preset actif ${activePresetId} reçu, mais aucun bouton correspondant trouvé.`);
    } else if (activePresetId !== undefined && foundActive) {
         console.log(`[markActivePresetButton] Bouton pour preset actif ${activePresetId} marqué.`);
    } else {
         console.log(`[markActivePresetButton] Aucun preset actif spécifié (ID: ${activePresetId}).`);
    }
}

// Nouvelle fonction pour gérer l'activation/désactivation des contrôles basé sur Busy
function updateControlsBasedOnBusyState(busy) {
    const isEncoding = (currentEncodingStatus === 1); // Lire l'état global d'encodage
    const shouldDisableSettings = busy || isEncoding; // Désactiver si occupé OU en enregistrement
    // console.log(`Mise à jour des contrôles UI - État Busy: ${busy}, Is Encoding: ${isEncoding}, ShouldDisableSettings: ${shouldDisableSettings}`);


    // 1. Mettre à jour les boutons Record/Stop/Hilight (qui dépendent aussi de l'encodage)
    // Cet appel est fait séparément dans parseIdLengthValueData quand Busy ou Encoding changent.

    // 2. Mettre à jour les selects des Settings
    const settingSelects = [
        settingResolutionSelect, 
        settingFpsSelect, 
        settingLensSelect,
        settingGpsSelect, 
        settingHypersmoothSelect, 
        settingTimelapseRateSelect, // AJOUTÉ
        settingTimewarpSpeedSelect  
        // Ajoutez d'autres selects ici
    ];
    settingSelects.forEach(select => {
        if (select) {
            select.disabled = shouldDisableSettings; // Utiliser la condition combinée
            // Optionnel: Style visuel pour indiquer 'occupé' ou 'enregistrement'
            select.style.opacity = shouldDisableSettings ? 0.6 : 1;
            select.style.cursor = shouldDisableSettings ? 'not-allowed' : '';
        }
    });

    // 3. Mettre à jour les boutons Preset
    const presetButtons = document.querySelectorAll('#presets-container .preset-button');
    presetButtons.forEach(button => {
        // On peut charger un preset même si on enregistre ? La documentation ne le précise pas.
        // Par sécurité, désactivons-les aussi pendant l'enregistrement pour l'instant.
        // Si on découvre qu'on PEUT charger un preset pendant l'enregistrement,
        // on utiliserait juste 'busy' ici au lieu de 'shouldDisableSettings'.
        button.disabled = shouldDisableSettings; // Désactiver si busy OU en enregistrement
        // Optionnel: Style visuel
        button.style.opacity = shouldDisableSettings ? 0.6 : 1;
        button.style.cursor = shouldDisableSettings ? 'not-allowed' : '';
    });

    // 4. Mettre à jour d'autres contrôles si nécessaire (ex: bouton Keep Alive manuel si vous en ajoutez un)
}

// --- Helper pour obtenir l'élément DIV parent ---
function getParentElementForSetting(settingId) {
    if (settingId === SETTING_ID_VIDEO_RESOLUTION) return settingItemResolutionDiv;
    if (settingId === SETTING_ID_FPS) return settingItemFpsDiv;
    if (settingId === SETTING_ID_VIDEO_LENS) return settingItemLensDiv;
    if (settingId === SETTING_ID_GPS) return settingItemGpsDiv;
    if (settingId === SETTING_ID_HYPERSMOOTH) return settingItemHypersmoothDiv;
    // --- AJOUT ---
    if (settingId === SETTING_ID_VIDEO_TIMELAPSE_RATE) return settingItemTimelapseRateDiv;
    if (settingId === SETTING_ID_TIMEWARP_SPEED) return settingItemTimewarpSpeedDiv;
    // --- FIN AJOUT ---
    // Ajoutez d'autres 'if' pour les futurs settings
    return null;
}

// --- Helper pour obtenir l'élément SELECT ---
function getSelectElementForSetting(settingId) {
    if (settingId === SETTING_ID_VIDEO_RESOLUTION) return settingResolutionSelect;
    if (settingId === SETTING_ID_FPS) return settingFpsSelect;
    if (settingId === SETTING_ID_VIDEO_LENS) return settingLensSelect;
    if (settingId === SETTING_ID_GPS) return settingGpsSelect;
    if (settingId === SETTING_ID_HYPERSMOOTH) return settingHypersmoothSelect;
    // --- AJOUT ---
    if (settingId === SETTING_ID_VIDEO_TIMELAPSE_RATE) return settingTimelapseRateSelect;
    if (settingId === SETTING_ID_TIMEWARP_SPEED) return settingTimewarpSpeedSelect;
    // --- FIN AJOUT ---
    // Ajoutez d'autres 'if' pour les futurs settings
    return null;
}

// --- Nouvelle fonction pour traiter les données et mettre à jour cache/UI ---
function processPresetData(presetGroups, actionId) {
    const isInitialResponse = (actionId === 0xF2); // Est-ce la première réponse complète?
    console.log(`[processPresetData] Traitement données presets. Réponse initiale: ${isInitialResponse}`); // Garder ce log

    if (isInitialResponse) {
        presetInfoCache = {}; // Vider le cache lors de la réponse initiale
        console.log("   -> Cache des presets vidé (réponse initiale).");
    }

    // Mettre à jour le cache avec les infos disponibles
    if (presetGroups) {
        presetGroups.forEach(group => {
            if (group.presetArray) {
                group.presetArray.forEach(preset => {
                     // Si réponse initiale OU si ce preset a des détails dans la notif
                     let name = null; // Utiliser 'let' pour une portée de bloc
                     let icon = null;
                     let titleFound = false;
                     let cacheNeedsUpdate = false;
                     
                     // Déterminer si on a assez d'info pour METTRE À JOUR LE CACHE
                     if (isInitialResponse || 
                        (!isInitialResponse && 
                         (preset.customName ||
                         (preset.titleId !== undefined && preset.titleId > 0) || // > 0 pour exclure ACTIVITY/VIDEO par défaut
                         (preset.icon !== undefined && preset.icon > 0) )       // > 0 pour exclure VIDEO par défaut
                          )
                        )
                     {
                         cacheNeedsUpdate = true; // On a des infos spécifiques ou c'est l'init

                         // --- Logique pour déterminer 'name' et 'icon' ---
                         // (Utiliser la version précédente avec fallback correct Titre -> Icône -> ID)
                         // 1. Icon Name
                         if (preset.icon !== undefined && EnumPresetIconValues) {
                            icon = EnumPresetIconValues[preset.icon]?.replace('PRESET_ICON_','');
                         }

                         // 2. Title Name
                         if (preset.titleId !== undefined && EnumPresetTitleValues) {
                            const titleEnum = EnumPresetTitleValues[preset.titleId];
                            if (titleEnum === 'PRESET_TITLE_USER_DEFINED_CUSTOM_NAME' && preset.customName) {
                                 name = preset.customName; // Priorité au nom custom
                                 titleFound = true; // Marquer comme trouvé (même si custom)
                            } else if (titleEnum) {
                                 name = titleEnum.replace('PRESET_TITLE_','');
                                 // Ajouter numéro si besoin (seulement si ce n'est PAS un nom custom)
                                 if (preset.titleNumber > 0) name += ` ${preset.titleNumber}`;
                                 titleFound = true; // Marquer comme trouvé
                            }
                         }

                         // 3. Si le titre n'a pas été trouvé, utiliser l'icône comme nom (si elle existe)
                         if (!titleFound && icon) { // icon contient le nom string de l'icône ou null
                            name = icon; // Utiliser le nom de l'icône
                            if (VERBOSE_LOGGING) console.log(`      Fallback to icon name for ID ${preset.id}: ${name}`);
                         }

                         // 4. Si toujours pas de nom, utiliser l'ID comme fallback ultime
                         if (!name) {
                            name = `ID ${preset.id}`;
                            if (VERBOSE_LOGGING) console.log(`      Fallback to ID for ${preset.id}`);
                         }

                         // Mettre à jour le cache
                         presetInfoCache[preset.id] = { name: name, iconName: icon }; // Stocker le nom final déterminé et l'icône trouvée
                         if (VERBOSE_LOGGING) console.log(`    Cache updated for ${preset.id}: Name='${name}', Icon='${icon}'`);
                     } else {
                            // Ne PAS mettre à jour le cache si les données de la notification sont incomplètes/par défaut (0)
                            if (VERBOSE_LOGGING) console.log(`    Cache NOT updated for ${preset.id} (partial/default data in notification: titleId=${preset.titleId}, icon=${preset.icon}, customName=${preset.customName})`);
                     }
                });
            }
        });
    }

    // Appeler la fonction UI qui utilisera MAINTENANT le cache
    updatePresetsUI(presetGroups);
}

// --- Nouvelle fonction UI qui utilise le cache ---
function updatePresetsUI(presetGroups) {
    console.log("Mise à jour de l'UI des presets (incluant marquage actif)..."); // Log modifié
    const presetsContainer = document.getElementById('presets-container');
    const presetsSection = document.getElementById('presets-section'); // Assurez-vous que cet ID correspond à votre HTML


    if (!presetsContainer || !presetsSection) { // Vérifier aussi presetsSection
        console.error("Éléments UI manquants pour les presets (container ou section).");
        return;
     }
    presetsContainer.innerHTML = '';

    let presetsFound = false;
    const activePresetId = currentSettings[STATUS_ID_PRESET_ACTIVE];
    // console.log(`  [UpdatePresetsUI] Preset Actif (depuis currentSettings): ${activePresetId}`);

    if (!presetGroups || presetGroups.length === 0) {
        presetsContainer.textContent = "Aucun preset disponible.";
        return;
    } else {
      presetGroups.forEach(group => {
        // ... titre groupe ...
        const presetList = document.createElement('div');
        presetList.className = 'preset-group-list grid grid-cols-2 md:grid-cols-3 gap-2';
        presetsContainer.appendChild(presetList);

        if (group.presetArray && group.presetArray.length > 0) {
            presetsFound = true;
            group.presetArray.forEach(preset => {
                 const presetButton = document.createElement('button');
                 // *** Utiliser le cache pour obtenir le nom ***
                 const cachedInfo = presetInfoCache[preset.id];
                 const presetName = cachedInfo ? cachedInfo.name : `ID ${preset.id}`; // Nom depuis cache ou ID
                 const iconName = cachedInfo ? cachedInfo.iconName : null; // Récupérer aussi l'icône du cache

                 // console.log(`  Preset ID: ${preset.id}`); // ID Brut
                 // console.log(`    -> Info Cache: Name='${presetName}', Icon='${iconName}'`); // Ce qui est utilisé

                 presetButton.textContent = presetName;
                 presetButton.className = 'preset-button bg-gray-200 text-sm p-2 rounded border hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed'; // Exemple classes
                 presetButton.dataset.presetId = preset.id;

                 // *** Marquer comme actif (utilise currentSettings, pas currentStatusValues) ***
                 // const activePresetId = currentSettings[STATUS_ID_PRESET_ACTIVE]; // Lire depuis settings mis à jour par statut 0x93
                 if (activePresetId !== undefined && preset.id === activePresetId) {
                      presetButton.classList.add('active-preset', 'bg-blue-500', 'text-white', 'border-blue-700');
                      presetButton.classList.remove('bg-gray-200', 'hover:bg-gray-300');
                 } else {
                      presetButton.classList.remove('active-preset', 'bg-blue-500', 'text-white', 'border-blue-700');
                      presetButton.classList.add('bg-gray-200', 'hover:bg-gray-300');
                 }

                 // Appliquer l'état busy directement ici aussi
                 presetButton.disabled = isCameraBusy;
                 presetButton.style.opacity = isCameraBusy ? 0.6 : 1;
                 presetButton.style.cursor = isCameraBusy ? 'not-allowed' : '';

                 presetButton.addEventListener('click', () => { 
                    const idToLoad = parseInt(presetButton.dataset.presetId, 10);
                    if (!isNaN(idToLoad)) {
                    // console.log(`>>>> Click handler triggered for ID: ${idToLoad}`); // Log le clic
                    loadPreset(idToLoad);
                    } 
                 });
                 presetList.appendChild(presetButton);
            });
        } else { 
            const emptyGroupMsg = document.createElement('p');
            emptyGroupMsg.textContent = "Aucun preset défini dans ce groupe."; // Message informatif
            emptyGroupMsg.className = 'text-sm text-gray-500 italic col-span-full'; // Style exemple (Tailwind) pour le message
            // Ajouter ce message à la 'presetList' (le div créé pour les boutons de ce groupe)
            presetList.appendChild(emptyGroupMsg);
            // Log pour indiquer qu'on a traité un groupe vide
            if (VERBOSE_LOGGING) console.log(`   [UpdatePresetsUI] Le groupe ${group.id || 'inconnu'} est vide ou presetArray est manquant/vide.`);   
         }
      });
    }
    // *** Afficher la section SI des presets ont été trouvés et ajoutés ***
    if (presetsFound) {
        console.log("[UpdatePresetsUI] Presets trouvés, affichage de la section 'presets-section'.");
        presetsSection.classList.remove('hidden'); // Retirer la classe 'hidden'
    } else {
        if (VERBOSE_LOGGING) console.log("[UpdatePresetsUI] Aucun preset trouvé, la section 'presets-section' reste masquée.");
        presetsSection.classList.add('hidden'); // Masquer s'il n'y a rien à montrer
    }
}

// Fonction pour initialiser Protobuf
async function initializeProtobuf() {
    try {

        Root = protobuf.parse(protoDefinitions).root; // Assignation à la variable globale Root
        console.log("Définitions Protobuf parsées avec succès.");

        // Obtenir les types de messages spécifiques
        RequestGetPresetStatusType = Root.lookupType("OpenGoPro.RequestGetPresetStatus");
        NotifyPresetStatusType = Root.lookupType("OpenGoPro.NotifyPresetStatus");
        ResponseGenericType = Root.lookupType("OpenGoPro.ResponseGeneric"); // <-- AJOUTÉ
        RequestCustomPresetUpdateType = Root.lookupType("OpenGoPro.RequestCustomPresetUpdate"); // <-- AJOUTÉ
        // Ajouter les autres lookupType ici si nécessaire
        // ResponseGeneric = root.lookupType("OpenGoPro.ResponseGeneric");

        EnumRegisterPresetStatusValues = Root.lookupEnum("OpenGoPro.EnumRegisterPresetStatus").values;
        EnumPresetGroupValues = Root.lookupEnum("OpenGoPro.EnumPresetGroup").valuesById; // Utiliser valuesById pour la recherche inverse
        EnumPresetTitleValues = Root.lookupEnum("OpenGoPro.EnumPresetTitle").valuesById;
        EnumPresetIconValues = Root.lookupEnum("OpenGoPro.EnumPresetIcon").valuesById;
        EnumPresetGroupIconValues = Root.lookupEnum("OpenGoPro.EnumPresetGroupIcon").valuesById;
        EnumFlatModeValues = Root.lookupEnum("OpenGoPro.EnumFlatMode").valuesById;
        // ... Ajoutez d'autres lookupEnum si nécessaire ...

        // Vérification simple si les types principaux sont trouvés
        if (!RequestGetPresetStatusType || !NotifyPresetStatusType) {
            throw new Error("Types Protobuf essentiels (Request/NotifyPresetStatus) non trouvés.");
        }
        
        console.log("Types Protobuf nécessaires chargés.");
        return true; // Succès

    } catch (error) {
        console.error("Erreur lors de l'initialisation de Protobuf:", error);
        displayError("Impossible de charger les définitions Protobuf. La gestion des presets sera désactivée.");
        Root = null; // Assurer que Root est null en cas d'erreur
        RequestGetPresetStatusType = null;
        NotifyPresetStatusType = null;
        ResponseGenericType = null; // <-- AJOUTÉ
        RequestCustomPresetUpdateType = null; // <-- AJOUTÉ
        // ... mettre les autres à null ...
        return false; // Échec
    }
}

// Fonction pour demander les presets et s'enregistrer aux mises à jour
async function getAvailablePresets() {
    // Vérifier le TYPE, pas juste la racine Root
    if (!RequestGetPresetStatusType || !isCameraReady || !queryCharacteristic) {
        console.error("Impossible de demander les presets : Protobuf non prêt, caméra non prête ou caractéristique Query manquante.");
        return;
    }
    console.log("Préparation de la requête GetAvailablePresets (Protobuf)...");

    // 1. Créer le payload Protobuf (RequestGetPresetStatus)
    const payloadProto = {
        // Demander les notifications pour les changements de presets et de groupes
        registerPresetStatus: [
            EnumRegisterPresetStatusValues.REGISTER_PRESET_STATUS_PRESET,
            EnumRegisterPresetStatusValues.REGISTER_PRESET_STATUS_PRESET_GROUP_ARRAY
        ],
        // IMPORTANT: Demander les IDs constants pour la cohérence entre modèles
        useConstantSettingIds: true
    };

    // Vérifier que le message est valide selon la définition .proto
    const errMsg = RequestGetPresetStatusType.verify(payloadProto);
    if (errMsg) {
        console.error("Payload Protobuf invalide pour RequestGetPresetStatus:", errMsg);
        throw Error(errMsg);
    }

    // Créer le message Protobuf
    const messageProto = RequestGetPresetStatusType.create(payloadProto);
    if (VERBOSE_LOGGING) console.log("Message Protobuf créé:", messageProto);

    // 2. Sérialiser le message Protobuf en buffer binaire (Uint8Array)
    const serializedPayload = RequestGetPresetStatusType.encode(messageProto).finish();
    if (VERBOSE_LOGGING) console.log(`Payload Protobuf sérialisé (${serializedPayload.length} bytes):`, serializedPayload);

    // 3. Construire le message BLE complet
    const featureId = 0xF5;
    const actionId = 0x72;

    // 4. Utiliser le helper Protobuf pour construire les paquets BLE ***
    const commandBuffers = buildProtobufBlePackets(featureId, actionId, serializedPayload);
    if (VERBOSE_LOGGING) console.log(`Paquets BLE Protobuf construits: ${commandBuffers.length}`);

    // 5. Envoyer les paquets sur la caractéristique Query 
    if (commandBuffers.length > 0) {
        console.log(`Envoi de GetAvailablePresets (Protobuf) sur Query Char...`);
        try {
            // Utiliser la fonction sendCommand existante qui gère writeValueWithResponse
            await sendCommand(queryCharacteristic, commandBuffers);
            console.log("Requête GetAvailablePresets envoyée.");
        } catch (error) {
            console.error("Erreur lors de l'envoi de GetAvailablePresets:", error);
            displayError("Échec de l'envoi de la requête des presets.");
        }
    }
}

function updateSettingUIOptions(settingId, possibleValues) {
    // Obtenir les éléments DOM via les helpers
    const selectElement = getSelectElementForSetting(settingId);
    const parentDivElement = getParentElementForSetting(settingId);
    let valueMap = {}; // Map pour noms lisibles

    // Log initial
    console.log(`Mise à jour UI Options pour Setting ID ${settingId} avec ${possibleValues?.length ?? 0} capacités.`);

    // Vérifier si les éléments DOM existent
    if (!selectElement || !parentDivElement) {
        // console.error(`Éléments DOM manquants pour Setting ID ${settingId}. Select: ${!!selectElement}, ParentDiv: ${!!parentDivElement}`);
        return; // Ne rien faire si les éléments ne sont pas trouvés
    }

    // *** 1. Déterminer si le contrôle doit être visible ***
    const shouldBeVisible = (possibleValues && possibleValues.length > 0);

    if (shouldBeVisible) {
        if (VERBOSE_LOGGING) console.log(`   (Options UI) Capacités reçues pour Setting ID ${settingId}. Affichage/Mise à jour du contrôle.`);
        parentDivElement.classList.remove('hidden'); // Afficher le conteneur

        // *** 2. Préparer la valueMap spécifique au setting ***
        if (settingId === SETTING_ID_VIDEO_RESOLUTION) { // ID 2
            valueMap = { 1: "4K", 4: "2.7K", 6: "2.7K 4:3", 9: "1080p", 18: "4K 4:3", 26: "5.3K 8:7", 27: "5.3K 4:3", 28: "4K 8:7", 100: "5.3K", 107: "5.3K 8:7 V2", 108: "4K 8:7 V2", 35: "5.3K 21:9", 36: "4K 21:9", 37: "4K 1:1", 38: "900", 109: "4K 9:16 V2", 110: "1080p 9:16 V2", 111: "2.7K 4:3 V2", 112: "4K 4:3 V2", 113: "5.3K 4:3 V2" };
       } else if (settingId === SETTING_ID_FPS) { // ID 3
            valueMap = { 0: "240", 1: "120", 2: "100", 5: "60", 6: "50", 8: "30", 9: "25", 10: "24", 13: "200" };
            Object.keys(valueMap).forEach(key => { valueMap[key] += " fps"; });
       } else if (settingId === SETTING_ID_VIDEO_LENS) { // ID 121
            valueMap = { 0: "Wide", 3: "SuperView", 4: "Linear", 7: "Max SuperView", 8: "Linear+HL", 9: "HyperView", 10: "Linear+Lock", 11: "Max HyperView", 12: "Ultra SuperView", 13: "Ultra Wide", 14: "Ultra Linear", 104: "Ultra HyperView" };
       } else if (settingId === SETTING_ID_GPS) { // ID 83
            valueMap = { 0: "Off", 1: "On" };
       } else if (settingId === SETTING_ID_HYPERSMOOTH) { // ID 135
            valueMap = { 0: "Off", 1: "On", 3: "Boost", 4: "AutoBoost", 100: "Standard" }; // Adaptez '1: On' vs '1: Low' selon caméra si besoin
       }
        // --- AJOUT pour nouveaux settings ---
        else if (settingId === SETTING_ID_VIDEO_TIMELAPSE_RATE) { // ID 5
            valueMap = { 0: "0.5s", 1: "1s", 2: "2s", 3: "5s", 4: "10s", 5: "30s", 6: "60s", 7: "2m", 8: "5m", 9: "30m", 10: "60m", 11: "3s" };
        } else if (settingId === SETTING_ID_TIMEWARP_SPEED) { // ID 111
            valueMap = { 0: "15x", 1: "30x", 2: "60x", 3: "150x", 4: "300x", 5: "900x", 6: "1800x", 7: "2x", 8: "5x", 9: "10x", 10: "Auto", 11: "1x", 12: "1/2x SLOW" };
        }
       // Ajouter d'autres 'else if' pour les futurs settings

       // *** 3. Peupler les options du Select ***
       selectElement.innerHTML = ''; // Vider les anciennes options
       let optionsAddedCount = 0;
       possibleValues.forEach(value => {
           const option = document.createElement('option');
           option.value = value;
           option.textContent = valueMap[value] !== undefined ? `${valueMap[value]}` : `Option ${value}`;
           selectElement.appendChild(option);
           optionsAddedCount++;
       });
       if (VERBOSE_LOGGING) console.log(`   (Options UI) ${optionsAddedCount} options ajoutées pour Setting ID ${settingId}.`);
       selectElement.disabled = false; // Activer le select

       // *** 4. Sélectionner la valeur actuelle (logique déplacée ici) ***
       const storedValue = currentSettings[settingId]; // Récupérer la dernière valeur stockée
       if (storedValue !== undefined) {
            if (VERBOSE_LOGGING) console.log(`   (Options UI - Post Peuplement) Tentative de sélection de ${storedValue} pour Setting ID ${settingId}`);
            selectElement.value = storedValue; // Tenter la sélection
            // Vérifier si la sélection a réussi
            if (selectElement.value != storedValue) {
                if (VERBOSE_LOGGING) console.warn(`   (Options UI - Post Peuplement) Impossible de sélectionner ${storedValue}. Option invalide pour les capacités actuelles (${possibleValues.join(',')})?`);
                 // Si la valeur stockée n'est pas dans les capacités, on ne peut rien faire de plus ici.
                 // Le select restera sur la première option par défaut.
            } else {
                if (VERBOSE_LOGGING) console.log(`   (Options UI - Post Peuplement) Sélection de ${storedValue} réussie.`);
            }
       } else {
            // console.log(`   (Options UI - Post Peuplement) Pas de valeur stockée à sélectionner pour Setting ${settingId}.`);
            // Le select restera sur la première option par défaut.
       }

    } else {
        // Pas de capacités reçues ou liste vide -> Masquer le contrôle
        if (VERBOSE_LOGGING) console.log(`   (Options UI) Pas de capacités valides reçues pour Setting ID ${settingId}. Masquage du contrôle.`);
        parentDivElement.classList.add('hidden'); // Masquer le conteneur
        selectElement.innerHTML = '<option value="">N/A</option>'; // Mettre à jour le select même si caché
        selectElement.disabled = true;
    }
}

// Met à jour l'état global currentSettings pour un setting donné
function updateSettingValueState(settingId, currentValue) {
    // 1. Stocker la valeur actuelle TOUJOURS
    currentSettings[settingId] = currentValue;
    console.log(`      Valeur actuelle stockée pour Setting ${settingId}:`, currentValue);

    // 2. Tenter de mettre à jour la sélection dans l'UI correspondante
    const selectElement = getSelectElementForSetting(settingId); // Récupérer l'élément <select>

    if (selectElement) {
        // Vérifier si les options existent déjà (ont été peuplées par les capacités)
        if (selectElement.options.length > 0) {
            if (VERBOSE_LOGGING) {
                console.log(`   (Update State) Tentative de sélection UI pour Setting ${settingId} avec valeur ${currentValue}`);
            }
            selectElement.value = currentValue; // Tenter de sélectionner la nouvelle valeur

            // Vérification optionnelle : est-ce que la sélection a fonctionné ?
            if (selectElement.value != currentValue) { // Utiliser != car les types peuvent différer (string vs number)
                 // console.warn(`   (Update State) Échec sélection UI pour Setting ${settingId} = ${currentValue}. Option non trouvée dans le select ?`);

                 // *** AJOUT LOGS DEBUG ***
                 // console.log(`      DEBUG: Échec sélection pour Setting ID: ${settingId}`); // Log l'ID
                 // console.log(`      DEBUG: Valeur reçue (currentValue): ${currentValue} (Type: ${typeof currentValue})`); // Log la valeur et son type
                 // const capabilities = settingCapabilities[settingId]; // Récupère les capacités stockées
                 // console.log(`      DEBUG: Capacités stockées (settingCapabilities[${settingId}]):`, capabilities ? JSON.stringify(capabilities) : 'Non définies'); // Log les capacités attendues
                 // const currentOptions = Array.from(selectElement.options).map(opt => ({ value: opt.value, text: opt.textContent })); // Récupère les options HTML actuelles
                 // console.log(`      DEBUG: Options actuelles dans le <select> HTML:`, JSON.stringify(currentOptions)); // Log les options réelles
                 // console.log(`      DEBUG: Valeur actuelle de selectElement.value après tentative: ${selectElement.value} (Type: ${typeof selectElement.value})`); // Log la valeur résultante
                 // *** FIN DES LOGS DE DÉBOGAGE ***

            } else {
                 console.log(`   (Update State) Sélection UI pour Setting ${settingId} mise à jour.`);
            }
        } else {
            // Si les options ne sont pas encore là, ce n'est pas grave.
            // updateSettingUIOptions s'en chargera quand les capacités arriveront.
            if (VERBOSE_LOGGING) console.log(`   (Update State) Options non encore peuplées pour Setting ${settingId}. Sélection UI différée.`);
        }
    } else {
         // Si on n'a pas d'élément select pour ce setting (non géré dans l'UI)
         if (VERBOSE_LOGGING) console.log(`   (Update State) Pas d'élément Select UI trouvé pour Setting ${settingId}.`);
    }

    // OPTIONNEL : On pourrait tenter une mise à jour ici SI les options existent déjà,
    // Tenter de mettre à jour la sélection dans l'UI
    // updateSettingUISelection(settingId, currentValue);
    // mais il est plus sûr de laisser updateSettingUIOptions s'en charger.
    // Laissons cette fonction uniquement pour le stockage pour l'instant.
}

async function setSetting(settingId, value) {
    if (!isCameraReady || !settingsCharacteristic) {
        displayError("Caméra non prête ou caractéristique Settings manquante.");
        return;
     }

    if (isCameraBusy) {
        console.warn(`Action Set Setting (ID: ${settingId}) bloquée: Caméra occupée.`);
        // Trouver l'élément de feedback pour afficher "Occupé..."
        let feedbackElementBusy = null;
        if (settingId === SETTING_ID_VIDEO_RESOLUTION) feedbackElementBusy = settingResolutionFeedback;
        else if (settingId === SETTING_ID_FPS) feedbackElementBusy = settingFpsFeedback;
        else if (settingId === SETTING_ID_VIDEO_LENS) feedbackElementBusy = settingLensFeedback;
        else if (settingId === SETTING_ID_GPS) feedbackElementBusy = settingGpsFeedback;
        else if (settingId === SETTING_ID_HYPERSMOOTH) feedbackElementBusy = settingHypersmoothFeedback;
        else if (settingId === SETTING_ID_VIDEO_TIMELAPSE_RATE) feedbackElementBusy = settingTimelapseRateFeedback; // AJOUT
        else if (settingId === SETTING_ID_TIMEWARP_SPEED) feedbackElementBusy = settingTimewarpSpeedFeedback;     // AJOUT
        // ... ajouter d'autres else if ici ...

        if (feedbackElementBusy) {
            feedbackElementBusy.textContent = "Occupé...";
            setTimeout(() => { if (feedbackElementBusy) feedbackElementBusy.textContent = ""; }, 2000);
        }
        return; // Ne pas envoyer la commande
     }

    // L'ID du setting détermine le type et la longueur de la valeur.
    // Pour Résolution (ID 2), c'est uint8.
    let valueBytes;
    let valueLength;
    let specificFeedbackElement = null;

    // --- Trouver l'élément de feedback AVANT le try/catch ---
    if (settingId === SETTING_ID_VIDEO_RESOLUTION) specificFeedbackElement = settingResolutionFeedback;
    else if (settingId === SETTING_ID_FPS) specificFeedbackElement = settingFpsFeedback;
    else if (settingId === SETTING_ID_VIDEO_LENS) specificFeedbackElement = settingLensFeedback;
    else if (settingId === SETTING_ID_GPS) specificFeedbackElement = settingGpsFeedback;
    else if (settingId === SETTING_ID_HYPERSMOOTH) specificFeedbackElement = settingHypersmoothFeedback;
    else if (settingId === SETTING_ID_VIDEO_TIMELAPSE_RATE) specificFeedbackElement = settingTimelapseRateFeedback; // AJOUT
    else if (settingId === SETTING_ID_TIMEWARP_SPEED) specificFeedbackElement = settingTimewarpSpeedFeedback;     // AJOUT
    // ... ajouter d'autres else if ici ...
    
    try {
         const numericValue = parseInt(value, 10); // Assurer que c'est un nombre
         if (isNaN(numericValue)) throw new Error("Valeur invalide");

         // --- Logique de préparation des bytes (devrait utiliser la fonction decodeSettingValue inversée idéalement, mais gardons simple pour uint8) ---
         // Adapter ceci si d'autres types de settings sont ajoutés
         if (settingId === SETTING_ID_VIDEO_RESOLUTION || // Exemple pour uint8
             settingId === SETTING_ID_FPS ||           // <-- AJOUTER
             settingId === SETTING_ID_VIDEO_LENS ||
             settingId === SETTING_ID_GPS ||            // <-- Ajouté
             settingId === SETTING_ID_HYPERSMOOTH ||
             settingId === SETTING_ID_VIDEO_TIMELAPSE_RATE || // AJOUT
             settingId === SETTING_ID_TIMEWARP_SPEED) {       // AJOUT
             valueBytes = new Uint8Array([numericValue]);
             valueLength = 1;
             if (VERBOSE_LOGGING) console.log(`   Préparation uint8 pour Setting ${settingId}: ${numericValue}`);
         }
         // Ajouter d'autres 'else if' pour des settings futurs avec des types différents
         // else if (settingId === AUTRE_SETTING_UINT32) {
         //    valueBytes = new Uint8Array(4);
         //    const dataView = new DataView(valueBytes.buffer);
         //    dataView.setUint32(0, numericValue, false); // Big Endian
         //    valueLength = 4;
         // }
         else {
            // Utiliser specificFeedbackElement trouvé plus haut
            const errMsg = `Type de valeur non géré pour Set Setting ID: ${settingId}`;
            console.error(errMsg); // Garder cette erreur
            if(specificFeedbackElement) {
                 specificFeedbackElement.textContent = "Type non géré!";
                 setTimeout(() => { if (specificFeedbackElement) specificFeedbackElement.textContent = ""; }, 3000);
            } else {
                 displayError(errMsg); // Erreur générale si pas de feedback spécifique
            }
            return; // TRÈS IMPORTANT: Arrêter l'exécution
         }
    } catch(e) {
        // Gérer les erreurs de parseInt ou autres erreurs de préparation
        const errMsg = `Erreur préparation valeur pour Set Setting ${settingId}: ${e.message}`;
        console.error(errMsg); // Garder cette erreur
        // Utiliser specificFeedbackElement trouvé plus haut
        if(specificFeedbackElement) {
            specificFeedbackElement.textContent = `Erreur valeur: ${e.message}`;
             setTimeout(() => { if (specificFeedbackElement) specificFeedbackElement.textContent = ""; }, 3000); // Effacer après délai
        } else {
            displayError(errMsg); // Erreur générale si pas de feedback spécifique
        }
        return; // Arrêter aussi en cas d'erreur ici
    }

    // Construire le payload TLV Command: ID(1), Len(1), Val(valueLength)
    const tlvPayload = new Uint8Array(1 + 1 + valueLength);
    tlvPayload[0] = settingId;
    tlvPayload[1] = valueLength;
    tlvPayload.set(valueBytes, 2);

    // Utiliser le helper
    const commandBuffers = buildBlePackets(tlvPayload);
    
    // Afficher feedback temporaire (utilise specificFeedbackElement trouvé plus haut)
    if (specificFeedbackElement) specificFeedbackElement.textContent = "Envoi...";

    if (commandBuffers.length > 0) {
        console.log(`Envoi Set Setting (ID: ${settingId}, Val: ${value}) sur Settings Char...`); // Garder ce log

        try {
            await sendCommand(settingsCharacteristic, commandBuffers);
            // Le feedback final ("OK" ou "Erreur X") viendra de la réponse (gérée dans handleSetSettingResponse)
        } catch (e) {
            // sendCommand gère déjà displayError, mais on peut mettre à jour le feedback spécifique
            if (specificFeedbackElement) specificFeedbackElement.textContent = "Erreur envoi.";
             setTimeout(() => { if (specificFeedbackElement) specificFeedbackElement.textContent = ""; }, 3000); // Effacer après délai
            // Pas besoin de relancer l'erreur ici, sendCommand l'a déjà fait/loggué
        }
    } else {
        // Ce cas ne devrait pas arriver si tlvPayload est valide, mais par sécurité:
        console.error(`[setSetting] buildBlePackets n'a retourné aucun paquet pour Setting ${settingId}.`); // Garder cette erreur
        if (specificFeedbackElement) specificFeedbackElement.textContent = "Erreur Interne.";
         setTimeout(() => { if (specificFeedbackElement) specificFeedbackElement.textContent = ""; }, 3000); // Effacer après délai
    }
}

// Fonction pour envoyer une requête Query générique
async function sendQuery(queryId, elementIds = []) {
    // Vérifier le drapeau de déconnexion en premier
    if (isDisconnecting && (queryId === QRY_UNREGISTER_STATUS_UPDATES || queryId === QRY_UNREGISTER_CAPABILITIES_UPDATES || queryId === QRY_UNREGISTER_SETTING_VALUE_UPDATES /*Ajouter si besoin*/)) {
        if (VERBOSE_LOGGING) console.log(`Tentative d'envoi Query Désinscription 0x${queryId.toString(16)} pendant la déconnexion.`);
        // Pour la désinscription, on ne vérifie PAS isCameraReady, juste la caractéristique
        if (!queryCharacteristic) {
             console.error(`Impossible d'envoyer Query Désinscription 0x${queryId.toString(16)}: Caractéristique Query manquante.`);
             return; // Ne peut pas envoyer sans la caractéristique
        }
        // Continuer l'envoi même si isCameraReady est false
    } else if (isDisconnecting) {
        // Annuler les autres requêtes Query pendant la déconnexion
        if (VERBOSE_LOGGING) console.warn(`Envoi Query 0x${queryId.toString(16)} annulé (déconnexion en cours).`);
        return;
    }
    // Vérifications normales si on n'est PAS en déconnexion
    else if (!isCameraReady || !queryCharacteristic) {
        console.error(`Impossible d'envoyer Query 0x${queryId.toString(16)}: Caméra non prête ou caractéristique Query manquante.`);
        return;
    }

    // Si on arrive ici, on envoie la requête
    console.log(`Envoi requête Query 0x${queryId.toString(16)} avec ${elementIds.length} éléments...`);

    // Construire le payload: Query ID + Array d'Element IDs
    const elementIdsArray = new Uint8Array(elementIds);
    const tlvPayload = new Uint8Array(1 + elementIdsArray.length); // Payload TLV est juste ID + elements
    tlvPayload[0] = queryId;
    tlvPayload.set(elementIdsArray, 1);

    // Utiliser le helper
    const commandBuffers = buildBlePackets(tlvPayload); // MTU par défaut 20

    // Envoyer sur la caractéristique Query (pas settings !)
    if (commandBuffers.length > 0) {
        try {
            // Utiliser la fonction sendCommand qui gère writeValueWithResponse
            await sendCommand(queryCharacteristic, commandBuffers); // Envoyer sur queryChar, pas settingsChar
            // Note: Le log "Commande envoyée." vient de sendCommand
        } catch (error) {
            // Erreur déjà logguée par sendCommand, mais on pourrait ajouter contexte ici
            console.error(` -> Échec spécifique lors de l'envoi Query 0x${queryId.toString(16)}`);
            // Ne pas redéclencher displayError si sendCommand l'a déjà fait.
        }
    }
}

// Fonction pour parser les données ID-Len-Val (pour statuts et capacités)
function parseIdLengthValueData(responseData, type) { // type = 'status' ou 'capability'
    // console.log(`    Parsing données ${type}...`);
    let offset = 0;
    // const updates = {}; // Pour les statuts
    const collectedCapabilities = {}; // <-- Objet temporaire pour collecter les capacités de CE message
    const collectedSettings = {}; // <-- Pour collecter les valeurs de settings
    let statusUpdates = {}; 

    // Drapeaux pour les changements DANS CE BATCH spécifique
    let presetChanged = false;
    let busyChanged = false;
    let encodingChanged = false;

    while (offset < responseData.length) {
        if (offset + 2 > responseData.length) {
            console.error(`    Données ${type} incomplètes (ID/Len manquants). Offset: ${offset}`);
            break;
        }
        const id = responseData[offset++];
        const valueLength = responseData[offset++];
        if (offset + valueLength > responseData.length) {
             console.error(`    Données ${type} incomplètes pour ID ${id}. Len attendue: ${valueLength}, Reste: ${responseData.length - offset}.`);
             break;
        }
        const valueBytes = responseData.slice(offset, offset + valueLength);
        offset += valueLength;

        // if (VERBOSE_LOGGING) console.log(`      ${type} ID: ${id}, Len: ${valueLength}, Val: ${Array.from(valueBytes).map(b => b.toString(16).padStart(2, '0')).join(':')}`);

        // Décoder et mettre à jour l'UI ou stocker la capacité
        try { // Ajouter un try/catch pour le décodage individuel
            if (type === 'status') {
                const decodedValue = decodeStatusValue(id, valueBytes);
                if (decodedValue !== null) {
                    // Stocker la mise à jour potentielle
                    statusUpdates[id] = decodedValue;
                    // Vérifier si la valeur a changé PAR RAPPORT À L'ÉTAT GLOBAL ACTUEL
                    if (currentSettings[id] !== decodedValue) {
                         console.log(`      Changement détecté pour Status ID ${id}: ${currentSettings[id]} -> ${decodedValue}`);
                        // Mettre à jour les drapeaux si nécessaire
                        if (id === STATUS_ID_PRESET_ACTIVE) presetChanged = true;
                        if (id === STATUS_ID_BUSY) busyChanged = true;
                        if (id === STATUS_ID_ENCODING) encodingChanged = true;
                    } else {
                         // Optionnel: logguer si la valeur reçue est la même que celle stockée
                         if (VERBOSE_LOGGING) console.log(`      Valeur Status ID ${id} inchangée (${decodedValue}).`);
                    }
                }
            } else if (type === 'capability') {
                // Décodage et Accumulation des capacités (inchangé, semble correct)
                const newPossibleValues = Array.from(valueBytes); // Supposant uint8 pour l'instant
                if (!collectedCapabilities[id]) collectedCapabilities[id] = [];
                collectedCapabilities[id].push(...newPossibleValues);
                // if (VERBOSE_LOGGING) console.log(`      Capacité accumulée pour Setting ID ${id}:`, collectedCapabilities[id]);

            } else if (type === 'setting') {
                // Décoder la valeur (supposant uint8 pour l'instant)
                const decodedValue = decodeSettingValue(id, valueBytes); // Appel ici !
                if (decodedValue !== null) { // Vérifier si le décodage a réussi
                    collectedSettings[id] = decodedValue;
                    // if (VERBOSE_LOGGING) console.log(`      Valeur Setting collectée - ID: ${id}, Val: ${decodedValue}`);
                } else {
                    console.warn(`      Échec décodage pour Setting ID ${id}, non collecté.`);
                }
            }
         } catch(decodeError) {
            console.error(`Erreur décodage ${type} ID ${id}:`, decodeError, valueBytes);
         }
    }

    // --- Traitement groupé APRÈS la boucle ---

    if (type === 'status') {
        if (VERBOSE_LOGGING) console.log(`    Fin parsing statuts. Changements détectés: Preset=${presetChanged}, Busy=${busyChanged}, Encoding=${encodingChanged}`);
        // Mettre à jour l'état global et l'UI pour chaque statut qui a changé
        for (const statusId in statusUpdates) {
             const numericId = parseInt(statusId, 10);
             const newValue = statusUpdates[statusId];

             // Mettre à jour l'état global
             currentSettings[numericId] = newValue;
             if (VERBOSE_LOGGING) console.log(`      État global mis à jour pour Status ${numericId}:`, newValue);

             // Mise à jour spécifique de l'UI (div de statut)
             updateSingleStatusDisplay(numericId, newValue); // Nouvelle fonction helper pour l'UI

        }

        // 2. Exécuter la logique spécifique au Timer SI l'état d'encodage a changé DANS CE BATCH
        if (encodingChanged) {
            console.log("    [PostLoop Status] Changement Encoding détecté, exécution logique Timer...");
            // La fonction utilise maintenant la variable globale currentEncodingStatus qui vient d'être mise à jour
            const newEncodingValue = currentSettings[STATUS_ID_ENCODING]; // Lire la valeur fraîchement mise à jour
            const wasEncoding = (currentEncodingStatus !== newEncodingValue); // Déduire si ça a changé (basé sur l'état AVANT ce batch)
            // Il faut stocker l'état précédent de l'encodage avant la boucle ou le passer en argument

            // --- Réintégration de la logique Timer ---
             const previousEncodingStatus = currentEncodingStatus; // Sauver l'état AVANT la mise à jour de ce batch
             // Mise à jour de l'état global (si encoding a changé)
             if (statusUpdates.hasOwnProperty(STATUS_ID_ENCODING)) {
                 currentEncodingStatus = statusUpdates[STATUS_ID_ENCODING];
             }

             if (isTimerEnabled) {
                  if (currentEncodingStatus === 1 && previousEncodingStatus === 0) { // Démarrage
                      recordingStartTime = Date.now();
                      const durationSeconds = parseInt(timedDurationInput.value, 10);
                      const durationMs = durationSeconds * 1000;
                      if (!isNaN(durationMs) && durationMs > 0) {
                          console.log("   (Timer Logic) Démarrage timer d'arrêt pour ${durationSeconds}s.");
                          if(stopTimerTimeoutId) clearTimeout(stopTimerTimeoutId);
                          stopTimerTimeoutId = setTimeout(stopRecordingViaTimer, durationMs);
                          if(countdownIntervalId) clearInterval(countdownIntervalId);
                          countdownIntervalId = setInterval(updateCountdownDisplay, 500);
                          if (timedDurationInput) timedDurationInput.disabled = true;
                          updateCountdownDisplay();
                      } else { /* log erreur durée */ }
                  } else if (currentEncodingStatus === 0 && previousEncodingStatus === 1) { // Arrêt
                      console.log("   (Timer Logic) Arrêt encodage (Timer était activé). Nettoyage timer/countdown.");
                      if (stopTimerTimeoutId) { clearTimeout(stopTimerTimeoutId); stopTimerTimeoutId = null; }
                      stopCountdownDisplay();
                      recordingStartTime = null;
                      if (timedDurationInput) timedDurationInput.disabled = false;
                  }
              } else { // Timer désactivé
                  if (currentEncodingStatus === 0 && previousEncodingStatus === 1) { // Si on arrête
                       stopCountdownDisplay();
                       recordingStartTime = null;
                  }
              }
            // --- Fin logique Timer ---
        }

        // 3. Mettre à jour l'état Busy global si changé
        if (busyChanged) {
            // L'état global currentSettings[STATUS_ID_BUSY] a déjà été mis à jour
             isCameraBusy = (currentSettings[STATUS_ID_BUSY] === 1);
             console.log(`      État global Busy mis à jour: ${isCameraBusy}`);
        }

        // 4. Mettre à jour les contrôles UI groupés SI Busy OU Encoding ont changé
        if (busyChanged || encodingChanged) {
             console.log("    [PostLoop Status] Appel groupé updateControlsBasedOnBusyState / updateRecordButtonStates");
             updateControlsBasedOnBusyState(isCameraBusy); 
             updateRecordButtonStates(); 
        }
        
        // 5. Mettre à jour le bouton preset actif si changé
        if (presetChanged) {
            markActivePresetButton();
        }

        if (VERBOSE_LOGGING) console.log("    Fin post-traitement parsing statuts.");

    } else if (type === 'capability') {
        console.log("    Capacités finales collectées:", collectedCapabilities);
        for (const settingId in collectedCapabilities) {
            const numericSettingId = parseInt(settingId, 10);
            const allPossibleValues = collectedCapabilities[settingId];
            // Mettre à jour le stockage global
            settingCapabilities[numericSettingId] = allPossibleValues;
            // Mettre à jour l'UI correspondante
            updateSettingUIOptions(numericSettingId, allPossibleValues);
        }
        console.log("    Fin parsing et mise à jour UI des capacités.");

    } else if (type === 'setting') {
        console.log("    Valeurs Settings finales collectées:", collectedSettings);
        for (const settingId in collectedSettings) {
            const numericSettingId = parseInt(settingId, 10);
            const currentValue = collectedSettings[settingId]; // Maintenant c'est la valeur unique
            // Appelle la fonction qui met à jour l'état global ET tente de maj l'UI select
            updateSettingValueState(numericSettingId, currentValue);
        }
        console.log("    Fin parsing et mise à jour UI des valeurs settings.");
    }
}

// Mettre à jour l'état VISUEL du toggle et de l'input durée
function updateTimerToggleVisualState() {
    if (!timerToggleButton || !timerToggleHandle || !timedDurationInput) return;

    const isEncoding = (currentEncodingStatus === 1);
    isTimerEnabled = timerToggleButton.getAttribute('aria-checked') === 'true'; // Lire l'état ARIA

    // Mettre à jour l'apparence du toggle
    if (isTimerEnabled) {
        timerToggleButton.classList.remove('bg-gray-200');
        timerToggleButton.classList.add('bg-blue-600'); // Couleur quand activé
        timerToggleHandle.classList.remove('translate-x-0');
        timerToggleHandle.classList.add('translate-x-5'); // Déplacer le curseur
    } else {
        timerToggleButton.classList.remove('bg-blue-600');
        timerToggleButton.classList.add('bg-gray-200');
        timerToggleHandle.classList.remove('translate-x-5');
        timerToggleHandle.classList.add('translate-x-0');
    }

    // Activer/Désactiver l'input de durée
    timedDurationInput.disabled = !isTimerEnabled || isEncoding;

    // Mettre à jour l'état des boutons record/stop (déplacé depuis l'ancienne fonction updateTimerUIState)
    updateRecordButtonStates();
}

// Fonction pour mettre à jour l'affichage du compte à rebours
function updateCountdownDisplay() {
    if (!recordingStartTime || !isTimerEnabled || !startRecordButton) {
        return; // Ne rien faire si pas en enregistrement timé
    }
    const durationMs = parseInt(timedDurationInput.value, 10) * 1000;
    const now = Date.now();
    const elapsedMs = now - recordingStartTime;
    let remainingMs = durationMs - elapsedMs;

    if (remainingMs <= 0) {
        remainingMs = 0;
        // *** AJOUT/VÉRIFICATION ***
        stopCountdownDisplay(); // Arrêter le rafraîchissement quand on atteint 0
    }

    const remainingSeconds = Math.ceil(remainingMs / 1000); // Arrondir à la seconde supérieure
    // Afficher sur le bouton Start (qui est désactivé)
    if (startRecordButton) { // Vérifier si le bouton existe
        startRecordButton.textContent = `Arrêt dans ${remainingSeconds}s`;
    }   
}

// Arrêter l'affichage du compte à rebours et restaurer le bouton
function stopCountdownDisplay() {
    if (countdownIntervalId) {
        clearInterval(countdownIntervalId);
        countdownIntervalId = null;
    }
    if (startRecordButton) {
       // Restaurer le texte seulement si on n'est plus en train d'enregistrer
        if (currentEncodingStatus === 0) {
            startRecordButton.textContent = "Démarrer Enreg.";
        }
    }
}

 // Fonction pour arrêter l'enregistrement (appelée par setTimeout)
 function stopRecordingViaTimer() {
    console.log("Timer expiré. Demande d'arrêt de l'enregistrement.");
    shutterFeedbackDiv.textContent = "Fin du temps ! Arrêt en cours...";
    stopTimerTimeoutId = null; // Effacer l'ID du timer
    stopCountdownDisplay(); // Arrêter l'affichage du compte à rebours
    setShutter(false); // Envoyer la commande d'arrêt
    // L'état final des boutons sera géré par updateStatusUI quand l'encodage passera à 0
}

// Met à jour l'état enabled/disabled des boutons principaux
function updateRecordButtonStates() {
    const isEncoding = (currentEncodingStatus === 1);
    // const busy = isCameraBusy; // Obtenir l'état Busy global
    // console.log(`[UpdateRecordButtons] Exécution. isEncoding=${isEncoding} (currentStatus=${currentEncodingStatus}), isTimerEnabled=${isTimerEnabled}`);

    if (startRecordButton && stopRecordButton && hilightButton) {
         // --- Logique START Button ---
         // Désactivé si on encode OU si la caméra est occupée (sauf si on arrête un timer?) - Gardons simple pour l'instant.
        startRecordButton.disabled = isEncoding; // || busy;
        startRecordButton.textContent = "Démarrer Enreg."; // Texte par défaut

        // --- Logique STOP Button ---
        // Désactivé si on N'encode PAS.
        stopRecordButton.disabled = !isEncoding;
        // Cacher si le timer est activé
        stopRecordButton.style.display = isTimerEnabled ? 'none' : 'inline-flex';

        // --- Logique HILIGHT Button ---
        // Désactivé si on N'encode PAS.
        hilightButton.disabled = !isEncoding;

        // --- Logique TIMER spécifique (affichage countdown) ---
        if (isTimerEnabled) {
            if (isEncoding) { // Si on encode ET que le timer est actif
                if (!countdownIntervalId && recordingStartTime) { // Démarrer l'intervalle si pas déjà fait
                    countdownIntervalId = setInterval(updateCountdownDisplay, 500);
                }
                updateCountdownDisplay(); // Mettre à jour l'affichage
                startRecordButton.disabled = true; // Le bouton start affiche le timer, il est désactivé
           } else { // Si on n'encode pas OU que le timer est désactivé
                stopCountdownDisplay(); // Arrêter l'affichage
                // Start button state is already handled above based on isEncoding/busy
           }

        } else {
            stopCountdownDisplay(); // S'assurer que le countdown est arrêté
        }

        // --- Log Final ---
        // console.log(`[UpdateRecordButtons] -> État final appliqué : Start.disabled=${startRecordButton.disabled}, Stop.disabled=${stopRecordButton.disabled}, Hilight.disabled=${hilightButton.disabled}, Stop.display=${stopRecordButton.style.display}`);

        // Ajouter un style visuel si désactivé à cause de 'busy' (optionnel)
        const busyOpacity = 0.6;
        const busyCursor = 'not-allowed';
        startRecordButton.style.opacity = startRecordButton.disabled ? busyOpacity : 1;
        startRecordButton.style.cursor = startRecordButton.disabled ? busyCursor : '';
        stopRecordButton.style.opacity = stopRecordButton.disabled ? busyOpacity : 1;
        stopRecordButton.style.cursor = stopRecordButton.disabled ? busyCursor : '';
        hilightButton.style.opacity = hilightButton.disabled ? busyOpacity : 1;
        hilightButton.style.cursor = hilightButton.disabled ? busyCursor : '';

    } else {
        console.warn("[UpdateRecordButtons] Un ou plusieurs boutons non trouvés.");
    }
}

// Met à jour l'indicateur et le texte de statut de connexion
function updateConnectionStatus(statusText, dotClass) {
    connectionStatusDiv.innerHTML = `<span class="connection-dot ${dotClass}"></span><span>${statusText}</span>`;
}

// Affiche les messages d'erreur
function displayError(message) {
    console.error(message);
    if (errorMessageDiv) errorMessageDiv.textContent = `Erreur: ${message}`;

    // --- Vérifier si cette logique est souhaitée ---
    // Doit-on VRAIMENT réinitialiser si l'appareil est encore connecté ?
    // Peut-être seulement si l'erreur est TRÈS critique ou si on sait qu'on est déconnecté.
    // if (goproDevice && !goproDevice.gatt.connected) {
    //    resetConnectionState(); // Réinitialiser seulement si DÉJÀ déconnecté.
    // }
    // --- Fin de la vérification ---

    // Si l'erreur GetHardwareInfo spécifique (status != 0 et != 1 et != 2) se produit,
    // on pourrait vouloir explicitement arrêter le readiness check ici :
    if (message.includes("statut inattendu") && message.includes("GetHardwareInfo")) {
         if (readinessCheckInterval) {
             clearInterval(readinessCheckInterval);
             readinessCheckInterval = null;
             console.log("Arrêt du Readiness Check dû à une erreur GetHardwareInfo inattendue.");
             // Peut-être déconnecter ici ?
             // if (goproDevice && goproDevice.gatt.connected) goproDevice.gatt.disconnect();
         }
    }
}


// Fonction pour envoyer une commande à la caméra
async function sendCommand(characteristic, commandBuffers) {
    if (!characteristic) {
        displayError("Caractéristique invalide pour sendCommand.");
        // Lancer une erreur pour arrêter la séquence appelante si critique
        throw new Error("Caractéristique invalide pour sendCommand.");
    }

    // Assurer que commandBuffers est un tableau
    if (!Array.isArray(commandBuffers)) {
        commandBuffers = [commandBuffers]; // Envelopper dans un tableau si un seul buffer est passé
    }
    if (commandBuffers.length === 0) {
        displayError("Aucune donnée de commande à envoyer.");
        throw new Error("Aucune donnée de commande à envoyer.");
    }

    if (VERBOSE_LOGGING) console.log(`[SendCommand] Tentative d'écriture sur Char UUID: ${characteristic.uuid}`);
    
    // Attendre que le verrou soit libéré
    while (isGattOperationInProgress) {
        // console.log("Attente verrou GATT...");
        await new Promise(resolve => setTimeout(resolve, GATT_RETRY_DELAY)); // Attente non bloquante
    }
    try {
        isGattOperationInProgress = true; // Prendre le verrou
        for (let i = 0; i < commandBuffers.length; i++) {
            const buffer = commandBuffers[i];
            // if (VERBOSE_LOGGING) console.log(`  -> Envoi paquet ${i + 1}/${commandBuffers.length} (${buffer.byteLength} bytes): ${Array.from(new Uint8Array(buffer)).map(b => b.toString(16).padStart(2, '0')).join(':')}`);
            // Utiliser writeValueWithoutResponse pour les paquets de continuation ?
            // La doc ne le précise pas, restons sur WithResponse pour le moment par sécurité.
            await characteristic.writeValueWithResponse(buffer);
            // Ajouter un petit délai si nécessaire entre les paquets ?
            // await new Promise(resolve => setTimeout(resolve, 20)); // Ex: 20ms
        }
        // if (VERBOSE_LOGGING) console.log(`[SendCommand] ${commandBuffers.length} paquet(s) envoyé(s) (confirmé(s) par BLE).`);
    } catch (error) {
        displayError(`Échec de l'envoi de la commande : ${error.message}`);
        console.error("Erreur détaillée sendCommand:", error); // Garder l'erreur détaillée en console
        throw error; // Toujours relancer pour que l'appelant soit notifié
    } finally {
        isGattOperationInProgress = false; // Libérer le verrou IMPÉRATIVEMENT
        // console.log("Verrou GATT libéré.");
    }
}

// Gère les notifications reçues (réponses aux commandes/requêtes)
function handleNotifications(event) {
    const characteristic = event.target;
    const packetDataView = characteristic.value; // C'est un DataView
    const packetBytes = new Uint8Array(packetDataView.buffer);
    if (VERBOSE_LOGGING) console.log(`[HandleNotify] Notification reçue sur ${characteristic.uuid} (${packetBytes.length} bytes): ${Array.from(packetBytes).map(b => b.toString(16).padStart(2, '0')).join(':')}`);
    
    if (packetBytes.length === 0) return;

    const firstByte = packetBytes[0];
    const isContinuation = (firstByte & 0b10000000) !== 0; // Bit 7 = 1

    if (!isContinuation) {
        // --- C'est un PAQUET DE DÉPART ---
        if (incomingMessage.isAssembling) {
            console.warn("Nouveau paquet de départ reçu alors qu'un message était en cours d'assemblage. Abandon de l'ancien.");
            // Réinitialiser l'état d'assemblage
        }
        incomingMessage.isAssembling = true;
        incomingMessage.buffer = new Uint8Array(0); // Vider le buffer précédent
        incomingMessage.receivedLength = 0;

        let headerLength = 0;
        let payloadPart = null;
        // Isoler les bits 6 et 5 pour le type (00, 01, 10)
        const headerType = (firstByte & 0b01100000) >> 5;

        if (headerType === 0b00) { // Type 00: General 5-bit (000xxxxx)
            incomingMessage.expectedLength = firstByte & 0b00011111;
            headerLength = 1;
            // if (VERBOSE_LOGGING) console.log(`  Paquet Départ (General 5-bit). Longueur attendue: ${incomingMessage.expectedLength}`);
        } else if (headerType === 0b01 && packetBytes.length >= 2) { // Type 01: Extended 13-bit (010xxxxx LLLLLLLL)
            incomingMessage.expectedLength = ((firstByte & 0b00011111) << 8) | packetBytes[1];
            headerLength = 2;
            // if (VERBOSE_LOGGING) console.log(`  Paquet Départ (Extended 13-bit). Longueur attendue: ${incomingMessage.expectedLength}`);
        } else if (headerType === 0b10 && packetBytes.length >= 3) { // Type 10: Extended 16-bit (100xxxxx LLLLLLLL LLLLLLLL)
            incomingMessage.expectedLength = (packetBytes[1] << 8) | packetBytes[2];
            headerLength = 3;
            // if (VERBOSE_LOGGING) console.log(`  Paquet Départ (Extended 16-bit). Longueur attendue: ${incomingMessage.expectedLength}`);
        } else {
            console.error(`  En-tête paquet départ non reconnu/incomplet: Byte=${firstByte.toString(16)}, Type=${headerType.toString(2)}, Len=${packetBytes.length}`);
            incomingMessage.isAssembling = false;
            return; // Impossible de continuer
        }

        // Vérifier si la longueur attendue est valide (au moins > 0 si paquet non vide)
        if (incomingMessage.expectedLength === 0 && packetBytes.length > headerLength) {
            console.warn(`  Longueur attendue calculée à 0, mais paquet contient des données (${packetBytes.length - headerLength} bytes). Header: ${firstByte.toString(16)}`);
            // Vous pourriez tenter de deviner la longueur ou ignorer le paquet. Ignorer est plus sûr.
            // incomingMessage.isAssembling = false;
            // return;
            // Ou tenter de prendre tout le reste ?
            incomingMessage.expectedLength = packetBytes.length - headerLength;
            console.warn(`  Ajustement longueur attendue à ${incomingMessage.expectedLength}`);

        } else if (incomingMessage.expectedLength > 50000) { // Limite arbitraire pour éviter allocations mémoire énormes
           console.error(`  Longueur attendue irréaliste (${incomingMessage.expectedLength}). Header: ${firstByte.toString(16)}`);
           incomingMessage.isAssembling = false;
           return;
        }

        if (VERBOSE_LOGGING) console.log(`[HandleNotify] Paquet Départ - Type: ${headerType.toString(2)}, Longueur Attendue: ${incomingMessage.expectedLength}`); // <-- NOUVEAU LOG

        // Extraction et stockage de la première partie du payload
        payloadPart = packetBytes.slice(headerLength);
        incomingMessage.buffer = new Uint8Array(payloadPart);
        incomingMessage.receivedLength = payloadPart.length;

    } else {
        // --- C'est un PAQUET DE CONTINUATION ---
        if (!incomingMessage.isAssembling) {
            console.warn("Paquet de continuation reçu sans paquet de départ préalable. Ignoré.");
            return;
        }

        // L'en-tête de continuation est juste le premier octet (1RRRCCCC)
        // const counter = firstByte & 0b00001111;
        // if (VERBOSE_LOGGING) console.log(`  Paquet Continuation (Compteur: ${counter})`);
        const payloadPart = packetBytes.slice(1); // Le reste est du payload
        if (VERBOSE_LOGGING) console.log(`[HandleNotify] Paquet Continuation reçu (${payloadPart.length} bytes payload).`); // <-- NOUVEAU LOG

        // Concaténer le nouveau payload au buffer existant
        const newBuffer = new Uint8Array(incomingMessage.buffer.length + payloadPart.length);
        newBuffer.set(incomingMessage.buffer, 0);
        newBuffer.set(payloadPart, incomingMessage.buffer.length);
        incomingMessage.buffer = newBuffer;
        incomingMessage.receivedLength += payloadPart.length;
    }

    if (VERBOSE_LOGGING) console.log(`[HandleNotify] Assemblage: ${incomingMessage.receivedLength} / ${incomingMessage.expectedLength} bytes.`); // <-- NOUVEAU LOG

    // --- Vérifier si le message est complet ---
    if (incomingMessage.isAssembling && incomingMessage.receivedLength >= incomingMessage.expectedLength) {
        const fullMessagePayload = incomingMessage.buffer.slice(0, incomingMessage.expectedLength); // S'assurer de ne pas dépasser
        console.log(`[HandleNotify] Message complet (${fullMessagePayload.length} bytes). Source UUID: ${characteristic.uuid}`); // <-- NOUVEAU LOG
        if (VERBOSE_LOGGING) console.log(`[HandleNotify]   Payload brut: ${Array.from(fullMessagePayload).map(b => b.toString(16).padStart(2, '0')).join(':')}`); // <-- NOUVEAU LOG

        // Réinitialiser l'état d'assemblage pour le prochain message
        incomingMessage.isAssembling = false;
        // incomingMessage.expectedLength = 0;
        // incomingMessage.receivedLength = 0;
        // NE PAS vider le buffer ici, il est passé à l'analyse

        // --- Analyser le message complet (Logique existante, mais avec fullMessagePayload) ---
        if (fullMessagePayload.length === 0) {
            if (VERBOSE_LOGGING) console.warn("Payload complet est vide après réassemblage.");
            return;
        }
             
        // Identifier le type de réponse basé sur l'UUID de la caractéristique source
        const sourceUuid = event.target.uuid;

        try {
                // 1. Est-ce une réponse COMMANDE ou SETTING ?
                if (sourceUuid === COMMAND_RSP_CHAR_UUID || sourceUuid === SETTINGS_RSP_CHAR_UUID) {
                    // --- Réponse Commande/Setting (TLV) ---
                    if (VERBOSE_LOGGING) console.log("[HandleNotify] -> Parsing comme Réponse Commande/Setting TLV..."); // <-- NOUVEAU LOG
                    const id = fullMessagePayload[0];
                    let status = -1;
                    let responseData = new Uint8Array(0);

                    if (fullMessagePayload.length >= 2) {
                        status = fullMessagePayload[1];
                        responseData = fullMessagePayload.slice(2);
                    } else { // Moins de 2 bytes: juste l'ID, pas de statut ? Très rare pour Cmd/Set Rsp.
                        responseData = fullMessagePayload.slice(1);
                    }

                    // --- LOG CRUCIAL ---
                    console.log(`[HandleNotify]   TLV ID: 0x${id.toString(16)}, Status Exec: ${status}, Payload Data Len: ${responseData.length}`);
                    // --- FIN LOG CRUCIAL ---

                    // Gestion spécifique des réponses TLV Command/Setting
                    if (id === CMD_GET_HARDWARE_INFO) {
                        if (VERBOSE_LOGGING) console.log("[HandleNotify]   -> Appel handleHardwareInfoResponse..."); // <-- NOUVEAU LOG
                        handleHardwareInfoResponse(status, responseData);
                    } else if (id === CMD_KEEP_ALIVE) {
                        if (status === 0) { if (VERBOSE_LOGGING) console.log("    Réponse Keep Alive (0x5B) OK."); }
                        else { console.warn(`    Réponse Keep Alive (0x5B) avec statut ${status}`); }
                    } else if (id === CMD_SET_SHUTTER) {
                        handleShutterResponse(status, responseData);
                    } else if (id === CMD_HILIGHT) {
                        handleHilightResponse(status, responseData);
                    } else if (id === CMD_LOAD_PRESET) { // CMD_LOAD_PRESET
                        // console.log(`    Réponse Load Preset (0x40), Status Exec=${status}`);
                        handleLoadPresetResponse(status, responseData);
                    } else if (id >= 2 && id <= 234) { // Supposition: Plage des Settings IDs
                        handleSetSettingResponse(id, status, responseData);
                    } else {
                        console.warn(`    Réponse Commande/Setting TLV non gérée pour ID 0x${id.toString(16)}`);
                    }
                    return;
                }
                    
                // 2. Est-ce une réponse QUERY ? (Peut être Protobuf ou TLV)
                if (sourceUuid === QUERY_RSP_CHAR_UUID) {
                    if (VERBOSE_LOGGING) console.log("[HandleNotify] -> Vérification Protobuf sur Query Response Char..."); // <-- NOUVEAU LOG
                 
                    // 2a. Essayer de traiter comme Protobuf Preset D'ABORD
                    if (!isDisconnecting && Root && NotifyPresetStatusType && fullMessagePayload.length >= 2) { // AJOUT: !isDisconnecting && Root check
                        const featureId = fullMessagePayload[0];
                        const actionId = fullMessagePayload[1];
                        if (featureId === 0xF5 && (actionId === 0xF2 || actionId === 0xF3)) {
                            console.log(`    >> Détecté Protobuf Preset Status (Feature: 0x${featureId.toString(16)}, Action: 0x${actionId.toString(16)})`);
                            const protoDataBytes = fullMessagePayload.slice(2);    
                            try {
                                // Décoder le message Protobuf
                                const decodedPresets = NotifyPresetStatusType.decode(protoDataBytes);
                                console.log("    >> Presets Protobuf décodés:", JSON.stringify(decodedPresets.toJSON())); // Log l'objet JS
                                
                                // *** Mise à jour Cache ET UI ***
                                processPresetData(decodedPresets.presetGroupArray, actionId); // Passer actionId pour savoir si c'est initial
                                return;
                                // Stocker et mettre à jour l'UI
                                /* availablePresets.groups = decodedPresets.presetGroupArray || [];
                                availablePresets.lastUpdateTime = Date.now();
                                updatePresetsUI(availablePresets.groups); */
                            } catch (e) {
                                console.error("    >> Erreur lors du décodage Protobuf des presets:", e);
                                displayError("Erreur lors de la lecture des données de presets.");
                            }
                            // **Important**: Sortir après avoir traité le message Protobuf
                            // return;
                        }
                        // Ajouter ici d'autres 'if' pour d'autres Feature/Action ID Protobuf si nécessaire
                    }
                    // *** FIN DE LA VÉRIFICATION PROTOBUF ***

                    // 2b. SI CE N'ÉTAIT PAS un Protobuf traité, ALORS traiter comme TLV Query
                    if (VERBOSE_LOGGING) console.log("[HandleNotify] -> Parsing comme TLV Query..."); // <-- NOUVEAU LOG
                    const id = fullMessagePayload[0]; // TLV Query ID
                    let status = -1;
                    let responseData = new Uint8Array(0);
                    if (fullMessagePayload.length >= 2) {
                        status = fullMessagePayload[1];
                        responseData = fullMessagePayload.slice(2);
                    } else { responseData = fullMessagePayload.slice(1); }
                    // console.log(`[HandleNotify]   TLV Query ID: 0x${id.toString(16)}, Status Exec: ${status}, Payload Data Len: ${responseData.length}`); // <-- NOUVEAU LOG

                     // ... (Logique switch(id) pour TLV Query 0x13, 0x53, 0x93, 0x32, 0x62, 0xA2, 0x12, 0x52, 0x92 comme avant) ...
                    if (status === 0 || id === ASYNC_STATUS_NOTIFICATION || id === ASYNC_CAPABILITIES_NOTIFICATION || id === ASYNC_SETTING_NOTIFICATION) { // Inclure 0x92
                        switch (id) {
                            case RSP_GET_STATUS_VALUES: case RSP_REGISTER_STATUS_UPDATES: case ASYNC_STATUS_NOTIFICATION:
                                if (VERBOSE_LOGGING) console.log(`[HandleNotify]      -> Parsing TLV Statuts (Source ID: 0x${id.toString(16)})`); // <-- NOUVEAU LOG
                                parseIdLengthValueData(responseData, 'status');
                                break;
                            case RSP_GET_SETTING_CAPABILITIES: case RSP_REGISTER_CAPABILITIES_UPDATES: case ASYNC_CAPABILITIES_NOTIFICATION:
                                console.log(`[HandleNotify]      -> Parsing TLV Capacités (Source ID: 0x${id.toString(16)})`); // <-- NOUVEAU LOG
                                parseIdLengthValueData(responseData, 'capability');
                                break;
                            case RSP_GET_SETTING_VALUES: case RSP_REGISTER_SETTING_UPDATES: case ASYNC_SETTING_NOTIFICATION: // <-- AJOUTER CAS SETTING VALUE
                            console.log(`[HandleNotify]      -> Parsing TLV Valeurs Settings (Source ID: 0x${id.toString(16)})`); // <-- NOUVEAU LOG
                                parseIdLengthValueData(responseData, 'setting'); // <-- Utiliser type 'setting'
                                break;
                            case QRY_UNREGISTER_STATUS_UPDATES: // 0x73
                            case QRY_UNREGISTER_CAPABILITIES_UPDATES: // 0x82
                            case QRY_UNREGISTER_SETTING_VALUE_UPDATES: // 0x72
                                // Réponse à une désinscription - Rien à faire de spécial
                                if (VERBOSE_LOGGING) console.log(`    Confirmation désinscription reçue (ID 0x${id.toString(16)}), ignorée.`);
                                break;
                            default:
                                console.warn(`    TLV Query ID 0x${id.toString(16)} non géré (ou notification async inconnue).`);
                        }
                    } else { // La requête initiale TLV Query a échoué (status != 0)
                        console.error(`    La requête TLV Query 0x${id.toString(16)} a échoué avec statut ${status}`);
                        displayError(`Erreur requête Query 0x${id.toString(16)} (status ${status})`);
                    }
                    return; // Sortir après traitement TLV Query
                }
            

                // 3. SINON, UUID inconnu
                console.warn(`    Réponse reçue sur UUID inconnu: ${sourceUuid}`);
                // Fin de la structure if/else if/else pour sourceUuid
    
        } catch (error) { // Fin du bloc try
                 // Si une erreur survient, s'assurer de réinitialiser l'état du timer
                 console.error("Erreur majeure lors de la gestion des notifications:", error);
                 displayError(`Erreur interne: ${error.message}`);
                 // Réinitialiser l'état du timer par sécurité
                 if (stopTimerTimeoutId) { clearTimeout(stopTimerTimeoutId); stopTimerTimeoutId = null; }
                 stopCountdownDisplay();
                 recordingStartTime = null;
                 updateRecordButtonStates(); // Mettre à jour les boutons vers un état sûr
        }

        
    } else if (incomingMessage.isAssembling) {
        // if (VERBOSE_LOGGING) console.log("  En attente de paquets de continuation...");
    }
}

// --- Fonctions de gestion des réponses spécifiques (à externaliser) ---
function handleShutterResponse(status, responseData) {
    // Logique de la réponse Set Shutter (0x01)
    if (status === 0) {
        if (VERBOSE_LOGGING) console.log("    Réponse Set Shutter (0x01) OK.");
         shutterFeedbackDiv.textContent = "Commande Shutter reçue.";
         setTimeout(() => { if(shutterFeedbackDiv) shutterFeedbackDiv.textContent = "" }, 1500);
         // Le changement d'état UI se fera via la notif status 10
    } else {
         console.error(`    Réponse Set Shutter (0x01) ÉCHEC avec statut ${status}`);
         shutterFeedbackDiv.textContent = `Erreur Shutter (${status})`;
         setTimeout(() => { if(shutterFeedbackDiv) shutterFeedbackDiv.textContent = "" }, 3000);
         // Annuler le timer si la commande de démarrage a échoué
         if (isTimerEnabled && recordingStartTime === null) { // Si timer activé et qu'on essayait de démarrer
            if (stopTimerTimeoutId) { clearTimeout(stopTimerTimeoutId); stopTimerTimeoutId = null; }
            stopCountdownDisplay();
            if (timedDurationInput) timedDurationInput.disabled = false;
         }
         updateRecordButtonStates(); // Mettre à jour l'état des boutons en cas d'échec
    }
}

function handleHilightResponse(status, responseData) {
    // Logique de la réponse Hilight (0x18)
    if (status === 0) {
         if (VERBOSE_LOGGING) console.log("    Réponse Hilight (0x18) OK.");
         if (hilightFeedbackDiv) hilightFeedbackDiv.textContent = "Hilight ajouté !";
         setTimeout(() => { if(hilightFeedbackDiv) hilightFeedbackDiv.textContent = "" }, 2000);
    } else {
         console.error(`    Réponse Hilight (0x18) ÉCHEC avec statut ${status}`);
         if (hilightFeedbackDiv) hilightFeedbackDiv.textContent = `Erreur Hilight (${status})`;
         setTimeout(() => { if(hilightFeedbackDiv) hilightFeedbackDiv.textContent = "" }, 3000);
    }
    // L'état du bouton Hilight est géré par updateStatusUI en fonction de l'encodage
}

function handleSetSettingResponse(settingId, status, responseData) {
    // Logique de la réponse Set Setting (IDs 2 à 234...)
    console.log(`    Réponse Set Setting pour ID ${settingId}, Status: ${status}`);
    // Trouver l'élément de feedback correspondant (ex: par data-attribute ou convention d'ID)
    const feedbackElement = document.getElementById(`setting-${settingId}-feedback`); // Convention exemple

    if (feedbackElement) {
        feedbackElement.textContent = (status === 0) ? "OK" : `Erreur (${status})`;
        setTimeout(() => { if (feedbackElement) feedbackElement.textContent = ""; }, 2000);
    }

    if (status !== 0) {
        console.error(`    Échec Set Setting (ID ${settingId}), Status: ${status}`);
        // Idéalement, ici on lirait la valeur actuelle via les statuts stockés
        // et on remettrait l'élément UI (ex: select) à cette valeur précédente.
        // Exemple simplifié :
        const selectElement = document.getElementById(`setting-${settingId}`); // Convention exemple
        // if (selectElement && typeof currentStatusValues !== 'undefined' && currentStatusValues[settingId] !== undefined) {
        //     selectElement.value = currentStatusValues[settingId];
        // }
    }
}

// --- Gérer la réponse Load Preset ---
function handleLoadPresetResponse(status, responseData) {
    const feedbackElement = document.getElementById('presets-feedback'); // Assumer existence
    if (status === 0) {
        // if (VERBOSE_LOGGING) console.log("    Réponse Load Preset (0x40) OK.");
        if(feedbackElement) feedbackElement.textContent = "Preset chargé !";
        // Rafraîchir potentiellement les statuts/capacités car le mode a changé
        console.log("    Preset chargé, demande des MAJ settings/capacités..."); // Garder ce log
        // sendQuery(QRY_GET_STATUS_VALUES, requestedStatusIds); // Optionnel
        sendQuery(QRY_GET_SETTING_CAPABILITIES, requestedCapabilityIds);
        sendQuery(QRY_GET_SETTING_VALUES, requestedSettingIds);
    } else {
        console.error(`    Réponse Load Preset (0x40) ÉCHEC avec statut ${status}`);
        if(feedbackElement) feedbackElement.textContent = `Erreur chargement preset (${status}).`;
    }
     setTimeout(() => { if(feedbackElement) feedbackElement.textContent = "";}, 2500);
}

// Gère spécifiquement la réponse de GetHardwareInfo
async function handleHardwareInfoResponse(status, responseData) { // responseData contient maintenant le payload *après* ID et Status

    // console.log(`[HandleHardwareInfoResponse] Reçu Status: ${status}, Payload Len: ${responseData.length}`);

    // Statuts documentés: 0 = Succès, 2 = Erreur (pas prêt)
    if (status === 0) {
        console.log("GetHardwareInfo: Succès ! La caméra est prête.");
        isCameraReady = true;
        if (readinessCheckInterval) { // Vérifier si l'intervalle existe avant de le clear
            clearInterval(readinessCheckInterval);
            readinessCheckInterval = null;
        }
        updateConnectionStatus('Prêt', 'dot-green');
        // Afficher les sections UI pertinentes
        if (controlsSection) controlsSection.classList.remove('hidden');
        if (statusSection) statusSection.classList.remove('hidden');
        // La section preset sera affichée par updatePresetsUI si des presets sont trouvés

        // Décoder les infos matérielles (basé sur la doc Query -> Get Hardware Info -> Parameters)
        console.log("Actions post-connexion: Démarrage Keep Alive, Enregistrement Notifs, Get Statuts Initiaux...");
        try {
            startKeepAlive(); // Pas besoin d'await ici car il lance juste un setInterval

            // Enregistrer pour les mises à jour de statut et de capacité (séquentiellement)
            await sendQuery(QRY_REGISTER_STATUS_UPDATES, requestedStatusIds);
            if (VERBOSE_LOGGING) console.log("  Enregistrement Status OK (commande envoyée).");

            await sendQuery(QRY_REGISTER_CAPABILITIES_UPDATES, requestedSettingIds); // Utiliser requestedSettingIds ici
            console.log("  Enregistrement Capacités OK.");

            // *** NOUVEAU: Enregistrement Valeurs Settings ***
            await sendQuery(QRY_REGISTER_SETTING_UPDATES, requestedSettingIds);
            console.log("  Enregistrement Valeurs Settings OK (commande envoyée).");

            // Demander l'état initial des statuts (une seule fois)
            if (!initialStatusRequested) {
                await sendQuery(QRY_GET_STATUS_VALUES, requestedStatusIds);
                if (VERBOSE_LOGGING) console.log("  Get Statuts Initiaux OK (commande envoyée).");
                initialStatusRequested = true; // Mettre le drapeau après la tentative d'envoi
             }
            // Demander l'état initial des settings (une seule fois)
            if (!initialSettingsRequested) { // Utiliser le nouveau drapeau
                 await sendQuery(QRY_GET_SETTING_VALUES, requestedSettingIds);
                 if (VERBOSE_LOGGING) console.log("  Get Settings Initiaux OK (commande envoyée).");
                 initialSettingsRequested = true; // Mettre le drapeau
             }
            // *** NOUVEL APPEL : Demander les presets via Protobuf ***
            if (RequestGetPresetStatusType) { // Vérifier si protobuf est prêt
                await getAvailablePresets();
                console.log("  Get Presets Initiaux Protobuf OK (requête envoyée).");
             } else {
                console.warn("    -> Protobuf non initialisé ou Type RequestGetPresetStatus non trouvé. Impossible de demander les presets.");
             }
            // ******************************************************

            console.log("Actions post-connexion terminées.");


        } catch (error) { // Catch l'erreur relancée par sendCommand via sendQuery
             console.error("Erreur pendant les actions post-connexion:", error);
             displayError("Erreur lors de l'initialisation des notifications/statuts après GetHardwareInfo.");
             // Peut-être essayer de se déconnecter proprement ici ?
             // await sendQuery(QRY_UNREGISTER_STATUS_UPDATES, []); // Essayer de désinscrire
             // if (goproDevice && goproDevice.gatt.connected) goproDevice.gatt.disconnect();
        }
        // ---- Fin du bloc try/catch ----

    } else if (status === 2 || status === 1) {
        const reason = (status === 2) ? "Pas Prêt (Status 2)" : "Erreur Initiale (Status 1)";
        console.log(`GetHardwareInfo: Caméra non prête (${reason}). Nouvel essai dans 2s...`);
        isCameraReady = false;
        // Le `readinessCheckInterval` va renvoyer la commande.
    } else {
        console.error(`[HandleHardwareInfoResponse] Status ${status}: VRAIMENT inattendu. Arrêt.`); // <-- Modifié pour insister sur l'erreur
        isCameraReady = false;
        if (readinessCheckInterval) {
            clearInterval(readinessCheckInterval);
            readinessCheckInterval = null;
        }
        displayError(`La caméra a répondu avec un statut inattendu (${status}) à GetHardwareInfo.`);
    }
}

// Fonction pour envoyer périodiquement GetHardwareInfo jusqu'à succès
function startReadinessCheck() {
    if (readinessCheckInterval) clearInterval(readinessCheckInterval); // Effacer l'ancien intervalle

    const check = async () => {
        if (goproDevice && goproDevice.gatt.connected && commandCharacteristic && !isCameraReady) {
            // if (VERBOSE_LOGGING) console.log("Envoi de GetHardwareInfo (0x3C)...");

            const tlvPayload = new Uint8Array([CMD_GET_HARDWARE_INFO]);
            const commandBuffers = buildBlePackets(tlvPayload); // Retourne un tableau


            if (commandBuffers.length > 0) {
                // if (VERBOSE_LOGGING) console.log(`[ReadyCheck] Données envoyées (1er paquet): ${Array.from(new Uint8Array(commandBuffers[0])).map(b => b.toString(16).padStart(2, '0')).join(':')}`); // Utiliser commandBuffers[0]
                try { // Ajouter un try/catch autour de l'envoi spécifique
                    await sendCommand(commandCharacteristic, commandBuffers);
                } catch (sendError) {
                    console.error("[ReadyCheck] Erreur lors de l'envoi de GetHardwareInfo:", sendError);
                    // On pourrait arrêter l'intervalle ici si l'envoi échoue ?
                    // clearInterval(readinessCheckInterval);
                    // readinessCheckInterval = null;
                }
            }

        } else if (isCameraReady) {
             clearInterval(readinessCheckInterval); // Arrêter si déjà prêt
             readinessCheckInterval = null;
        } else if (!goproDevice || !goproDevice.gatt.connected) {
             console.log("Arrêt de la vérification de disponibilité (déconnecté).");
             clearInterval(readinessCheckInterval);
             readinessCheckInterval = null;
             resetConnectionState();
        }
    };

    // Envoyer immédiatement, puis toutes les 2 secondes jusqu'à succès
    if (VERBOSE_LOGGING) console.log("[ReadyCheck] Premier appel de check()"); 
    check();
    if (!readinessCheckInterval && !isCameraReady) { // Ne démarrer que si pas déjà prêt
        readinessCheckInterval = setInterval(check, 2000);
        if (VERBOSE_LOGGING) console.log("[ReadyCheck] Intervalle de vérification démarré (2s)."); // <-- NOUVEAU LOG
    }
}

// Gère la déconnexion
function onDisconnected(event) {
    displayError('GoPro déconnectée.');
    resetConnectionState();
}

// Fonction de connexion principale
async function connectGopro() {
    errorMessageDiv.textContent = ''; // Effacer les erreurs précédentes
    if (goproDevice && goproDevice.gatt.connected) {
        console.log("Déconnexion...");
        isDisconnecting = true; // <-- Mettre le drapeau IMMÉDIATEMENT

        // --- Arrêter les timers/intervalles d'abord ---
        if (keepAliveInterval) { clearInterval(keepAliveInterval); keepAliveInterval = null; /* console.log("Keep Alive arrêté."); */ }
        if (readinessCheckInterval) { clearInterval(readinessCheckInterval); readinessCheckInterval = null; /* console.log("Readiness Check arrêté."); */ }
        if (stopTimerTimeoutId) { clearTimeout(stopTimerTimeoutId); stopTimerTimeoutId = null; }
        if (countdownIntervalId) { clearInterval(countdownIntervalId); countdownIntervalId = null; }
         // ... arrêter d'autres timers si nécessaire ...

        try {
            // --- Désinscription AVANT de déconnecter ---
            // Utiliser un try/catch séparé car la désinscription peut échouer si déjà déconnecté
            if (queryCharacteristic && queryResponseCharacteristic) { // Vérifier si les caractéristiques existent
                console.log("Tentative de désinscription des notifications...");
                await sendQuery(QRY_UNREGISTER_STATUS_UPDATES, []); // Envoyer tableau vide pour tout désinscrire
                await sendQuery(QRY_UNREGISTER_CAPABILITIES_UPDATES, []);
                await sendQuery(QRY_UNREGISTER_SETTING_VALUE_UPDATES, []);
                console.log("Désinscription des notifications de statut, capacité et settings (commandes envoyées).");
            } else {
                console.warn("Caractéristiques Query non disponibles pour la désinscription.");
            }
        } catch (unregisterError) {
            // Afficher l'erreur mais continuer la déconnexion
            console.warn(`Erreur lors de la désinscription (peut être normal si déjà déconnecté): ${unregisterError.message}`);
        }

        // --- Déconnecter ---
        console.log("Appel de goproDevice.gatt.disconnect().");
        if (goproDevice && goproDevice.gatt) { // Vérifier existence avant d'appeler
            goproDevice.gatt.disconnect(); // Maintenant on peut déconnecter en toute sécurité
        }
        // L'événement onDisconnected et resetConnectionState s'occuperont du reste du nettoyage UI/état


        return; // Sortir de la fonction ici
    }

    try {
        updateConnectionStatus('Scan en cours...', 'dot-yellow');
        console.log("Demande de périphérique Bluetooth...");
        goproDevice = await navigator.bluetooth.requestDevice({
            filters: [{ services: [GOPRO_SERVICE_UUID] }],
            optionalServices: [ /* Ajouter ici d'autres services si nécessaire plus tard */ ]
        });

        updateConnectionStatus('Connexion...', 'dot-yellow');
        console.log(`Connexion à ${goproDevice.name}...`);
        goproDevice.addEventListener('gattserverdisconnected', onDisconnected);
        goproServer = await goproDevice.gatt.connect();
        console.log("Connecté au serveur GATT.");

        updateConnectionStatus('Découverte services...', 'dot-yellow');
        console.log("Obtention du service GoPro...");
        goproService = await goproServer.getPrimaryService(GOPRO_SERVICE_UUID);
        console.log("Service obtenu.");

        console.log("Obtention des caractéristiques...");
        // Obtenir les caractéristiques nécessaires
        commandCharacteristic = await goproService.getCharacteristic(COMMAND_CHAR_UUID);
        commandResponseCharacteristic = await goproService.getCharacteristic(COMMAND_RSP_CHAR_UUID);
        settingsCharacteristic = await goproService.getCharacteristic(SETTINGS_CHAR_UUID);
        settingsResponseCharacteristic = await goproService.getCharacteristic(SETTINGS_RSP_CHAR_UUID);
        // S'assurer que Query et Query Response sont bien récupérés
        // if (!queryCharacteristic || !queryResponseCharacteristic) {
            queryCharacteristic = await goproService.getCharacteristic(QUERY_CHAR_UUID);
            queryResponseCharacteristic = await goproService.getCharacteristic(QUERY_RSP_CHAR_UUID);
             // S'assurer de la souscription si on ne l'avait pas fait avant
            // await queryResponseCharacteristic.startNotifications();
            // queryResponseCharacteristic.addEventListener('characteristicvaluechanged', handleNotifications);
        // }
        console.log("Caractéristiques obtenues (Command, Settings, Query)."); // <-- Mettre à jour log

        console.log("Souscription aux notifications...");
        // Souscrire à toutes les réponses nécessaires
        await commandResponseCharacteristic.startNotifications();
        commandResponseCharacteristic.addEventListener('characteristicvaluechanged', handleNotifications);
        
        await settingsResponseCharacteristic.startNotifications(); // <-- Souscrire ici
        settingsResponseCharacteristic.addEventListener('characteristicvaluechanged', handleNotifications); // Utiliser le même handler

        await queryResponseCharacteristic.startNotifications();
        queryResponseCharacteristic.addEventListener('characteristicvaluechanged', handleNotifications);

        console.log("Souscrit aux notifications (Command Rsp, Settings Rsp, Query Rsp)."); // <-- Mettre à jour log
        // console.log("[Connect] Fin de la phase de connexion/souscription. Démarrage Readiness Check..."); // <-- NOUVEAU LOG

        updateConnectionStatus('Connecté, vérification...', 'dot-yellow');
        isDisconnecting = false; // <-- Assurer que le drapeau est false après une connexion réussie
        connectButton.textContent = 'Déconnecter';
        connectButton.disabled = false; // Peut se déconnecter

        // Démarrer la vérification de disponibilité
        startReadinessCheck();

    } catch (error) {
         isDisconnecting = false; // <-- S'assurer qu'il est false même en cas d'erreur de connexion
         displayError(`Échec de la connexion: ${error.message}`);
         resetConnectionState();
    }
}

// Formate les tailles de stockage (KB -> MB/GB)
function formatSize(kilobytes) {
    if (kilobytes === null || isNaN(kilobytes)) return 'N/A';
    const megabytes = kilobytes / 1024;
    const gigabytes = megabytes / 1024;
    if (gigabytes >= 1) {
        return gigabytes.toFixed(0) + ' Go';
        // return Math.round(gigabytes) + ' Go'; // Arrondi à l'entier
    } else if (megabytes >= 1) {
        return megabytes.toFixed(0) + ' Mo';
    } else {
        return kilobytes.toFixed(0) + ' Ko';
        // return Math.round(kilobytes) + ' Ko'; // Arrondir les Ko aussi
    }
    
}

// Formate la durée (secondes -> MM:SS ou HH:MM:SS)
function formatDuration(seconds) {
    if (seconds === null || isNaN(seconds)) return 'N/A';
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    if (hours > 0) {
        return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    } else {
        return `${minutes}:${secs.toString().padStart(2, '0')}`;
    }
}

// Décode une valeur de statut basée sur son ID
function decodeStatusValue(statusId, valueBytes) {
    if (!valueBytes || valueBytes.length === 0) return null;
    const dataView = new DataView(valueBytes.buffer, valueBytes.byteOffset, valueBytes.length);

    try {
        switch (statusId) {
            case STATUS_ID_BATTERY_PERCENT: // uint8
            case STATUS_ID_BATTERY_BARS:    // uint8
            case STATUS_ID_ENCODING:       // uint8 (bool)
            case STATUS_ID_COLD:           // uint8 (bool)
            case STATUS_ID_OVERHEATING:    // uint8 (bool)
            case STATUS_ID_BUSY:           // uint8 (bool)
                return dataView.getUint8(0);

            case STATUS_ID_VIDEO_REMAINING:   // uint32
            case STATUS_ID_PRESET_ACTIVE:     // uint32
                if (valueBytes.length < 4) {
                    console.warn(`Données insuffisantes pour uint32 (ID: ${statusId}, Len: ${valueBytes.length})`);
                    return null;
                }
                return dataView.getUint32(0, false); // Big Endian

            case STATUS_ID_SD_REMAINING:      // uint64
            case STATUS_ID_SD_CAPACITY:       // uint64  
                 if (valueBytes.length < 8) {
                     console.warn(`Données insuffisantes pour uint64 (ID: ${statusId}, Len: ${valueBytes.length})`);
                     return null;
                 }
                try {
                    return dataView.getBigUint64(0, false); // Big Endian -> retourne un BigInt
                } catch (bigIntError) {
                     console.error(`Erreur getBigUint64 pour Status ID ${statusId}:`, bigIntError);
                     return null; // Retourner null si BigInt n'est pas supporté
                }

            default:
                if (VERBOSE_LOGGING)  console.warn(`Décodage non implémenté pour Status ID: ${statusId}`);
                return null;
        }
    } catch (e) {
        console.error(`Erreur de décodage pour Status ID ${statusId}:`, e, valueBytes);
        return null;
    }
}

// Décode les valeurs de settings
// Pour l'instant, on suppose que c'est similaire aux statuts pour uint8/uint32
function decodeSettingValue(settingId, valueBytes) {
    if (!valueBytes || valueBytes.length === 0) return null;
    const dataView = new DataView(valueBytes.buffer, valueBytes.byteOffset, valueBytes.length);
    try {
        // Exemple avec switch (plus robuste)
        switch (settingId) {
            case SETTING_ID_VIDEO_RESOLUTION:
            case SETTING_ID_FPS:
            case SETTING_ID_VIDEO_LENS:
            case SETTING_ID_GPS:
            case SETTING_ID_HYPERSMOOTH:
            // --- AJOUT ---
            case SETTING_ID_VIDEO_TIMELAPSE_RATE: // ID 5
            case SETTING_ID_TIMEWARP_SPEED:       // ID 111
            // --- FIN AJOUT ---
                 if (valueBytes.length === 1) return dataView.getUint8(0);
                 console.warn(`Setting ${settingId} attendu uint8 mais reçu ${valueBytes.length} bytes.`);
                 return null;
            // case AUTRE_SETTING_ID_UINT32:
            //      if (valueBytes.length === 4) return dataView.getUint32(0, false); // Big Endian
            //      console.warn(...); return null;
            default:
                console.warn(`Décodage non implémenté pour Setting Value ID: ${settingId}, Length: ${valueBytes.length}`);
                 // Peut-être retourner les bytes bruts pour inspection ?
                 // return valueBytes;
                return null;
        }
    } catch (e) {
        console.error(`Erreur de décodage pour Setting Value ID ${settingId}:`, e, valueBytes);
        return null;
    }
}

// --- Fonction pour charger un preset (à créer/compléter) ---
async function loadPreset(presetId) {
    if (!isCameraReady || !commandCharacteristic) {
        displayError("Caméra non prête ou caractéristique Commande manquante pour Load Preset.");
        return;
    }

    if (isCameraBusy) {
        console.warn(`Action Load Preset (ID: ${presetId}) bloquée: Caméra occupée.`);
        const feedbackElement = document.getElementById('presets-feedback');
        if (feedbackElement) {
            feedbackElement.textContent = "Caméra occupée...";
            setTimeout(() => { if(feedbackElement) feedbackElement.textContent = "";}, 2000);
        }
        return; // Ne pas envoyer la commande
    }

    console.log(`Demande de chargement du Preset ID: ${presetId}...`);

    // Ajouter feedback UI si nécessaire
    const feedbackElement = document.getElementById('presets-feedback'); // Assumer existence
    if(feedbackElement) feedbackElement.textContent = `Chargement preset ${presetId}...`;


    // Commande TLV: Load Preset (ID: 0x40)
    const commandId = 0x40; // Ou utiliser une constante CMD_LOAD_PRESET = 0x40;
    const valueLength = 4; // presetId est uint32

    // Créer le payload TLV: ID(1), Len(1), Val(4)
    const tlvPayload = new Uint8Array(1 + 1 + valueLength);
    const dataView = new DataView(tlvPayload.buffer);
    dataView.setUint8(0, commandId);   // ID Commande
    dataView.setUint8(1, valueLength); // Longueur Paramètre
    dataView.setUint32(2, presetId, false); // Valeur Paramètre (Preset ID) en Big Endian

    // Utiliser le helper
    const commandBuffers = buildBlePackets(tlvPayload);

    // Envoyer sur la caractéristique Commande
    if (commandBuffers.length > 0) {
        try {
            await sendCommand(commandCharacteristic, commandBuffers);
            // console.log(`Commande Load Preset ${presetId} envoyée.`);
            // Le feedback de succès/échec viendra de la réponse dans handleNotifications
        } catch (error) {
            console.error(`Erreur envoi Load Preset ${presetId}:`, error);
            if(feedbackElement) feedbackElement.textContent = `Erreur envoi Load Preset.`;
            setTimeout(() => { if(feedbackElement) feedbackElement.textContent = "";}, 3000);
        }
    }
}

async function setShutter(start) { // true pour démarrer, false pour arrêter
    if (!isCameraReady || !commandCharacteristic) {
        displayError("La caméra n'est pas prête ou la caractéristique de commande est manquante.");
        return;
    }
    if (start && isCameraBusy) {
        console.warn(`Action Shutter (${start}) bloquée: Caméra occupée.`);
        shutterFeedbackDiv.textContent = "Caméra occupée...";
        setTimeout(() => { if(shutterFeedbackDiv) shutterFeedbackDiv.textContent = "" }, 2000);
         // Réactiver les boutons si le démarrage est bloqué
         updateRecordButtonStates(); // Remettre les boutons dans un état cohérent
        return; // Ne pas envoyer la commande
    }

    shutterFeedbackDiv.textContent = start ? "Demande de démarrage..." : "Demande d'arrêt...";
    startRecordButton.disabled = true; // Désactiver les deux pendant l'envoi
    stopRecordButton.disabled = true;

    const commandId = CMD_SET_SHUTTER;
    const paramValue = start ? 1 : 0; // 1 pour ON, 0 pour OFF
    const paramLength = 1;
    const tlvPayload = new Uint8Array([commandId, paramLength, paramValue]);

    // Utiliser le helper pour construire les paquets
    const commandBuffers = buildBlePackets(tlvPayload);

    if (commandBuffers.length > 0) {
        console.log(`Envoi Set Shutter (0x01) - Mode: ${paramValue}`);
        await sendCommand(commandCharacteristic, commandBuffers); // Envoyer le tableau de buffers
    }
}

async function addHilight() {
    // console.log("Listener Hilight déclenché");
    // Vérifier si prêt ET en enregistrement (Status 10 = 1)
    // Utiliser la variable globale au lieu de lire l'UI
    if (!isCameraReady || currentEncodingStatus !== 1 || !commandCharacteristic) {
        const reason = !isCameraReady ? "Caméra non prête" : (currentEncodingStatus !== 1 ? "Pas en enregistrement" : "Caractéristique manquante");
        console.error(`Hilight impossible: ${reason}`);
        hilightFeedbackDiv.textContent = "Hilight possible uniquement pendant l'enregistrement.";
        setTimeout(() => { hilightFeedbackDiv.textContent = "" }, 3000);
        // S'assurer que le bouton est bien désactivé si on n'est pas en train d'encoder
        if (hilightButton) hilightButton.disabled = (currentEncodingStatus !== 1);
        return;
    }

    /* if (isCameraBusy) {
        console.warn(`Action Hilight bloquée: Caméra occupée.`);
        hilightFeedbackDiv.textContent = "Caméra occupée...";
         setTimeout(() => { hilightFeedbackDiv.textContent = "" }, 2000);
        return; // Ne pas envoyer la commande
    } */

    // hilightFeedbackDiv.textContent = "Ajout Hilight...";
    if (hilightFeedbackDiv) hilightFeedbackDiv.textContent = "Ajout Hilight..."; // Remettre le feedback si souhaité

    const commandId = CMD_HILIGHT; // 0x18

    // Payload TLV simple : juste l'ID -> 1 byte
    const tlvPayload = new Uint8Array([commandId]);

    // Utiliser le helper
    const commandBuffers = buildBlePackets(tlvPayload);
    
    if (commandBuffers.length > 0) {
        console.log("Envoi Hilight (0x18)...");
        try {
            await sendCommand(commandCharacteristic, commandBuffers);
            // La réponse est gérée dans handleNotifications, mais on ne réactive rien ici.
            // L'état du bouton reste géré par updateRecordButtonStates via le statut d'encodage.
        } catch (error) {
             console.error("Erreur envoi Hilight:", error);
            if (hilightFeedbackDiv) hilightFeedbackDiv.textContent = "Erreur envoi Hilight";
            // Ne pas toucher à l'état disabled ici non plus.
        }
    }
}

// --- Mise à jour resetConnectionState ---
// Réinitialise l'état de connexion et l'UI
function resetConnectionState() {
    console.log("Réinitialisation de l'état de connexion et de l'UI...");

    // 1. Réinitialiser les états internes et BLE
    isCameraReady = false;
    isCameraBusy = false;
    goproDevice = null;
    goproServer = null;
    goproService = null;
    settingCapabilities = {};
    commandCharacteristic = null;
    commandResponseCharacteristic = null;
    settingsCharacteristic = null;
    settingsResponseCharacteristic = null;
    queryCharacteristic = null;
    queryResponseCharacteristic = null;
    currentEncodingStatus = 0; // État d'encodage par défaut

    // 2. Arrêter tous les timers et intervalles
    if (readinessCheckInterval) clearInterval(readinessCheckInterval);
    if (keepAliveInterval) clearInterval(keepAliveInterval);
    // if (statusPollingInterval) clearInterval(statusPollingInterval);
    if (stopTimerTimeoutId) clearTimeout(stopTimerTimeoutId); // Timer d'arrêt d'enregistrement
    if (countdownIntervalId) clearInterval(countdownIntervalId); // Intervalle du compte à rebours visuel

    readinessCheckInterval = null;
    keepAliveInterval = null;
    currentSettings = {}; // Vider les settings actuels
    initialSettingsRequested = false; // Réinitialiser le drapeau
    initialStatusRequested = false; // Réinitialiser le drapeau
    isDisconnecting = false;
    stopTimerTimeoutId = null;
    countdownIntervalId = null;
    recordingStartTime = null;
    isTimerEnabled = false; // Désactiver le mode timer
    presetInfoCache = {}; // Vider le cache des presets
    availablePresets.groups = []; // Vider les groupes de presets

    // Références aux éléments UI pour masquer/afficher
    const controlsSection = document.getElementById('controls-section');
    const statusSection = document.getElementById('status-section');
    const presetsSection = document.getElementById('presets-section'); // Récupérer ici aussi


    // 3. Réinitialiser l'état de l'UI principale (Connexion, Info, Erreurs, Visibilité)
    // Utiliser 'if (element)' pour la robustesse
    if (connectionStatusDiv) updateConnectionStatus('Déconnecté', 'dot-red');
    if (connectButton) {
        connectButton.textContent = 'Connecter';
        connectButton.disabled = false; // Permettre de reconnecter
    }
    if (deviceInfoDiv) deviceInfoDiv.textContent = '';
    if (errorMessageDiv) errorMessageDiv.textContent = '';
    if (controlsSection) controlsSection.classList.add('hidden');
    if (statusSection) statusSection.classList.add('hidden');
    if (presetsSection) presetsSection.classList.add('hidden');

    // 4. Réinitialiser l'état de l'UI des Contrôles
    if(startRecordButton) {
        startRecordButton.disabled = true; // Désactivé tant que non connecté/prêt
        startRecordButton.textContent = "Démarrer Enreg.";
    }
     if(stopRecordButton) {
        stopRecordButton.disabled = true;
        stopRecordButton.style.display = 'inline-flex'; // Afficher par défaut
    }
    if(hilightButton) hilightButton.disabled = true;

    // Réinitialiser l'UI spécifique au Timer
    if (timerToggleButton) timerToggleButton.setAttribute('aria-checked', 'false'); // État ARIA initial
    // Mettre à jour l'apparence visuelle du toggle via la fonction
    updateTimerToggleVisualState();
    if(timedDurationInput) timedDurationInput.value = "15"; // Valeur par défaut (input sera désactivé par updateTimerToggleVisualState)


    // Réinitialiser les zones de feedback
    if(shutterFeedbackDiv) shutterFeedbackDiv.textContent = "";
    if(hilightFeedbackDiv) hilightFeedbackDiv.textContent = "";
    if(presetsFeedback) presetsFeedback.textContent = ""; // <-- AJOUTÉ
    // timedFeedbackDiv n'existe plus

    // 5. Réinitialiser l'état de l'UI des Statuts
    if (statusBatteryDiv) statusBatteryDiv.textContent = 'N/A';
    if (statusEncodingDiv) statusEncodingDiv.innerHTML = 'N/A'; // Utiliser innerHTML si contient du HTML
    if (statusSdRemainingDiv) statusSdRemainingDiv.textContent = 'N/A';
    if (statusSdCapacityDiv) statusSdCapacityDiv.textContent = 'N/A';
    if (statusVideoRemainingDiv) statusVideoRemainingDiv.textContent = 'N/A';
    if (statusTempDiv) statusTempDiv.innerHTML = 'N/A'; // Utiliser innerHTML
    if (statusBusyDiv) statusBusyDiv.innerHTML = 'N/A'; // Utiliser innerHTML
    if (statusPresetDiv) statusPresetDiv.textContent = 'N/A';

    // 6. Réinitialiser l'état de l'UI des Settings
    if (settingResolutionSelect) { 
        settingResolutionSelect.innerHTML = '<option value="">...</option>'; 
        settingResolutionSelect.disabled = true;
    }
    if (settingFpsSelect) { 
        settingFpsSelect.innerHTML = '<option value="">...</option>'; 
        settingFpsSelect.disabled = true;
    }
    if (settingLensSelect) {
        settingLensSelect.innerHTML = '<option value="">...</option>';
        settingLensSelect.disabled = true;
    }
    if (settingGpsSelect) {
        settingGpsSelect.innerHTML = '<option value="">...</option>';
        settingGpsSelect.disabled = true;
    }
    if (settingHypersmoothSelect) {
        settingHypersmoothSelect.innerHTML = '<option value="">...</option>';
        settingHypersmoothSelect.disabled = true;
    }
    if (settingTimelapseRateSelect) {
        settingTimelapseRateSelect.innerHTML = '<option value="">...</option>';
        settingTimelapseRateSelect.disabled = true;
    }
    if (settingTimewarpSpeedSelect) {
        settingTimewarpSpeedSelect.innerHTML = '<option value="">...</option>';
        settingTimewarpSpeedSelect.disabled = true;
    }
    // Réinitialiser les feedbacks des settings
    if(settingResolutionFeedback) settingResolutionFeedback.textContent = "";
    if(settingFpsFeedback) settingFpsFeedback.textContent = "";
    if(settingLensFeedback) settingLensFeedback.textContent = "";
    if(settingGpsFeedback) settingGpsFeedback.textContent = "";
    if(settingHypersmoothFeedback) settingHypersmoothFeedback.textContent = "";
    if(settingTimelapseRateFeedback) settingTimelapseRateFeedback.textContent = "";
    if(settingTimewarpSpeedFeedback) settingTimewarpSpeedFeedback.textContent = "";
    

    // Masquer TOUS les conteneurs de settings au reset
    if (settingItemResolutionDiv) settingItemResolutionDiv.classList.add('hidden');
    if (settingItemFpsDiv) settingItemFpsDiv.classList.add('hidden');
    if (settingItemLensDiv) settingItemLensDiv.classList.add('hidden');
    if (settingItemGpsDiv) settingItemGpsDiv.classList.add('hidden');
    if (settingItemHypersmoothDiv) settingItemHypersmoothDiv.classList.add('hidden');
    // --- AJOUT ---
    if (settingItemTimelapseRateDiv) settingItemTimelapseRateDiv.classList.add('hidden');
    if (settingItemTimewarpSpeedDiv) settingItemTimewarpSpeedDiv.classList.add('hidden');
    // --- FIN AJOUT ---

    // Réinitialiser l'UI des presets
    if (presetsContainer) presetsContainer.innerHTML = '<p class="text-gray-500 italic">Non connecté.</p>';

    // 7. Réinitialiser l'état du bouton/grille de statut toggle
    if (statusGrid) statusGrid.classList.remove('hidden'); // Rendre visible par défaut
    if (toggleStatusButton) toggleStatusButton.textContent = 'Masquer';
    
    // Mettre à jour l'état des boutons record/stop/hilight (prendra en compte isCameraBusy=false et encoding=0)
    updateRecordButtonStates();
    // Mettre à jour l'état des autres contrôles (selects, presets) basé sur l'état non-busy
    updateControlsBasedOnBusyState(false); // Appeler avec busy = false
        
    isDisconnecting = false; // <-- Réinitialiser le drapeau à la fin
}

function startKeepAlive() {
    // 1. Nettoyer tout ancien intervalle au cas où
    if (keepAliveInterval) {
        clearInterval(keepAliveInterval);
        keepAliveInterval = null; // Bonne pratique de remettre à null
    }

    // 2. Définir la fonction qui sera appelée périodiquement
    const sendKeepAlive = async () => {
         // Vérifier le drapeau de déconnexion et l'existence de l'intervalle/connexion
        if (isDisconnecting || !keepAliveInterval || !goproDevice || !goproDevice.gatt.connected) {
            // console.log("Keep Alive annulé (déconnexion en cours, intervalle arrêté ou déconnecté).");
            // Arrêter l'intervalle ici aussi par sécurité s'il n'est pas null
            if (keepAliveInterval) {
                clearInterval(keepAliveInterval);
                keepAliveInterval = null;
            }
            return; // Ne rien faire
        }
        
        // Utiliser la variable globale settingsCharacteristic récupérée lors de la connexion
        if (isCameraReady && settingsCharacteristic) {
             // Préparer la commande TLV pour Keep Alive (ID: 0x5B, ParamLen: 1, ParamVal: 0x42)
             
             const commandId = CMD_KEEP_ALIVE; // 0x5B
             const paramValue = 0x42;
             const paramLength = 1; // uint8

             // Créer le payload TLV (sans l'en-tête BLE pour l'instant)
             const tlvPayload = new Uint8Array([commandId, paramLength, paramValue]);

             // Étape 3: Ajouter l'en-tête BLE (General 5-bit car payload court)
             const commandBuffers = buildBlePackets(tlvPayload);

             if (commandBuffers.length > 0) {
                try {
                    // console.log("Envoi Keep Alive (0x5B) sur Settings Char...");
                    await sendCommand(settingsCharacteristic, commandBuffers);
                } catch (error) {
                    // Gérer l'erreur si sendCommand échoue même après les vérifications
                    // (Peut arriver si la déconnexion survient entre la vérif et l'envoi)
                    console.warn(`Erreur pendant l'envoi Keep Alive (peut être dû à une déconnexion rapide): ${error.message}`);
                    if (keepAliveInterval) { // Arrêter l'intervalle si une erreur se produit
                        clearInterval(keepAliveInterval);
                        keepAliveInterval = null;
                    }
                }
             }
        } else if (!settingsCharacteristic) {
                console.error("Keep Alive: Caractéristique Settings non disponible.");
                if (keepAliveInterval) {
                    clearInterval(keepAliveInterval);
                    keepAliveInterval = null;
                }
        }
    };

    // 3. DÉMARRER l'intervalle ici, dans startKeepAlive
    keepAliveInterval = setInterval(sendKeepAlive, 3000);
    console.log("Keep Alive démarré (intervalle 3s).");
}

// --- Logique Principale ---

// --- Initialisation ---
document.addEventListener('DOMContentLoaded', async () => { // Rendre l'event listener async

// --- Éléments UI ---
connectButton = document.getElementById('connect-button');
connectionStatusDiv = document.getElementById('connection-status');
deviceInfoDiv = document.getElementById('device-info');
errorMessageDiv = document.getElementById('error-message');
controlsSection = document.getElementById('controls-section'); // Pour plus tard
statusSection = document.getElementById('status-section');
statusBatteryDiv = document.getElementById('status-battery');
statusEncodingDiv = document.getElementById('status-encoding');
statusSdRemainingDiv = document.getElementById('status-sd-remaining');
statusSdCapacityDiv = document.getElementById('status-sd-capacity');
statusVideoRemainingDiv = document.getElementById('status-video-remaining');
statusTempDiv = document.getElementById('status-temp');
statusBusyDiv = document.getElementById('status-busy');
statusPresetDiv = document.getElementById('status-preset');
startRecordButton = document.getElementById('start-record-button');
stopRecordButton = document.getElementById('stop-record-button');
shutterFeedbackDiv = document.getElementById('shutter-feedback');
hilightButton = document.getElementById('hilight-button');
hilightFeedbackDiv = document.getElementById('hilight-feedback');
timerToggleButton = document.getElementById('timer-toggle-button');
timerToggleHandle = document.getElementById('timer-toggle-handle');
timedDurationInput = document.getElementById('timed-duration');
toggleStatusButton = document.getElementById('toggle-status-button');
statusGrid = document.getElementById('status-grid');
presetsContainer = document.getElementById('presets-container'); // <-- AJOUTÉ
presetsSection = document.getElementById('presets-section');   // <-- AJOUTÉ
presetsFeedback = document.getElementById('presets-feedback');  // <-- AJOUTÉ

// --- Fonctions UI ---
settingResolutionSelect = document.getElementById('setting-2');
settingResolutionFeedback = document.getElementById('setting-2-feedback');
settingFpsSelect = document.getElementById('setting-3'); // <-- Nouveau
settingFpsFeedback = document.getElementById('setting-3-feedback'); // <-- Nouveau
settingTimelapseRateSelect = document.getElementById('setting-5');
settingTimelapseRateFeedback = document.getElementById('setting-5-feedback');
settingGpsSelect = document.getElementById('setting-83');
settingGpsFeedback = document.getElementById('setting-83-feedback');
settingTimewarpSpeedSelect = document.getElementById('setting-111');
settingTimewarpSpeedFeedback = document.getElementById('setting-111-feedback');
settingLensSelect = document.getElementById('setting-121'); 
settingLensFeedback = document.getElementById('setting-121-feedback');
settingHypersmoothSelect = document.getElementById('setting-135');
settingHypersmoothFeedback = document.getElementById('setting-135-feedback');

// Références aux conteneurs pour masquer/afficher
settingItemResolutionDiv = document.getElementById('setting-item-2'); // Assurez-vous que l'ID existe dans votre HTML
settingItemFpsDiv = document.getElementById('setting-item-3');       // Assurez-vous que l'ID existe
settingItemTimelapseRateDiv = document.getElementById('setting-item-5'); 
settingItemGpsDiv = document.getElementById('setting-item-83');
settingItemTimewarpSpeedDiv = document.getElementById('setting-item-111');
settingItemLensDiv = document.getElementById('setting-item-121'); 
settingItemHypersmoothDiv = document.getElementById('setting-item-135');


const protobufReady = await initializeProtobuf(); // Attendre l'initialisation

if (!protobufReady) {
    // Gérer le cas où protobuf n'a pas pu s'initialiser
    // (par exemple, désactiver les fonctionnalités liées aux presets)
    console.warn("Protobuf non initialisé. Fonctionnalités dépendantes désactivées.");
}

// Listener connexion
if (connectButton) {
    connectButton.addEventListener('click', connectGopro);
} else { console.error("Bouton Connecter non trouvé !"); }

// listener du bouton toggle
if (timerToggleButton) {
    timerToggleButton.addEventListener('click', () => {
        const currentState = timerToggleButton.getAttribute('aria-checked') === 'true';
        const newState = !currentState;
        timerToggleButton.setAttribute('aria-checked', newState.toString()); // Mettre à jour l'attribut ARIA
        isTimerEnabled = newState; // Mettre à jour la variable globale
        updateTimerToggleVisualState(); // Mettre à jour l'apparence et l'état des boutons
    });
}

// Listeners pour les boutons Start/Stop/Hilight
if (startRecordButton) startRecordButton.addEventListener('click', () => setShutter(true));
if (stopRecordButton) stopRecordButton.addEventListener('click', () => setShutter(false)); // Arrêt manuel (mode timer désactivé)
if (hilightButton) hilightButton.addEventListener('click', addHilight);


// --- Initialisation (Ajouter les listeners pour les boutons) ---
// Attacher les autres listeners
if (toggleStatusButton && statusGrid) {
    toggleStatusButton.addEventListener('click', () => {
        statusGrid.classList.toggle('hidden'); // Basculer la classe 'hidden'
        // Changer le texte du bouton
        toggleStatusButton.textContent = statusGrid.classList.contains('hidden') ? 'Afficher' : 'Masquer';
    });
}

// Listeners pour les paramètres résolution, ...
if (settingResolutionSelect) {
    settingResolutionSelect.addEventListener('change', (event) => {
        const selectedValue = event.target.value;
        if (selectedValue) { // Vérifier qu'une option est bien sélectionnée
            setSetting(SETTING_ID_VIDEO_RESOLUTION, selectedValue);
        }
    });
}
if (settingFpsSelect) {
    settingFpsSelect.addEventListener('change', (event) => {
        const selectedValue = event.target.value;
        if (selectedValue) {
            setSetting(SETTING_ID_FPS, selectedValue);
        }
    });
}
if (settingLensSelect) {
    settingLensSelect.addEventListener('change', (event) => {
        const selectedValue = event.target.value;
        if (selectedValue) {
            setSetting(SETTING_ID_VIDEO_LENS, selectedValue);
        }
    });
}
if (settingGpsSelect) {
    settingGpsSelect.addEventListener('change', (event) => {
        if (event.target.value !== "") setSetting(SETTING_ID_GPS, event.target.value);
    });
}
 if (settingHypersmoothSelect) {
    settingHypersmoothSelect.addEventListener('change', (event) => {
         if (event.target.value !== "") setSetting(SETTING_ID_HYPERSMOOTH, event.target.value);
    });
}
if (settingTimelapseRateSelect) {
    settingTimelapseRateSelect.addEventListener('change', (event) => {
         if (event.target.value !== "") setSetting(SETTING_ID_VIDEO_TIMELAPSE_RATE, event.target.value);
    });
}
if (settingTimewarpSpeedSelect) {
    settingTimewarpSpeedSelect.addEventListener('change', (event) => {
         if (event.target.value !== "") setSetting(SETTING_ID_TIMEWARP_SPEED, event.target.value);
    });
}
// --- FIN AJOUT Listeners ---


// Vérifier si Web Bluetooth est supporté
if (!navigator.bluetooth) {
    displayError("Web Bluetooth n'est pas supporté par ce navigateur.");
    if (connectButton) connectButton.disabled = true;
}

// Appeler resetConnectionState une fois au début pour mettre l'UI dans l'état initial désactivé/N/A
resetConnectionState();
// updateTimerUIState(); // Mettre l'UI timer dans l'état initial (désactivé)


}); // Fin de DOMContentLoaded


// Dark Mode Toggle Script
// Placé ici pour s'assurer que le DOM est chargé car gopro_ble.js est à la fin du body.
// Si gopro_ble.js était dans le <head>, il faudrait wrapper ceci dans DOMContentLoaded.
const darkModeToggle = document.getElementById('dark-mode-toggle');
const htmlElement = document.documentElement;

// Function to apply theme and update button text
function applyTheme(theme) {
    if (theme === 'dark') {
        htmlElement.classList.add('dark');
        if (darkModeToggle) darkModeToggle.textContent = 'Mode Clair'; 
    } else {
        htmlElement.classList.remove('dark');
        if (darkModeToggle) darkModeToggle.textContent = 'Mode Sombre';
    }
}

// Check localStorage and apply initial theme
const storedTheme = localStorage.getItem('theme');
if (storedTheme) {
    applyTheme(storedTheme);
} else {
    // Default to light theme if nothing is stored or system preference isn't checked
    applyTheme('light'); 
}

// Event listener for the toggle button
if (darkModeToggle) {
    darkModeToggle.addEventListener('click', () => {
        if (htmlElement.classList.contains('dark')) {
            applyTheme('light');
            localStorage.setItem('theme', 'light');
        } else {
            applyTheme('dark');
            localStorage.setItem('theme', 'dark');
        }
    });
}
