package me.pajic.mapstitch.mixin.accessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BundleContents.class)
public interface BundleContentsAccessor {

	@Invoker("getWeight")
	static Fraction getWeight(final ItemStack item) {
		throw new AssertionError();
	}
}
