<p align="center">
   <img src="https://raw.githubusercontent.com/noramibu/lumen-tooltips/refs/heads/main/common/src/main/resources/icon.png" alt="Lumen Tooltips Logo" width="150">
</p>

<h1 align="center">Lumen Tooltips</h1>

<p align="center">
   <strong>A client-side tooltip enhancement and visual preview mod for Minecraft: Java Edition.</strong>
</p>

<div align="center">

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/ay9TFvI5?style=for-the-badge&logo=Modrinth&label=Modrinth&color=1bd96a)](https://modrinth.com/project/tooltips)
[![GitHub Source](https://img.shields.io/badge/Source-181717?style=for-the-badge&logo=GitHub&label=GitHub)](https://github.com/noramibu/lumen-tooltips)
[![Discord Join](https://img.shields.io/badge/Join-5865F2?style=for-the-badge&logo=Discord&label=Discord)](https://discord.gg/FaxbR9eEFW)

</div>

---

## Overview
Lumen Tooltips is a powerful client-side mod that enhances Minecraft's tooltips adding details, dynamic sizing, and visual previews directly in your inventory. Designed to give you more information at a glance, it replaces the need for multiple standalone mods by integrating hunger stats, container previews, and gear comparisons into one seamless package.

---

## Key Features
- **Enhanced Tooltip Data** - Durability, food stats, enchantment descriptions, and gear comparisons.
- **Visual Previews** - See inside containers, books, fireworks, and spawn eggs without placing or using them.
- **Inventory Actions** - Scroll, wrap, and interact with tooltips using intuitive hotkeys.
- **Text Safety** - Includes a patch for the Translation Crash, which applies beyond just Tooltips.
- **Fully Configurable** - Change features, activation modes, keybinds, and control hints in-game without restarting.
- **Cross-Loader Support** - Fully compatible with both Fabric and NeoForge.
- **Seamless Integration** - Edit items or save them directly to [Item Editor](https://modrinth.com/mod/item-editor) storage with configurable keybinds.

---

## Installation
### Requirements
- Minecraft (Fabric or NeoForge compatible version)
- Fabric API (if using Fabric)

### Steps
1. Install [Fabric Loader](https://fabricmc.net/use/) or [NeoForge](https://neoforged.net/)
2. Download the following files:
   - [Lumen Tooltips](https://modrinth.com/project/lumen-tooltips) (this mod)
   - [Fabric API](https://modrinth.com/mod/fabric-api) (if using Fabric)
3. Place all `.jar` files into your `mods` folder
4. Launch the game
5. Configure via the `/lumen` command in-game, or the `Lumen Tooltips` button in Minecraft's settings menu.

---

## Feature Categories

### Configuration
- **In-Game Access**: Open with `/lumen` or the Lumen Tooltips button in Minecraft's settings.
- **Immediate Changes**: Options apply without a save button or restart.
- **Easy Navigation**: Compact icon categories, option search, and collapsible advanced settings.
- **Clear Changes**: Changed-value markers plus per-option and current-menu reset buttons.
- **Keybind Center**: Rebind or unbind every Lumen shortcut with conflict warnings in one place.
- **Live Examples**: Visual options show formatted before-and-after tooltip previews.
- **Control Hints**: Configure preview, open, edit, and save hints without disabling their shortcuts.

### Tooltip Information
- **Durability Percentage**: Added over Vanilla debug tooltip.
- **Durability Coloring**: Based on health (configurable thresholds).
- **Hunger & Saturation**: Icons for food (taken from AppleSkin).
- **Status Effects**: Food and Suspicious Stew effects.
- **Enchantment Descriptions**: Brief description of what the Enchantment does.
- **Numeric Enchantments**: Numeric levels instead of Roman numerals (disabled by default).
- **Equipment Comparison**: Hovered vs currently equipped item comparison.
- **Vanilla Flags**: Individual toggles for every registered vanilla `hidden_components` flag.

### Navigation
- **Map Exploration**: Filled map / explored map percentage.
- **Off-Map Direction**: Direction when the player is outside the map.
- **Unexplored Areas**: Direction of the nearest unexplored map area.
- **Lodestone Target**: Target dimension and coordinates.
- **Recovery Compass**: Death location.
- **Dimension Warning**: Warning when a compass target is in another dimension.

### Extra Statistics
*Disabled by default; every statistic can be enabled separately.*
- **Fuel and Composting**: Furnace burn duration and Composter success chance.
- **Item Properties**: Use cooldown, enchantability, and anvil prior-work penalty.
- **Block Properties**: Hardness, blast resistance, and Enchanting Table power.
- **Tool Properties**: Data-driven mining level and highest finite base mining speed.
- **Item Source**: Display name of the mod that registered the item.
- **Display Controls**: Always, hold-key, or advanced-tooltip activation and seconds/ticks units.

### Tooltip Layout
- **Screen Boundaries**: Keeps tooltips inside screen boundaries.
- **Text Wrapping**: Wraps overly wide tooltip text.
- **Maximum Width**: Automatic and/or explicitly configured maximum width.
- **Scrolling**: Mouse wheel scrolling for tooltips taller than the screen.
- **Scroll Speed**: Configurable scroll speed/step.
- **Control Hints**: Configurable control hints for available actions.

### Visual Previews
*Configurable keybind, default `Left Shift`*
- **Shulker Boxes**: Content grid with matching shulker color.
- **Generic Containers**: Container and chest content grids.
- **Bundles**: Contents with selected-item highlighting and fullness bar like vanilla.
- **Filled Maps**: Live map image and markers from client-known map data.
- **Banners, Shields, and Decorated Pots**: Enlarged vanilla item rendering with stored patterns.
- **Potions and Tipped Arrows**: Optional vanilla effect icons, levels, colors, and durations; disabled by default.
- **Ender Chests**: Session-only preview of the last Ender Chest opened in the current world.
- **Paintings and Signs**: Painting entities and signs or hanging signs with stored text.
- **Player Heads**: Enlarged native head models using stored profiles and skins.
- **Written and Writable Books**: First-page preview and page count, plus player heads beside author.
- **Fireworks**: Animated rocket and firework-star simulation, flight time, burst count, shapes, colors, trails, and twinkle.
- **Spawn Eggs**: Entity rendering with stored data.
- **Mob Buckets**: Entity rendering with stored bucket data.
- **Spawners**: Entity rendering from SpawnData and spawn potentials.
- **Animations**: Animated living entities and TNT fuse animation.
- **Area-Effect Clouds**: Radius, potion information and animated particles.
- **Display Entities**: Block, item, and text display entities.
- **Item Frames**: Item frames and glow item frames.
- **Falling Blocks**: Falling-block rendering, including end portals and gateway.
- **Entity Positioning**: Configurable fixed yaw and pitch for display entities.
- **Entity Fitting**: Automatic entity fitting, including special horse sizing.
- **Invisible Entities**: Shows custom_name fallback for invisible entities.
- **Appearance**: Compact, Vanilla, and Comfortable density plus full/compacted container contents.
- **Tooltip Styles**: Custom `tooltip_style` backgrounds and frames are respected where applicable.
- **Preview Colors**: Configurable container tint, counts, titles, and item-derived colors.
- **Accessibility**: Colorblind durability colors and reduced-motion entity and firework previews.

### Opening Items
*Configurable keybind, default `Left Alt`*
- **Container Contents**: Open as read-only container screens.
- **Written Books**: Open in Minecraft’s native book viewer.
- **Writable Books**: Open read-only without signing or modifying them.
- **Large Chests**: Supports up to six vanilla chest rows.
- **Inventory Support**: Works from normal inventories, the Creative inventory, and Lumen-opened container screens.
- **Nested Navigation**: Shows breadcrumbs and a native back button while opening stored containers.
- **Native Feedback**: Returning to the parent plays the matching chest, Ender Chest, or Shulker Box close sound.

### Text Safety
- **Recursive Protection**: Protection against malicious recursive translation components, highly configurable.
- **Component.visit**: Optional global `Component.visit` protection.

### Extra
- **Item Editor**: Deep API integration with [Item Editor](https://modrinth.com/mod/item-editor), including shortcuts for editing and saving hovered items.
- **Storage Routing**: Save to the first available slot, a page number, or a named page, with optional page creation and page-number feedback.

---

## Screenshots
<div align="center">
 <img src="https://cdn.modrinth.com/data/ay9TFvI5/images/d6472fbb7e361e5d23774169f835cf26372495bd.png" alt="Entity Preview" width="419" />
 <img src="https://cdn.modrinth.com/data/ay9TFvI5/images/5ff0876c71b50282e12f0ed1903c6538f449cf7f.png" alt="Book Preview" width="419" />
 </div>
<div align="center">
 <img src="https://cdn.modrinth.com/data/ay9TFvI5/images/ad3d88ec66fe0a2a56131275c1c235d907837dc5.png" alt="Gear Comparison" width="419" />
 <img src="https://cdn.modrinth.com/data/ay9TFvI5/images/49db0b17d54ff43b880252959589cd9fd1fa6d15.png" alt="Firework Preview" width="419" />
 </div>
<div align="center">
 <img src="https://cdn.modrinth.com/data/ay9TFvI5/images/3629553b81c22050ff9b060ab0625e59c988c52b.png" alt="Firework Details" width="419" />
 <img src="https://cdn.modrinth.com/data/ay9TFvI5/images/8b5ab64f0d854795372e284ea25509c8e58960de.png" alt="Configuration Menu" width="419" />
 </div>

---

## Supported Languages
| Language            | Native name | Code  |
|---------------------|-------------|-------|
| English             | English     | en_us |

---

## Contributing
Contributions are welcome! Please feel free to:
- Report bugs via [GitHub Issues](https://github.com/noramibu/lumen-tooltips/issues)
- Submit feature requests
- Join the [Discord](https://discord.gg/FaxbR9eEFW) for discussion

---

## Related Projects & Inspirations
If you enjoy Lumen Tooltips, you might also be interested in the projects that inspired some of its features:
- **AppleSkin**: For the original hunger and saturation visualization (Lumen Tooltips implements this identically thanks to AppleSkins Unlicense).
- **Meteor Client**: For the shulker-box grid and rotating entity preview ideas.
- **Item Editor**: Lumen Tooltips features deep integration with Item Editor, allowing you to seamlessly transition from viewing an item's tooltip to editing its components.

---

## License
This project is licensed under the terms of the license specified in the [LICENSE](LICENSE) file.

---

<p align="center">
 <a href="https://modrinth.com/project/lumen-tooltips">Modrinth</a> •
 <a href="https://github.com/noramibu/lumen-tooltips">GitHub</a> •
 <a href="https://discord.gg/FaxbR9eEFW">Discord</a>
 </p>