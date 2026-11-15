# Changelog

## 1.0.1 - 2026-11-15

### Fixed

- Dismantling could rotate instead of dismantle when the server's sneaking state lagged behind the click.
  The action mode and hand state are now sent by the client.
- Ghost items and client/server hand desync after dismantling; the inventory and selected hotbar slot are resynchronized.
- Holding the wrench modifier no longer places the held item or causes a place/dismantle loop.
- Non-wrench blocks (chests, etc.) pass through to vanilla while the modifier is held.
- The Ctrl sprint-key conflict is handled: the mod does not activate while sprinting with that key.

## 1.0.0 - 2026-11-15

Initial release.

### Features

- Adds a Curios "Wrench" slot that only accepts Create's wrench (`create:wrench`).
- Hold `Ctrl` + right-click to rotate.
- Hold `Ctrl` + sneak + right-click to dismantle.
- Hold `Ctrl` + Create's "Open Block Rotation Menu" key to open the radial menu.
- Empty-hand left-click instantly destroys minecarts.
- `allowNonEmptyHand` config option, default `false` (empty hand only).
- English and Simplified Chinese localization.
- Custom mod icon and slot icon.

### Requirements

- Minecraft 1.21.1
- NeoForge 21.1.219+
- Create 6.0.x
- Curios 9.2.0+
