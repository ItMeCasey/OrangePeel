"""A real NeoForge install for the viewer check, launched from its version JSON like the vanilla launcher would.

Usage: python scripts/real_install.py <install dir> <neoforge version id> [--prepare | --write-cmd]

<install dir> is a folder that holds game/ (the output of `java -jar neoforge-installer.jar --install-client <install dir>/game`).
--prepare downloads the libraries and the asset index that the vanilla version JSON lists and that are missing (from Mojang's
servers, SHA-1 checked). --write-cmd writes <install dir>/launch.cmd, the command line the launcher would run: Java 25, the
merged JVM and game arguments, and a classpath of every library plus the client jar. launch.cmd is then started through
launch-isolated.ps1 (see docs/COMPATIBILITY.md). No account is used: the game runs as the offline player "Dev".
"""
import hashlib
import json
import os
import shutil
import sys
import urllib.request
from pathlib import Path

JAVA = shutil.which("java") or "java"


def load(game, vid):
    version = json.loads((game / "versions" / vid / f"{vid}.json").read_text(encoding="utf-8"))
    parent = version.get("inheritsFrom")
    base = json.loads((game / "versions" / parent / f"{parent}.json").read_text(encoding="utf-8")) if parent else {}
    return version, base, parent


def allowed(rules):
    if not rules:
        return True
    result = False
    for rule in rules:
        os_rule = rule.get("os")
        applies = not os_rule or os_rule.get("name") in (None, "windows")
        if os_rule and "arch" in os_rule and os_rule["arch"] not in ("x86_64", "amd64"):
            applies = False
        if "features" in rule:
            applies = False  # demo, custom resolution and quick play are all off
        if applies:
            result = rule["action"] == "allow"
    return result


def libraries(version, base):
    seen = {}
    for lib in base.get("libraries", []) + version.get("libraries", []):
        if not allowed(lib.get("rules")):
            continue
        artifact = lib.get("downloads", {}).get("artifact")
        if artifact:
            seen[artifact["path"]] = artifact
    return list(seen.values())


def fetch(url, target, sha1):
    target.parent.mkdir(parents=True, exist_ok=True)
    with urllib.request.urlopen(url, timeout=60) as response:
        data = response.read()
    if sha1 and hashlib.sha1(data).hexdigest() != sha1:
        raise RuntimeError(f"SHA-1 mismatch for {url}")
    target.write_bytes(data)


def prepare(game, vid):
    version, base, parent = load(game, vid)
    missing = 0
    for artifact in libraries(version, base):
        target = game / "libraries" / artifact["path"]
        if not target.exists() and artifact.get("url"):
            fetch(artifact["url"], target, artifact.get("sha1"))
            missing += 1
    index = base.get("assetIndex")
    if index:
        target = game / "assets" / "indexes" / f"{index['id']}.json"
        if not target.exists():
            fetch(index["url"], target, index.get("sha1"))
            missing += 1
    print(f"downloaded {missing} missing files")


def assets_lite(game, vid):
    """Downloads the asset objects the game needs to start (fonts, sound index, pack metadata), not the 480 MB of sounds and languages."""
    version, base, parent = load(game, vid)
    index = json.loads((game / "assets" / "indexes" / f"{base['assets']}.json").read_text(encoding="utf-8"))["objects"]
    got = 0
    for name, obj in index.items():
        if name.startswith(("minecraft/sounds/", "minecraft/lang/")) or name.startswith("realms/"):
            continue
        target = game / "assets" / "objects" / obj["hash"][:2] / obj["hash"]
        if not target.exists():
            fetch(f"https://resources.download.minecraft.net/{obj['hash'][:2]}/{obj['hash']}", target, obj["hash"])
            got += 1
    print(f"downloaded {got} asset objects")


def substitute(text, values):
    for key, value in values.items():
        text = text.replace("${" + key + "}", value)
    return text


def flatten(arguments, values):
    out = []
    for item in arguments:
        if isinstance(item, str):
            out.append(substitute(item, values))
        elif allowed(item.get("rules")):
            value = item["value"]
            out.extend(substitute(v, values) for v in ([value] if isinstance(value, str) else value))
    return out


def write_cmd(install, vid):
    game = install / "game"
    version, base, parent = load(game, vid)
    jars = [str(game / "libraries" / a["path"]) for a in libraries(version, base)]
    jars.append(str(game / "versions" / parent / f"{parent}.jar"))
    values = {
        "library_directory": str(game / "libraries"), "classpath_separator": os.pathsep, "classpath": os.pathsep.join(jars),
        "natives_directory": str(install / "natives"), "launcher_name": "orangepeel-check", "launcher_version": "1",
        "version_name": vid, "game_directory": str(game), "assets_root": str(game / "assets"),
        "assets_index_name": base.get("assets", ""), "auth_player_name": "Dev",
        "auth_uuid": "00000000-0000-0000-0000-000000000000", "auth_access_token": "0", "clientid": "0", "auth_xuid": "0",
        "user_type": "legacy", "version_type": "release",
    }
    jvm = flatten(base.get("arguments", {}).get("jvm", []), values)
    jvm_neo = flatten(version.get("arguments", {}).get("jvm", []), values)
    jvm = jvm + jvm_neo  # repeated flags are harmless; dropping duplicates would split a flag from its value
    # -cp and its value come from the vanilla arguments; keep them once
    game_args = flatten(base.get("arguments", {}).get("game", []), values) + flatten(
        version.get("arguments", {}).get("game", []), values)
    command = [JAVA, "-Xmx3G", *jvm, version["mainClass"], *game_args, "--width", "1904", "--height", "1041"]
    line = " ".join(f'"{a}"' if (" " in a or a.startswith("-") is False and "\\" in a) else a for a in command)
    (install / "launch.cmd").write_text(f'@echo off\ncd /d "{game}"\n{line}\n', encoding="utf-8")
    print("wrote", install / "launch.cmd")


if __name__ == "__main__":
    install, vid = Path(sys.argv[1]), sys.argv[2]
    if "--prepare" in sys.argv:
        prepare(install / "game", vid)
    if "--assets-lite" in sys.argv:
        assets_lite(install / "game", vid)
    if "--write-cmd" in sys.argv:
        write_cmd(install, vid)
