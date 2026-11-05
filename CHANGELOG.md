# Changelog

## 2026.11.1

First release. One combined jar per Minecraft version (1.21.1, 26.1.2, 26.2, 26.3) for Forge and NeoForge.

- Hunger bar: a pulsing preview of what the held food restores, a saturation strip under each shank, and a preview of the health regeneration the food adds.
- Food tooltip: hunger and saturation, effect names with levels and durations, an orange warning when hunger would be wasted, and how long the food keeps you full.
- Meal comparator: hover a food to see how it ranks against the other foods in your inventory, with a delta bar against your best food and a flag on the best one.
- Reads food from the game's own data, so modded foods work with no extra support.
- Config file for HUD opacity, position and every overlay and tooltip line. A small API lets other mods add tooltip lines.
- Not in this release: the exhaustion underlay needs the server to send exhaustion, which is not built yet.
