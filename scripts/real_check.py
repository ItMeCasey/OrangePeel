"""Runs the viewer check in a real NeoForge install (see scripts/real_install.py) through launch-isolated.ps1.

Usage: python scripts/real_check.py <install dir> <viewer> <click> <type> <hover> [<name>]

The mods folder of <install dir>/game must already hold the OrangePeel jar built with -PwithTour (it carries the dev screenshot
tour) and the viewer. Starts launch.cmd on the hidden desktop (waiting in the launch queue for up to 90 minutes), waits for the
tour to finish or 3 minutes, copies the screenshots to docs/frames/<name>_*.png and stops only its own run.
"""
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LAUNCH = r"C:\Users\casey\.claude\tools\launch-isolated.ps1"


def main():
    install, viewer, click, typed, hover = Path(sys.argv[1]), sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5]
    name = sys.argv[6] if len(sys.argv) > 6 else f"real_{viewer}"
    game = install / "game"
    shots = game / "screenshots"
    if shots.exists():
        shutil.rmtree(shots)
    (game / "options.txt").write_text(
        "pauseOnLostFocus:false\nguiScale:3\nlang:en_us\nfullscreen:false\nonboardAccessibility:false\n"
        "skipMultiplayerWarning:true\njoinedFirstServer:true\ntutorialStep:none\n")
    env = dict(os.environ, ORANGEPEEL_SHOTS="1", ORANGEPEEL_VIEWER=viewer, ORANGEPEEL_CLICK=click,
               ORANGEPEEL_TYPE=typed, ORANGEPEEL_HOVER=hover, **({"ORANGEPEEL_CTRLF": "1"} if os.environ.get("CTRLF") else {}))
    started = subprocess.run(["powershell", "-NoProfile", "-File", LAUNCH, "-Run", "-WaitMinutes", "90", "-WorkDir", str(install),
                              "-RunDir", str(game), "-Command", str(install / "launch.cmd")], env=env, capture_output=True,
                             text=True)
    print((started.stdout + started.stderr).strip()[-500:], flush=True)
    start = time.time()
    latest = game / "logs" / "latest.log"
    text = ""
    hung = True
    while time.time() - start < 180:
        time.sleep(5)
        if latest.exists() and latest.stat().st_mtime >= start - 5:
            text = latest.read_text(encoding="utf-8", errors="replace")
            if re.search(r"ORANGEPEEL-SHOTS (DONE|FAILED)|Crash report saved|Failed to start FML", text):
                hung = False
                break
    time.sleep(8)
    if hung:
        subprocess.run(["powershell", "-NoProfile", "-File", LAUNCH, "-Kill"], capture_output=True)
    done = "ORANGEPEEL-SHOTS DONE" in text
    print("TOUR", "PASS" if done else "FAIL", f"({int(time.time() - start)}s, hung={hung})")
    for line in text.splitlines():
        if re.search(r"ORANGEPEEL-SHOTS (window|click|searching|FAILED)|Crash report|Mod .* requires|jei|Roughly", line):
            print(line[-200:])
    for shot in sorted(shots.glob("orangepeel_*.png")) if shots.exists() else []:
        if "_page" in shot.name:
            continue  # page-flip shots stay in the game's screenshots folder
        target = ROOT / "docs" / "frames" / f"{name}_{shot.name.replace('orangepeel_', '')}"
        shutil.copy(shot, target)
        print("frame", target)


if __name__ == "__main__":
    main()
