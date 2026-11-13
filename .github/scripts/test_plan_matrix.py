#!/usr/bin/env python3
"""Tests for plan_matrix.py. Run from the repo root: python -I -m unittest discover -s .github/scripts  (or run this file)."""
import os
import subprocess
import sys
import tempfile
import unittest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import plan_matrix as pm  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
EVERY = sorted(pm.NAMES)


def names(picked):
    return sorted(m for m, _ in picked)


def both(mc):
    return [f"forge-{mc}", f"neoforge-{mc}"]


def git_diff(a, b):
    """Changed files between two commits, or skip the test when a commit is missing from this clone."""
    out = subprocess.run(["git", "diff", "--name-only", a, b], cwd=ROOT, capture_output=True, text=True)
    if out.returncode != 0:
        raise unittest.SkipTest(f"commit {a} or {b} is not in this clone")
    return [line for line in out.stdout.splitlines() if line]


class SyntheticChanges(unittest.TestCase):
    def test_docs_and_status_only_build_nothing(self):
        files = ["PROJECT_STATUS.md", "README.md", "CHANGELOG.md", "LICENSE", "docs/TESTING.md", "docs/frames/a.png",
                 "branding/banners/banner.png", "curseforge/DESCRIPTION.md", "scripts/client_tour.py", "scripts/real_check.py",
                 "dist/orangepeel-mc26.3-2026.11.3.jar", ".gitignore"]
        self.assertEqual(pm.plan_build(files), [])

    def test_workflow_and_ci_script_only_build_nothing(self):
        files = [".github/workflows/build.yml", ".github/workflows/release.yml", ".github/scripts/plan_matrix.py",
                 ".github/scripts/publish_curseforge.py", ".github/ISSUE_TEMPLATE/config.yml"]
        self.assertEqual(pm.plan_build(files), [])

    def test_common_selects_every_module(self):
        self.assertEqual(names(pm.plan_build(["common/src/main/java/com/itmecasey/orangepeel/X.java"])), EVERY)
        self.assertEqual(names(pm.plan_build(["common/src/test/java/com/itmecasey/orangepeel/XTest.java"])), EVERY)

    def test_combine_script_selects_every_module(self):
        self.assertEqual(names(pm.plan_build(["scripts/combine_jars.py"])), EVERY)

    def test_version_folder_selects_both_loaders_of_that_version_only(self):
        self.assertEqual(names(pm.plan_build(["version-26.2/src/main/java/A.java"])), both("26.2"))
        self.assertEqual(names(pm.plan_build(["version-1.21.1/src/main/java/A.java"])), both("1.21.1"))

    def test_single_module(self):
        self.assertEqual(names(pm.plan_build(["forge-1.21.1/src/main/templates/pack.mcmeta"])), ["forge-1.21.1"])
        self.assertEqual(names(pm.plan_build(["neoforge-26.3/build.gradle", "docs/a.md"])), ["neoforge-26.3"])

    def test_mixed_files_union(self):
        picked = pm.plan_build(["docs/a.md", "forge-26.2/src/main/java/A.java", "version-26.3/src/main/java/B.java"])
        self.assertEqual(names(picked), sorted(["forge-26.2"] + both("26.3")))

    def test_unknown_path_selects_everything(self):
        self.assertEqual(names(pm.plan_build(["something/new.txt"])), EVERY)
        self.assertEqual(names(pm.plan_build([".gitattributes"])), EVERY)
        self.assertEqual(names(pm.plan_build(["version-27.0/src/A.java"])), EVERY)
        self.assertEqual(names(pm.plan_build(["forge-27.0/src/A.java"])), EVERY)
        self.assertEqual(names(pm.plan_build([".github/dependabot.yml"])), EVERY)

    def test_diff_failure_selects_everything(self):
        self.assertEqual(names(pm.plan_build(None)), EVERY)
        self.assertIsNone(pm.changed_files("0123456789abcdef0123456789abcdef01234567", "HEAD"))

    def test_matrix_entries_keep_module_and_java(self):
        by_name = dict(pm.ALL)
        self.assertEqual(by_name["neoforge-1.21.1"], 21)
        self.assertEqual(by_name["forge-26.1.2"], 25)
        self.assertEqual(len(pm.ALL), 8)

    def test_files_option_and_outputs(self):
        with tempfile.TemporaryDirectory() as tmp:
            script = os.path.join(ROOT, ".github", "scripts", "plan_matrix.py")
            out = os.path.join(tmp, "out.txt")
            run = subprocess.run([sys.executable, "-I", script, "build", "--files", "PROJECT_STATUS.md"],
                                 cwd=ROOT, env=dict(os.environ, GITHUB_OUTPUT=out), capture_output=True, text=True)
            self.assertEqual(run.returncode, 0, run.stderr)
            with open(out, encoding="utf-8") as fh:
                text = fh.read()
            self.assertIn('matrix={"include": []}', text)
            self.assertIn("any=false", text)
            self.assertIn("test=false", text)

    def test_unit_test_job_runs_exactly_when_neoforge_1_21_1_builds(self):
        with tempfile.TemporaryDirectory() as tmp:
            script = os.path.join(ROOT, ".github", "scripts", "plan_matrix.py")
            for files, expected in (([ "forge-26.3/build.gradle"], "test=false"), (["common/src/main/java/A.java"], "test=true"),
                                    (["version-1.21.1/src/main/java/A.java"], "test=true")):
                out = os.path.join(tmp, "out.txt")
                if os.path.exists(out):
                    os.remove(out)
                subprocess.run([sys.executable, "-I", script, "build", "--files", *files], cwd=ROOT,
                               env=dict(os.environ, GITHUB_OUTPUT=out), capture_output=True, text=True, check=True)
                with open(out, encoding="utf-8") as fh:
                    self.assertIn(expected, fh.read(), files)


