# Project Status
<!-- status-format: 1 -->
Updated: 2026-11-05
Summary: Food HUD and tooltip mod with a meal comparator; all eight Forge and NeoForge layers compile and pass unit tests, server and client checks are in progress.
Spend: none

## Foundation
- [x] Layout: shared common/ plus per-version version-<mc>/ and eight loader layers (w:3)
- [x] Gradle builds for NeoForge and Forge on 1.21.1, 26.1.2, 26.2 and 26.3 (w:4)
- [x] Combined jar per Minecraft version (scripts/combine_jars.py, scripts/build_all.sh) (w:2)
- [ ] GitHub repository (w:1)

## Features
- [x] Food data read from the game, modded foods included (w:3)
- [~] Hunger bar preview, saturation overlay, health regen preview (w:4)
- [ ] Exhaustion underlay fed by a server sync (w:3)
- [x] Food tooltip with hunger, saturation, effects and waste warning (w:3)
- [x] Meal comparator: rank against inventory, delta bar, best-food flag (w:4)
- [x] Fullness timer line (w:1)
- [x] Config file: opacity, offset, switches per overlay and tooltip line (w:2)
- [x] API for other mods to add tooltip lines (w:2)

## Testing and release
- [x] Unit tests for food math, comparator and tooltip lines (w:3)
- [~] Unit tests run in CI on every layer (workflow written, not run yet: no remote) (w:1)
- [~] Headless server load check on every layer (w:3)
- [ ] Client check with screenshots on every layer (w:4)
- [x] build.yml and release.yml with dry_run input (w:2)
- [x] CurseForge project created, id 1784834 recorded in the upload script (w:1)
- [ ] First release tagged and uploaded (w:2)

## Documentation and branding
- [x] Icon, banners and section headers drawn in the SMWTI style (w:2)
- [~] README, CHANGELOG, CurseForge description and listing kit (w:2)
- [x] Issue templates (w:1)
- [~] Wiki pages written for itmecasey.com (w:2)
- [ ] Wiki deployed and checked live (w:1)
