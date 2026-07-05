package me.pajic.mapstitch.mixin.accessor;

import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BundleItem.class)
public interface BundleItemAccessor {

	@Accessor("BAR_COLOR")
	static int mapstitch$getBarColor() {
		throw new AssertionError();
	}
}
