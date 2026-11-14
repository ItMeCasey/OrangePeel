# OrangePeel

A food HUD and tooltip for Forge and NeoForge. Hold a food and the hunger bar previews what it restores; hover a food and the
tooltip ranks it against the other food in your inventory.

## Features

- **Hunger bar preview:** a pulsing preview of the hunger the held food restores, golden rails around the shanks that
  saturation covers, and a preview of the health regeneration the food adds (`steadyPreview=true` stops the pulse).
- **Food tooltip:** drawn shanks for the hunger the food restores (the part that would be wasted fades to orange), golden
  shanks for saturation, hearts for the regeneration it adds, and a drawn comparison bar, under plain-text lines with the
  numbers, effect names with levels and durations, a waste warning, and how long the food keeps you full. The text lines
  are the fallback for item viewers that cannot draw tooltip graphics; `graphicalTooltip=false` keeps text only.
- **Meal comparator:** hover a food to see how it ranks against the other foods you carry ("Better than 4 of your other 5
  foods"), with a bar showing how far it is from your best food, and a flag on the best one.
- **Scripting:** with ModJS installed, client scripts get an `OrangePeel` global: add tooltip lines, read the held food, hunger
  and ranking, and change settings (see [docs/scripting.md](docs/scripting.md)).
- **Modded foods work:** food values are read from the game's own food data, so no per-mod support is needed.
- **Config:** `config/orangepeel-client.toml` sets HUD opacity and offset and turns each overlay and tooltip line on or off (`orangepeel-common.toml` holds the server switch `sendExhaustion`); NeoForge also has an in-game config screen. Settings from the old `orangepeel-client.properties` are imported once and that file is renamed to `orangepeel-client.properties.back`.
- **API:** other mods can add tooltip lines. See [docs/API.md](docs/API.md).

## Versions and loaders

One combined jar per Minecraft version works on Forge and NeoForge: 1.21.1, 26.1.2, 26.2 and 26.3. Fabric is not supported.
OrangePeel is client side; it also loads on a dedicated server and does nothing there.

## Tested

Every layer compiles, passes the unit tests, loads on a dedicated server and was seen in a real game client (hunger bar
overlays and the tooltip). Item viewers were run on NeoForge only: JEI (1.21.1 on both loaders, 26.1.2, 26.2, 26.3), REI (1.21.1, 26.1.2, 26.3) and EMI (1.21.1) draw the graphical tooltip; BEI works from BEI version 2026.11.4 (BEI's own version number, not OrangePeel's)
(2026.11.3 and older draw the text only). Foods from another mod (Croptopia on NeoForge 26.3) show their real values in the drawn tooltip. Other viewer and version cells and Forge viewers other than JEI 1.21.1 were not run. See
[docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) for the table and [docs/TESTING.md](docs/TESTING.md) for what the checks do not cover.

## Known gaps

- **The exhaustion underlay needs OrangePeel on the server.** The game never sends exhaustion to the client, so OrangePeel
  sends it from the server in a small packet (`sendExhaustion` in the config turns that off). Without OrangePeel on the
  server the underlay stays hidden and the health and fullness estimates assume no exhaustion.
- The meal comparator ranks by hunger plus saturation. It does not try to guess what you would rather eat.
- The pictures in the listing are drawings, not game screenshots.

## Building

Each layer is its own Gradle build (`neoforge-1.21.1`, `forge-26.2`, and so on) over the shared `common/` and `version-<mc>/`
sources. `scripts/build_all.sh` builds and tests every layer and combines the jars into `dist/`.
`python scripts/server_check.py all` loads each layer on a headless dedicated server.

## License

MIT. See [LICENSE](LICENSE).
