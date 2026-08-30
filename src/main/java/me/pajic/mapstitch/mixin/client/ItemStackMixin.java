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
                    //? >=26.1 {
					target = "Lnet/minecraft/world/item/ItemStack;addDetailsToTooltip(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;Ljava/util/function/Consumer;)V"
                    //?} else {
                    /*target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
                    ordinal = 0
                    *///?}
			)
	)
	private void addAtlasTooltip(
			CallbackInfoReturnable<List<Component>> cir,
			@Local List<Component> lines
	) {
		ItemStack self = (ItemStack) (Object) this;
		if (self.is(ModItems.ATLAS)) lines.addAll(AtlasItem.getTooltip(self));
	}
}
