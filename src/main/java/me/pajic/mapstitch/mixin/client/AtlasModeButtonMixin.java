package me.pajic.mapstitch.mixin.client;

import me.pajic.mapstitch.extension.CartographyTableMenuExtension;
import me.pajic.mapstitch.recipe.AtlasCartographyHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
            mapstitch$atlasModeButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.gameMode != null)
                    mc.gameMode.handleInventoryButtonClick(menu.containerId, AtlasCartographyHandler.TOGGLE_MODE_BUTTON);
            }).pos(leftPos + 136, topPos + 14).size(34, 16).build());
            mapstitch$updateAtlasModeButton();
        }
    }

    @Inject(
            method = "containerTick",
            at = @At("HEAD")
    )
    private void tickAtlasModeButton(CallbackInfo ci) {
        mapstitch$updateAtlasModeButton();
    }

    @Unique
    private void mapstitch$updateAtlasModeButton() {
        Button button = mapstitch$atlasModeButton;
        if (button == null) return;
        button.visible = AtlasCartographyHandler.isAtlasRelatedRecipe(menu.getSlot(0).getItem(), menu.getSlot(1).getItem());
        if (!button.visible) return;
        CartographyTableMenuExtension ext = (CartographyTableMenuExtension) menu;
        boolean copy = ext.mapstitch$getAtlasMode() == AtlasCartographyHandler.MODE_COPY;
        String key = "mapstitch.gui.cartography.atlas_mode." + (copy ? "copy" : "merge");
        button.setMessage(Component.translatable(key));
        MutableComponent tooltip = Component.translatable(key + ".tooltip");
        Component problem = AtlasCartographyHandler.describe(ext.mapstitch$getAtlasStatus(), ext.mapstitch$getAtlasAmount());
        if (problem != null) tooltip.append("\n\n").append(problem);
        button.setTooltip(Tooltip.create(tooltip));
    }
}
