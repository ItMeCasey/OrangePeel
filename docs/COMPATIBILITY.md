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
| JEI | Drew graphics | Drew graphics | Drew graphics | Drew graphics, in a real NeoForge 26.3.0.101 install with JEI 26.3, hovered by hand (frame `real_neoforge-26.3_jei_hover_by_hand.png`: shanks, saturation and comparison bar under the text lines) | 1.21.1: drew graphics; 26.x: no upstream build |
| REI | Drew graphics | Drew graphics | Crashed: REI 26.2.820 throws `IllegalAccessError` in its own menu code (upstream; no newer 26.2 build) | Drew graphics, in a real NeoForge 26.3.0.101 install with REI 26.3.824, Architectury 22.0.5 and Cloth Config 26.3.159, hovered by hand (frame `real_neoforge-26.3_rei_hover_by_hand.png`: Golden Carrot with shanks and the comparison bar) | No upstream build |
| EMI | Drew graphics | No upstream build | No upstream build | No upstream build | No upstream build |
| BEI | Works from BEI 2026.11.4 or newer (BEI's own version number); the released BEI 2026.11.3 and older draw the text only. Checked by the BEI project in its own browser, recipe popup and planner on NeoForge and Forge, 1.21.1, 26.1.2, 26.2 and 26.3. Also seen here: BEI 2026.11.15 on NeoForge 26.3 in a real install, hovered by hand (frame `real_neoforge-26.3_bei_hover_by_hand.png`: Baked Potato with shanks, saturation and the comparison bar, every value once). | | | | |
| SMWTI | Nothing to show: OrangePeel adds no blocks or entities | | | | |
| Jade | Nothing to show (no blocks or entities); builds exist on NeoForge only | | | | |

## What was wrong with the 26.3 test window

On NeoForge 26.3 the test client's window flipped between 1904x1041 and 1920x1080 whenever the tour typed a search. The cause is
in the game: `key.fullscreen` defaults to the D key (key code 68, read from the 26.3 `Options` bytecode), so the D in "golden"
toggled fullscreen and rebuilt the open screen. The tour now sends no key press for D. The same flips happen in a real install
(NeoForge installer, launched from its version JSON), so it was never the dev client. With the flips gone, typing into JEI 26.3's
search box through the tour still does not take (cause not found), so the JEI 26.3 tooltip was confirmed by hand in the real
install instead (the tour's own frames `real_neoforge-26.3_jei_viewer_jei_*.png` show the empty search box and a non-food, so they are not evidence). REI 26.3 was confirmed by hand in the same real install (typing through the tour garbled its search text, so the hover was done by hand).

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
