package me.pajic.mapstitch.compat;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.AtlasTrinketItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class TrinketsCompat {

	public static List<ItemStack> getTrinketAtlases(LivingEntity entity) {
		return TrinketsApi.getAttachment(entity)
				.equipped(stack -> stack.is(ModItems.ATLAS), false)
				.stream().map(TrinketSlotAccess::get).toList();
	}

	public static AtlasItem makeTrinketAtlas(Item.Properties properties) {
		return new AtlasTrinketItem(properties);
	}
}
