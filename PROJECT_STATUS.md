# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-14
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
- [~] Loader-standard config (.toml, NeoForge config screen) with one-time migration from the old .properties file (verified, then moved to .back) (unit tests with fixtures from each release; loader builds compiled; toml import not yet seen in a running game on every layer) (w:2)
- [x] API for other mods to add tooltip lines (w:2)

## Testing and release
- [x] Unit tests for food math, comparator and tooltip lines (w:3)
- [x] Unit tests run in CI on every layer (build.yml green on 2026-11-05) (w:1)
- [x] Headless server load check on every layer (w:3)
- [x] Client check with screenshots on every layer (w:4)
- [x] Item viewer check, every viewer that has a build: JEI draws the graphical tooltip on NeoForge 1.21.1, 26.1.2, 26.2 and 26.3 and on Forge 1.21.1 (26.3 hovered by hand in a real install, docs/frames/real_neoforge-26.3_jei_hover_by_hand.png); REI on NeoForge 1.21.1, 26.1.2 and 26.3; EMI on 1.21.1; BEI from BEI version 2026.11.4 (BEI's own number; verified by the BEI project, and BEI 2026.11.15 seen on NeoForge 26.3 in a real install). REI 26.2 crashes in its own code (upstream), and EMI, REI and the other JEI builds have no Forge or 26.x build: n/a. See docs/COMPATIBILITY.md (w:3)
- [x] JEI 26.2 viewer cell: found why typing did not land (JEI 30.x wants a real click and key sequence), fixed in the tour, JEI 26.2 draws the graphics (w:2)
- [x] Exhaustion underlay from a client on a separate dedicated server (NeoForge 1.21.1): server log `Dev joined the game`, client log `received the first exhaustion update from the server`, frames docs/frames/dedicated_neoforge-1.21.1_join_*.png show the veil growing after the server applied the hunger effect (EULA accepted by Casey for this test server, 2026-11-09; run-server and eula.txt are git-ignored) (w:2)
- [x] JEI 26.3 viewer cell: drew the graphical tooltip in a real NeoForge 26.3.0.101 install, hovered by hand (docs/frames/real_neoforge-26.3_jei_hover_by_hand.png); the window flips were the game's fullscreen key (D) (w:1)
- [x] REI 26.3 viewer cell: drew the graphical tooltip in a real NeoForge 26.3.0.101 install with REI 26.3.824, hovered by hand (docs/frames/real_neoforge-26.3_rei_hover_by_hand.png) (w:1)
- [x] BEI works from BEI version 2026.11.4 (BEI's own version number, not OrangePeel's), recorded in docs, listing and wiki (w:1)
- [x] Compatibility standard: no recipes, blocks or entities, so no SMWTI/Jade/recipe integration needed; stated in docs/COMPATIBILITY.md (w:1)
- [x] Screenshot tour classes left out of release jars (w:1)
- [x] build.yml and release.yml with dry_run input (w:2)
- [x] CurseForge project created, id 1784834 recorded in the upload script (w:1)
- [x] Release workflow dry run passed (ids resolved for all four jars) (w:1)
- [x] First release v2026.11.1 tagged: GitHub pre-release with four jars, CurseForge upload run succeeded (w:2)
- [x] CurseForge approval: the project and the 2026.11.1, 2026.11.2 and 2026.11.3 files are public (checked 2026-11-06) (w:1)

- [x] Foods from another mod: Croptopia 4.3.2 on NeoForge 26.3 in a real install shows its own values (docs/frames/real_neoforge-26.3_modded_food_croptopia.png) (w:1)

- [~] ModJS scripting support: OrangePeel global for client scripts (tooltip lines, held food, player, ranking, settings), unit tested with Rhino and a ModJS stand-in on all layers' test task via the shared tests; not yet run with the real ModJS jar in a game (w:3)
- [x] No hints: removed the best-pick advice lines from the tooltip (and the code and tests behind them; a unit test checks they are gone), the CurseForge description has no test or install text (w:1)
- [x] Tooltip frames after the hint removal on NeoForge 1.21.1 and Forge 1.21.1 (docs/frames/*_hints_tooltip_*.png); server self test on NeoForge 26.2 and Forge 26.3 passed (w:1)

## Documentation and branding
- [x] Icon, banners and section headers drawn in the SMWTI style (w:2)
- [x] README, CHANGELOG, CurseForge description and listing kit, matching the tested state (w:2)
- [x] Issue templates (w:1)
- [x] Wiki pages written, links checked (w:2)
- [x] Wiki deployed, itmecasey.com/wiki/orangepeel answers 200 (w:1)
- [x] Wiki for 2026.11.2 and 2026.11.3 deployed: every page on itmecasey.com equals the local file (checked 2026-11-09) (w:1)
