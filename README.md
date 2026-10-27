# StrayArmor

![Minecraft](https://img.shields.io/badge/minecraft-1.21%20to%201.21.11%20%2B%20126.1.2-blue)
![Fabric](https://img.shields.io/badge/fabric-loader%20%7C%20fabric%20api-orange)
![ModMenu](https://img.shields.io/badge/modmenu-supported-green)
![YACL](https://img.shields.io/badge/yet%20another%20config%20lib-blue)

A client-side Fabric mod that tints player armor by the StrayTags category of the player wearing it:

- **Own** - green
- **Ally** - blue
- **Enemy** - red
- **Neutral** - uncolored (normal armor) by default
- Anything that does not map to one of those groups is left alone.

StrayArmor reads `config/straytags.json` directly, so it does not need a compile-time dependency on StrayTags. It works with StrayTags 1.21, 1.21.4, 1.21.8, 1.21.11, and 26.1.2, and is built for the same five versions.

## Install

1. Install Fabric Loader and Fabric API for your Minecraft version.
2. Install StrayTags.
3. Install ModMenu and YetAnother Config Lib (YACL) for the in-game config screen.
4. Drop the matching `strayarmor-<minecraft version>-0.1.0.jar` into `mods/`.
5. Launch the game once. The mod writes its own config and installs the bundled Valor StrayTags config (see below).

## Valor config install

On first launch, with `installStrayTagsConfig` enabled (the default), StrayArmor writes the bundled config to `config/straytags.json`:

- A new **Own** category at the top containing the `Valor` and `Valor2` clans.
- `Valor` and `Valor2` removed from the **Allied** category.
- The existing Latte, Elysium, Enemy, and Neutral categories kept.
- A matching Own category added to `__default__` so Valor is own on other whitelisted servers too.

The previous `straytags.json` is copied to `config/straytags.json.strayarmor.bak` before it is replaced. If StrayTags is present, its config is reloaded immediately.

To apply the bundled config again later, run:

```text
/strayarmor setup
```

To stop StrayArmor from ever writing the StrayTags config, set `"installStrayTagsConfig": false` in `config/strayarmor.json`.

## StrayTags import code

The import code below is the `stray.gg` server config with Valor as Own. Paste it into StrayTags' Import field and save:

```text
ST2:eyJjYXRlZ29yaWVzIjpbeyJpZCI6IjdjMWE4ZTQyLTlmM2ItNGQ2ZS04YTUxLTJiN2M5ZDBlNGYxMyIsIm5hbWUiOiI8Z3JhZGllbnQ6IzAwZmY2NjojMDBjYzU1OiNiNmZmY2M6IzAwY2M1NTojMDBmZjY2PuKLhOC8uuKlvOKAlOKAky0tLeKAk+KAlOKlveC8u+KLhCA8Yj5Pd248L2I+IOKLhOC8uuKlvOKAlOKAky0tLeKAk+KAlOKlveC8u+KLhDwvZ3JhZGllbnQ+IiwiZm9ybWF0IjoiPGNvbG9yOiMwMGZmNjY+PGJvbGQ+T1dOPC9ib2xkPjwvY29sb3I6IzAwZmY2Nj4gPGdyYXk+JVVzZXJuYW1lcyUgWzxjb2xvcjojMDBmZjY2PiVDbGFucyU8L2NvbG9yOiMwMGZmNjY+XSIsImdyb3VwVmFsdWVzIjp7IkNsYW5zIjpbIlZhbG9yIiwiVmFsb3IyIl0sIlVzZXJuYW1lcyI6W119LCJtYXRjaFByaW9yaXR5IjpbXSwic2VydmVyRmlsdGVycyI6W119LHsiaWQiOiJlNWU1MmVjYy1mZThhLTRmMTktOWRkYi00NmUwOTM4YjZjYWUiLCJuYW1lIjoiPGdyYWRpZW50OiM2YjQyMjY6I2Q0YTM3MzojZjZlN2Q3OiNkNGEzNzM6IzZiNDIyNj7ii4TgvLripbzigJTigJMtLS3igJPigJTipb3gvLvii4QgPGI+TGF0dGU8L2I+IOKLhOC8uuKlvOKAlOKAky0tLeKAk+KAlOKlveC8u+KLhDwvZ3JhZGllbnQ+IiwiZm9ybWF0IjoiPGNvbG9yOiMwMGZmMDA+PGJvbGQ+TEFUVEU8L2JvbGQ+PC9jb2xvcjojMDBmZjAwPiA8Z3JheT4lVXNlcm5hbWVzJSBbPGNvbG9yOiMwMGZmMDA+JUNsYW5zJTwvY29sb3I6IzAwZmYwMD5dIiwiZ3JvdXBWYWx1ZXMiOnsiQ2xhbnMiOlsiTGF0dGUiLCJMYXR0ZTIiXSwiVXNlcm5hbWVzIjpbIkNvbHVzaXRlIiwiS3lsYXoiLCJZb2UiLCJLbnVjaW9yIl19LCJtYXRjaFByaW9yaXR5IjpbXSwic2VydmVyRmlsdGVycyI6W119LHsiaWQiOiI2ZjEzMWU3Ny1iOTkyLTQ2ZmUtODg1ZC00ZmI0YWI1ZjkxNjUiLCJuYW1lIjoiPGdyYWRpZW50OiMwZjhmMmY6IzFlZDc2MDojYjZmZmNjOiMxZWQ3NjA6IzBmOGYyZj7ii4TgvLripbzigJTigJMtLS3igJPigJTipb3gvLvii4QgPGI+QWxsaWVkPC9iPiDii4TgvLripbzigJTigJMtLS3igJPigJTipb3gvLvii4Q8L2dyYWRpZW50PiIsImZvcm1hdCI6Ijxjb2xvcjojMDBmZjAwPjxib2xkPkFMTFk8L2JvbGQ+PC9jb2xvcjojMDBmZjAwPiA8Z3JheT4lVXNlcm5hbWVzJSBbPGNvbG9yOiMwMGZmMDA+JUNsYW5zJTwvY29sb3I6IzAwZmYwMD5dIiwiZ3JvdXBWYWx1ZXMiOnsiQ2xhbnMiOlsiUkVWT0wiLCJSRVZPTFVUSU9OIiwiU2VyZW5pdHkiLCJLbmlnaHQiLCJLbmlnaHQyIiwiS25pZ2h0cyIsIktuaWdodHMyIiwiTGVnaW9uS25pZ2h0cyIsIkFic29sdXQiLCIxMDEiLCJCZXN0IiwiU2FrdXJhIiwiRlVOIl0sIlVzZXJuYW1lcyI6WyJXYXNodXAiLCJTbm93eUF2aSIsInZocm8iLCJTaW1wbHlBUGFuY2FrZSIsIl9WYWx6eiJdfSwibWF0Y2hQcmlvcml0eSI6W10sInNlcnZlckZpbHRlcnMiOltdfSx7ImlkIjoiMTNlOTFiZmItMzI1OC00NjBhLWJjNGUtYTRiMzI4NWJjMzc5IiwibmFtZSI6IjxncmFkaWVudDojMDAxYTk5OiMwMDNjZmY6Izk5YmJmZjojMDAzY2ZmOiMwMDFhOTk+4ouE4Ly64qW84oCU4oCTLS0t4oCT4oCU4qW94Ly74ouEIDxiPkVseXNpdW08L2I+IOKLhOC8uuKlvOKAlOKAky0tLeKAk+KAlOKlveC8u+KLhDwvZ3JhZGllbnQ+IiwiZm9ybWF0IjoiPGNvbG9yOiNmZjAwMDA+PGJvbGQ+RU5FTVk8L2JvbGQ+PC9jb2xvcjojZmYwMDAwPiA8Z3JheT4lVXNlcm5hbWVzJSBbPGNvbG9yOiNmZjAwMDA+JUNsYW5zJTwvY29sb3I6I2ZmMDAwMD5dIiwiZ3JvdXBWYWx1ZXMiOnsiQ2xhbnMiOlsiRWx5c2l1bSIsIkVseXNpdW1OQSIsIkVseXNpdW1BUyIsIkVseXNpdW0yIl19LCJtYXRjaFByaW9yaXR5IjpbXSwic2VydmVyRmlsdGVycyI6WyJldSJdfSx7ImlkIjoiMjhhMTU4ZTAtODJlMi00ZjlkLTgzYjYtMzhlMzhkYjgxMGQ2IiwibmFtZSI6IjxncmFkaWVudDojNjYwMDAwOiNmZjFhMWE6I2ZmODA4MDojZmYxYTFhOiM2NjAwMDA+4ouE4Ly64qW84oCU4oCTLS0t4oCT4oCU4qW94Ly74ouEIDxiPkVuZW15PC9iPiDii4TgvLripbzigJTigJMtLS3igJPigJTipb3gvLvii4Q8L2dyYWRpZW50PiIsImZvcm1hdCI6Ijxjb2xvcjojZmYwMDAwPjxib2xkPkVORU1ZPC9ib2xkPjwvY29sb3I6I2ZmMDAwMD4gPGdyYXk+JVVzZXJuYW1lcyUgWzxjb2xvcjojZmYwMDAwPiVDbGFucyU8L2NvbG9yOiNmZjAwMDA+XSIsImdyb3VwVmFsdWVzIjp7IkNsYW5zIjpbIkFzcGVjdCIsIkFTUENUIiwiVmFwaW5nIiwiRm9yc2FrZW4iLCJSZXNpc3RhbmNlIiwiU29sYWNlIiwiRmF0ZSIsIkJldHRlciIsImJldHRlciIsIkZyb3plbiIsIkhlYXZlbiIsIk5pZ2h0TWFyZXMiLCJEZW5zZSIsIkJhdG1hbiIsIlRyYWNlIiwiU1lORElDQVRFIiwiTHVtaW5vdXMiLCJFVFJOTCIsIkVURVJOQUwiLCJSYWdlIiwiTGVnaW9uIiwiTXl0aGljIiwiVUJDIiwia2luZGFjb2xkIl0sIlVzZXJuYW1lcyI6WyJuZGxhIl19LCJtYXRjaFByaW9yaXR5IjpbXSwic2VydmVyRmlsdGVycyI6W119LHsiaWQiOiJhMWIyYzNkNC1lNWY2LTc4OTAtYWJjZC1lZjEyMzQ1Njc4OTAiLCJuYW1lIjoiPGdyYWRpZW50OiM4ODg4MDA6I2ZmZmYwMDojZmZmZmFhOiNmZmZmMDA6Izg4ODgwMD7ii4TgvLripbzigJTigJMtLS3igJPigJTipb3gvLvii4QgPGI+TmV1dHJhbDwvYj4g4ouE4Ly64qW84oCU4oCTLS0t4oCT4oCU4qW94Ly74ouEPC9ncmFkaWVudD4iLCJmb3JtYXQiOiI8Y29sb3I6I2ZmZmY1NT48Ym9sZD5ORVVUUkFMPC9ib2xkPjwvY29sb3I6I2ZmZmY1NT4gPGdyYXk+JVVzZXJuYW1lcyUgWzxjb2xvcjojZmZmZjU1PiVDbGFucyU8L2NvbG9yOiNmZmZmNTU+XSIsImdyb3VwVmFsdWVzIjp7IkNsYW5zIjpbIkNvbXBpbGFuY2UiLCJWYW5pdHkiLCJWYW5pdHlBUyIsIlZMUlNFIiwiVmFsZXJzZSJdfSwibWF0Y2hQcmlvcml0eSI6W10sInNlcnZlckZpbHRlcnMiOltdfV0sIm5ldXRyYWxGb3JtYXQiOiIiLCJub0NsYW5Gb3JtYXQiOiIiLCJuYW1lUGF0dGVybiI6Il4oPzxVc2VybmFtZXM+XFxTKykoPzpcXHMrXFxbKD88Q2xhbnM+W15cXF1dKylcXF0pPyQiLCJ1dWlkR3JvdXBzIjpbIlVzZXJuYW1lcyJdfQ==
```

The same code is available in [`valor-import.txt`](valor-import.txt). A StrayTags import replaces only the selected server config; it does not replace the whole `straytags.json`.

## StrayArmor config

`config/strayarmor.json` is created on first launch:

```json
{
  "enabled": true,
  "ownColor": "#00FF66",
  "allyColor": "#3BA7FF",
  "enemyColor": "#FF3B3B",
  "neutralColor": "",
  "tintMode": "vivid",
  "tintStrength": 1.0,
  "installStrayTagsConfig": true,
  "strayTagsConfigInstalled": true
}
```

- Colors are `#RRGGBB` (or `#AARRGGBB`).
- `tintStrength` is `0` (no color) to `1` (full color). The default is `1.0`, which uses the exact configured color. Lower values blend the tint toward white so armor shading stays visible.
- `tintMode` is how the color is applied: `vivid` (default; pure saturated color, texture shading still shows), `solid` (flat color, no texture detail), or `multiply` (color multiplied into the texture).
- Leave `neutralColor` empty to keep neutral/clanless players on normal, uncolored armor. Set it to a hex color to tint them.
- Configs from older StrayArmor builds are upgraded once to `tintStrength: 1.0`; after that, edits to the value are kept.
- Reload after editing with `/strayarmor reload`.

## ModMenu config screen

StrayArmor ships a ModMenu config screen (requires ModMenu and YetAnother Config Lib):

- Install the **ModMenu** and **YetAnother Config Lib (YACL)** mods.
- In-game: ModMenu → StrayArmor.

The screen edits the same `config/strayarmor.json`, grouped into **General** (enabled, install StrayTags config) and **Armor Colors** (own/ally/enemy/neutral colors, tint strength slider, tint mode dropdown). Saving writes the config and re-applies the tints immediately.

## Commands

- `/strayarmor reload` - reload `strayarmor.json` and `straytags.json`, and ask StrayTags to reload.
- `/strayarmor setup` - reinstall the bundled Valor StrayTags config, then reload.

## How matching works

For each armor-wearing player, StrayArmor:

1. Checks that the current server is whitelisted and has a StrayTags server config.
2. Finds the player's riding `TextDisplay` nametag, cleans it the same way StrayTags does, and applies the category regex and group lists in category order.
3. Maps the matching category to Own/Ally/Enemy/Neutral from its name and format (`OWN`, `ALLY`, `ENEMY`, `NEUTRAL`, `Latte`, `Valor`, and similar words are recognized).
4. Falls back to the local player being Own, and to Neutral when an unlisted player still has a clan tag.
5. If the close-range tag drops the clan (or a display is missing), checks the tab-list name and keeps the last known side while the shorter tag still matches that player.

The actual tint is applied only while the vanilla `HumanoidArmorLayer` is drawing, so skin, capes, and held items are not recolored. On 1.21.11 and 26.1.2 armor is submitted as deferred model nodes, so the tint is baked into the submitted node color.

## Build

StrayArmor uses Java 25 for the Gradle build (the 26.1.2 subproject needs it) and targets Java 21 bytecode for 1.21-1.21.11. Run Gradle with a JDK 25, for example:

```
JAVA_HOME="/path/to/jdk-25" ./gradlew buildAll
```

On Windows PowerShell:

```
$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"; .\gradlew.bat buildAll
```

The collected jars are written to `build/libs/`. Per-version jars are in `versions/<version>/build/libs/`.
