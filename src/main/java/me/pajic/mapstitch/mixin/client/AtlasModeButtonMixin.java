package me.pajic.mapstitch.mixin.client;

import me.pajic.mapstitch.recipe.AtlasCartographyHandler;
import me.pajic.mapstitch.util.ModClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AtlasModeButtonMixin extends Screen {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow @Final protected AbstractContainerMenu menu;
    @Unique private @Nullable Button mapstitch$atlasModeButton;

    protected AtlasModeButtonMixin(Component title) { super(title); }

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    private void addAtlasModeButton(CallbackInfo ci) {
        mapstitch$atlasModeButton = null;
        if ((AbstractContainerScreen<?>) (Object) this instanceof CartographyTableScreen) {
            int w = ModClientUtil.cartographyButtonWidth(font);
            mapstitch$atlasModeButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.gameMode != null)
                    mc.gameMode.handleInventoryButtonClick(menu.containerId, AtlasCartographyHandler.TOGGLE_MODE_BUTTON);
            }).bounds(
                    leftPos + 67 + 66 / 2 - w / 2,
                    topPos + 13 + 66 / 2 - 16 / 2,
                    w, 16
            ).build());
            ModClientUtil.cartographyUpdateButton(mapstitch$atlasModeButton, menu);
        }
    }

    @Inject(
            method = "containerTick",
            at = @At("HEAD")
    )
    private void tickAtlasModeButton(CallbackInfo ci) {
        ModClientUtil.cartographyUpdateButton(mapstitch$atlasModeButton, menu);
    }
}
