"""
compare_emulator.py — Compare les données d'une vraie caméra Insta360 avec l'émulateur.

Usage :
    python compare_emulator.py x3_capture.log [--profile insta360_x3] [--emulator-url http://localhost:8080]

Étapes :
    1. Connecter la caméra via airbuble et récupérer les logs :
           adb logcat -s Insta360Debug > x3_capture.log
    2. Lancer ce script (l'émulateur peut être éteint — comparaison offline possible)
    3. Lire compare_report.txt pour savoir quoi corriger dans le profil YAML

Sortie :
    compare_report.txt  dans le même dossier que ce script
"""

import argparse
import json
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

# Emulateur HTTP optionnel — comparaison offline si absent
try:
    import requests
    _REQUESTS_OK = True
except ImportError:
    _REQUESTS_OK = False


# ────────────────────────────────────────────────────────────────────────────
#  Structures de données
# ────────────────────────────────────────────────────────────────────────────

@dataclass
class RealCameraData:
    """Données parsées depuis les logs logcat Insta360Debug."""
    camera_type: str = ""
    firmware: str = ""
    serial: str = ""
    mode_count: int = 0
    modes: list = field(default_factory=list)          # [{name, isVideo, isPhoto, isLive}]
    settings: dict = field(default_factory=dict)        # {mode_name: {key: [values]}}
    battery_level: int = -1
    is_charging: bool = False
    storage_free: int = -1
    storage_total: int = -1
    temperature: str = ""


@dataclass
class EmulatorData:
    """Données récupérées depuis le serveur OSC de l'émulateur."""
    model: str = ""
    firmware: str = ""
    serial: str = ""
    modes: list = field(default_factory=list)
    battery_level: int = -1
    is_charging: bool = False
    storage_free: int = -1
    storage_total: int = -1
    new_capture_flow: bool = False
    available: bool = False


# ────────────────────────────────────────────────────────────────────────────
#  Parser logcat
# ────────────────────────────────────────────────────────────────────────────

def parse_logcat(log_path: Path) -> RealCameraData:
    """Parse x3_capture.log et retourne RealCameraData."""
    data = RealCameraData()

    # Lignes logcat : "... I Insta360Debug: [TAG] key=val ..."
    # Ou format adb logcat compact : "I/Insta360Debug( 1234): [TAG] ..."
    line_re = re.compile(r'Insta360Debug[^\[]*(\[(?:INFO|CONFIG|MODE|SETTING|BATTERY|STORAGE|TEMP)\].*)')

    current_mode_settings: Optional[dict] = None

    with open(log_path, encoding="utf-8", errors="replace") as f:
        for line in f:
            m = line_re.search(line)
            if not m:
                continue
            content = m.group(1).strip()
            _parse_line(content, data)

    return data


def _parse_line(content: str, data: RealCameraData):
    """Applique une ligne [TAG] ... à RealCameraData."""

    def kv(text: str, key: str) -> str:
        """Extrait key=value depuis une ligne."""
        m = re.search(rf'\b{key}=(\S+)', text)
        return m.group(1) if m else ""

    def kv_list(text: str, key: str) -> list:
        """Extrait key=[val1, val2, ...] depuis une ligne."""
        m = re.search(rf'\b{key}=\[([^\]]*)\]', text)
        if not m:
            return []
        raw = m.group(1)
        return [v.strip() for v in raw.split(",") if v.strip()]

    if content.startswith("[INFO]"):
        data.camera_type = kv(content, "cameraType")
        data.firmware    = kv(content, "firmware")
        data.serial      = kv(content, "serial")

    elif content.startswith("[CONFIG]"):
        cnt = kv(content, "modeCount")
        data.mode_count = int(cnt) if cnt.isdigit() else 0

    elif content.startswith("[MODE]"):
        name = kv(content, "name")
        if name:
            data.modes.append({
                "name":    name,
                "isVideo": kv(content, "isVideo") == "true",
                "isPhoto": kv(content, "isPhoto") == "true",
                "isLive":  kv(content, "isLive")  == "true",
            })
            if name not in data.settings:
                data.settings[name] = {}

    elif content.startswith("[SETTING]"):
        mode_name = kv(content, "mode")
        key       = kv(content, "key")
        values    = kv_list(content, "values")
        cnt_str   = kv(content, "count")
        cnt       = int(cnt_str) if cnt_str.isdigit() else len(values)
        if mode_name and key:
            if mode_name not in data.settings:
                data.settings[mode_name] = {}
            data.settings[mode_name][key] = values

    elif content.startswith("[BATTERY]"):
        lvl = kv(content, "level")
        data.battery_level = int(lvl) if lvl.lstrip('-').isdigit() else -1
        data.is_charging = kv(content, "charging") == "true"

    elif content.startswith("[STORAGE]"):
        free_str  = kv(content, "free")
        total_str = kv(content, "total")
        data.storage_free  = int(free_str)  if free_str.lstrip('-').isdigit()  else -1
        data.storage_total = int(total_str) if total_str.lstrip('-').isdigit() else -1

    elif content.startswith("[TEMP]"):
        data.temperature = kv(content, "level")


