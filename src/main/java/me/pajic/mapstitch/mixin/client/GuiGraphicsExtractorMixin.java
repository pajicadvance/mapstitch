package me.pajic.mapstitch.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kikugie.fletching_table.annotation.MixinEnvironment;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@MixinEnvironment(type = MixinEnvironment.Env.CLIENT)
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
		if (ModUtil.DECORS_REQUIRING_COMPASS.contains(type)) return original && ModUtil.hasCompass(minecraft);
		return original;
	}

	@ModifyArgs(
			method = "map",
			at = @At(
					value = "INVOKE",
					target = "Lorg/joml/Matrix3x2fStack;scale(FF)Lorg/joml/Matrix3x2f;",
					ordinal = 0
			)
	)
	private void scaleDecorationsInWorldMap(Args args) {
		if (ModUtil.worldMapOpen) {
			float s = (float) Math.pow(2, WorldMapScreen.getZoomLevel());
			args.setAll((float) args.get(0) / s, (float) args.get(1) / s);
		}
	}

	@ModifyExpressionValue(
			method = "map",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
			)
	)
	private float scaleDecorationNamesInWorldMap(float original) {
		return ModUtil.worldMapOpen ? original / (float) Math.pow(2, WorldMapScreen.getZoomLevel()) : original;
	}

	@ModifyArg(
			method = "map",
			at = @At(
					value = "INVOKE",
					target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;",
					ordinal = 2
			),
			index = 1
	)
	private float fixDecorationNameHeightWhenScaled(float original) {
		return ModUtil.worldMapOpen ? original - 4F + 4F / (float) Math.pow(2, WorldMapScreen.getZoomLevel()) : original;
	}
}
