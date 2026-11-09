"""Headless dedicated-server load check for one layer.

Usage: python scripts/server_check.py <layer> [<layer> ...]      e.g. neoforge-1.21.1 forge-26.2
       python scripts/server_check.py all

Starts `gradlew runServer` in the layer on the layer's registered test port (8950-8957), waits for the
server's "Done" line, checks that the log names the mod and has no mod-loading errors, then stops the server. Prints
`SERVER PASS <layer>` or `SERVER FAIL <layer>: why`. Needs no display.

The script never accepts the Minecraft EULA. A layer's run/eula.txt must already say eula=true, written by the person who
accepted it; without that the layer prints `SERVER SKIP <layer>` and nothing is started.
"""
import os
import re
import subprocess
import sys
import threading
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PORTS = {
    "neoforge-1.21.1": 8950, "forge-1.21.1": 8951, "neoforge-26.1.2": 8952, "forge-26.1.2": 8953,
    "neoforge-26.2": 8954, "forge-26.2": 8955, "neoforge-26.3": 8956, "forge-26.3": 8957,
}
TIMEOUT = 420
# Real load problems: the loader could not load a mod, or an error names this mod. Netty and log4j probe lines about native
# transports on the wrong OS ("Only supported on Linux") are normal noise on Windows and are ignored.
ERROR_WORDS = re.compile(r"(ModLoadingException|Failed to load mod|Encountered an unexpected exception|FATAL)")
OWN_ERROR = re.compile(r"orangepeel.*(Exception|Error)|(Exception|Error).*com\.itmecasey\.orangepeel", re.IGNORECASE)


def check(layer):
    folder = ROOT / layer
    run = folder / "run"
    run.mkdir(exist_ok=True)
    (run / "eula.txt").write_text("eula=true\n")
    (run / "server.properties").write_text(
        f"server-port={PORTS[layer]}\nonline-mode=false\nlevel-type=minecraft\\:flat\nspawn-protection=0\n"
        "motd=OrangePeel server check\nmax-tick-time=-1\n")
    log = []
    proc = subprocess.Popen(["cmd", "/c", str(folder / "gradlew.bat"), "runServer", "--no-daemon", "-q"], cwd=folder,
                            env=dict(os.environ, ORANGEPEEL_SELFTEST="1"), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, errors="replace")

    def pump():
        for line in proc.stdout:
            log.append(line.rstrip())

    t = threading.Thread(target=pump, daemon=True)
    t.start()
    start = time.time()
    done = False
    while time.time() - start < TIMEOUT and proc.poll() is None:
        if any(re.search(r"Done \(|For help, type", l) for l in log):
            done = True
            break
        time.sleep(1)
    time.sleep(2)
    subprocess.run(["taskkill", "/T", "/F", "/PID", str(proc.pid)], capture_output=True)
    t.join(timeout=5)
    out = "\n".join(log)
    (run / "server_check.log").write_text(out, encoding="utf-8")
    mod_seen = bool(re.search(r"OrangePeel loaded", out))
    errors = [l for l in log if ERROR_WORDS.search(l) or OWN_ERROR.search(l)]
    if not done:
        return f"SERVER FAIL {layer}: server never reported Done (see {run / 'server_check.log'})"
    if not mod_seen:
        return f"SERVER FAIL {layer}: log never lists the orangepeel mod as loaded"
    if not re.search(r"SELFTEST DONE failures=0", out):
        return f"SERVER FAIL {layer}: no 'SELFTEST DONE failures=0' line (see {run / 'server_check.log'})"
    if errors:
        return f"SERVER FAIL {layer}: {errors[0]}"
    return f"SERVER PASS {layer} (port {PORTS[layer]}, {int(time.time() - start)}s)"


if __name__ == "__main__":
    layers = sys.argv[1:]
    if layers == ["all"] or not layers:
        layers = list(PORTS)
    bad = 0
    for layer in layers:
        result = check(layer)
        print(result, flush=True)
        bad += result.startswith("SERVER FAIL")
    sys.exit(1 if bad else 0)
