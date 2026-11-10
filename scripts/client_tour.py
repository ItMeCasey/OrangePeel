"""Runs the dev screenshot tour on a real game client for one layer and collects the screenshots.

Usage: python scripts/client_tour.py [--viewer <name> --deps <curse.maven:...,...>] <layer> [<layer> ...]
       e.g. neoforge-1.21.1 forge-26.2. With --viewer the item viewer check runs instead of the HUD tour (see docs/TESTING.md).

Starts `gradlew runClient` with ORANGEPEEL_SHOTS set, moves the game window to DISPLAY6 (the right-hand monitor) as soon as it
exists, waits for the tour to finish (it quits the game itself), and prints the screenshot names. The game is muted and the
tour lets go of the mouse, so nothing needs a person at the keyboard. Screenshots land in <layer>/run/screenshots.
"""
import os
import re
import shutil
import subprocess
import sys
import threading
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LAUNCH = r"C:\Users\casey\.claude\tools\launch-isolated.ps1"
OWNS = r"C:\Users\casey\.claude\tools\who-owns-minecraft.ps1"
SIZE = "1904x1041"
TIMEOUT = 180
OPTIONS = (
    "soundCategory_master:0.0\nmusic:0.0\npauseOnLostFocus:false\nonboardAccessibility:false\n"
    "skipMultiplayerWarning:true\njoinedFirstServer:true\ntutorialStep:none\nfullscreen:false\nguiScale:3\nlang:en_us\n"
)


def tour(layer, viewer=None, deps=""):
    folder = ROOT / layer
    run = folder / "run"
    run.mkdir(exist_ok=True)
    (run / "options.txt").write_text(OPTIONS)
    shots = run / "screenshots"
    if shots.exists():
        shutil.rmtree(shots)
    env = dict(os.environ, ORANGEPEEL_SHOTS="1")
    extra = []
    if viewer:
        env["ORANGEPEEL_VIEWER"] = viewer
        extra = [f"-Pviewers={deps}"] if deps else []
    env["ORANGEPEEL_SIZE"] = SIZE
    latest = run / "logs" / "latest.log"
    start = time.time()
    # The client runs on the hidden MCTest desktop (launch-isolated.ps1), so it cannot touch Casey's desktop, focus or cursor.
    command = ".\\gradlew.bat runClient --no-daemon -q " + " ".join(extra) + " > run\\launch.out 2>&1"
    started = subprocess.run(["powershell", "-NoProfile", "-File", LAUNCH, "-Run", "-WaitMinutes", "90", "-WorkDir",
                              str(folder), "-Command", command], env=env, capture_output=True, text=True)
    print((started.stdout + started.stderr).strip()[-600:], flush=True)
    start = time.time()  # the launcher may have queued for the single client slot; the time limit starts when the game does
    log = []
    timed_out = False
    while True:
        time.sleep(3)
        text = latest.read_text(encoding="utf-8", errors="replace") if latest.exists() and latest.stat().st_mtime >= start else ""
        if "ORANGEPEEL-SHOTS DONE" in text or "ORANGEPEEL-SHOTS FAILED" in text or "Crash report saved" in text:
            break
        if time.time() - start > TIMEOUT:
            timed_out = True
            break
    # The game quits by itself after DONE; give it 30 s, then stop the run so no client is left behind.
    for _ in range(0 if os.environ.get("ORANGEPEEL_JOIN") else 15):  # with a server in the same folder the owner check cannot tell them apart
        owned = subprocess.run(["powershell", "-NoProfile", "-File", OWNS], capture_output=True, text=True).stdout
        if str(folder) not in owned:
            break
        time.sleep(2)
    else:
        timed_out = timed_out or not os.environ.get("ORANGEPEEL_JOIN")
        if os.environ.get("ORANGEPEEL_JOIN"):
            time.sleep(10)  # the client quits itself after DONE
    if timed_out:
        # Only when this run hung: -Kill stops every run on the shared hidden desktop, so never as a routine step.
        subprocess.run(["powershell", "-NoProfile", "-File", LAUNCH, "-Kill"], capture_output=True)
    log = text.splitlines() if "text" in dir() else []
    (run / "client_tour.log").write_text("\n".join(log), encoding="utf-8")
    names = sorted(p.name for p in shots.glob("*.png")) if shots.exists() else []
    done = any("ORANGEPEEL-SHOTS DONE" in line for line in log)
    errors = [l for l in log if re.search(r"orangepeel.*(Exception|Error)|ModLoadingException|Crash report saved", l, re.I)]
    status = "TOUR PASS" if done and not timed_out and not errors else "TOUR FAIL"
    detail = "" if status == "TOUR PASS" else f": done={done} timed_out={timed_out} first_error={errors[:1]}"
    return f"{status} {layer} ({len(names)} screenshots, {int(time.time() - start)}s){detail}"


if __name__ == "__main__":
    args = sys.argv[1:]
    viewer, deps = None, ""
    if "--viewer" in args:
        i = args.index("--viewer")
        viewer = args[i + 1]
        del args[i:i + 2]
    if "--deps" in args:
        i = args.index("--deps")
        deps = args[i + 1]
        del args[i:i + 2]
    bad = 0
    for layer in args:
        result = tour(layer, viewer, deps)
        print(result, flush=True)
        bad += result.startswith("TOUR FAIL")
    sys.exit(1 if bad else 0)
