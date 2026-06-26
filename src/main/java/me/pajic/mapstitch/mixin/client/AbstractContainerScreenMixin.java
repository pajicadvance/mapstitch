package me.pajic.mapstitch.mixin.client;

import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.mapstitch.keybind.AtlasMouseActions;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

	@Shadow protected abstract void addItemSlotMouseAction(ItemSlotMouseAction itemSlotMouseAction);

	@Inject(
			method = "init",
			at = @At("TAIL")
	)
	private void registerAtlasMouseAction(CallbackInfo ci) {
		addItemSlotMouseAction(new AtlasMouseActions());
	}
}
