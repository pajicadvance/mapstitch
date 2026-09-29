package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.compat.ToolPouchCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import me.pajic.mapstitch.networking.payload.S2CSyncWorldMap;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;

import java.util.ArrayList;
import java.util.List;

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
        List<ItemStack> pouchAtlases = ToolPouchCompat.getAtlases(player);
        items.addAll(pouchAtlases);
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
                    int pouchIndex = pouchAtlases.indexOf(stack);
                    if (pouchIndex != -1) ToolPouchCompat.saveAtlas(player, stack, pouchIndex);
                    MultiLoaderUtil.INSTANCE.s2c((ServerPlayer) player, new S2CSyncWorldMap());
					break;
				}
			}
		}
	}
}