# ────────────────────────────────────────────────────────────────────────────
#  Requêtes émulateur
# ────────────────────────────────────────────────────────────────────────────

def query_emulator(base_url: str) -> EmulatorData:
    """Interroge le serveur OSC de l'émulateur et retourne EmulatorData."""
    emu = EmulatorData()
    if not _REQUESTS_OK:
        print("[WARN] Module 'requests' absent — comparaison offline uniquement.")
        return emu

    try:
        # /osc/info
        r = requests.get(f"{base_url}/osc/info", timeout=3)
        info = r.json()
        emu.model    = info.get("model", "")
        emu.firmware = info.get("firmwareVersion", "")
        emu.serial   = info.get("serialNumber", "")

        # /osc/state
        r = requests.get(f"{base_url}/osc/state", timeout=3)
        state = r.json().get("state", {})
        bat = state.get("_batteryLevel", -1)
        emu.battery_level = int(bat * 100) if isinstance(bat, float) and bat <= 1.0 else int(bat)
        emu.is_charging   = state.get("_batteryState", "") == "charging"
        emu.storage_free  = state.get("_remainingSpace", -1)
        emu.storage_total = state.get("_totalSpace", -1)

        # camera._insta360FetchSupportConfig → modes
        r = requests.post(f"{base_url}/osc/commands/execute", timeout=3,
                          json={"name": "camera._insta360FetchSupportConfig", "parameters": {}})
        cfg = r.json().get("results", {})
        emu.new_capture_flow = cfg.get("supportNewCaptureControlFlow", False)
        emu.modes = cfg.get("supportCaptureModes", [])
        emu.available = True

    except Exception as e:
        print(f"[WARN] Émulateur inaccessible ({e}) — comparaison offline uniquement.")

    return emu


# ────────────────────────────────────────────────────────────────────────────
#  Comparaison + rapport
# ────────────────────────────────────────────────────────────────────────────

def compare(real: RealCameraData, emu: EmulatorData) -> list[str]:
    """Retourne la liste des lignes du rapport de comparaison."""
    lines = []

    def section(title: str):
        lines.append("")
        lines.append("=" * 60)
        lines.append(f"  {title}")
        lines.append("=" * 60)

    def ok(msg: str):   lines.append(f"  ✓  {msg}")
    def warn(msg: str): lines.append(f"  ⚠  {msg}")
    def diff(msg: str): lines.append(f"  ✗  {msg}")
    def info(msg: str): lines.append(f"     {msg}")

    # ── Infos caméra ─────────────────────────────────────────────────────
    section("INFOS CAMÉRA")

    if real.camera_type:
        ok(f"cameraType  = {real.camera_type!r}")
    else:
        warn("cameraType  absent du log (connexion incomplète ?)")

    if real.firmware:
        ok(f"firmware    = {real.firmware!r}")
        if emu.available and emu.firmware and emu.firmware != real.firmware:
            diff(f"  émulateur firmware = {emu.firmware!r} → corriger dans YAML")
    else:
        warn("firmware absent du log")

    if real.serial:
        ok(f"serial      = {real.serial!r}")
    else:
        warn("serial absent du log")

    # ── Modes de capture ──────────────────────────────────────────────────
    section("MODES DE CAPTURE")
    real_names = [m["name"] for m in real.modes]
    info(f"Caméra réelle : {real.mode_count} modes détectés, {len(real_names)} parsés")

    if not real_names:
        warn("Aucun mode parsé — vérifier que adb logcat était actif pendant la connexion")
    else:
        for m in real.modes:
            ok(f"MODE  {m['name']:<25} video={m['isVideo']} photo={m['isPhoto']} live={m['isLive']}")

    if emu.available:
        emu_names = [m.get("name", "") for m in emu.modes]
        missing_in_emu = [n for n in real_names if n not in emu_names]
        extra_in_emu   = [n for n in emu_names  if n not in real_names]

        if missing_in_emu:
            diff(f"Modes RÉELS absents du profil émulateur : {missing_in_emu}")
            info("→ Ajouter ces modes dans YAML (capture.modes)")
        if extra_in_emu:
            warn(f"Modes ÉMULATEUR absents de la caméra réelle : {extra_in_emu}")
            info("→ Ces modes sont probablement X5-only, commenter dans le profil X3")
        if not missing_in_emu and not extra_in_emu:
            ok("Tous les modes correspondent")

    # ── Settings par mode ─────────────────────────────────────────────────
    section("SETTINGS PAR MODE")
    if not real.settings:
        warn("Aucun setting parsé — vérifier que adb logcat couvrait initCameraSupportConfig")
    else:
        for mode_name, settings in real.settings.items():
            if not settings:
                continue
            info(f"--- {mode_name} ---")
            for key, values in settings.items():
                if values:
                    ok(f"  {key:<20} ({len(values)} valeurs) : {values}")
                else:
                    warn(f"  {key:<20} : liste vide (mode ne supporte pas ce paramètre)")

    # ── Batterie ──────────────────────────────────────────────────────────
    section("BATTERIE")
    if real.battery_level >= 0:
        ok(f"Niveau réel   = {real.battery_level}%  charging={real.is_charging}")
        if emu.available:
            info(f"Émulateur     = {emu.battery_level}%  (initial_state.battery_level dans YAML)")
    else:
        warn("Batterie non reçue — vérifier que fetchCameraBatteryState() a été appelé")

    # ── Stockage ──────────────────────────────────────────────────────────
    section("STOCKAGE")
    if real.storage_free >= 0:
        free_gb  = real.storage_free  / 1e9
        total_gb = real.storage_total / 1e9 if real.storage_total >= 0 else 0
        ok(f"Stockage réel  = {free_gb:.1f} GB libre / {total_gb:.1f} GB total")
        if emu.available and emu.storage_free >= 0:
            emu_free_gb  = emu.storage_free  / 1e9
            emu_total_gb = emu.storage_total / 1e9
            info(f"Émulateur      = {emu_free_gb:.1f} GB libre / {emu_total_gb:.1f} GB total")
            info("→ Mettre à jour sd_remaining_kb / sd_capacity_kb dans YAML si nécessaire")
    else:
        warn("Stockage non reçu — vérifier que fetchCameraStorageState() a été appelé")

    # ── Température ───────────────────────────────────────────────────────
    section("TEMPÉRATURE")
    if real.temperature:
        ok(f"Niveau = {real.temperature}")
    else:
        warn("Température non reçue (normal si camera pas surchauffée + SDK ne l'envoie pas à l'init)")

    # ── Résumé des corrections à apporter ────────────────────────────────
    section("RÉSUMÉ — CORRECTIONS À APPORTER AU PROFIL YAML")
    # Déduire le nom du profil YAML depuis le cameraType détecté
    model_raw = real.camera_type.upper().replace(" ", "")  # "X3" / "X4" / "X5" / ""
    if model_raw:
        # cameraType peut être "X3", "ONE X2", "GO 3"... → normaliser en nom de fichier
        slug = model_raw.lower().replace(" ", "_")
        yaml_file = f"config/profiles/insta360_{slug}.yaml"
    else:
        yaml_file = "config/profiles/insta360_<modèle>.yaml"

    issues = [l for l in lines if "✗" in l or "⚠" in l]
    if not issues:
        ok("Aucune correction nécessaire — émulateur fidèle à la caméra réelle !")
    else:
        info(f"{len(issues)} point(s) à corriger (voir sections ✗ et ⚠ ci-dessus)")
        info("Fichier profil à modifier :")
        info(f"  {yaml_file}")
        info("Puis relancer les tests :")
        info("  python -m pytest tests/ -v")

    return lines


