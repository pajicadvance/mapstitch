package me.pajic.mapstitch.mixin.client;

//? >=26.1 {

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import me.pajic.mapstitch.util.ModClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapRenderer.class)
public class MapRendererMixin {

	@Inject(
			method = "extractDecorationRenderState",
			at = @At("TAIL")
	)
	private void extractDecorationType(MapDecoration decoration, CallbackInfoReturnable<MapRenderState.MapDecorationRenderState> cir) {
		((MapDecorationRenderStateExtension) cir.getReturnValue()).mapstitch$setDecorationType(decoration.type());
	}

	@Definition(id = "showOnlyFrame", local = @Local(type = boolean.class, name = "showOnlyFrame", argsOnly = true))
	@Expression("showOnlyFrame")
	@ModifyExpressionValue(
			method = "render",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean checkCompass(boolean original) {
		return original || !ModClientUtil.hasCompass(Minecraft.getInstance(), "playerMarker");
	}

	@ModifyExpressionValue(
			method = "render",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/renderer/state/MapRenderState$MapDecorationRenderState;renderOnFrame:Z",
					opcode = Opcodes.GETFIELD
			)
	)
	private boolean checkCompass(boolean original, @Local(name = "decoration") MapRenderState.MapDecorationRenderState decoration) {
		Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decoration).mapstitch$getDecorationType();
		if (ModClientUtil.DECORS_REQUIRING_COMPASS.contains(type)) return original && ModClientUtil.hasCompass(Minecraft.getInstance(), "playerMarker");
		return original;
	}
}
//?}