class RealCommits(unittest.TestCase):
    def test_status_only_commit(self):
        self.assertEqual(pm.plan_build(git_diff("fced053^", "fced053")), [])

    def test_docs_only_commit(self):
        self.assertEqual(pm.plan_build(git_diff("905a90a^", "905a90a")), [])

    def test_branding_and_ci_script_commit(self):
        self.assertEqual(pm.plan_build(git_diff("88959e9^", "88959e9")), [])

    def test_python_check_scripts_and_docs_commit(self):
        self.assertEqual(pm.plan_build(git_diff("456f97a^", "456f97a")), [])

    def test_four_forge_modules_commit(self):
        self.assertEqual(names(pm.plan_build(git_diff("57b53b1^", "57b53b1"))),
                         ["forge-1.21.1", "forge-26.1.2", "forge-26.2", "forge-26.3"])

    def test_version_code_commit(self):
        self.assertEqual(names(pm.plan_build(git_diff("a289033^", "a289033"))), EVERY)

    def test_common_commit(self):
        self.assertEqual(names(pm.plan_build(git_diff("0d5fab2^", "0d5fab2"))), EVERY)


class ReleaseMode(unittest.TestCase):
    def make_repo(self, versions):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        for module, _ in pm.ALL:
            os.makedirs(os.path.join(tmp.name, module))
            with open(os.path.join(tmp.name, module, "gradle.properties"), "w", encoding="utf-8") as fh:
                fh.write(f"mod_id=orangepeel\nmod_version={versions.get(module, '2026.11.1')}\n")
        return tmp.name

    def test_only_minecraft_versions_at_the_tag(self):
        root = self.make_repo({m: "2026.11.2" for m in both("26.3")})
        version, picked, problems = pm.plan_release("v2026.11.2", root)
        self.assertEqual((version, names(picked), problems), ("2026.11.2", both("26.3"), []))

    def test_all_modules_at_the_tag_release_everything(self):
        root = self.make_repo({})
        version, picked, problems = pm.plan_release("v2026.11.1", root)
        self.assertEqual((names(picked), problems), (EVERY, []))

    def test_one_loader_at_the_tag_is_a_problem(self):
        root = self.make_repo({"forge-26.2": "2026.11.5"})
        version, picked, problems = pm.plan_release("v2026.11.5", root)
        self.assertEqual(names(picked), ["forge-26.2"])
        self.assertEqual(len(problems), 1)
        self.assertIn("26.2", problems[0])

    def test_tag_nobody_carries_selects_nothing(self):
        root = self.make_repo({})
        self.assertEqual(pm.plan_release("v2026.11.9", root)[1], [])

    def test_manual_run_uses_the_newest_version(self):
        root = self.make_repo({"forge-26.2": "2026.11.10", "neoforge-26.2": "2026.11.10", "forge-26.3": "2026.11.9"})
        version, picked, _ = pm.plan_release("main", root)
        self.assertEqual((version, names(picked)), ("2026.11.10", both("26.2")))

    def test_real_repo_current_version(self):
        current = pm.module_version("forge-26.3", ROOT)
        if current is None:
            self.skipTest("forge-26.3 is not in this checkout")
        version, picked, problems = pm.plan_release("v" + current, ROOT)
        self.assertIn("forge-26.3", names(picked))
        self.assertEqual(problems, [])


if __name__ == "__main__":
    unittest.main()
