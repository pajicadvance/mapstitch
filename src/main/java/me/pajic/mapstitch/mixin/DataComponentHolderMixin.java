package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.pajic.mapstitch.extension.BundleContentsExtension;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DataComponentHolder.class)
public interface DataComponentHolderMixin {

	@SuppressWarnings("RedundantCast")
	@ModifyReturnValue(
			method = "get",
			at = @At("RETURN")
	)
	private <T> T flagAtlas(@Nullable T original) {
		if (original instanceof BundleContents contents) {
			if ((DataComponentHolder) (Object) this instanceof ItemStack stack) {
				if (stack.is(ModItems.ATLAS)) {
					((BundleContentsExtension) (Object) contents).mapstitch$setIsAtlas();
				}
			}
		}
		return original;
	}
}
