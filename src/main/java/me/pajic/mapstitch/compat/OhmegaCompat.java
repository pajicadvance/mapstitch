package me.pajic.mapstitch.compat;

import com.swacky.ohmega.api.AccessoryHelper;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.AtlasOhmegaItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

public class OhmegaCompat {

	public static boolean hasCompassInOhmegaSlot(LivingEntity entity) {
		if (entity instanceof Player player) {
			NonNullList<ItemStack> items = AccessoryHelper.getAccessoryStacks(player);
			if (items.stream().anyMatch(stack -> stack.is(Items.COMPASS))) return true;
			List<ItemStack> list = items.stream().filter(stack -> stack.has(DataComponents.CONTAINER)).toList();
			return list.stream().anyMatch(stack -> {
				ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
				for (ItemStackTemplate itemStackTemplate : contents.nonEmptyItems()) {
					if (itemStackTemplate.is(Items.COMPASS)) return true;
				}
				return false;
			});
		}
		return false;
	}

	public static List<ItemStack> getOhmegaAtlases(LivingEntity entity) {
		if (entity instanceof Player player) {
			return AccessoryHelper.getAccessoryStacks(player).stream().filter(stack -> stack.is(ModItems.ATLAS)).toList();
		}
		return List.of();
	}

	public static AtlasItem makeOhmegaAtlas(Item.Properties properties) {
		return new AtlasOhmegaItem(properties);
	}
}
