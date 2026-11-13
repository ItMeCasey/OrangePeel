#!/usr/bin/env python3
"""Pick the builds a workflow run needs, so unchanged versions are never built.

  plan_matrix.py build   [--before SHA] [--sha SHA] [--files F ...]   builds affected by the changed files
  plan_matrix.py release --tag vYYYY.M.N                              builds of the Minecraft versions whose modules carry the tag

Writes `matrix=<json>`, `any=true|false` and `test=true|false` (neoforge-1.21.1 is picked, so the unit test job is needed)
to $GITHUB_OUTPUT (and prints them).

How OrangePeel shares code (checked against every module's build.gradle):
  <loader>-<mc>/     one loader layer, its own folder
  version-<mc>/      Minecraft-version code compiled into both loader layers of that version (not a Gradle module)
  common/            compiled into every layer (java, resources, unit tests)
  scripts/combine_jars.py   merges the two layers of a version into the released jar (CI only; changes select every layer so
                            the combine step is exercised)
Docs, other scripts (tours and checks), branding, curseforge text, markdown and CI files are not compiled into any jar and
select nothing. An unknown path, or a diff that cannot be computed, selects everything.

A release ships one combined jar per Minecraft version holding BOTH loader layers, so release mode picks a Minecraft
version only when both of its modules carry the tag version (it fails when only one does).
"""
import argparse
import json
import os
import re
import subprocess
import sys

# (module, java); the folder of a module is its name
ALL = [
    ("neoforge-1.21.1", 21),
    ("forge-1.21.1", 21),
    ("neoforge-26.1.2", 25),
    ("forge-26.1.2", 25),
    ("neoforge-26.2", 25),
    ("forge-26.2", 25),
    ("neoforge-26.3", 25),
    ("forge-26.3", 25),
]
NAMES = [m for m, _ in ALL]
MCS = ["1.21.1", "26.1.2", "26.2", "26.3"]
ZERO = "0" * 40

# Folders and files that no jar is built from.
NO_BUILD_PREFIXES = (
    "docs/", "branding/", "curseforge/", "dist/", "logs/", "release/",
    ".github/ISSUE_TEMPLATE/", ".github/workflows/", ".github/scripts/",
)
NO_BUILD_ROOT_FILES = {".gitignore", "LICENSE", "README.md", "CHANGELOG.md", "PROJECT_STATUS.md"}


def mc_of(module):
    return module.split("-", 1)[1]


def modules_of_mc(mc):
    return {m for m in NAMES if mc_of(m) == mc}


def changed_files(before, sha):
    """The files changed between two commits, or None when git cannot tell (then everything is built)."""
    if not before or before == ZERO:
        # first push of a branch or a manual run: compare with the parent commit
        before = sha + "^"
    out = subprocess.run(["git", "diff", "--name-only", before, sha], capture_output=True, text=True)
    if out.returncode != 0:
        return None
    return [line.strip() for line in out.stdout.splitlines() if line.strip()]


def affected(path):
    """The module names one changed path affects (a set). Unknown paths affect every module."""
    path = path.replace("\\", "/")
    if path.startswith("./"):
        path = path[2:]
    if path in NO_BUILD_ROOT_FILES or path.startswith(NO_BUILD_PREFIXES):
        return set()
    if "/" not in path and path.endswith(".md"):
        return set()
    top = path.split("/", 1)[0]
    if top in NAMES:
        return {top}
    if top.startswith("version-") and top[len("version-"):] in MCS:
        return modules_of_mc(top[len("version-"):])
    if top == "common":
        return set(NAMES)
    if top == "scripts":
        # combine_jars.py makes the released jar, so it re-runs every layer; the tours and checks are not part of any jar
        return set(NAMES) if path == "scripts/combine_jars.py" else set()
    return set(NAMES)


def plan_build(files):
    """The modules to build for a list of changed files (None means the list is unknown)."""
    if files is None:
        return list(ALL)
    picked = set()
    for f in files:
        picked |= affected(f)
    return [m for m in ALL if m[0] in picked]


def module_version(module, root="."):
    try:
        with open(os.path.join(root, module, "gradle.properties"), encoding="utf-8") as fh:
            text = fh.read()
    except OSError:
        return None
    m = re.search(r"^mod_version=(\S+)", text, re.M)
    return m.group(1) if m else None


def version_key(v):
    return [int(x) for x in v.split(".")]


def plan_release(tag, root="."):
    """(version, picked modules, problems): the modules whose own mod_version equals the tag version (the newest version
    found when the run is not a tag). A Minecraft version with only one of its two loaders at that version is a problem."""
    version = (tag or "").lstrip("v")
    if not re.fullmatch(r"\d+\.\d+\.\d+", version):
        found = {module_version(m, root) for m, _ in ALL} - {None}
        version = sorted(found, key=version_key)[-1] if found else ""
    picked = [m for m in ALL if module_version(m[0], root) == version]
    names = {m for m, _ in picked}
    problems = []
    for mc in MCS:
        have = modules_of_mc(mc) & names
        if have and have != modules_of_mc(mc):
            problems.append(f"Minecraft {mc}: only {', '.join(sorted(have))} carries {version}; both loaders must (one combined jar holds both)")
    return version, picked, problems


def emit(picked):
    matrix = {"include": [{"module": m, "java": j} for m, j in picked]}
    test = any(m == "neoforge-1.21.1" for m, _ in picked)
    lines = [f"matrix={json.dumps(matrix)}", f"any={'true' if picked else 'false'}", f"test={'true' if test else 'false'}"]
    print("\n".join(lines))
    print("builds:", ", ".join(m for m, _ in picked) or "none")
    out = os.environ.get("GITHUB_OUTPUT")
    if out:
        with open(out, "a", encoding="utf-8") as fh:
            fh.write("\n".join(lines) + "\n")


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("mode", choices=["build", "release"])
    ap.add_argument("--before")
    ap.add_argument("--sha", default="HEAD")
    ap.add_argument("--tag")
    ap.add_argument("--files", nargs="*", help="changed files to use instead of asking git (for tests)")
    args = ap.parse_args(argv)

    if args.mode == "release":
        version, picked, problems = plan_release(args.tag)
        emit(picked)
        if problems:
            sys.exit("\n".join(problems))
        if not picked:
            sys.exit(f"No build has mod_version {version}")
    else:
        files = args.files if args.files is not None else changed_files(args.before, args.sha)
        emit(plan_build(files))


if __name__ == "__main__":
    main()
