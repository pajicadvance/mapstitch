package me.pajic.mapstitch.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(
			method = "getTooltipLines",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;addDetailsToTooltip(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;Ljava/util/function/Consumer;)V"
			)
	)
	private void addAtlasTooltip(
			CallbackInfoReturnable<List<Component>> cir,
			@Local(name = "lines") List<Component> lines
	) {
		ItemStack self = (ItemStack) (Object) this;
		if (self.is(ModItems.ATLAS)) lines.addAll(AtlasItem.getTooltip(self));
	}
}