# ────────────────────────────────────────────────────────────────────────────
#  Point d'entrée
# ────────────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Compare les données Insta360 réelles (logcat) avec l'émulateur."
    )
    parser.add_argument("log_file", nargs="?", default="capture.log",
                        help="Fichier logcat (défaut: capture.log)")
    parser.add_argument("--emulator-url", default="http://localhost:8080",
                        help="URL du serveur OSC de l'émulateur (défaut: http://localhost:8080)")
    parser.add_argument("--output", default="compare_report.txt",
                        help="Fichier de sortie (défaut: compare_report.txt)")
    args = parser.parse_args()

    log_path = Path(args.log_file)
    if not log_path.exists():
        print(f"[ERREUR] Fichier log introuvable : {log_path}")
        print("Générer avec : adb logcat -s Insta360Debug > x3_capture.log")
        sys.exit(1)

    print(f"[1/3] Parsing {log_path} …")
    real = parse_logcat(log_path)
    print(f"      → {len(real.modes)} modes, {sum(len(v) for v in real.settings.values())} settings parsés")

    print(f"[2/3] Requête émulateur {args.emulator_url} …")
    emu = query_emulator(args.emulator_url)
    if emu.available:
        print(f"      → {len(emu.modes)} modes émulés")
    else:
        print("      → Émulateur offline — comparaison partielle")

    print("[3/3] Génération du rapport …")
    report_lines = compare(real, emu)

    model_detected = real.camera_type if real.camera_type else "modèle inconnu"
    header = [
        "compare_emulator.py — Rapport de calibrage émulateur Insta360",
        f"Caméra     : Insta360 {model_detected}  (firmware={real.firmware or '?'})",
        f"Log source : {log_path}",
        f"Émulateur  : {args.emulator_url} ({'disponible' if emu.available else 'offline'})",
        "",
    ]
    full_report = "\n".join(header + report_lines) + "\n"

    out_path = Path(args.output)
    out_path.write_text(full_report, encoding="utf-8")
    print(f"\nRapport sauvegardé : {out_path}")
    print()

    # Afficher aussi dans le terminal
    print(full_report)


if __name__ == "__main__":
    main()
