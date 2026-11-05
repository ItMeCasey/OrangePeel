# OrangePeel

A food HUD and tooltip for Forge and NeoForge. Hold a food and the hunger bar previews what it restores; hover a food and the
tooltip ranks it against the other food in your inventory.

## Features

- **Hunger bar preview:** a pulsing preview of the hunger the held food restores, a saturation strip under the shanks, and a
  preview of the health regeneration the food adds.
- **Food tooltip:** hunger and saturation, effect names with levels and durations, an orange warning when hunger would be
  wasted, and how long the food keeps you full.
- **Meal comparator:** hover a food to see how it ranks against the other foods you carry ("Better than 4 of your other 5
  foods"), with a bar showing how far it is from your best food, a flag on the best one, and a note when another food fits
  your hunger better right now.
- **Modded foods work:** food values are read from the game's own food data, so no per-mod support is needed.
- **Config:** `config/orangepeel-client.properties` sets HUD opacity and offset and turns each overlay and tooltip line on or off.
- **API:** other mods can add tooltip lines. See [docs/API.md](docs/API.md).

## Versions and loaders

One combined jar per Minecraft version works on Forge and NeoForge: 1.21.1, 26.1.2, 26.2 and 26.3. Fabric is not supported.
OrangePeel is client side; it also loads on a dedicated server and does nothing there.

## Tested

See [PROJECT_STATUS.md](PROJECT_STATUS.md) and the table in [docs/TESTING.md](docs/TESTING.md) for exactly what has been built,
unit tested, loaded on a server and seen in a game client, per loader and version.

## Known gaps

- **The exhaustion underlay is not active yet.** The game never sends exhaustion to the client, so it needs OrangePeel on the
  server too, and that sync is not built. Until it is, the exhaustion underlay stays hidden and the health and fullness
  estimates assume no exhaustion.
- The meal comparator ranks by hunger plus saturation. It does not try to guess what you would rather eat.
- The pictures in the listing are drawings, not game screenshots.

## Building

Each layer is its own Gradle build (`neoforge-1.21.1`, `forge-26.2`, and so on) over the shared `common/` and `version-<mc>/`
sources. `scripts/build_all.sh` builds and tests every layer and combines the jars into `dist/`.
`python scripts/server_check.py all` loads each layer on a headless dedicated server.

## License

MIT. See [LICENSE](LICENSE).
