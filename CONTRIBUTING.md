# Contributing to StrayArmor

Thanks for helping out! A few things to know before you start.

## Building

StrayArmor is a multi-version Fabric project. One shared code base lives in `shared/`, and each `versions/<minecraft version>` folder wires it up for that Minecraft version.

```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew buildAll
```

The collected jars land in `build/libs/`. Per-version jars are in `versions/<version>/build/libs/`.

## Adding a Minecraft version

1. Copy `versions/26.2/` to `versions/<new>/`.
2. Update `minecraft_version`, `loader_version`, `fabric_version`, `yacl_version`, and `modmenu_version` in its `build.gradle` (check the Fabric and YACL mavens for the right numbers).
3. Add the version to the list in `settings.gradle`.
4. Run `./gradlew buildAll`.

## Source layout

- `shared/java` - code shared by every version (config, commands, matching, ModMenu screen).
- `shared/modern/java` - 1.21.11+ mixins (armor is submitted as deferred model nodes).
- `shared/state/java` - 1.21.4/1.21.8 mixins (render-state based).

## Config format

`config/strayarmor.json` fields are documented in `README.md`. When the schema changes, bump `StrayArmorConfig.CURRENT_VERSION` and add the migration to `StrayArmorConfigManager.upgrade()` so old configs upgrade once instead of resetting.

## Commands

- `/strayarmor reload` - reload both configs
- `/strayarmor reset` - restore defaults
- `/strayarmor set <side> <color>` - set a tint color
- `/strayarmor clan add|edit ...` - manage the StrayTags clan lists
