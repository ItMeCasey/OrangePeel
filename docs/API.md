# OrangePeel API

Other mods can add lines to the food tooltip. The API is client side and lives in the `com.itmecasey.orangepeel.api` package.

```java
import com.itmecasey.orangepeel.api.OrangePeelApi;
import net.minecraft.network.chat.Component;

// During client setup:
OrangePeelApi.registerTooltipProvider((stack, food, lines) ->
        lines.add(Component.literal("Tastes like " + food.name())));
```

- `stack` is the item being shown.
- `food` is OrangePeel's reading of that stack: `id()`, `name()`, `nutrition()` (hunger points, two per shank),
  `saturation()` (absolute saturation points), `eatTicks()` and `effects()`.
- `lines` is the tooltip's line list. Append to it; do not remove or reorder lines that are already there.

Providers run only for items that are foods, after OrangePeel's own lines, once per tooltip draw. Keep them cheap.

OrangePeel reads food from the game's own data components, so a modded food shows up without any call to this API.
Register a provider only when you want to add information of your own.

To depend on the API, compile against the OrangePeel jar for your Minecraft version and loader and mark the
dependency optional in your mod metadata.
