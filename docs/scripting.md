# Scripting (ModJS)

If [ModJS](https://github.com/ItMeCasey/ModJS) is installed, OrangePeel adds a global `OrangePeel` to its **client scripts**. Without
ModJS nothing is loaded. The global is not available in server or startup scripts, because OrangePeel runs on the client.

A script that throws is logged once as a plain line and skipped; it never crashes the game.

## Events

| Call | What it does |
|---|---|
| `OrangePeel.onFoodTooltip(fn)` | Adds lines to the tooltip of every food. `fn(food)` returns a string, an array of strings, or nothing. It runs on the client thread each time a food tooltip is built, so keep it quick. |

## Reading state

| Call | Returns |
|---|---|
| `OrangePeel.player()` | The local player: `foodLevel()`, `saturation()`, `exhaustion()`, `health()`, `maxHealth()`, `absorption()`, `hudVisible()`. `null` outside a world. |
| `OrangePeel.heldFood()` | The food in the player's hands as a food object, or `null`. |
| `OrangePeel.carriedFoods()` | The foods in the inventory, one food object per stack. |
| `OrangePeel.ranking()` | How the held food ranks against the carried foods (a ranking object), or `null` when no food is held. |
| `OrangePeel.ranking(food)` | The same for any food object. |
| `OrangePeel.wasted(food)` | Hunger points of that food that would be wasted if eaten now. |

A **food object** has `id()`, `name()`, `nutrition()` (hunger points, 2 per shank), `saturation()`, `eatTicks()`, `effects()` and
`score()` (nutrition plus saturation). Each effect has `name()`, `amplifier()`, `durationTicks()`, `chance()` and `beneficial()`.

A **ranking object** has `others()` (how many other kinds of food are carried), `betterThan()`, `best()` (true when nothing
carried scores higher), `bestFood()`, `fractionOfBest()` (0 to 1) and `deltaVsBest()` (0 or negative, in points).

## Settings

| Call | What it does |
|---|---|
| `OrangePeel.configNames()` | The setting names, the same keys as `config/orangepeel-client.properties`. |
| `OrangePeel.config(name)` | A setting's current value (a number or true/false). |
| `OrangePeel.setConfig(name, value)` | Changes a setting until the game closes. The config file is not changed. `hudOpacity` is clamped to 0 to 100. |

An unknown name or a value of the wrong kind throws, so use `try`/`catch` if the name comes from data.

## Example

`kubejs`-style file in `client_scripts`:

```js
// Mark foods that fill at least four shanks, and say how well the held food compares.
OrangePeel.onFoodTooltip(food => {
  const lines = [];
  if (food.nutrition() >= 8) lines.push("Hearty");
  const r = OrangePeel.ranking(food);
  if (r.others() > 0 && r.best()) lines.push("Best of what you carry");
  return lines;
});

// Make the overlays steady and half transparent.
OrangePeel.setConfig("steadyPreview", true);
OrangePeel.setConfig("hudOpacity", 50);
```

## What is not scripted

OrangePeel has no recipes, blocks or data-driven content, so there is nothing to add or replace from scripts. Food values come
from the game's own food data, which ModJS recipe and item scripts already change.
