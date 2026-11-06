# Changelog

## 2026.11.2

- Checked the food tooltip in item viewers on NeoForge. JEI (1.21.1 and 26.1.2), REI and EMI (1.21.1) draw the shanks, saturation and comparison bar; BEI draws them from BEI 2026.11.4 (older BEI shows the text lines only). The text lines remain the fallback everywhere. See docs/COMPATIBILITY.md for every version and the cells not run.
- The development screenshot tour is no longer part of the release jars.

## 2026.11.1

First release. One combined jar per Minecraft version (1.21.1, 26.1.2, 26.2, 26.3) for Forge and NeoForge.

- Hunger bar: a pulsing preview of what the held food restores, a saturation strip under each shank, and a preview of the health regeneration the food adds.
- Food tooltip: hunger and saturation, effect names with levels and durations, an orange warning when hunger would be wasted, and how long the food keeps you full.
- Meal comparator: hover a food to see how it ranks against the other foods in your inventory, with a delta bar against your best food and a flag on the best one.
- Reads food from the game's own data, so modded foods work with no extra support.
- Config file for HUD opacity, position and every overlay and tooltip line. A small API lets other mods add tooltip lines.
- Not in this release: the exhaustion underlay needs the server to send exhaustion, which is not built yet.
