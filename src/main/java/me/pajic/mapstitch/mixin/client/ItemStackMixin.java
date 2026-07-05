package me.pajic.mapstitch.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(
			method = "getTooltipLines",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
					ordinal = 0
			)
	)
	private void addAtlasTooltip(
			CallbackInfoReturnable<List<Component>> cir,
			@Local List<Component> list
	) {
		ItemStack self = (ItemStack) (Object) this;
		if (self.is(ModItems.ATLAS)) list.addAll(AtlasItem.getTooltip(self));
	}
}
