# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-09
Summary: Food HUD and tooltip mod with a meal comparator and an exhaustion underlay; 2026.11.3 is released and public on CurseForge for 1.21.1, 26.1.2, 26.2 and 26.3.
Spend: none

## Foundation
- [x] Layout: shared common/ plus per-version version-<mc>/ and eight loader layers (w:3)
- [x] Gradle builds for NeoForge and Forge on 1.21.1, 26.1.2, 26.2 and 26.3 (w:4)
- [x] Combined jar per Minecraft version (scripts/combine_jars.py, scripts/build_all.sh) (w:2)
- [x] GitHub repository ItMeCasey/OrangePeel, public, CI on every push (w:1)

## Features
- [x] Food data read from the game, modded foods included (w:3)
- [x] Hunger bar preview, saturation rails, health regen preview, seen on all eight layers (w:4)
- [x] Exhaustion underlay fed by a server sync (packet, throttle, veil, config switch, unit tests, server self test on all eight layers; seen in a client on all eight layers) (w:3)
- [x] Exhaustion underlay server side without a remote client: 5 unit tests of what a dedicated server does each tick (ExhaustionSenderTest) plus the self test that reads exhaustion from the game's own FoodData on all eight layers; the integrated server's packet reached the client on all eight layers. A client on a separate dedicated server was not run (needs an accepted EULA) (w:2)
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
- [~] Item viewer check (NeoForge: JEI 1.21.1 (both loaders), 26.1.2 and 26.2, REI 1.21.1 and 26.1.2, EMI 1.21.1 draw the graphics; BEI works from BEI version 2026.11.4 (BEI's own version number, not OrangePeel's) (verified by the BEI project, accepted by Casey, not rerun here); JEI 26.3 not run (search box stays empty on three tries); see docs/COMPATIBILITY.md) (w:3)
- [x] JEI 26.2 viewer cell: found why typing did not land (JEI 30.x wants a real click and key sequence), fixed in the tour, JEI 26.2 draws the graphics (w:2)
- [ ] Exhaustion underlay from a client on a separate dedicated server: BLOCKED by the shared client slot (scripts/dedicated_check.py is ready; Casey accepted the EULA for this test server on 2026-11-09; the server starts and loads OrangePeel, but the launcher waited 20 minutes for a free slot and gave up; one earlier launch started a client that never wrote a log) (w:2)
- [ ] JEI 26.3 and REI 26.3 viewer cells: BLOCKED by the 26.3 test window flipping between 1904x1041 and 1920x1080 during the search (three attempts, three flips; cause not found); REI 26.3 also has a Cloth Config 26.3 build now (w:2)
- [x] BEI works from BEI version 2026.11.4 (BEI's own version number, not OrangePeel's), recorded in docs, listing and wiki (w:1)
- [x] Compatibility standard: no recipes, blocks or entities, so no SMWTI/Jade/recipe integration needed; stated in docs/COMPATIBILITY.md (w:1)
- [x] Screenshot tour classes left out of release jars (w:1)
- [x] build.yml and release.yml with dry_run input (w:2)
- [x] CurseForge project created, id 1784834 recorded in the upload script (w:1)
- [x] Release workflow dry run passed (ids resolved for all four jars) (w:1)
- [x] First release v2026.11.1 tagged: GitHub pre-release with four jars, CurseForge upload run succeeded (w:2)
- [x] CurseForge approval: the project and the 2026.11.1, 2026.11.2 and 2026.11.3 files are public (checked 2026-11-06) (w:1)

## Documentation and branding
- [x] Icon, banners and section headers drawn in the SMWTI style (w:2)
- [x] README, CHANGELOG, CurseForge description and listing kit, matching the tested state (w:2)
- [x] Issue templates (w:1)
- [x] Wiki pages written, links checked (w:2)
- [x] Wiki deployed, itmecasey.com/wiki/orangepeel answers 200 (w:1)
- [x] Wiki for 2026.11.2 and 2026.11.3 deployed: every page on itmecasey.com equals the local file (checked 2026-11-09) (w:1)
