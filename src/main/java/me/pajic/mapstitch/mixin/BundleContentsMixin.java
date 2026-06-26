package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.pajic.mapstitch.extension.BundleContentsExtension;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.mixin.accessor.BundleContentsAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BundleContents.class)
public class BundleContentsMixin implements BundleContentsExtension {
	@Unique private boolean mapstitch$isAtlas = false;

	@Override
	public void mapstitch$setIsAtlas() {
		mapstitch$isAtlas = true;
	}

	@Override
	public boolean mapstitch$isAtlas() {
		return mapstitch$isAtlas;
	}

	@Mixin(BundleContents.Mutable.class)
	private static abstract class MutableMixin implements BundleContentsMutableExtension {
		@Shadow @Final private List<ItemStack> items;
		@Shadow public abstract @Nullable ItemStack removeOne();

		@Shadow private Fraction weight;
		@Unique private boolean mapstitch$isAtlas = false;

		@Override
		public void mapstitch$removeOneItemAtIndex(int index) {
			if (!items.isEmpty()) {
				ItemStack stack = items.get(index).copy();
				stack.shrink(1);
				weight = weight.subtract(BundleContentsAccessor.getWeight(stack).getOrThrow().multiplyBy(Fraction.getFraction(stack.getCount(), 1)));
				if (stack.isEmpty()) items.remove(index);
				else items.set(index, stack);
			}
		}

		@Override
		public ItemStack mapstitch$removeOneStackOrdered(boolean filledMapsFirst) {
			if (!items.isEmpty()) {
				if (filledMapsFirst) {
					int filledMapIndex = -1;
					for (int i = 0; i < items.size(); i++) {
						ItemStack stack = items.get(i);
						if (stack.is(Items.FILLED_MAP)) {
							filledMapIndex = i;
							break;
						}
					}
					if (filledMapIndex == -1) return removeOne();
					else {
						ItemStack stack = items.remove(filledMapIndex).copy();
						weight = weight.subtract(BundleContentsAccessor.getWeight(stack).getOrThrow().multiplyBy(Fraction.getFraction(stack.getCount(), 1)));
						return stack;
					}
				} else {
					int emptyMapIndex = -1;
					for (int i = 0; i < items.size(); i++) {
						ItemStack stack = items.get(i);
						if (stack.is(Items.MAP)) {
							emptyMapIndex = i;
							break;
						}
					}
					if (emptyMapIndex == -1) return removeOne();
					else {
						ItemStack stack = items.remove(emptyMapIndex).copy();
						weight = weight.subtract(BundleContentsAccessor.getWeight(stack).getOrThrow().multiplyBy(Fraction.getFraction(stack.getCount(), 1)));
						return stack;
					}
				}
			}
			return ItemStack.EMPTY;
		}

		@Inject(
				method = "<init>",
				at = @At("TAIL")
		)
		private void passAtlasFlag(BundleContents contents, CallbackInfo ci) {
			mapstitch$isAtlas = ((BundleContentsExtension) (Object) contents).mapstitch$isAtlas();
		}

		@ModifyReturnValue(
				method = "toImmutable",
				at = @At("RETURN")
		)
		private BundleContents setAtlasFlag(BundleContents original) {
			if (mapstitch$isAtlas) ((BundleContentsExtension) (Object) original).mapstitch$setIsAtlas();
			return original;
		}

		@ModifyExpressionValue(
				method = "getMaxAmountToAdd",
				at = @At(
						value = "FIELD",
						target = "Lorg/apache/commons/lang3/math/Fraction;ONE:Lorg/apache/commons/lang3/math/Fraction;",
						opcode = Opcodes.GETSTATIC
				)
		)
		private Fraction increaseCapacity(Fraction original) {
			return mapstitch$isAtlas ? AtlasItem.MAX_SIZE : original;
		}

		@WrapOperation(
				method = "findStackIndex",
				at = @At(
						value = "INVOKE",
						target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
				)
		)
		private boolean checkStackSizeLimit(ItemStack a, ItemStack b, Operation<Boolean> original) {
			boolean result = original.call(a, b);
			if (mapstitch$isAtlas) return result && a.count() + b.count() <= a.getMaxStackSize();
			return result;
		}
	}
}
