package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? >=26.1
import net.minecraft.world.entity.EquipmentSlot;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Shadow @Final public Player player;

    @Inject(
			method = "tick",
			at = @At("HEAD")
	)
	private void initAtlasCount(CallbackInfo ci, @Share("atlasCount") LocalIntRef atlasCountRef) {
		atlasCountRef.set(0);
	}

	@WrapWithCondition(
			method = "tick",
			at = @At(
					value = "INVOKE",
                    //? >=26.1
					target = "Lnet/minecraft/world/item/ItemStack;inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
                    //? <26.1
                    //target = "Lnet/minecraft/world/item/ItemStack;inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;IZ)V"
			)
	)
    //~ if <26.1 'EquipmentSlot slot' -> 'int inventorySlot, boolean isCurrentItem'
	private boolean checkAtlasTick(ItemStack instance, Level level, Entity owner, EquipmentSlot slot, @Share("atlasCount") LocalIntRef atlasCountRef) {
		if (instance.is(ModItems.ATLAS)) {
			int c = atlasCountRef.get();
			if (c == 5) return false;
			atlasCountRef.set(c + 1);
		}
		return true;
	}

    @ModifyExpressionValue(
            method = "dropAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean keepAtlas(boolean original, @Local ItemStack itemStack) {
        return (MapStitch.CONFIG.keepAtlasOnDeath.get() && itemStack.is(ModItems.ATLAS)) || original;
    }
}
