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
- Item viewers: run on NeoForge only, and not on every version. See [COMPATIBILITY.md](COMPATIBILITY.md) for what each
  viewer drew on each version and why some cells were not run. No viewer was run on Forge.
- The exhaustion underlay needs OrangePeel on the server. Its packet was seen reaching the client in singleplayer on all eight
  layers (frames `*_hud_exhaustion_*.png`), and every layer passes the server self test (it reads exhaustion from the game's
  own food data). A client joining a separate dedicated server was not run.
- Multiplayer: not tested. OrangePeel is client side.
- Other mods' foods: the code reads the game's own food data, but only vanilla foods were run.
