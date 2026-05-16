"""
Test de bout-en-bout du protocole legacy Hero 7 sans BLE.

Rejoue la séquence exacte que l'app gopro_native_v3 v4.1 envoie à la caméra
lorsqu'elle détecte une Hero 5/6/7/8, et vérifie chaque réponse.

Utile pour valider la logique du protocole legacy sans dépendre de
l'advertising BLE (bloqué sur Windows) ou d'une vraie caméra.

Usage :
    python tools/test_legacy_flow.py
"""

import asyncio
import sys
from pathlib import Path

# Forcer UTF-8 sur stdout pour les box-drawing chars (Windows cp1252 sinon)
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# Permettre d'importer depuis la racine du projet
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from core.profile_loader import load_profile
from core.camera_state import CameraState
from ble.gopro.command_handler import CommandHandler
from ble.gopro.settings_handler import SettingsHandler
from ble.gopro.query_handler import QueryHandler


GREEN = "\033[92m"
RED = "\033[91m"
YELLOW = "\033[93m"
DIM = "\033[2m"
RESET = "\033[0m"


def hexdump(data: bytes, max_len: int = 32) -> str:
    if not data:
        return "(empty)"
    if len(data) > max_len:
        return data[:max_len].hex(" ") + f"... (+{len(data) - max_len} bytes)"
    return data.hex(" ")


async def step(label: str, expected: str | None, action):
    print(f"\n{DIM}→ {label}{RESET}")
    try:
        result = await action()
    except Exception as e:
        print(f"  {RED}EXCEPTION : {e}{RESET}")
        return None

    if result is None:
        print(f"  {YELLOW}(pas de réponse){RESET}")
        return None

    print(f"  ← {hexdump(result)}")
    if expected and expected in (result.hex() if isinstance(result, bytes) else ""):
        print(f"  {GREEN}OK{RESET} — contient '{expected}'")
    elif expected:
        print(f"  {RED}KO{RESET} — devrait contenir '{expected}'")
    return result


async def main():
    print(f"{YELLOW}═══ Test legacy flow Hero 7 Black ═══{RESET}\n")

    profile = load_profile("gopro_hero7")
    state = CameraState(profile)
    cmd = CommandHandler(state, profile)
    qry = QueryHandler(state, profile)
    setg = SettingsHandler(state)

    print(f"Profil chargé : {profile.brand} {profile.model}")
    print(f"  Settings : {len(profile.settings)}, presets : "
          f"{sum(len(g.presets) for g in profile.preset_groups)}")

    # ── 1. Connexion : l'app envoie CMD_GET_HARDWARE_INFO ─────────────
    r = await step(
        "1. HW Info (0x3C) — l'app détecte la génération via le nom",
        expected="4845524f37",  # "HERO7" en hex
        action=lambda: cmd.handle(bytes([0x3C])),
    )
    if r:
        print(f"  {DIM}→ Modèle dans la réponse : "
              f"{bytes([b for b in r if 32 <= b < 127]).decode('ascii', errors='replace')}{RESET}")

    # ── 2. Subscribe legacy : Get Status one-shot ────────────────────
    # L'app envoie 0x13 + liste de status IDs (recording, batterie, etc.)
    status_ids = [10, 70, 2, 54, 35, 6]
    await step(
        f"2. Get Status (0x13) one-shot — {len(status_ids)} status IDs",
        expected="13",
        action=lambda: qry.handle(bytes([0x13] + status_ids)),
    )

    # ── 3. Tap sur preset "Photo continue" (id=12, modeGroup=1, sub=1) ──
    print(f"\n{YELLOW}─── Scénario : utilisateur tape sur 'Photo continue' ───{RESET}")
    await step(
        "3a. Set Mode Group → Photo (0x02 0x01 0x01)",
        expected="0200",
        action=lambda: cmd.handle(bytes([0x02, 0x01, 0x01])),
    )
    await step(
        "3b. Set Sub-Mode → Photo Continue (0x03 0x01 0x01 0x01 0x01)",
        expected="0300",
        action=lambda: cmd.handle(bytes([0x03, 0x01, 0x01, 0x01, 0x01])),
    )

    # ── 4. Utilisateur change la résolution photo (setting 17 = 8 / Medium) ──
    print(f"\n{YELLOW}─── Scénario : changement de réglage ───{RESET}")
    await step(
        "4. Set Photo Resolution → 12MP Medium (id=17, value=8)",
        expected="1100",
        action=lambda: setg.handle(bytes([17, 0x01, 8])),
    )
    print(f"  {DIM}→ État caméra : setting[17] = {state.settings.get(17)}{RESET}")

    # ── 5. HiLight tag (déjà testé en v4 mais on vérifie) ─────────────
    await step(
        "5. HiLight (0x18)",
        expected="1800",
        action=lambda: cmd.handle(bytes([0x18])),
    )
    print(f"  {DIM}→ HiLight count : {state.hilight_count}{RESET}")

    # ── 6. Shutter ON ─────────────────────────────────────────────────
    print(f"\n{YELLOW}─── Scénario : déclenchement vidéo ───{RESET}")
    await step(
        "6a. Shutter ON (0x01 0x01 0x01)",
        expected="0100",
        action=lambda: cmd.handle(bytes([0x01, 0x01, 0x01])),
    )
    print(f"  {DIM}→ Recording : {state.is_recording}{RESET}")

    await step(
        "6b. Shutter OFF (0x01 0x01 0x00)",
        expected="0100",
        action=lambda: cmd.handle(bytes([0x01, 0x01, 0x00])),
    )
    print(f"  {DIM}→ Recording : {state.is_recording}{RESET}")

    # ── 7. KeepAlive (envoyé toutes les 3s par l'app) ─────────────────
    await step(
        "7. KeepAlive (0x5B 0x01 0x42)",
        expected="5b00",
        action=lambda: cmd.handle(bytes([0x5B, 0x01, 0x42])),
    )

    # ── 8. Set Date (sync horloge) ────────────────────────────────────
    # Payload : 0x0D 0x07 YYhi YYlo MM DD hh mm ss
    await step(
        "8. Set Date (0x0D + 7 bytes)",
        expected="0d00",
        action=lambda: cmd.handle(bytes([0x0D, 0x07, 0x07, 0xEA, 5, 16, 14, 30, 0])),
    )

    print(f"\n{GREEN}═══ Test terminé ═══{RESET}\n")
    print("Tous les opcodes legacy critiques de l'app v4.1 sont gérés.")
    print("Si tu vois 'OK' sur chaque ligne, le protocole côté émulateur")
    print("est conforme à ce que l'app envoie. Le retour terrain de ton ami")
    print("sur la vraie Hero 7 dira si les payloads sont aussi conformes à")
    print("ce que le firmware réel accepte.")


if __name__ == "__main__":
    asyncio.run(main())
