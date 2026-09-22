# DEVLOG — MapStitch, Gameoverse fork

## 2026-09-21: Fixed a real dedicated-server crash and a first-insert UX papercut in Atlas

Came up while designing a map-based discovery/exploration system for the
server (progression-gated minimap/atlas via item requirements, matching
how we already restrict the F3 debug menu — see `docs/current-state.md`
for the full design conversation). MapStitch was the mod already chosen
for this; testing it in-game with a real dedicated server crashed the
server the first time a player tried to insert a map into an Atlas.

**Bug 1 — dedicated-server crash on any Atlas insert.**

```
java.lang.NoClassDefFoundError: net/minecraft/client/renderer/state/MapRenderState
	at me.pajic.mapstitch.platform.version.Util26_1_2.<clinit>(Util26_1_2.java:23)
	at me.pajic.mapstitch.platform.MultiVersionUtil.<clinit>(MultiVersionUtil.java:16)
	at me.pajic.mapstitch.item.AtlasItem.overrideOtherStackedOnMe(AtlasItem.java:141)
```

`AtlasItem#overrideOtherStackedOnMe` calls `MultiVersionUtil.INSTANCE.toMutable(...)`
— a genuinely server-needed method, converting the Atlas's bundle
contents to a mutable form when a player inserts an item. But
`MultiVersionUtil.INSTANCE` is a single eagerly-initialized interface
constant that also carries pure client-rendering methods (`renderMap`,
`blit`, etc.) on the same implementation class, `Util26_1_2`. That
class's `private static final MapRenderState STATE = new MapRenderState()`
field is constructed in the static initializer — `MapRenderState` is a
client-only render-state class that doesn't exist on a dedicated
server. Referencing *any* method on `MultiVersionUtil.INSTANCE` forces
the whole class to load, including that field, crashing the server tick
loop the moment any player combines a map into an Atlas — completely
ordinary, expected gameplay, not an edge case.

**Fix**: deferred `MapRenderState` construction to `renderMap()`, the
only place it's actually used, and a genuinely client-only code path.
No functional change on the client — the instance is still reused
across minimap render calls the same way, just lazily on first real use
instead of eagerly at class-load time.

**Bug 2 — can't insert an already-filled map as an Atlas's first item.**

`isValidItemForAtlas()` only accepts a filled map (`Items.FILLED_MAP`)
as the very first thing inserted into an Atlas if the Atlas's scale
already happens to match — which is never true for a freshly-crafted
Atlas (`atlasScale == -1`, no scale established yet). A still-blank
`Items.MAP` is always accepted as the first item, but the instant a
player right-clicks/views a fresh Empty Map — ordinary vanilla behavior,
nothing Atlas-specific, maps have always activated on use — it becomes
a filled map and is then silently rejected by a brand-new Atlas, with
no error message explaining why. A new player's first natural instinct
(look at the interesting new item) permanently blocks the intended
workflow.

**Fix**: also accept a filled map when the Atlas has no established
scale yet (`atlasScale == -1`), matching how a still-blank map is
already handled. Checked before changing this that it's actually safe:
`ATLAS_SCALE` is only ever read as the default zoom level for new maps
the Atlas auto-generates to fill coverage gaps (defaulting to `0` if
unset) — never used to enforce scale consistency between maps already
inside the bundle, so this doesn't reintroduce any mixed-scale
rendering issue the original check might have been guarding against.

**Both confirmed fixed in-game** on this project's real dedicated
server (26.1.2 Fabric) — reproduced the original crash first (gave a
player an Atlas + Empty Map, clicked the map onto the Atlas, server
crashed), confirmed clean after fix 1, then reproduced the second issue
separately (viewed a fresh Empty Map before inserting it into a
brand-new Atlas, silently rejected) and confirmed fixed after fix 2.

**Upstream**: [PR #31](https://github.com/pajicadvance/mapstitch/pull/31)
against `pajicadvance/mapstitch`. MIT licensed, so this fork's source is
public — see `AGENTS.md`.
