# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-06
Summary: Food HUD and tooltip mod with a meal comparator; 2026.11.1 is released (GitHub, CurseForge file waiting for approval); 2026.11.2 (viewer checks, BEI note) and the exhaustion underlay are next.
Spend: none

## Foundation
- [x] Layout: shared common/ plus per-version version-<mc>/ and eight loader layers (w:3)
- [x] Gradle builds for NeoForge and Forge on 1.21.1, 26.1.2, 26.2 and 26.3 (w:4)
- [x] Combined jar per Minecraft version (scripts/combine_jars.py, scripts/build_all.sh) (w:2)
- [x] GitHub repository ItMeCasey/OrangePeel, public, CI on every push (w:1)

## Features
- [x] Food data read from the game, modded foods included (w:3)
- [x] Hunger bar preview, saturation rails, health regen preview, seen on all eight layers (w:4)
- [x] Exhaustion underlay fed by a server sync (packet, throttle, veil, config switch, unit tests, server self test on all eight layers; seen in a client on NeoForge and Forge 1.21.1, NeoForge 26.1.2, Forge 26.3) (w:3)
- [ ] Exhaustion underlay seen in a client on the other four layers, and from a client on a dedicated server (w:2)
- [x] Food tooltip: drawn shanks, saturation, hearts and comparison bar plus text lines, seen on all eight layers (w:4)
- [x] Meal comparator: rank against inventory, delta bar, best-food flag (w:4)
- [x] Fullness timer line (w:1)
- [x] Config file: opacity, offset, switches per overlay and tooltip line (w:2)
- [x] API for other mods to add tooltip lines (w:2)

## Testing and release
- [x] Unit tests for food math, comparator and tooltip lines (w:3)
- [x] Unit tests run in CI on every layer (build.yml green on 2026-11-05) (w:1)
- [x] Headless server load check on every layer (w:3)
- [x] Client check with screenshots on every layer (w:4)
- [~] Item viewer check (NeoForge: JEI 1.21.1 (both loaders) and 26.1.2, REI 1.21.1 and 26.1.2, EMI 1.21.1 draw the graphics; BEI works from 2026.11.4 (verified by the BEI project, accepted by Casey, not rerun here); JEI 26.2/26.3 not run (search typing fails on 26.2); see docs/COMPATIBILITY.md) (w:3)
- [ ] JEI 26.2 and 26.3 viewer cells: find why typing into JEI's search box does not land (read JEI's code), then one launch each (w:2)
- [x] BEI works from 2026.11.4, recorded in docs, listing and wiki (w:1)
- [x] Compatibility standard: no recipes, blocks or entities, so no SMWTI/Jade/recipe integration needed; stated in docs/COMPATIBILITY.md (w:1)
- [x] Screenshot tour classes left out of release jars (w:1)
- [x] build.yml and release.yml with dry_run input (w:2)
- [x] CurseForge project created, id 1784834 recorded in the upload script (w:1)
- [x] Release workflow dry run passed (ids resolved for all four jars) (w:1)
- [x] First release v2026.11.1 tagged: GitHub pre-release with four jars, CurseForge upload run succeeded (w:2)
- [ ] CurseForge approval of the 2026.11.1 files (Casey; no file is public yet) (w:1)

## Documentation and branding
- [x] Icon, banners and section headers drawn in the SMWTI style (w:2)
- [x] README, CHANGELOG, CurseForge description and listing kit, matching the tested state (w:2)
- [x] Issue templates (w:1)
- [x] Wiki pages written, links checked (w:2)
- [x] Wiki deployed, itmecasey.com/wiki/orangepeel answers 200 (w:1)
- [ ] Wiki updated for 2026.11.2 and deployed (the project manager deploys) (w:1)
