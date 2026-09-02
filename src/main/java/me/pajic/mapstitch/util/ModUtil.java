package me.pajic.mapstitch.util;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.enchantment.ModEnchantments;
import me.pajic.mapstitch.extension.MapItemSavedDataExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;

public class ModUtil {

    public static List<Identifier> dimensionIds = List.of();

    public static void sendVanillaMapPacket(MapId id, MapItemSavedData data, ServerPlayer player, boolean force) {
        Packet<?> packet = force ? ((MapItemSavedDataExtension) data).mapstitch$forceUpdatePacket(id, player) : data.getUpdatePacket(id, player);
        if (packet != null) player.connection.send(packet);
    }

    public static LootPool.Builder getGlobetrotterLootPool(HolderLookup.Provider registry) {
        return MapStitch.CONFIG.globetrotter.enabled.get() ? LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(MapStitch.CONFIG.globetrotter.chance.get())
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(
                                registry.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.GLOBETROTTER),
                                ConstantValue.exactly(1))))
                .add(EmptyLootItem.emptyItem().setWeight(100 - MapStitch.CONFIG.globetrotter.chance.get())) : LootPool.lootPool();
    }

    public static boolean isGlobetrotterLootTable(ResourceKey<LootTable> key) {
        Identifier id = key.identifier();
        return id.equals(BuiltInLootTables.STRONGHOLD_LIBRARY.identifier()) ||
                // d&t stronghold overhaul library pool
                id.equals(ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("chests/stronghold/library_bookshelf")).identifier());
    }

    public static boolean isExplorationMap(MapItemSavedData data) {
        for (MapDecoration decor : data.getDecorations()) {
            if (isExplorationMarker(decor)) return true;
        }
        return false;
    }

    // vanilla doesn't consider buried treasure maps to be explorer maps for some reason
    public static boolean isExplorationMarker(MapDecoration decor) {
        return decor.type().value().explorationMapElement() || decor.type().is(MapDecorationTypes.RED_X.unwrapKey().orElseThrow());
    }

}
