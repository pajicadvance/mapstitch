package me.pajic.mapstitch.item;

//? fabric && 1.21.1 {

/*import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketEnums;
import me.pajic.mapstitch.MapStitch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AtlasTrinketLegacyItem extends AtlasItem implements Trinket {

    public AtlasTrinketLegacyItem(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        stack.inventoryTick(entity.level(), entity, -1, false);
    }

    @Override
    public TrinketEnums.DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return MapStitch.CONFIG.keepAtlasOnDeath.get() ? TrinketEnums.DropRule.KEEP : TrinketEnums.DropRule.DEFAULT;
    }
}
*///?}
