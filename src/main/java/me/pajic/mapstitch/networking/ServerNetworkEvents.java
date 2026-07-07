package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerNetworkEvents {

    public static void playSound(C2SPlaySound payload, IPayloadContext context) {
        context.player().playSound(payload.sound().value());
    }

    public static void setEjectMode(C2SSetEjectMode payload, IPayloadContext context) {
        ItemStack stack = context.player().containerMenu.getSlot(payload.slotId()).getItem();
        if (stack.is(ModItems.ATLAS)) {
            boolean current = stack.getOrDefault(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true);
            stack.set(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, !current);
        }
    }
}
