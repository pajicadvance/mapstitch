package me.pajic.mapstitch.item;

//? neoforge {

/*import me.pajic.mapstitch.MapStitch;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

//? <26.1 {
import top.theillusivec4.curios.api.type.capability.ICurio;
//?} else {
/^import top.theillusivec4.curios.api.common.DropRule;
^///?}

public class AtlasCurioItem extends AtlasItem implements ICurioItem {

    public AtlasCurioItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        //~ if <26.1 'null' -> '0, true'
		stack.inventoryTick(slotContext.entity().level(), slotContext.entity(), 0, true);
    }

    //~ if <26.1 ' DropRule' -> ' ICurio.DropRule' {
    @Override @NotNull
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, boolean recentlyHit, ItemStack stack) {
        return MapStitch.CONFIG.keepAtlasOnDeath.get() ? ICurio.DropRule.ALWAYS_KEEP : ICurio.DropRule.DEFAULT;
    }
    //~}
}
*///?}
