#!/usr/bin/env python3
"""
compare_emulator.py — Compare les données réelles X3 (logcat) avec le profil émulateur.

Usage :
    cd actioncam_emulator
    python tools/compare_emulator.py x3_capture.log
    python tools/compare_emulator.py x3_capture.log --profile insta360_x3 --out rapport.txt

Prérequis :
    pip install pyyaml  (déjà dans requirements.txt)

Entrée :
    x3_capture.log — fichier logcat filtré :
        adb logcat -s Insta360Debug > x3_capture.log

Sortie :
    compare_report.txt (+ affichage console)
"""

import argparse
import re
import sys
from pathlib import Path

import yaml


# ---------------------------------------------------------------------------
# Parser logcat
# ---------------------------------------------------------------------------

def parse_logcat(logfile: Path) -> dict:
    """
    Parse les lignes Insta360Debug du logcat.
    Retourne un dict avec clés : info, config, modes, settings, battery, storage, temp.
    """
    data = {
        "info": {},
        "config": {},
        "modes": [],
        "settings": [],
        "battery": {},
        "storage": {},
        "temp": None,
        "raw_lines": 0,
        "debug_lines": 0,
    }

    # Pattern : tout ce qui contient [SECTION] key=val key2=val2...
    section_re = re.compile(r'\[(\w+)\]\s*(.*)')
    kv_re = re.compile(r'(\w+)=(\S+)')

    for line in logfile.read_text(encoding='utf-8', errors='replace').splitlines():
        data["raw_lines"] += 1
        if "Insta360Debug" not in line:
            continue
        data["debug_lines"] += 1

        m = section_re.search(line)
        if not m:
            continue

        section = m.group(1).upper()
        content = m.group(2)
        kv = dict(kv_re.findall(content))

        if section == "INFO":
            data["info"].update(kv)
        elif section == "CONFIG":
            data["config"].update(kv)
        elif section == "MODE":
            data["modes"].append(kv)
        elif section == "SETTING":
            data["settings"].append(kv)
        elif section == "BATTERY":
            data["battery"] = kv
        elif section == "STORAGE":
            data["storage"] = kv
        elif section == "TEMP":
            data["temp"] = kv.get("level")

    return data


# ---------------------------------------------------------------------------
# Chargement profil
# ---------------------------------------------------------------------------

def load_profile(profile_name: str) -> dict:
    profile_path = (
        Path(__file__).parent.parent / "config" / "profiles" / f"{profile_name}.yaml"
    )
    if not profile_path.exists():
        print(f"[ERREUR] Profil introuvable : {profile_path}")
        sys.exit(1)
    return yaml.safe_load(profile_path.read_text(encoding="utf-8"))


# ---------------------------------------------------------------------------
# Comparaison
# ---------------------------------------------------------------------------

