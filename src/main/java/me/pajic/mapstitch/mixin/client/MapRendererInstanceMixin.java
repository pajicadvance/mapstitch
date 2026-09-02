package me.pajic.mapstitch.mixin.client;

//? <26.1 {

/*import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.util.ModClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MapRenderer.MapInstance.class)
public class MapRendererInstanceMixin {

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
        return original || !ModClientUtil.hasCompass(Minecraft.getInstance(), "playerMarker");
    }

    @ModifyExpressionValue(
            method = "draw",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/saveddata/maps/MapDecoration;renderOnFrame()Z"
            )
    )
    private boolean modifyRenderCondition(boolean original, @Local MapDecoration decor) {
        if (ModClientUtil.worldMapOpen) return false;
        if (ModClientUtil.DECORS_REQUIRING_COMPASS.contains(decor.type())) {
            return ModClientUtil.hasCompass(Minecraft.getInstance(), "playerMarker");
        }
        return original;
    }
}
*///?}
