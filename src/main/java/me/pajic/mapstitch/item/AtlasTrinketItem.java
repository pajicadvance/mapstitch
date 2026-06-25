package me.pajic.mapstitch.item;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AtlasTrinketItem extends AtlasItem implements TrinketCallback {
	public AtlasTrinketItem(Properties properties) {
		super(properties);
	}

	@Override
	public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
		stack.inventoryTick(entity.level(), entity, null);
	}
}
