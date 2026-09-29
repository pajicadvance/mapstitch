# Local client smoke fixture

Run `python3 qa/client/run.py` after building the Fabric 26.3 MapStitch and Tool Pouch JARs. This development-only fixture requires the neighboring `toolpouch` checkout and the existing `mapstitch-polymer-compat-26.3/qa/launch.py` cached-runtime helper, its dependencies and original Mojang launch audits. It is not a portable Gradle test. A working X11 display and permission to bind localhost port 25666 are required.

The runner copies the built production JARs unchanged into isolated profiles, compiles separate QA mods, then connects a native Fabric client to a dedicated server. Only MapStitch, Tool Pouch, their dependencies and QA fixtures are loaded; Polymer is not loaded. Mod SHA256 hashes and commands are recorded in each `runs/*/launch-audit.json`, and `integrity.json` verifies the source JARs remained unchanged.

For both an inventory pouch and a pouch attached to equipped leggings, the client verifies map packet delivery and synchronized active ID, atlas discovery through the accessories scan, compass/clock requirements, actual minimap render state and center caching, and actual world-map rendered tiles. It also disables accessories scanning and verifies atlas, compass and clock discovery all stop. A passing run records 24 assertions in `control/result.txt`.

This fixture uses server-seeded inventories and programmatically opens the original world-map screen. It does not test physical key presses, crafting or attachment recipes, accessory-provider integrations, clicks inside the pouch menu, restart persistence, other Minecraft versions, or NeoForge. Server-side mutation and menu conservation are covered separately by the sibling server fixture.

## Recorded result

On 2026-09-29, the Fabric 26.3 client/server run passed all 24 assertions. Both inventory-pouch and equipped-leggings scenarios rendered the minimap and one world-map tile, received their map records, detected the nested compass and clock, and respected disabling accessories scanning. The runner stopped both processes afterward and verified unchanged production JAR hashes:

- MapStitch: `6800da7b7407738452d82bdc4cb819dcea2fbca9a6627e974cd5ee8517520342`
- Tool Pouch: `b90b29fa9a87684256191958b637c67088ed587a75eeb4278c66af9e06897866`
