package me.pajic.mapstitch.util;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.enchantment.ModEnchantments;
import me.pajic.mapstitch.extension.MapItemSavedDataExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

//? >=26.1 {
import net.minecraft.world.item.ItemStackTemplate;
//?}

public class ModUtil {

	public static boolean worldMapOpen = false;
	public static final Set<Holder<MapDecorationType>> DECORS_REQUIRING_COMPASS = Set.of(
			MapDecorationTypes.PLAYER,
			MapDecorationTypes.PLAYER_OFF_MAP,
			MapDecorationTypes.PLAYER_OFF_LIMITS
	);
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

	public static boolean hasCompass(Minecraft mc, String context) {
        return hasItem(mc, Items.COMPASS, context, MapStitch.CONFIG.itemRequirements.compass);
	}

    public static boolean hasClock(Minecraft mc, String context) {
        return hasItem(mc, Items.CLOCK, context, MapStitch.CONFIG.itemRequirements.clock);
    }

    public static boolean hasItem(Minecraft mc, Item item, String context, List<String> reqs) {
        if (mc.player == null) return false;
        if (!reqs.contains(context)) return true;
        List<String> locations = MapStitch.CONFIG.itemRequirements.compassAndClockScan;
        if (locations.contains("accessories") && AccessoryUtil.INSTANCE != null && AccessoryUtil.INSTANCE.hasItem(item, mc.player)) return true;
        if (locations.contains("mainHand") && mc.player.getMainHandItem().is(item)) return true;
        Set<BundleContents> bundles = new HashSet<>();
        Set<ItemContainerContents> containers = new HashSet<>();
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                if (locations.contains("offhand") && i == 40) return true;
                if (locations.contains("hotbar") && i < 9) return true;
                if (locations.contains("inventory") && i >= 9 && i < 36) return true;
            }
            if (locations.contains("bundles") && stack.has(DataComponents.BUNDLE_CONTENTS)) {
                bundles.add(stack.get(DataComponents.BUNDLE_CONTENTS));
            }
            if (locations.contains("containerItems") && stack.has(DataComponents.CONTAINER)) {
                containers.add(stack.get(DataComponents.CONTAINER));
            }
        }
        if (!bundles.isEmpty()) for (BundleContents bundle : bundles) {
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate stack : bundle.items()) if (stack.is(item)) return true;
        }
        if (!containers.isEmpty()) for (ItemContainerContents container : containers) {
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate stack : container.nonEmptyItems()) if (stack.is(item)) return true;
        }
        return false;
    }

    public static ItemStack getFirstItem(Minecraft mc, Item item) {
        if (mc.player == null) return ItemStack.EMPTY;
        List<String> locations = MapStitch.CONFIG.itemRequirements.minimapAtlasScan;
        if (locations.contains("accessories") && AccessoryUtil.INSTANCE != null) {
            ItemStack accessory = AccessoryUtil.INSTANCE.getFirstItem(item, mc.player);
            if (accessory.is(item)) return accessory;
        }
        if (locations.contains("mainHand")) {
            ItemStack mainHand = mc.player.getMainHandItem();
            if (mainHand.is(item)) return mainHand;
        }
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                if (locations.contains("offhand") && i == 40) return stack;
                if (locations.contains("hotbar") && i < 9) return stack;
                if (locations.contains("inventory") && i >= 9 && i < 36) return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
