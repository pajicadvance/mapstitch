package me.pajic.mapstitch.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BundleItem.class)
public interface BundleItemAccessor {

	@Accessor("FULL_BAR_COLOR")
	static int mapstitch$getFullBarColor() {
		throw new AssertionError();
	}

	@Accessor("BAR_COLOR")
	static int mapstitch$getBarColor() {
		throw new AssertionError();
	}

	@Invoker("playInsertFailSound")
	static void mapstitch$callPlayInsertFailSound(final Entity entity) {}

	@Invoker("playInsertSound")
	static void mapstitch$callPlayInsertSound(final Entity entity) {}

	@Invoker("playRemoveOneSound")
	static void mapstitch$callPlayRemoveOneSound(final Entity entity) {}
}
