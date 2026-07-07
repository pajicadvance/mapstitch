package me.pajic.mapstitch.compat;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.AtlasTrinketItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;

public class TrinketsCompat {

	public static boolean hasCompassInTrinketSlot(LivingEntity entity) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(entity);
		if (attachment.isEquipped(Items.COMPASS)) return true;
		List<ItemStack> list = attachment.equipped(stack -> stack.has(DataComponents.CONTAINER), false)
				.stream().map(TrinketSlotAccess::get).toList();
		return list.stream().anyMatch(stack -> {
			ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
			for (ItemStackTemplate itemStackTemplate : contents.nonEmptyItems()) {
				if (itemStackTemplate.is(Items.COMPASS)) return true;
			}
			return false;
		});
	}

	public static List<ItemStack> getTrinketAtlases(LivingEntity entity) {
		return TrinketsApi.getAttachment(entity)
				.equipped(stack -> stack.is(ModItems.ATLAS), false)
				.stream().map(TrinketSlotAccess::get).toList();
	}

	public static AtlasItem makeTrinketAtlas(Item.Properties properties) {
		return new AtlasTrinketItem(properties);
	}
}
