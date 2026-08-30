package me.pajic.mapstitch.mixin.client;

//? fabric && <26.1 {

/*import me.pajic.mapstitch.minimap.MinimapOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(
            method = "renderEffects",
            at = @At("HEAD")
    )
    private void renderMinimap(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        MinimapOverlay.render(guiGraphics);
    }
}
*///?}
