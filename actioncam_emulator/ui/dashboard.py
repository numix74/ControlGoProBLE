"""
Rich TUI dashboard for real-time camera state monitoring.
"""

import time
import asyncio
from typing import TYPE_CHECKING

from rich.console import Console
from rich.table import Table
from rich.panel import Panel
from rich.layout import Layout
from rich.live import Live
from rich.text import Text
from rich import box

if TYPE_CHECKING:
    from core.camera_state import CameraState
    from core.profile_loader import CameraProfile

console = Console()


def _format_duration(seconds: int) -> str:
    h = seconds // 3600
    m = (seconds % 3600) // 60
    s = seconds % 60
    return f"{h:02d}:{m:02d}:{s:02d}"


def _battery_bar(level: int) -> str:
    filled = round(level / 100 * 10)
    bar = "[" + "#" * filled + "." * (10 - filled) + "]"
    return bar


def _format_bytes(kb: int) -> str:
    if kb >= 1_000_000:
        return f"{kb / 1_000_000:.1f} GB"
    if kb >= 1_000:
        return f"{kb / 1_000:.0f} MB"
    return f"{kb} KB"


class Dashboard:
    """Live TUI dashboard using Rich."""

    REFRESH_HZ = 4  # redraws per second

    def __init__(self, state, profile):
        self.state = state
        self.profile = profile

    def _make_header(self) -> Panel:
        st = self.state
        pr = self.profile
        rec = st.is_recording
        dur_ms = st.get_recording_duration_ms()
        dur_s = dur_ms // 1000

        status = Text()
        if rec:
            status.append(" REC ", style="bold white on red")
            status.append(f"  {_format_duration(dur_s)}", style="bold red")
        else:
            status.append(" STANDBY ", style="bold white on dark_green")

        title = Text()
        title.append(f"{pr.brand.upper()} {pr.model}", style="bold cyan")
        title.append(f"  SN:{pr.serial}", style="dim")
        title.append(f"  FW:{pr.firmware}", style="dim")
        title.append("  ")
        title.append_text(status)

        return Panel(title, box=box.DOUBLE_EDGE, style="cyan")

    def _make_state_table(self) -> Table:
        st = self.state
        tbl = Table(box=box.SIMPLE, expand=True, show_header=False, padding=(0, 1))
        tbl.add_column("Key", style="bold yellow", width=20)
        tbl.add_column("Value", style="white")
        tbl.add_column("Key", style="bold yellow", width=20)
        tbl.add_column("Value", style="white")

        batt_color = "green" if st.battery_level > 40 else ("yellow" if st.battery_level > 15 else "red")
        batt_str = f"[{batt_color}]{_battery_bar(st.battery_level)} {st.battery_level}%[/{batt_color}]"
        sd_used = st.sd_capacity_kb - st.sd_remaining_kb
        sd_pct = (sd_used / st.sd_capacity_kb * 100) if st.sd_capacity_kb else 0
        sd_str = f"{_format_bytes(st.sd_remaining_kb)} free / {_format_bytes(st.sd_capacity_kb)} ({sd_pct:.0f}% used)"
        heat_str = "[bold red]OVERHEAT[/bold red]" if st.is_overheating else "[green]Normal[/green]"
        preset_str = str(st.active_preset_id) if st.active_preset_id else "None"

        tbl.add_row("Battery", batt_str, "SD Card", sd_str)
        tbl.add_row("Temperature", heat_str, "Active Preset", preset_str)
        tbl.add_row("Photos", str(st.photos_remaining) + " rem", "Videos", str(st.videos_count))
        tbl.add_row("Charging", "Yes" if st.is_charging else "No", "System", "Ready" if st.is_system_ready else "Busy")
        return tbl

    def _make_settings_table(self) -> Table:
        st = self.state
        pr = self.profile
        tbl = Table(title="Settings", box=box.SIMPLE, expand=True, header_style="bold magenta")
        tbl.add_column("ID", style="dim", width=5)
        tbl.add_column("Value", width=8)
        tbl.add_column("ID", style="dim", width=5)
        tbl.add_column("Value", width=8)

        items = sorted(st.settings.items())
        half = (len(items) + 1) // 2
        left = items[:half]
        right = items[half:]
        for i, (lid, lv) in enumerate(left):
            rid, rv = right[i] if i < len(right) else ("", "")
            tbl.add_row(str(lid), str(lv), str(rid), str(rv))
        return tbl

    def _make_log_panel(self) -> Panel:
        st = self.state
        if st.last_command:
            age = time.time() - st.last_command_time
            age_str = f"[dim]{age:.1f}s ago[/dim]"
            cmd_text = Text()
            cmd_text.append(f"  {st.last_command}", style="bold white")
            cmd_text.append(f"  {age_str}")
        else:
            cmd_text = Text("  Waiting for commands...", style="dim")
        return Panel(cmd_text, title="Last Command", box=box.SIMPLE, style="blue")

    def _build_renderable(self):
        layout = Layout()
        layout.split_column(
            Layout(self._make_header(), size=3),
            Layout(name="mid", size=8),
            Layout(self._make_settings_table(), size=12),
            Layout(self._make_log_panel(), size=3),
        )
        layout["mid"].split_row(
            Layout(Panel(self._make_state_table(), title="Camera State", box=box.ROUNDED)),
        )
        return layout

    async def run(self):
        """Run the live dashboard until cancelled."""
        with Live(
            self._build_renderable(),
            console=console,
            refresh_per_second=self.REFRESH_HZ,
            screen=True,
        ) as live:
            try:
                while True:
                    await asyncio.sleep(1.0 / self.REFRESH_HZ)
                    live.update(self._build_renderable())
            except asyncio.CancelledError:
                pass
