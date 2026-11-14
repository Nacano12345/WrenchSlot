# Wrench Slot

A Minecraft **NeoForge** mod for **Minecraft 1.21.1** (NeoForge 21.1.265).

## Requirements

- JDK 21
- Gradle is not required — use the included wrapper (`./gradlew`)

## Commands

| Command | Description |
| --- | --- |
| `./gradlew build` | Build the mod jar into `build/libs/` |
| `./gradlew runClient` | Launch a development client |
| `./gradlew runServer` | Launch a development server |
| `./gradlew runData` | Run data generation into `src/generated/resources/` |
| `./gradlew runGameTestServer` | Run the game test server |

## Project layout

```
src/main/java/com/proto781m/wrenchslot/
├── WrenchSlot.java   # @Mod entry point
└── Config.java       # COMMON config options
src/main/templates/META-INF/neoforge.mods.toml   # expanded at build time
```

## Renaming

Mod metadata lives in `gradle.properties` (`mod_id`, `mod_name`, `mod_group_id`, …).
The main class' `MOD_ID` constant must always match `mod_id`.
