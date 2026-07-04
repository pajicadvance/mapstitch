package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public class InventoryMixin {

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
					target = "Lnet/minecraft/world/item/ItemStack;inventoryTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
			)
	)
	private boolean checkAtlasTick(ItemStack instance, Level level, Entity owner, EquipmentSlot slot, @Share("atlasCount") LocalIntRef atlasCountRef) {
		if (instance.is(ModItems.ATLAS)) {
			int c = atlasCountRef.get();
			if (c == 5) return false;
			atlasCountRef.set(c + 1);
		}
		return true;
	}
}
