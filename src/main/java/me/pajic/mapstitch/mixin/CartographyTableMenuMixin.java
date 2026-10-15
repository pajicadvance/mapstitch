package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.recipe.AtlasCartographyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CartographyTableMenu.class)
public class CartographyTableMenuMixin {

    @Shadow @Final public Container container;
    @Shadow @Final private ResultContainer resultContainer;

    @Mixin(targets = {
            "net/minecraft/world/inventory/CartographyTableMenu$3",
            "net/minecraft/world/inventory/CartographyTableMenu$4"
    })
    public static class CartographyTableMenuInputSlotsMixin {

        @ModifyReturnValue(
                method = "mayPlace",
                at = @At("RETURN")
        )
        private boolean allowAtlases(boolean original, @Local(argsOnly = true) final ItemStack itemStack) {
            return original || itemStack.is(ModItems.ATLAS);
        }
    }

    @Mixin(targets = "net/minecraft/world/inventory/CartographyTableMenu$5")
    public static class CartographyTableMenuResultSlotMixin {

        @Shadow @Final private CartographyTableMenu this$0;

        @WrapOperation(
                method = "onTake",
                at = @At(
                        value = "INVOKE",
                        target = "Lnet/minecraft/world/inventory/Slot;remove(I)Lnet/minecraft/world/item/ItemStack;",
                        ordinal = 0
                )
        )
        private ItemStack keepOriginalAtlasOnCopy(Slot instance, int i, Operation<ItemStack> original) {
            return AtlasCartographyHandler.isCopyRecipe(
                    this$0.container.getItem(0),
                    this$0.container.getItem(1)
            ) ? ItemStack.EMPTY : original.call(instance, i);
        }
    }

    @WrapMethod(method = {"lambda$setupResultSlot$0", "method_17382"})
    private void handleAtlasCopy(
            ItemStack mapStack,
            ItemStack additionalStack,
            ItemStack resultStack,
            Level level,
            BlockPos pos,
            Operation<Void> original
    ) {
        if (AtlasCartographyHandler.isCopyRecipe(container.getItem(0), container.getItem(1))) {
            resultContainer.setItem(2, AtlasCartographyHandler.createCopy(mapStack, additionalStack, level));
        } else {
            original.call(mapStack, additionalStack, resultStack, level, pos);
        }
    }
}
