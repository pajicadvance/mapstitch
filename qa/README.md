# Tool Pouch engine regression (Fabric 26.3)

Validated on 2026-09-29: 75 engine assertions passed with patched MapStitch 1.1.6
and Tool Pouch 1.1.10, followed by a successful dedicated-server smoke test with
Tool Pouch absent. Both runs exited successfully, and input JAR hashes remained
unchanged. Runtime dependencies were Fabric API 0.161.0+26.3, Fzzy Config
0.7.7+fix2+26.3, and Fabric Language Kotlin 1.14.1+kotlin.2.4.20.

`ToolPouchQa.java` is a development-only Fabric server fixture. It runs against the
packaged MapStitch and Tool Pouch JARs in a fresh world, with the production mixins
enabled. It constructs server players with no-op outbound packet connections and
invokes the real player `aiStep`, pouch menu, and map-ejection handler.

Coverage:

- Atlas and compass discovery in an inventory pouch and attached leggings.
- Map creation through the player tick, stored atlas replacement, exact blank-map
  consumption, retained maps, and preservation of unrelated pouch slots.
- Inventory-use policy, including continued access through equipped leggings.
- Editing a pouch menu: stale contents cannot tick or eject maps, and closing the
  menu preserves removal without duplicating the atlas.
- Ejection from the second atlas, exact dropped-map conservation, and repeated
  requests for an absent map.
- All six atlases in a pouch tick, including atlases beyond the fifth slot.
- Tool Pouch config defaults, migration limits, explicit settings, idempotence,
  and opt-outs.

`SmokeQa.java` separately checks dedicated-server startup and optional API access
without Tool Pouch installed. Neither fixture tests a live client, GUI rendering,
network transport, or third-party accessory slots.

The local `run.py` adapter accepts `--runtime-reference PATH`, pointing to an
existing official-JAR runtime launcher exporting `audits`, `cp`, `deps`,
`base_command`, and `sha`. It also accepts `--mapstitch JAR`, `--toolpouch JAR`, and
`--without-toolpouch`. This adapter relies on an existing local runtime installation;
it does not download Minecraft or dependencies. Generated worlds, fixture JARs,
launch hashes, logs, and results go under ignored `qa/build/`.

To use the Java fixture with another launcher, compile it against the Minecraft
26.3, Fabric API, MapStitch, Tool Pouch, Fzzy Config, and Kotlin runtime JARs. Package
the classes into a Fabric mod with a server `main` entrypoint of
`qa.ToolPouchQa` (or `qa.SmokeQa` without Tool Pouch). Launch it in a disposable
dedicated-server directory. It writes `result.txt` and stops the server when done.
