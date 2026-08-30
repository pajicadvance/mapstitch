# MapStitch

This mod adds a world map and a minimap using vanilla maps and makes vanilla maps cheaper and easier to manage by using the new Atlas item.

Unlike other world map/minimap mods which ignore vanilla maps and make them obsolete, MapStitch makes them much more enjoyable to use and turns them into a core part of the world map and minimap.

Requires: 
- [Fzzy Config](https://modrinth.com/mod/fzzy-config)
- [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric only)

## World map

MapStitch uses atlases from your inventory to create a world map which you can open at any time by pressing M or right-clicking while holding an atlas.

![worldmap](https://raw.githubusercontent.com/pajicadvance/mapstitch/refs/heads/v3/images/worldmap.png)

The world map can only show one dimension and scale at once. These can be switched in the world map itself. There are basic controls like panning and zooming using the mouse, and toggleable player following and map grid overlay. A keybind reference can be brought up at any time by pressing H. Most of the keybinds can be rebound in the vanilla Controls menu.

The player marker, coordinates, and grid overlay require having a compass anywhere in the inventory.

![help](https://raw.githubusercontent.com/pajicadvance/mapstitch/refs/heads/v3/images/help.png)

## Atlas

Create an atlas by combining a filled map and a book. The atlas can then be used like a bundle to store empty maps and filled maps matching the scale of the filled map used to create the atlas (up to 16384 items in total, configurable via a game rule).

Maps from the atlas will be used to create the world map, and the atlas will automatically turn empty maps inside it into filled maps as you enter unmapped areas.

- Atlases can be enchanted with Globetrotter, which makes them consume paper instead of empty maps to create new maps. This is a treasure enchantment and can only be found in stronghold library chests.
- Adding an explorer map or buried treasure map to an atlas will make the target marker visible in the world map and minimap.
- Scrolling while hovering over an atlas will switch between ejecting filled or empty maps first.
- Right-clicking with an atlas in hand when targeting a banner will create a banner marker on the currently active map, just like regular maps do.

![atlas](https://raw.githubusercontent.com/pajicadvance/mapstitch/refs/heads/v3/images/atlas.png)

## Minimap

Having an atlas in the hotbar and a compass anywhere in your inventory will display a minimap showing the map where you're currently located.

If multiple atlases are present, the minimap will prioritize the atlas in your hands, otherwise the first atlas will be used.

![minimap](https://raw.githubusercontent.com/pajicadvance/mapstitch/refs/heads/v3/images/minimap.png)

The minimap can show some basic information like real time, game time, coordinates, biome and weather, with each being toggleable. These are all disabled by default. Note that in order to enable these, just enabling them in the client configuration isn't enough - they also need to be allowed in the common/server configuration.

## Working Nether maps

MapStitch enables maps in the Nether - no other mods required. The way Nether maps are displayed can be configured, and additional ceiling dimensions can be added to enable maps in them.

![nethermap](https://raw.githubusercontent.com/pajicadvance/mapstitch/refs/heads/v3/images/nethermap.png)

## Cheaper maps

Making maps is now cheaper, requiring only 9 paper instead of 8 paper and a compass, but they won't show your position unless you have a compass in your inventory.

![emptymap](https://raw.githubusercontent.com/pajicadvance/mapstitch/refs/heads/v3/images/emptymap.png)

## Mod integration

- [Trinkets (Updated)](https://modrinth.com/mod/trinkets-updated), [Ohmega](https://modrinth.com/mod/ohmega), [Curios API](https://modrinth.com/mod/curios), or [Trinkets](https://modrinth.com/mod/trinkets): Allows equipping one atlas as an accessory, from where it will function as usual and be prioritized for the minimap
- [Remapped](https://modrinth.com/mod/remapped): Fabric mod that significantly improves map colors, MapStitch works with it out of the box without any configuration needed, highly recommended

## Configuration

Many aspects of the mod are configurable. It's highly recommended to use the in-game configuration screen to configure the mod. On Fabric, you can access it by installing [Mod Menu](https://modrinth.com/mod/modmenu). On NeoForge, you can access it in the mod loader's built-in mod menu.

Client-side settings can also be configured from the Sodium video settings screen if [Sodium](https://modrinth.com/mod/sodium) is installed.

Some notable settings include:
- Customizing the appearance of the world map, minimap, and Nether maps
- Granular control over the requirement of items to use certain features (such as the compass being required for the player marker and coordinates, clock being required for time and weather etc.)
- Toggling the cheaper map recipe and whether it should replace the vanilla recipe
- Toggling the Globetrotter enchantment and adjusting the chance for it to appear in stronghold library chests
- Toggling whether to keep atlases on death
- Toggling each accessory slot when an accessory mod is installed
