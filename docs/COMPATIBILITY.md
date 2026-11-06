# Compatibility with item viewers and info mods

OrangePeel adds no items, blocks, fluids, entities or recipes, so there is nothing to register with BEI, REI, JEI or EMI
(no recipe categories, catalysts or lookups) and nothing for SMWTI or Jade to show (no blocks or entities). The one thing
that matters is the **food tooltip**: each viewer draws item tooltips its own way, and OrangePeel's drawn block (shanks,
hearts, comparison bar) only appears where the viewer draws tooltip components. Where it cannot, the text lines show instead.

How it works: the text lines come through the loader's item tooltip event (so every viewer that asks the game for a
tooltip gets them). The graphic is a tooltip component added on `RenderTooltipEvent.GatherComponents`; that event also
removes the text lines the graphic replaces. A viewer that never fires the event keeps the text lines only.

No integration classes are needed, and none were added: the mod loads and works with none of these installed.

## Per version and loader

Upstream builds checked with the CurseForge API on 2026-11-05. Cells say what was seen in the frames in `docs/frames/`
(`<layer>_viewer_<viewer>_hover0.png`, a golden apple hovered in the viewer's item list with five other foods in the inventory).

- **Drew graphics** = the shanks, saturation shanks and comparison bar were drawn in the viewer's tooltip.
- **Text only** = the viewer showed our text lines but no graphics.
- **Not tested** = a build exists but no usable frame; the reason is given.

| Viewer | NeoForge 1.21.1 | NeoForge 26.1.2 | NeoForge 26.2 | NeoForge 26.3 | Forge |
|---|---|---|---|---|---|
| JEI | Drew graphics | Drew graphics | Not tested (typing into JEI's search box did not take on two tries, so no food was hovered; the 26.2 frames in `docs/frames` show a non-food and are not evidence) | Not tested (same; one try) | 1.21.1: drew graphics; 26.x: no upstream build |
| REI | Drew graphics | Drew graphics (after the tour fix) | Crashed: REI 26.2.820 throws `IllegalAccessError` in its own menu code (upstream) | No usable build: REI 26.3 needs Cloth Config 26.3, which has no build | No upstream build |
| EMI | Drew graphics | No upstream build | No upstream build | No upstream build | No upstream build |
| BEI | Works from BEI 2026.11.4; the released BEI 2026.11.3 and older draw the text only. Checked by the BEI project in its own browser, recipe popup and planner on NeoForge and Forge, 1.21.1, 26.1.2, 26.2 and 26.3; not rerun here. | | | | |
| SMWTI | Nothing to show: OrangePeel adds no blocks or entities | | | | |
| Jade | Nothing to show (no blocks or entities); builds exist on NeoForge only | | | | |

## Known upstream issues

- **REI 26.2 crashes** when it renders its menu (`GuiGraphics.withFreshScissorStack`, an access error against NeoForge 26.2);
  this is in REI itself.
- **REI 26.3** cannot load without Cloth Config 26.3, which does not exist yet.
- Backing frames in `docs/frames/` exist only for the JEI, REI and EMI cells marked above; BEI was verified by the BEI project, and empty cells were not rerun to fill them.

## How the viewer check works (development only)

```
python scripts/client_tour.py --viewer <name> --deps <curse.maven:...,...> <layer>
scripts/viewer_check.sh <layer> <viewer> <deps> "<search box x,y>" "golden apple" "<first result x,y>"
```

`-Pviewers=` in each layer's `build.gradle` adds the given `curse.maven:` coordinates (or `file:<path>` for a local jar) to
the run classpath only; no release build or CI job uses it. The tour opens the inventory, clicks the viewer's search box,
types a food name through the game's own keyboard handler and hovers the first result. The tour classes (`ShotTour`,
`UiInput`) are excluded from the release jars.
