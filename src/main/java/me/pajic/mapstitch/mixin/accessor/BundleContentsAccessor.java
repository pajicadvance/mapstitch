package me.pajic.mapstitch.mixin.accessor;

import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

//~ if <26.1 'ItemInstance' -> 'ItemStack'
import net.minecraft.world.item.ItemInstance;

//? >=26.1
import com.mojang.serialization.DataResult;

@Mixin(BundleContents.class)
public interface BundleContentsAccessor {

	@Invoker("getWeight")
    //~ if <26.1 'DataResult<Fraction>' -> 'Fraction'
    //~ if <26.1 'ItemInstance' -> 'ItemStack'
	static DataResult<Fraction> getWeight(final ItemInstance item) {
		throw new AssertionError();
	}
}
