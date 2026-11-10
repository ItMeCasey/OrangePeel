"""Dedicated-server check for the exhaustion underlay: a real server and one separate client.

Usage: python scripts/dedicated_check.py

Starts the NeoForge 1.21.1 dedicated server in neoforge-1.21.1/run-server on port 8950 (registered for OrangePeel), then
runs one client through scripts/client_tour.py with ORANGEPEEL_JOIN=localhost:8950. The client joins the server, asks it
for the hunger effect (which builds exhaustion on the server) and shoots the hunger bar. The server sends the exhaustion
to the client, and the check prints the evidence: the server's log, the client's "received the first exhaustion update"
line, and the frames, which it copies to docs/frames/dedicated_neoforge-1.21.1_*.png.

The Minecraft EULA: Casey accepted it in chat on 2026-11-09 for this test server only. This script writes eula.txt into
run-server (git-ignored, never committed) and nowhere else.
"""
import hashlib
import os
import re
import shutil
import subprocess
import sys
import threading
import time
import uuid
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import client_tour  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
LAYER = "neoforge-1.21.1"
PORT = 8950
NAME = "Dev"


def offline_uuid(name):
    digest = bytearray(hashlib.md5(("OfflinePlayer:" + name).encode("utf-8")).digest())
    digest[6] = (digest[6] & 0x0F) | 0x30
    digest[8] = (digest[8] & 0x3F) | 0x80
    return str(uuid.UUID(bytes=bytes(digest)))


def main():
    folder = ROOT / LAYER
    run = folder / "run-server"
    run.mkdir(exist_ok=True)
    (run / "eula.txt").write_text("eula=true\n")
    (run / "server.properties").write_text(
        f"server-port={PORT}\nonline-mode=false\nenable-command-block=false\nspawn-protection=0\nview-distance=6\n"
        "level-type=minecraft\\:flat\ngamemode=survival\ndifficulty=peaceful\nmotd=OrangePeel dedicated check\n"
        "max-tick-time=-1\n")
    (run / "ops.json").write_text(
        '[{"uuid": "%s", "name": "%s", "level": 4, "bypassesPlayerLimit": false}]\n' % (offline_uuid(NAME), NAME))
    # The client goes first (it may queue for the single client slot for a while); the server starts as soon as the client's
    # Gradle run has begun, so no server sits idle during the wait. The client's tour retries the join until the server is up.
    results = []
    os.environ["ORANGEPEEL_JOIN"] = f"localhost:{PORT}"
    launch_out = folder / "run" / "launch.out"
    t0 = time.time()
    client = threading.Thread(target=lambda: results.append(client_tour.tour(LAYER)), daemon=True)
    client.start()
    while client.is_alive() and not (launch_out.exists() and launch_out.stat().st_mtime >= t0):
        time.sleep(2)
    log = []
    proc = subprocess.Popen(["cmd", "/c", str(folder / "gradlew.bat"), "runServer", "--no-daemon", "-q"], cwd=folder,
                            env=dict(os.environ, ORANGEPEEL_SERVER_DIR="run-server"), stdin=subprocess.PIPE,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, errors="replace")

    def pump():
        for line in proc.stdout:
            log.append(line.rstrip())

    threading.Thread(target=pump, daemon=True).start()
    try:
        client.join(timeout=900)
    finally:
        try:
            proc.stdin.write("stop" + chr(10))
            proc.stdin.flush()
        except OSError:
            pass
        for _ in range(30):
            if proc.poll() is not None:
                break
            time.sleep(1)
        if proc.poll() is None:
            subprocess.run(["taskkill", "/T", "/F", "/PID", str(proc.pid)], capture_output=True)
    result = results[0] if results else "TOUR FAIL (no result)"
    (run / "dedicated_check.log").write_text("\n".join(log), encoding="utf-8")
    print(result)
    server_lines = [l for l in log if re.search(r"OrangePeel loaded|joined the game|Dev left|logged in", l)]
    client_log = (folder / "run" / "logs" / "latest.log").read_text(encoding="utf-8", errors="replace").splitlines()
    client_lines = [l for l in client_log if re.search(r"ORANGEPEEL-SHOTS (joining|asking|DONE)|exhaustion update", l)]
    print("--- server log")
    print("\n".join(server_lines))
    print("--- client log")
    print("\n".join(client_lines))
    frames = ROOT / "docs" / "frames"
    for shot in sorted((folder / "run" / "screenshots").glob("orangepeel_join_*.png")):
        target = frames / f"dedicated_{LAYER}_{shot.name.replace('orangepeel_', '')}"
        shutil.copy(shot, target)
        print("frame", target)
    got = any("received the first exhaustion update" in l for l in client_lines)
    print("DEDICATED PASS" if got and result.startswith("TOUR PASS") else "DEDICATED FAIL")
    return 0 if got else 1


if __name__ == "__main__":
    sys.exit(main())
