package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SPlacePin;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SRemovePin;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import me.pajic.mapstitch.networking.payload.C2SSharePin;
import me.pajic.mapstitch.networking.payload.S2CSyncWorldMap;
import me.pajic.mapstitch.pin.Pin;
import me.pajic.mapstitch.pin.PinRefunds;
import me.pajic.mapstitch.pin.PinShares;
import me.pajic.mapstitch.pin.PinStorage;
import me.pajic.mapstitch.pin.PinUtil;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//? >=26.1
import net.minecraft.world.item.ItemStackTemplate;

//? >26.2
import net.minecraft.util.Prediction;

public class ServerNetworkEvents {

    public static void playSound(C2SPlaySound payload, Player player) {
        player.playSound(payload.sound().value());
    }

    public static void setEjectMode(C2SSetEjectMode payload, Player player) {
		AbstractContainerMenu menu = player.containerMenu;
		int slotIndex = payload.slotId();
		if (slotIndex >= 0 && slotIndex < menu.slots.size()) {
		    ItemStack stack = player.containerMenu.getSlot(slotIndex).getItem();
			if (stack.is(ModItems.ATLAS)) {
				stack.set(
						ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST,
						!stack.getOrDefault(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true)
				);
			}
	    }
    }

	public static void ejectMap(C2SEjectMap payload, Player player) {
        //~ if <26.1 '.getNonEquipmentItems()' -> '.items'
		List<ItemStack> items = new ArrayList<>(player.getInventory().getNonEquipmentItems());
        if (AccessoryUtil.INSTANCE != null) items.addAll(AccessoryUtil.INSTANCE.getAtlases(player));
		for (ItemStack stack : items) {
			if (stack.is(ModItems.ATLAS)) {
				BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				int indexToRemove = -1;
				for (int i = 0; i < contents.size(); i++) {
                    //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
                    //~ if <26.1 'items().get(i)' -> 'getItemUnsafe(i)'
					ItemStackTemplate map = contents.items().get(i);
					if (map.get(DataComponents.MAP_ID) != null) {
						MapId mapId = map.get(DataComponents.MAP_ID);
						if (mapId != null && mapId.equals(payload.mapId())) {
							indexToRemove = i;
							break;
						}
					}
				}
				if (indexToRemove != -1) {
					BundleContents.Mutable mutable = ModUtil.toMutable(contents);
					player.drop(
							((BundleContentsMutableExtension) mutable).mapstitch$removeOneItemAtIndex(indexToRemove),
							true
                            //? >26.2
                            , Prediction.SERVER_ONLY
					);
					player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
					((AtlasItem) stack.getItem()).updateAtlas(mutable, stack, player);
                    MultiLoaderUtil.INSTANCE.s2c((ServerPlayer) player, new S2CSyncWorldMap());
					break;
				}
			}
		}
	}

    public static void placePin(C2SPlacePin payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!ModUtil.dimensionIds.isEmpty() && !ModUtil.dimensionIds.contains(payload.dimension())) return;
        if (PinStorage.get(serverPlayer).size() >= PinStorage.MAX_PINS) return;
        String name = StringUtil.filterText(payload.name()).strip();
        if (name.length() > Pin.MAX_NAME_LENGTH) name = name.substring(0, Pin.MAX_NAME_LENGTH);
        int color = payload.color() & 0xFFFFFF;
        boolean pay = PinUtil.itemRequired();
        if (pay && !PinUtil.consumePin(serverPlayer, color)) return;
        PinStorage.add(serverPlayer, new Pin(
                UUID.randomUUID(), name, color, payload.dimension(),
                Mth.clamp(payload.x(), -30_000_000, 30_000_000),
                Mth.clamp(payload.z(), -30_000_000, 30_000_000),
                pay ? color : Pin.UNPAID
        ));
        PinStorage.sync(serverPlayer);
    }

    public static void removePin(C2SRemovePin payload, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Pin removed = PinStorage.remove(serverPlayer, payload.pinId());
        if (removed == null) return;
        if (removed.isPaid() && !PinRefunds.refund(serverPlayer.level().getServer(), serverPlayer.getUUID(), removed.paidColor())) {
            serverPlayer.sendSystemMessage(Component.translatable("mapstitch.chat.pin.refund_pending")
                    .withStyle(ChatFormatting.GRAY));
        }
        PinStorage.sync(serverPlayer);
    }

    public static void sharePin(C2SSharePin payload, Player player) {
        if (player instanceof ServerPlayer serverPlayer) PinShares.share(serverPlayer, payload.pinId(), payload.target());
    }
}
