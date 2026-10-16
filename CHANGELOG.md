### Atlas pins

- New Atlas Pin item:
  - Craft 16 atlas pins by combining paper, any wooden planks, and an iron ingot.
  - Atlas pins can be dyed in bulk by combining 2-8 identical atlas pins with one or more dyes, with the default color being white.
  - Atlas pins can be carried inside your inventory, bundles, or shulker boxes to be used in the world map screen.
- Pin placement:
  - In the world map screen, press P to turn on pin placement mode.
  - Click anywhere on the world map to open the pin placement menu.
  - In the menu, set the pin name and select the pin color from the palette.
  - Press the "Confirm" button to place the pin on the clicked location, consuming the atlas pin of the matching color.
  - Click on an existing pin while holding Ctrl to remove the pin under the cursor, placing it back into your inventory.
  - Press P again to turn off pin placement mode.
- Pin sharing:
  - Left-click a pin outside of pin placement mode to share it with an online player.
  - Sharing a pin consumes an atlas pin of a similar color.
  - The receiving player gets a chat message with a link to accept the pin.
  - If the request isn't accepted within 60 seconds, the atlas pin is refunded.
- The atlas pin item can be disabled in the config. When disabled, pins can be placed and shared without atlas pins, choosing from the dye colors and the colors of existing pins.

### Player marker improvements

- Player markers of other players can now be seen in the world map and minimap.
- Hovering over a player marker in the world map shows the player name.
- Player markers in the minimap show the player name while the player list is open.
- Player markers are now colored the same way the locator bar colors them (based on player UUID).
- Player markers now move and rotate smoothly without any lag.

### Copy and merge atlases

- The cartography table can now be used to copy and merge atlases.
- When two atlases are placed in the cartography table input slots, a button will show up which switches between copying and merging when clicked.
  - Hover over the button to see why the atlases can't be copied or merged.
- Copying:
  - Insert the atlas to be copied (source atlas) in the top slot.
  - Insert the atlas where the maps from the source atlas will be copied to (target atlas) in the bottom slot.
  - The target atlas needs one empty map/paper for each filled map from the source atlas that it doesn't already have.
  - Take out the resulting atlas from the rightmost slot. The source atlas will remain intact, while the target atlas will be consumed.
  - The resulting atlas keeps everything from the target atlas, including its name and enchantments, and adds the filled maps of the source atlas.
- Merging:
  - Place two atlases in the two leftmost slots. The resulting atlas keeps the name of the top atlas and the enchantments of both.
  - Take out the resulting atlas from the rightmost slot. Both input atlases will be consumed.
  - The resulting atlas will contain the contents of both atlases combined, and must fit within the atlas capacity.
- If both atlases contain maps of the same area, the more explored map is kept and the other is turned back into an empty map/paper.
- Atlases holding filled maps of different scales can't be copied or merged.
  
### Configuration

- All item requirements are now disabled by default.
- All minimap info entries are now allowed by default.
- Added config option "Globetrotter" → "Loot locations", which allows specifying loot tables where Globetrotter can be found, with defaults being:
  - Vanilla stronghold library chest
  - Enderscape stronghold library chest
  - Dungeons and Taverns stronghold overhaul library chest
