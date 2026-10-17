package me.pajic.mapstitch.util;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.enchantment.ModEnchantments;
import me.pajic.mapstitch.extension.MapItemSavedDataExtension;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;

//? <26.3 {
/*import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
*///?} else {
import net.minecraft.tags.ItemTags;
//?}

//~ if >26.2 'number.ConstantValue' -> 'number.ints.ContextIntProviders'
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.List;

public class ModUtil {

    public static List<Identifier> dimensionIds = List.of();

    public static void sendVanillaMapPacket(MapId id, MapItemSavedData data, ServerPlayer player, boolean force) {
        Packet<?> packet = force ? ((MapItemSavedDataExtension) data).mapstitch$forceUpdatePacket(id, player) : data.getUpdatePacket(id, player);
        if (packet != null) player.connection.send(packet);
    }

    public static LootPool.Builder getGlobetrotterLootPool(HolderGetter.Provider registry) {
        return MapStitch.CONFIG.globetrotter.enabled.get() ? LootPool.lootPool()
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(MapStitch.CONFIG.globetrotter.chance.get())
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(
                                registry.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.GLOBETROTTER),
                                //~ if >26.2 'ConstantValue' -> 'ContextIntProviders'
                                ContextIntProviders.exactly(1))))
                .add(EmptyLootItem.emptyItem().setWeight(100 - MapStitch.CONFIG.globetrotter.chance.get())) : LootPool.lootPool();
    }

    public static boolean isGlobetrotterLootTable(ResourceKey<LootTable> key) {
        return MapStitch.CONFIG.globetrotter.lootTables.get().contains(key.identifier());
    }

    public static boolean isExplorationMap(ItemStack map, Level level) {
        MapItemSavedData mapData = MapItem.getSavedData(map.get(DataComponents.MAP_ID), level);
        if (mapData != null) {
            //? <26.3 {
            /*for (MapDecoration decor : mapData.getDecorations()) {
                if (decor.type().value().explorationMapElement() ||
                        decor.type().is(MapDecorationTypes.RED_X.unwrapKey().orElseThrow())) return true;
            }
            *///?} else {
            return !map.is(ItemTags.EXTENDABLE_MAPS);
            //?}
        }
        return false;
    }

    public static BundleContents.Mutable toMutable(BundleContents contents) {
        //? <26.3 {
        /*return new BundleContents.Mutable(contents);
        *///?} else {
        return contents.asMutable();
        //?}
    }
}
