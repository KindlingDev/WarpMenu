# WarpMenu

*By Kindling Dev*

Server warps with a clean, clickable menu. Lightweight, no dependencies, fully configurable.

**Supports:** Paper 1.21.4 – 26.x (and forks like Purpur)

## Features

- `/warp` opens a paginated chest menu of every warp. Click an icon to teleport.
- Pick warp icons your way: `/setwarp <name> <item>`, the item in your hand, or drop an item onto a warp in the menu.
- Teleport delay that cancels if the player moves, plus a cooldown between warps.
- Optional per-warp permissions (`warpmenu.warp.<name>`). Players only see warps they can use.
- Admins can manage warps straight from the menu: shift + right-click to delete, drop an item on a warp to change its icon.
- Every message, title and lore line is editable with [MiniMessage](https://docs.advntr.dev/minimessage/format.html) (gradients, hex colours, etc.).
- Tab completion for all commands. `/warpmenu reload` applies config changes without a restart.

## Commands

| Command | Description | Permission |
|---|---|---|
| `/warp` (`/warps`) | Open the warp menu | `warpmenu.use` (everyone) |
| `/warp <name>` | Teleport to a warp | `warpmenu.use` |
| `/setwarp <name> [icon]` | Create or move a warp at your location (icon: typed, held item, or unchanged) | `warpmenu.admin` (op) |
| `/delwarp <name>` | Delete a warp | `warpmenu.admin` |
| `/warpmenu reload` | Reload config and warps | `warpmenu.admin` |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `warpmenu.use` | everyone | Use the menu and warps |
| `warpmenu.admin` | op | Manage warps |
| `warpmenu.warp.<name>` | – | Use one warp (only when `per-warp-permissions: true`) |
| `warpmenu.warp.*` | op | Use every warp |
| `warpmenu.bypass.delay` | op | Teleport instantly |
| `warpmenu.bypass.cooldown` | op | Ignore the cooldown |

## Configuration

See [`config.yml`](src/main/resources/config.yml). Main options:

```yaml
teleport-delay: 3          # seconds, 0 = instant
cooldown: 5                # seconds between warps, 0 = off
per-warp-permissions: false
default-icon: ENDER_PEARL
```

Warps are saved in `plugins/WarpMenu/warps.yml`.

## Building

Requires JDK 21+.

```
./gradlew build      # jar in build/libs/
./gradlew deploy -PserverDir=<server folder>   # build and copy into <server>/plugins
```

## License

[MIT](LICENSE) © 2026 Kindling Dev
