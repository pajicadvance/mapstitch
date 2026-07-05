package me.pajic.mapstitch.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ContainerEventHandler.class)
public interface ContainerEventHandlerMixin {

    @SuppressWarnings("RedundantCast")
    @WrapMethod(method = "mouseScrolled")
    private boolean scrollAtlas(double mouseX, double mouseY, double scrollX, double scrollY, Operation<Boolean> original) {
        ContainerEventHandler self = (ContainerEventHandler) (Object) this;
        if (self instanceof AbstractContainerScreen<?> screen) {
            Slot slot = screen.getSlotUnderMouse();
            if (slot != null) {
                ItemStack stack = slot.getItem();
                if (stack.is(ModItems.ATLAS) && scrollY != 0 && stack.has(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST)) {
                    PacketDistributor.sendToServer(new C2SSetEjectMode(slot.index));
                    return true;
                }
            }
        }
        return original.call(mouseX, mouseY, scrollX, scrollY);
    }
}
