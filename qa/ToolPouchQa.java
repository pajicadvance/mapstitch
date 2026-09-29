package qa;

import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import me.pajic.mapstitch.compat.ToolPouchCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.ServerNetworkEvents;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.toolpouch.ToolPouch;
import me.pajic.toolpouch.config.ModConfig;
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.AllowedItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.*;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.saveddata.maps.*;

/** Runs production methods and mixins in a real dedicated server; no client or packet claims. */
public final class ToolPouchQa implements ModInitializer {
    int checks, drain;
    boolean finished;
    final List<String> lines = new ArrayList<>();
    void check(boolean value, String why) {
        if (!value) throw new AssertionError(why);
        checks++;
    }
    ServerPlayer player(ServerLevel level) {
        var profile = new GameProfile(UUID.randomUUID(), "ToolPouchQA");
        var p = new ServerPlayer(level.getServer(), level, profile, ClientInformation.createDefault());
        p.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), p, CommonListenerCookie.createInitial(profile, false)) {
            @Override public void send(Packet<?> packet) {}
        };
        p.setPos(256, 100, 0);
        return p;
    }
    ItemStack atlas(ItemStack map, int blanks) {
        var a = new ItemStack(ModItems.ATLAS);
        a.set(ModDataComponents.ATLAS_SCALE, 0);
        a.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY.copyWithContents(
                Stream.of(map.copy(), new ItemStack(Items.MAP, blanks)).filter(s -> !s.isEmpty())));
        return a;
    }
    ItemStack holder(ItemStack a, boolean leggings) {
        var h = new ItemStack(leggings ? Items.IRON_LEGGINGS : me.pajic.toolpouch.item.ModItems.TOOL_POUCH);
        var contents = NonNullList.withSize(16, ItemStack.EMPTY);
        contents.set(1, new ItemStack(Items.COMPASS));
        contents.set(3, a);
        contents.set(9, new ItemStack(Items.CLOCK));
        h.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        return h;
    }
    List<ItemStack> items(ItemStack holder) {
        return holder.get(DataComponents.CONTAINER).itemCopies().toList();
    }
    ItemStack stored(ItemStack holder) { return items(holder).get(3); }
    int count(ItemStack a, Item type) {
        return a.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).items().stream()
                .filter(s -> s.is(type)).mapToInt(ItemStackTemplate::count).sum();
    }
    int countId(ItemStack a, MapId id) {
        return a.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).items().stream()
                .filter(s -> id.equals(s.get(DataComponents.MAP_ID))).mapToInt(ItemStackTemplate::count).sum();
    }
    int droppedCount(ServerPlayer p, MapId id) {
        return p.level().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(10)).stream()
                .map(ItemEntity::getItem).filter(s -> id.equals(s.get(DataComponents.MAP_ID)))
                .mapToInt(ItemStack::getCount).sum();
    }
    void unchangedNeighbors(ItemStack holder) {
        var contents = items(holder);
        check(contents.get(1).is(Items.COMPASS) && contents.get(1).getCount() == 1, "compass slot preserved");
        check(contents.get(9).is(Items.CLOCK) && contents.get(9).getCount() == 1, "clock slot preserved");
        check(contents.get(0).isEmpty() && contents.get(2).isEmpty(), "empty slots preserved");
    }
    void caseFor(ServerLevel level, boolean leggings) {
        var p = player(level);
        var map = MapItem.create(level, 0, 0, (byte) 0, true, false);
        var mapId = map.get(DataComponents.MAP_ID);
        level.setMapData(mapId, MapItem.getSavedData(mapId, level).locked());
        var h = holder(atlas(map, 4), leggings);
        if (leggings) p.setItemSlot(EquipmentSlot.LEGS, h); else p.getInventory().setItem(12, h);
        check(ToolPouchCompat.getAtlases(p).size() == 1, "active pouch atlas discovery " + leggings);
        check(ToolPouchCompat.hasItem(p, Items.COMPASS), "pouch compass discovery");
        check(ToolPouchCompat.hasItem(p, ModItems.ATLAS), "pouch atlas presence");
        // Runs the injected Player.aiStep method, not just the compatibility helper.
        p.aiStep();
        var updated = stored(h);
        check(count(updated, Items.FILLED_MAP) == 2, "aiStep creates and persists map in pouch");
        check(count(updated, Items.MAP) == 3, "exactly one blank map consumed");
        check(countId(updated, mapId) == 1, "original map retained");
        unchangedNeighbors(h);
        p.aiStep();
        check(count(stored(h), Items.FILLED_MAP) == 2 && count(stored(h), Items.MAP) == 3, "repeat tick does not duplicate map");
        ToolPouch.CONFIG.allowUseFromInventory.accept(false);
        check(ToolPouchCompat.getAtlases(p).size() == (leggings ? 1 : 0), "inventory policy preserves equipped leggings");
        var snapshot = h.get(DataComponents.CONTAINER);
        p.setPos(512, 100, 0);
        p.aiStep();
        if (!leggings) check(snapshot.equals(h.get(DataComponents.CONTAINER)), "disabled inventory pouch does not mutate");
        ToolPouch.CONFIG.allowUseFromInventory.accept(true);

        var menu = new ToolPouchMenu(7, p.getInventory(), h);
        p.containerMenu = menu;
        var removedAtlas = menu.getSlot(3).remove(1);
        snapshot = h.get(DataComponents.CONTAINER);
        check(ToolPouchCompat.getAtlases(p).isEmpty(), "open menu hides stale atlas snapshot");
        p.aiStep();
        ServerNetworkEvents.ejectMap(new C2SEjectMap(mapId), p);
        check(snapshot.equals(h.get(DataComponents.CONTAINER)), "open menu blocks ticks and stale ejection");
        menu.removed(p);
        p.containerMenu = p.inventoryMenu;
        check(stored(h).isEmpty() && ToolPouchCompat.getAtlases(p).isEmpty(), "closing menu persists atlas removal");
        check(countId(removedAtlas, mapId) == 1, "removed atlas retains original map after attempted stale ejection");
        unchangedNeighbors(h);

        // Put two copies of a map in the second atlas, exercising index mapping and removal-one semantics.
        var extra = atlas(map.copyWithCount(2), 0);
        var contents = NonNullList.withSize(16, ItemStack.EMPTY);
        h.get(DataComponents.CONTAINER).copyInto(contents);
        contents.set(3, atlas(ItemStack.EMPTY, 2));
        contents.set(6, extra);
        h.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        var first = items(h).get(3);
        int droppedBefore = droppedCount(p, mapId);
        ServerNetworkEvents.ejectMap(new C2SEjectMap(mapId), p);
        check(countId(items(h).get(6), mapId) == 1, "eject removes exactly one from correct atlas");
        check(droppedCount(p, mapId) == droppedBefore + 1, "eject drops exactly one matching map into world");
        check(ItemStack.isSameItemSameComponents(first, items(h).get(3)), "eject leaves first atlas unchanged");
        ServerNetworkEvents.ejectMap(new C2SEjectMap(mapId), p);
        check(countId(items(h).get(6), mapId) == 0, "second eject removes final matching map");
        check(droppedCount(p, mapId) == droppedBefore + 2, "two ejections conserve both map items");
        snapshot = h.get(DataComponents.CONTAINER);
        ServerNetworkEvents.ejectMap(new C2SEjectMap(mapId), p);
        check(snapshot.equals(h.get(DataComponents.CONTAINER)), "repeat eject cannot recreate map");
        check(droppedCount(p, mapId) == droppedBefore + 2, "repeated missing-map eject cannot duplicate dropped maps");
        unchangedNeighbors(h);
        lines.add("PASS " + (leggings ? "attached leggings" : "inventory pouch") + ": discovery, aiStep, allocation, policy, open-menu removal, ejection, unchanged slots");
    }
    void manyAtlases(ServerLevel level) {
        var p = player(level);
        var h = holder(ItemStack.EMPTY, false);
        var contents = NonNullList.withSize(16, ItemStack.EMPTY);
        for (int i = 0; i < 6; i++) contents.set(i, atlas(ItemStack.EMPTY, 2));
        h.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
        p.getInventory().setItem(12, h);
        p.aiStep();
        for (int i = 0; i < 6; i++) {
            check(count(items(h).get(i), Items.FILLED_MAP) == 1, "atlas " + i + " ticked");
            check(count(items(h).get(i), Items.MAP) == 1, "atlas " + i + " blank consumed once");
        }
        lines.add("PASS six atlases all tick and persist");
    }
    void migration() {
        var c = new ModConfig();
        check(c.allowedItems.stream().anyMatch(a -> a.id.get().equals("mapstitch:atlas")), "new config allows MapStitch");
        c.allowedItems.accept(new LinkedHashSet<>(List.of(new AllowedItem("improved-maps:atlas", 1, 3), new AllowedItem("minecraft:clock", 1, 1))));
        c.update(1);
        var migrated = c.allowedItems.stream().filter(a -> a.id.get().equals("mapstitch:atlas")).findFirst().orElseThrow();
        check(migrated.maxStackSize.get() == 1 && migrated.maxStackCount.get() == 3, "migration preserves legacy limits");
        check(c.allowedItems.size() == 3, "migration retains existing entries");
        c.update(1);
        check(c.allowedItems.size() == 3, "migration is idempotent");
        c.allowedItems.accept(new LinkedHashSet<>(List.of(new AllowedItem("improved-maps:atlas", 1, 3), new AllowedItem("mapstitch:atlas", 1, 7))));
        c.update(1);
        check(c.allowedItems.stream().filter(a -> a.id.get().equals("mapstitch:atlas")).findFirst().orElseThrow().maxStackCount.get() == 7, "explicit MapStitch limits retained");
        c.allowedItems.accept(new LinkedHashSet<>(List.of(new AllowedItem("minecraft:clock", 1, 1))));
        c.update(1);
        check(c.allowedItems.size() == 1, "legacy atlas opt-out preserved");
        lines.add("PASS config defaults and upgrade policy");
    }
    @Override public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> { if (finished && ++drain >= 3) server.halt(false); });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                migration();
                caseFor(server.overworld(), false);
                caseFor(server.overworld(), true);
                manyAtlases(server.overworld());
                lines.addFirst("PASS " + checks + " engine assertions; no GUI/network claims");
                Files.write(Path.of("result.txt"), lines);
                lines.forEach(System.out::println);
            } catch (Throwable t) {
                t.printStackTrace();
                try { Files.writeString(Path.of("result.txt"), "FAIL " + t + "\n" + String.join("\n", lines)); }
                catch (Exception e) { throw new RuntimeException(e); }
            } finally { finished = true; }
        });
    }
}
