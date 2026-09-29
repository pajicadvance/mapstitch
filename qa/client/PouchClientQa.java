package pouchqa;

import java.nio.file.*;
import java.util.*;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.util.ModClientUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;

public class PouchClientQa implements ClientModInitializer {
    final Path control = Path.of(System.getProperty("pouch.qa.control"));
    final List<String> evidence = new ArrayList<>();
    int phase, ticks, scenario, checks;
    boolean done;
    MapId id;
    void check(boolean value, String message) { if (!value) throw new AssertionError(message); checks++; }
    Object field(Class<?> type, String name) throws Exception {
        var field = type.getDeclaredField(name); field.setAccessible(true); return field.get(null);
    }
    void scans(String location) {
        var requirements = MapStitch.CONFIG.itemRequirements;
        requirements.minimapAtlasScan.trySetQuiet(List.of(location));
        requirements.worldMapAtlasScan.trySetQuiet(List.of(location));
        requirements.compassAndClockScan.trySetQuiet(List.of(location));
    }
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (done || client.player == null || client.level == null) return;
            try {
                if (++ticks > 900) throw new AssertionError("timeout phase=" + phase);
                switch (phase) {
                    case 0 -> {
                        if (ticks < 60) return;
                        scans("accessories");
                        ((Map<?, ?>) field(MinimapOverlay.class, "CACHED_CENTERS")).clear();
                        WorldMapScreen.clearMaps();
                        Files.writeString(control.resolve("command"), scenario == 0 ? "inventory" : "leggings");
                        phase = 1; ticks = 0;
                    }
                    case 1 -> {
                        if (ticks < 100 || !Files.exists(control.resolve("ack"))) return;
                        var ack = Files.readString(control.resolve("ack")).split(" ");
                        if (!ack[0].equals(scenario == 0 ? "inventory" : "leggings")) return;
                        id = new MapId(Integer.parseInt(ack[1]));
                        check(client.level.getMapData(id) != null, "pouch map packet received");
                        var atlas = ModClientUtil.getFirstItem(client, ModItems.ATLAS);
                        check(atlas.is(ModItems.ATLAS), "accessories scan discovers pouch atlas");
                        check(atlas.getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1) == id.id(), "active map component synchronized");
                        check(ModClientUtil.hasCompass(client, "minimap"), "pouch compass detected");
                        check(ModClientUtil.hasClock(client, "time"), "pouch clock detected");
                        check(((ItemStack) field(MinimapOverlay.class, "lastAtlas")).getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1) == id.id(), "real minimap render selected atlas");
                        check(((Map<?, ?>) field(MinimapOverlay.class, "CACHED_CENTERS")).containsKey(id), "real minimap render cached map center");
                        scans("hotbar");
                        check(ModClientUtil.getFirstItem(client, ModItems.ATLAS).isEmpty(), "disabled accessories excludes nested atlas");
                        check(!ModClientUtil.hasCompass(client, "minimap"), "disabled accessories excludes nested compass");
                        check(!ModClientUtil.hasClock(client, "time"), "disabled accessories excludes nested clock");
                        scans("accessories");
                        client.setScreenAndShow(new WorldMapScreen(-1));
                        phase = 2; ticks = 0;
                    }
                    case 2 -> {
                        if (ticks < 60) return;
                        check(client.gui.screen() instanceof WorldMapScreen, "worldmap screen opened");
                        int maps = ((Map<?, ?>) field(WorldMapScreen.class, "MAPS")).size();
                        check(maps > 0, "real worldmap render discovered pouch source");
                        evidence.add((scenario == 0 ? "inventory" : "leggings") + ": map=" + id.id() + "; minimap rendered; compass and clock detected; disabling accessories excludes all three; worldmap tiles=" + maps);
                        client.gui.screen().onClose();
                        if (scenario++ == 0) { phase = 0; ticks = 0; }
                        else {
                            Files.writeString(control.resolve("result.txt"), "PASS " + checks + " assertions; real client/server Tool Pouch smoke\n" + String.join("\n", evidence) + "\n");
                            done = true;
                        }
                    }
                }
            } catch (Throwable failure) {
                failure.printStackTrace(); done = true;
                try { Files.writeString(control.resolve("result.txt"), "FAIL phase=" + phase + " " + failure + "\n" + String.join("\n", evidence)); } catch (Exception ignored) {}
            }
        });
    }
}
