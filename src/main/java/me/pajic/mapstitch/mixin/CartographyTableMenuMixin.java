package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.extension.CartographyTableMenuExtension;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.recipe.AtlasCartographyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuMixin extends AbstractContainerMenu implements CartographyTableMenuExtension {

    @Shadow @Final public Container container;
    @Shadow @Final private ResultContainer resultContainer;

    @Unique private final DataSlot mapstitch$atlasMode = DataSlot.standalone();
    @Unique private final DataSlot mapstitch$atlasStatus = DataSlot.standalone();
    @Unique private final DataSlot mapstitch$atlasAmount = DataSlot.standalone();

    protected CartographyTableMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(
            method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL")
    )
    private void addAtlasDataSlots(CallbackInfo ci) {
        addDataSlot(mapstitch$atlasMode);
        addDataSlot(mapstitch$atlasStatus);
        addDataSlot(mapstitch$atlasAmount);
    }

    @Override public int mapstitch$getAtlasMode() {
        return mapstitch$atlasMode.get();
    }

    @Override
    public void mapstitch$setAtlasMode(int mode) {
        mapstitch$atlasMode.set(mode);
    }

    @Override public int mapstitch$getAtlasStatus() {
        return mapstitch$atlasStatus.get();
    }

    @Override public int mapstitch$getAtlasAmount() {
        return mapstitch$atlasAmount.get();
    }

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
        private ItemStack keepSourceAtlasOnCopy(Slot instance, int i, Operation<ItemStack> original) {
            boolean copy = AtlasCartographyHandler.isAtlasRelatedRecipe(
                    this$0.container.getItem(0),
                    this$0.container.getItem(1)
            ) && ((CartographyTableMenuExtension) this$0).mapstitch$getAtlasMode() == AtlasCartographyHandler.MODE_COPY;
            return copy ? ItemStack.EMPTY : original.call(instance, i);
        }
    }

    @WrapOperation(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;has(Lnet/minecraft/core/component/DataComponentType;)Z"
            )
    )
    private boolean allowAtlasQuickMove(ItemStack instance, DataComponentType<?> dataComponentType, Operation<Boolean> original) {
        return original.call(instance, dataComponentType) || instance.is(ModItems.ATLAS);
    }

    @WrapOperation(
            method = "quickMoveStack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/CartographyTableMenu;moveItemStackTo(Lnet/minecraft/world/item/ItemStack;IIZ)Z"
            )
    )
    private boolean handleAtlasQuickMove(
            CartographyTableMenu instance,
            ItemStack stack,
            int start,
            int end,
            boolean reverse,
            Operation<Boolean> original
    ) {
        if (start == 0 && end == 1 && stack.is(ModItems.ATLAS)) {
            ItemStack top = container.getItem(0);
            end = top.isEmpty() || top.is(ModItems.ATLAS) ? 2 : 1;
        }
        return original.call(instance, stack, start, end, reverse);
    }

    @WrapMethod(method = {"lambda$setupResultSlot$0", "method_17382"})
    private void handleAtlasRecipes(
            ItemStack mapStack,
            ItemStack additionalStack,
            ItemStack resultStack,
            Level level, BlockPos pos,
            Operation<Void> original
    ) {
        if (AtlasCartographyHandler.isAtlasRelatedRecipe(mapStack, additionalStack)) {
            AtlasCartographyHandler.Result result = AtlasCartographyHandler.assemble(
                    mapStack, additionalStack, mapstitch$atlasMode.get(), level
            );
            mapstitch$atlasStatus.set(result.status().ordinal());
            mapstitch$atlasAmount.set(result.amount());
            if (!ItemStack.matches(result.stack(), resultStack)) resultContainer.setItem(2, result.stack());
            broadcastChanges();
        } else {
            mapstitch$atlasStatus.set(0);
            original.call(mapStack, additionalStack, resultStack, level, pos);
        }
    }
}
