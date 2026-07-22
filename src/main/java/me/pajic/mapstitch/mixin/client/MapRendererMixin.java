package me.pajic.mapstitch.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(MapRenderer.MapInstance.class)
public class MapRendererMixin {

	@Definition(id = "active", local = @Local(type = boolean.class))
	@Expression("active")
	@ModifyExpressionValue(
			method = "draw",
			at = @At(
					value = "MIXINEXTRAS:EXPRESSION",
					ordinal = 0
			)
	)
	private boolean checkCompass(boolean original) {
		return original || !ModUtil.hasCompass(Minecraft.getInstance());
	}

	@ModifyExpressionValue(
			method = "draw",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/saveddata/maps/MapDecoration;renderOnFrame()Z"
			)
	)
	private boolean checkCompass(boolean original, @Local MapDecoration mapdecoration) {
		if (ModUtil.DECORS_REQUIRING_COMPASS.contains(mapdecoration.type())) {
			return !ModUtil.worldMapOpen && ModUtil.hasCompass(Minecraft.getInstance());
		}
		return original;
	}

	@ModifyArgs(
			method = "draw",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V",
					ordinal = 0
			)
	)
	private void scaleDecorationsInWorldMap(Args args) {
		if (ModUtil.worldMapOpen) {
			float s = (float) Math.pow(2, WorldMapScreen.getZoomLevel());
			args.setAll((float) args.get(0) / s, (float) args.get(1) / s, args.get(2));
		}
	}

	@ModifyExpressionValue(
			method = "draw",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Mth;clamp(FFF)F"
			)
	)
	private float scaleDecorationNamesInWorldMap(float original) {
		return ModUtil.worldMapOpen ? original / (float) Math.pow(2, WorldMapScreen.getZoomLevel()) : original;
	}

	@ModifyArg(
			method = "draw",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
					ordinal = 2
			),
			index = 1
	)
	private float fixDecorationNameHeightWhenScaled(float original) {
		return ModUtil.worldMapOpen ? original - 4F + 4F / (float) Math.pow(2, WorldMapScreen.getZoomLevel()) : original;
	}
}
