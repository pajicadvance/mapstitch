package me.pajic.mapstitch.item;

//? >=26.1 {

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import me.pajic.mapstitch.MapStitch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AtlasTrinketItem extends AtlasItem implements TrinketCallback {

	public AtlasTrinketItem(Properties properties) {
		super(properties);
	}

	@Override
	public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        //~ if <26.1 'null' -> '0, true'
		stack.inventoryTick(entity.level(), entity, null);
	}

    @Override
    public TrinketDropRule getDropRule(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        return MapStitch.CONFIG.keepAtlasOnDeath.get() ? TrinketDropRule.KEEP : TrinketDropRule.DEFAULT;
    }
}
//?}
