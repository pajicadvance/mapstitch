## Summary

I heard complaints that my mod is too singleplayer oriented.

- **Atlas pins** - Craft them with wool and iron nuggets, dye them, pin locations in the world map with them, and share pinned locations with your friends.
- **Player marker improvements** - See your friends in the world map and minimap with new colored, smoothly animated player markers, with unique colors for each player, and team support.
- **Copy and merge atlases** - In the cartography table, make copies of your atlas to share with your friends, and merge your atlas with atlases from your friends to share progress.
- **Centered minimap mode** - This incredible, never-before-seen technology keeps your player marker in the center and moves the minimap contents around it.
- A bunch of bug fixes.

## Full changelog

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
  - If the request isn't accepted within 60 seconds (configurable), the atlas pin is refunded.
- The atlas pin item can be disabled in the config. When disabled, pins can be placed and shared without atlas pins, choosing from the 16 base dye colors and the colors of existing pins.

### Centered minimap mode

- Added a new "Centered" minimap mode, which locks your player marker to the center and moves maps around it.
- Added a "Lock player marker north" option that locks your player marker north in Centered mode, which makes maps rotate around your player marker. Off by default.
- The old minimap mode is now called "Static". Centered is the new default mode.

### Player marker improvements

- Player markers of other players can now be seen in the world map and minimap.
- Hovering over a player marker in the world map shows the player name.
- Player markers in the minimap show the player name while the player list is open.
- Player markers are now colored the same way the locator bar colors them (based on player UUID).
- Player markers now move and rotate smoothly without any lag.
- Showing other player markers and player marker colors can be configured.

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
  
### Other changes

- Improved map rendering precision, which gets rid of seams that sometimes appear between maps in the world map screen.
- Added a checkerboard background to fill out the empty space in the world map screen, toggleable in the config.
- All item requirements are now disabled by default.
- All minimap info entries are now allowed by default.
- If a required item for some action is missing, the button of that action will now be greyed out in the world map screen.
- Added config option "Globetrotter" → "Loot locations", which allows specifying loot tables where Globetrotter can be found, with defaults being:
  - Vanilla stronghold library chest
  - Enderscape stronghold library chest
  - Dungeons and Taverns stronghold overhaul library chest
