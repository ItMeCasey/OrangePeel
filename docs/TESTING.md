# What has been tested

Checked on 2026-11-05. "Seen" means a screenshot from a real game client that was opened and looked at.

| Layer | Compiled | Unit tests (26) | Dedicated server loads | Client tour (HUD and tooltip frames seen) |
|---|---|---|---|---|
| NeoForge 1.21.1 | yes | pass | yes | yes |
| Forge 1.21.1 | yes | pass | yes | yes |
| NeoForge 26.1.2 | yes | pass | yes | yes |
| Forge 26.1.2 | yes | pass | yes | yes |
| NeoForge 26.2 | yes | pass | yes | yes |
| Forge 26.2 | yes | pass | yes | yes |
| NeoForge 26.3 | yes | pass | yes | yes |
| Forge 26.3 | yes | pass | yes | yes |

- **Compiled and unit tested:** `scripts/build_all.sh` builds every layer, runs the unit tests (food math, comparator, tooltip
  lines) and combines the jars.
- **Server:** `python scripts/server_check.py all` starts each layer on a headless dedicated server on its own port
  (8950 to 8957) and requires the mod's "OrangePeel loaded" log line and no mod-loading errors.
- **Client tour:** `python scripts/client_tour.py <layer>` runs a real client in a fresh survival world, sets hunger and
  health, selects foods in the hotbar and saves screenshots. It checks the hunger bar preview, saturation rails, regeneration
  hearts and the drawn tooltip. Cropped, zoomed proof sheets for each layer are in `docs/frames`.

`scripts/server_check.py` never accepts the Minecraft EULA: a layer's `run/eula.txt` must already say `eula=true`, or the layer is
skipped. (The eight run folders on this PC were written before that change.)

The tour starts the game through `launch-isolated.ps1` on a hidden Windows desktop, so it cannot touch the desktop, focus or
cursor of the person using the PC. `scripts/client_tour.py --viewer <name> --deps <coordinates> <layer>` runs the item viewer check.

## What the client tour does not cover

- The tooltip is drawn at a fixed spot by the tour (inside the inventory screen on 26.x), not by hovering a slot with a mouse.
- Item viewers: run on NeoForge (JEI 1.21.1 to 26.3, REI 1.21.1, 26.1.2 and 26.3, EMI 1.21.1, BEI) and JEI on Forge 1.21.1. See
  [COMPATIBILITY.md](COMPATIBILITY.md) for each cell. The 26.3 JEI, REI and BEI checks were done by hand in a real NeoForge
  install (`scripts/real_install.py`, `scripts/real_check.py`); the tour's own frames there are not evidence.
- The exhaustion underlay needs OrangePeel on the server. Its packet was seen reaching the client in singleplayer on all eight
  layers (frames `*_hud_exhaustion_*.png`), and every layer passes the server self test (it reads exhaustion from the game's
  own food data). A client joining a separate dedicated server was not run.
- Multiplayer: not tested. OrangePeel is client side.
- Other mods' foods: seen with Croptopia 4.3.2 on NeoForge 26.3 in a real install (`real_neoforge-26.3_modded_food_croptopia.png`:
  Apple Pie and Anchovy Pizza with their own hunger, saturation, hearts and comparison bar). Not run on the other layers.

## Exhaustion underlay on a dedicated server

`python scripts/dedicated_check.py` starts the NeoForge 1.21.1 dedicated server in `neoforge-1.21.1/run-server` (port 8950, offline
mode, the test player is an operator) and one client that joins it, asks for the hunger effect and shoots the hunger bar. Seen on
2026-11-09: the server log has `Dev joined the game`, the client log has `OrangePeel: received the first exhaustion update from
the server`, and `docs/frames/dedicated_neoforge-1.21.1_join_before.png` (no veil) against `join_exhaustion_a` and `_c` (the veil
grows) show it. The EULA was accepted by Casey for this test server only; `run-server` and `eula.txt` are git-ignored. Only
NeoForge 1.21.1 was run this way; the other layers use the same server code and were seen through the integrated server.

## Round 2026.11.4 (ModJS scripting, advice lines removed)

- **Unit tests:** 42 on NeoForge 1.21.1 and Forge 1.21.1, including six that run real JavaScript through Rhino against a ModJS
  stand-in (`ScriptApiTest`) and one that checks the tooltip gives no advice lines.
- **Server self test** (`scripts/server_check.py`, Gradle through the slot, prints `SELFTEST DONE failures=0`): NeoForge 26.2 and
  Forge 26.3 passed. The other layers were not run for this round.
- **Tooltip frames after removing "Best pick for your hunger right now" and "Less waste right now":** NeoForge 1.21.1 and Forge
  1.21.1 (`docs/frames/*_hints_tooltip_*.png`: steak when hungry, bread and golden apple when nearly full). The names, hunger and
  saturation rows, the ranking line, the waste warning, effects and the fullness time remain; the two advice lines are gone.
- **Not run:** the `OrangePeel` global with the real ModJS jar in a game.

