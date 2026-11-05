# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-05
Summary: Food HUD and tooltip mod with a meal comparator; all eight Forge and NeoForge layers compile, pass unit tests, load on a server and were seen in a real client; first release and item viewer checks are partly done (see docs/COMPATIBILITY.md).
Spend: none

## Foundation
- [x] Layout: shared common/ plus per-version version-<mc>/ and eight loader layers (w:3)
- [x] Gradle builds for NeoForge and Forge on 1.21.1, 26.1.2, 26.2 and 26.3 (w:4)
- [x] Combined jar per Minecraft version (scripts/combine_jars.py, scripts/build_all.sh) (w:2)
- [ ] GitHub repository (w:1)

## Features
- [x] Food data read from the game, modded foods included (w:3)
- [x] Hunger bar preview, saturation rails, health regen preview, seen on all eight layers (w:4)
- [ ] Exhaustion underlay fed by a server sync (later, not in the first release) (w:3)
- [x] Food tooltip: drawn shanks, saturation, hearts and comparison bar plus text lines, seen on all eight layers (w:4)
- [x] Meal comparator: rank against inventory, delta bar, best-food flag (w:4)
- [x] Fullness timer line (w:1)
- [x] Config file: opacity, offset, switches per overlay and tooltip line (w:2)
- [x] API for other mods to add tooltip lines (w:2)

## Testing and release
- [x] Unit tests for food math, comparator and tooltip lines (w:3)
- [~] Unit tests run in CI on every layer (workflow written, not run yet: no remote) (w:1)
- [x] Headless server load check on every layer (w:3)
- [x] Client check with screenshots on every layer (w:4)
- [~] Item viewer check (NeoForge: JEI 1.21.1 and 26.1.2, REI and EMI 1.21.1 draw the graphics; released BEI text only; JEI 26.2/26.3, REI 26.1.2 and Forge viewers not run; see docs/COMPATIBILITY.md) (w:3)
- [ ] Rerun the not-tested viewer cells (JEI 26.2/26.3, REI 26.1.2, Forge JEI 1.21.1 and BEI) after the search-typing step is fixed (w:2)
- [ ] BEI 2026.11.3: drop the text lines the graphic replaces (BEI task T-029), then recheck (w:1)
- [x] Compatibility standard: no recipes, blocks or entities, so no SMWTI/Jade/recipe integration needed; stated in docs/COMPATIBILITY.md (w:1)
- [x] Screenshot tour classes left out of release jars (w:1)
- [x] build.yml and release.yml with dry_run input (w:2)
- [x] CurseForge project created, id 1784834 recorded in the upload script (w:1)
- [x] Release workflow dry run passed (ids resolved for all four jars) (w:1)
- [ ] First release tagged and uploaded (w:2)

## Documentation and branding
- [x] Icon, banners and section headers drawn in the SMWTI style (w:2)
- [x] README, CHANGELOG, CurseForge description and listing kit, matching the tested state (w:2)
- [x] Issue templates (w:1)
- [x] Wiki pages written in the site folder, links checked, not committed or deployed (w:2)
- [ ] Wiki deployed and checked live (w:1)
