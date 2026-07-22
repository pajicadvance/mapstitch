package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.compat.OhmegaCompat;
import me.pajic.mapstitch.compat.TrinketsCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import me.pajic.mapstitch.util.CompatFlags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.ArrayList;
import java.util.List;

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
		List<ItemStack> items = new ArrayList<>(player.getInventory().getNonEquipmentItems());
		if (CompatFlags.TRINKETS_LOADED) items.addAll(TrinketsCompat.getTrinketAtlases(player));
		if (CompatFlags.OHMEGA_LOADED) items.addAll(OhmegaCompat.getOhmegaAtlases(player));
		for (ItemStack stack : items) {
			boolean dropped = false;
			if (stack.is(ModItems.ATLAS)) {
				BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				int indexToRemove = -1;
				for (int i = 0; i < contents.size(); i++) {
					ItemStackTemplate map = contents.items().get(i);
					if (map.is(Items.FILLED_MAP)) {
						MapId mapId = map.get(DataComponents.MAP_ID);
						if (mapId != null && mapId.equals(payload.mapId())) {
							indexToRemove = i;
							break;
						}
					}
				}
				if (indexToRemove != -1) {
					BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
					player.drop(
							((BundleContentsMutableExtension) mutable).mapstitch$removeOneItemAtIndex(indexToRemove),
							true
					);
					player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
					((AtlasItem) stack.getItem()).updateAtlas(mutable, stack);
					dropped = true;
				}
			} else if (stack.is(Items.FILLED_MAP)) {
				MapId mapId = stack.get(DataComponents.MAP_ID);
				if (mapId != null && mapId.equals(payload.mapId())) {
					player.drop(stack.split(1), true);
					player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
					dropped = true;
				}
			}
			if (dropped) break;
		}
	}
}
