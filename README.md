# Wrench Slot

A Minecraft **NeoForge** mod that adds a [Curios](https://modrinth.com/mod/curios) slot for
Create's wrench, so the wrench can be used without occupying a hand.

## Requirements

| Dependency | Version |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.219+ |
| [Create](https://modrinth.com/mod/create) | 6.0.x (1.21.1) |
| [Curios](https://modrinth.com/mod/curios) | 9.2.0+ (1.21.1) |

## Usage

1. Craft a Create wrench as usual.
2. Open the Curios panel from the player inventory and put the wrench into the new **Wrench** slot.
3. With an **empty main hand**:

| Input | Action |
| --- | --- |
| Hold `Left Ctrl` (this mod's modifier) + right-click | **Rotate** |
| Hold `Left Ctrl` + sneak (`Shift`) + right-click | **Dismantle** |
| Hold `Left Ctrl` + Create's *Open Block Rotation Menu* key | Open the block rotation menu |
| Left-click a minecart | Instantly destroy it |

Notes:

- `Left Ctrl` is this mod's primary modifier and can be changed in
  **Options → Controls → Key Binds → Wrench Slot**.
- Sneaking is Create's original primary modifier and acts as this mod's secondary
  modifier. It is the normal vanilla sneak key.
- Without the modifier, right-clicking a wrenchable block behaves like vanilla.
- Create's *Open Block Rotation Menu* is unbound by default. Bind it once in the
  controls menu; this mod makes it work while the wrench is in the Curios slot.
- Only blocks related to the wrench are intercepted (`IWrenchable` blocks and Create's
  `wrench_pickup` tag, e.g. shulker boxes). Chests, buttons and other blocks keep their
  vanilla behaviour.
- Holding any item in the main hand keeps normal vanilla behaviour.
- The wrench drops on death (`ALWAYS_DROP` Curios rule).

## Configuration

`config/wrenchslot-common.toml`:

| Option | Default | Description |
| --- | --- | --- |
| `enabled` | `true` | Master switch for the empty-hand wrench behaviour |
| `enableMinecartKill` | `true` | Empty-hand attacks instantly destroy minecarts |
| `enableRotationMenu` | `true` | Allow Create's rotation menu with the Curios wrench |

## Development

```bash
./gradlew build        # jar in build/libs/
./gradlew runClient    # dev client
./gradlew runServer    # dev server
```

If the NeoForged Maven is unreachable, point Gradle at your proxy, for example:

```bash
JAVA_TOOL_OPTIONS="-Dhttps.proxyHost=192.168.0.122 -Dhttps.proxyPort=10808 \
  -Dhttp.proxyHost=192.168.0.122 -Dhttp.proxyPort=10808" ./gradlew build
```

### Project layout

```
src/main/java/com/proto781m/wrenchslot/
├── WrenchSlot.java                 # mod entry point, payload + Curios predicate
├── Config.java                     # common config
├── curio/CurioWrench.java          # lookup of the equipped wrench
├── event/ClientEvents.java         # empty-hand right click -> action packet
├── event/ServerEvents.java         # cancels vanilla use, minecart kill
├── event/Keybinds.java             # R key bind
├── logic/WrenchActions.java        # server-side validation and execution
├── network/WrenchActionPayload.java
├── util/WrenchUseContext.java      # UseOnContext carrying the Curios stack
├── util/WrenchPickup.java          # wrench_pickup block handling
└── mixin/RadialWrenchHandlerMixin.java
src/main/resources/
├── data/wrenchslot/curios/slots/wrench.json
├── data/wrenchslot/curios/entities/player.json
├── assets/wrenchslot/textures/slot/wrench_slot.png
└── assets/wrenchslot/lang/{en_us,zh_cn}.json
```
