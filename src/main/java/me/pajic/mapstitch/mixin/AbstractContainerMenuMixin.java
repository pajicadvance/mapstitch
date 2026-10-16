package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.mapstitch.extension.CartographyTableMenuExtension;
import me.pajic.mapstitch.recipe.AtlasCartographyHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {

    @WrapMethod(method = "clickMenuButton")
    private boolean cartographyClickMenuButton(Player player, int buttonId, Operation<Boolean> original) {
        if ((AbstractContainerMenu) (Object) this instanceof CartographyTableMenu ctm) {
            if (buttonId == AtlasCartographyHandler.TOGGLE_MODE_BUTTON
                    && AtlasCartographyHandler.isAtlasRelatedRecipe(ctm.container.getItem(0), ctm.container.getItem(1))) {
                CartographyTableMenuExtension ext = (CartographyTableMenuExtension) ctm;
                ext.mapstitch$setAtlasMode(ext.mapstitch$getAtlasMode() == AtlasCartographyHandler.MODE_COPY ?
                        AtlasCartographyHandler.MODE_MERGE : AtlasCartographyHandler.MODE_COPY);
                ctm.slotsChanged(ctm.container);
                return true;
            }
        }
        return original.call(player, buttonId);
    }
}
