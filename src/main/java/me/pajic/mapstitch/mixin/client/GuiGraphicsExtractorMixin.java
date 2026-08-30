package me.pajic.mapstitch.mixin.client;

//? >=26.1 {

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsExtractorMixin {

	@Shadow @Final private Minecraft minecraft;

	@ModifyExpressionValue(
			method = "map",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/state/MapRenderState$MapDecorationRenderState;renderOnFrame:Z",
					opcode = Opcodes.GETFIELD
			)
	)
	private boolean checkCompass(boolean original, @Local(name = "decoration") MapRenderState.MapDecorationRenderState decoration) {
		Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decoration).mapstitch$getDecorationType();
		if (ModUtil.DECORS_REQUIRING_COMPASS.contains(type)) return original && ModUtil.hasCompass(minecraft, "playerMarker");
		return original;
	}
}
//?}
