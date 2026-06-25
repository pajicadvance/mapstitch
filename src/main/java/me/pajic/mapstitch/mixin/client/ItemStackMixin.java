package me.pajic.mapstitch.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
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
		if (self.is(ModItems.ATLAS)) {
			int scale = self.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
			if (scale != -1) lines.add(Component.translatable("mapstitch.gui.worldmap.scale", Math.powExact(2, scale)).withStyle(ChatFormatting.GRAY));
			BundleContents contents = self.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
			int filledMapCount = 0;
			int emptyMapCount = 0;
			for (ItemStackTemplate map : contents.items()) {
				int count = map.count();
				if (map.is(Items.FILLED_MAP)) filledMapCount += count;
				if (map.is(Items.MAP)) emptyMapCount += count;
			}
			lines.add(Component.translatable("mapstitch.tooltip.atlas.filled_maps", filledMapCount).withStyle(ChatFormatting.GRAY));
			lines.add(Component.translatable("mapstitch.tooltip.atlas.empty_maps", emptyMapCount).withStyle(ChatFormatting.GRAY));
		}
	}
}