def compare(real: dict, profile: dict, profile_name: str) -> list[str]:
    lines = []
    ok = []
    issues = []

    def add_ok(msg):
        ok.append(f"  ✓  {msg}")

    def add_issue(msg):
        issues.append(f"  ✗  {msg}")

    def add_info(msg):
        issues.append(f"  →  {msg}")

    # --- Vérifier qu'on a bien des données ---
    if real["debug_lines"] == 0:
        issues.append("  ✗  Aucune ligne Insta360Debug trouvée dans le log.")
        issues.append("  →  Vérifier : adb logcat -s Insta360Debug > x3_capture.log")
        issues.append("  →  Ou que l'APK airbuble avec DebugLogger est bien installé.")
        return ok + issues

    # --- Infos caméra ---
    if real["info"]:
        real_type = real["info"].get("cameraType", "")
        prof_model = profile.get("model", "")
        if real_type and prof_model.lower() in real_type.lower():
            add_ok(f"cameraType : {real_type!r}")
        elif real_type:
            add_issue(f"cameraType  → réel={real_type!r}  profil={prof_model!r}")
            add_info(f"  Corriger 'model' dans {profile_name}.yaml")

        real_fw = real["info"].get("firmware", "")
        prof_fw = str(profile.get("firmware", ""))
        if real_fw == prof_fw:
            add_ok(f"firmware : {real_fw}")
        elif real_fw:
            add_issue(f"firmware    → réel={real_fw!r}  profil={prof_fw!r}")
            add_info(f"  Corriger 'firmware: {real_fw}' dans {profile_name}.yaml")

        real_serial = real["info"].get("serial", "")
        if real_serial:
            add_info(f"serial réel = {real_serial!r}  (mettre à jour 'serial' + SSID wifi dans le profil)")
    else:
        add_issue("Aucun [INFO] trouvé — connexion réussie mais camera info non capturée ?")

    # --- Modes ---
    real_mode_names = [m.get("name", "") for m in real["modes"]]
    prof_mode_names = [m.get("name", "") for m in profile.get("capture", {}).get("modes", [])]

    real_set = set(real_mode_names)
    prof_set = set(prof_mode_names)

    missing_in_profile = real_set - prof_set
    extra_in_profile = prof_set - real_set

    if not real_mode_names:
        add_issue("Aucun [MODE] trouvé — initCameraSupportConfig n'a pas été appelé ou a échoué.")
    else:
        add_ok(f"{len(real_mode_names)} modes capturés depuis la X3")

    for name in sorted(missing_in_profile):
        rm = next((m for m in real["modes"] if m.get("name") == name), {})
        add_issue(
            f"MODE MANQUANT dans profil : {name!r}  "
            f"(isVideo={rm.get('isVideo','?')} isPhoto={rm.get('isPhoto','?')} isLive={rm.get('isLive','?')})"
        )
        add_info(f"  Ajouter dans {profile_name}.yaml → capture.modes")

    for name in sorted(extra_in_profile):
        add_issue(f"MODE EN TROP dans profil : {name!r}  (absent sur la X3 réelle)")
        add_info(f"  Supprimer de {profile_name}.yaml → capture.modes")

    # Vérifier flags is_video / is_photo
    real_mode_map = {m.get("name", ""): m for m in real["modes"]}
    for pm in profile.get("capture", {}).get("modes", []):
        pname = pm.get("name", "")
        rm = real_mode_map.get(pname)
        if not rm:
            continue
        for real_key, prof_key in [("isVideo", "is_video"), ("isPhoto", "is_photo")]:
            real_val = rm.get(real_key, "").lower()
            prof_val = str(pm.get(prof_key, False)).lower()
            if real_val and real_val != prof_val:
                add_issue(
                    f"FLAG {pname}.{real_key} : réel={real_val}  profil={prof_val}"
                )

    # --- Settings ---
    if real["settings"]:
        add_ok(f"{len(real['settings'])} settings capturés")
        lines.append("")
        lines.append("  DETAIL SETTINGS (copier dans le profil si besoin) :")
        seen = set()
        for s in real["settings"]:
            mode = s.get("mode", "?")
            key = s.get("key", "?")
            count = s.get("count", "0")
            values = s.get("values", "[]")
            tag = f"{mode}:{key}"
            if tag not in seen:
                seen.add(tag)
                lines.append(f"    mode={mode:<22} key={key:<20} count={count}  values={values}")
    else:
        add_issue("Aucun [SETTING] trouvé — vérifier que tu as navigué dans les modes.")

    # --- Batterie ---
    if real["battery"]:
        lvl = real["battery"].get("level", "?")
        chrg = real["battery"].get("charging", "?")
        add_ok(f"Batterie capturée : level={lvl}% charging={chrg}")
        prof_lvl = str(profile.get("initial_state", {}).get("battery_level", "?"))
        if lvl != prof_lvl:
            add_info(f"  initial_state.battery_level dans profil = {prof_lvl} (réel = {lvl})")
    else:
        add_issue("Aucun [BATTERY] — fetchCameraBatteryState non déclenché.")

    # --- Stockage ---
    if real["storage"]:
        free = real["storage"].get("free", "?")
        total = real["storage"].get("total", "?")
        add_ok(f"Stockage capturé : free={free} total={total} bytes")
        try:
            free_kb = int(free) // 1024
            total_kb = int(total) // 1024
            add_info(
                f"  Mettre à jour initial_state dans profil :\n"
                f"    sd_remaining_kb: {free_kb}\n"
                f"    sd_capacity_kb:  {total_kb}"
            )
        except ValueError:
            pass
    else:
        add_issue("Aucun [STORAGE] — fetchCameraStorageState non déclenché.")

    # --- Température ---
    if real["temp"]:
        add_ok(f"Température capturée : {real['temp']}")
    else:
        add_info("Température non capturée (normal si caméra froide).")

    # --- Assemblage ---
    if ok and not issues:
        lines.insert(0, "  Profil conforme à la X3 réelle !")
    else:
        if issues:
            lines.insert(0, f"  {len([i for i in issues if '✗' in i])} écart(s) à corriger :")
    return ok + issues + lines


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(
        description="Compare logcat X3 réel vs profil émulateur"
    )
    parser.add_argument("logfile", help="Fichier logcat (adb logcat -s Insta360Debug)")
    parser.add_argument(
        "--profile", default="insta360_x3", help="Nom du profil (défaut: insta360_x3)"
    )
    parser.add_argument(
        "--out", default="compare_report.txt", help="Fichier de sortie (défaut: compare_report.txt)"
    )
    args = parser.parse_args()

    logfile = Path(args.logfile)
    if not logfile.exists():
        print(f"[ERREUR] Fichier introuvable : {logfile}")
        sys.exit(1)

    print(f"Parsing {logfile} ({logfile.stat().st_size} octets) ...")
    real = parse_logcat(logfile)
    print(f"  → {real['debug_lines']} lignes Insta360Debug / {real['raw_lines']} lignes totales")

    print(f"Chargement profil {args.profile} ...")
    profile = load_profile(args.profile)

    result_lines = compare(real, profile, args.profile)

    separator = "=" * 65
    report_parts = [
        separator,
        f"  RAPPORT COMPARAISON — X3 réel vs profil [{args.profile}]",
        separator,
        f"  Modes   X3 réel  : {len(real['modes'])}",
        f"  Modes   profil   : {len(profile.get('capture', {}).get('modes', []))}",
        f"  Settings capturés: {len(real['settings'])}",
        f"  Lignes debug     : {real['debug_lines']}",
        separator,
        "",
    ] + result_lines + [
        "",
        separator,
        "  PROCHAINES ÉTAPES :",
        "  1. Corriger insta360_x3.yaml selon les écarts ci-dessus",
        "  2. python -m pytest tests/ -v  →  vérifier 73/73 verts",
        "  3. Si tout vert : émulateur calibré X3 ✓",
        separator,
    ]

    output = "\n".join(report_parts)
    print(output)

    out_path = Path(args.out)
    out_path.write_text(output, encoding="utf-8")
    print(f"\nRapport sauvegardé : {out_path.resolve()}")


if __name__ == "__main__":
    main()
