# Changelog

## 2026.11.5

- Settings now use the loader's standard config: `orangepeel-client.toml` (everything a player sets) and `orangepeel-common.toml` (`sendExhaustion`, decided by the server), with a comment and range for every value. NeoForge adds an in-game config screen from the Mods screen.
- Migration: the first time no `.toml` exists and the old `config/orangepeel-client.properties` does, every old value is imported once. After the new files are written and checked, the old file is renamed to `orangepeel-client.properties.back` with a first line that says where and why it migrated (`.back2`, `.back3` if a backup already exists; never overwritten). A file that cannot be read completely stays where it is and the game log says why.

## 2026.11.4

- ModJS scripting (optional): client scripts get an `OrangePeel` global to add food tooltip lines, read the held food, your hunger and the meal ranking, and change settings for the session. See docs/scripting.md. Nothing changes without ModJS.
- Removed the advice lines "Best pick for your hunger right now" and "Less waste right now: <food>" from the food tooltip. The tooltip keeps names and values: hunger, saturation, the ranking, the waste warning, effects and how long the food keeps you full.

## 2026.11.3

- Exhaustion underlay: the server sends each player's exhaustion to that player's client (a one-float packet, optional on both sides, at most every 5 ticks and only when it changed), and the hunger bar draws it as a translucent veil over the shanks. The health regeneration and fullness estimates now use it. New config switch `sendExhaustion` (server side). The server self test now logs `SELFTEST DONE failures=0` and checks that exhaustion can be read.

## 2026.11.2

- Checked the food tooltip in item viewers on NeoForge. JEI (1.21.1 and 26.1.2), REI and EMI (1.21.1) draw the shanks, saturation and comparison bar; BEI draws them from BEI version 2026.11.4 or newer (BEI's own number, not OrangePeel's) (older BEI shows the text lines only). The text lines remain the fallback everywhere. See docs/COMPATIBILITY.md for every version and the cells not run.
- The development screenshot tour is no longer part of the release jars.

## 2026.11.1

First release. One combined jar per Minecraft version (1.21.1, 26.1.2, 26.2, 26.3) for Forge and NeoForge.

- Hunger bar: a pulsing preview of what the held food restores, a saturation strip under each shank, and a preview of the health regeneration the food adds.
- Food tooltip: hunger and saturation, effect names with levels and durations, an orange warning when hunger would be wasted, and how long the food keeps you full.
- Meal comparator: hover a food to see how it ranks against the other foods in your inventory, with a delta bar against your best food and a flag on the best one.
- Reads food from the game's own data, so modded foods work with no extra support.
- Config file for HUD opacity, position and every overlay and tooltip line. A small API lets other mods add tooltip lines.
- Not in this release: the exhaustion underlay needs the server to send exhaustion, which is not built yet.
