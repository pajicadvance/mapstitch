package me.pajic.mapstitch.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BundleItem.class)
public interface BundleItemAccessor {

	@Accessor("BAR_COLOR")
	static int mapstitch$getBarColor() {
		throw new AssertionError();
	}

    //? >=26.1 {
	@Invoker("playInsertFailSound")
	static void mapstitch$callPlayInsertFailSound(final Entity entity) {}
    //?}
}
