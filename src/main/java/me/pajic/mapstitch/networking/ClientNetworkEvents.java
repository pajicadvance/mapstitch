package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientNetworkEvents {

    public static void setDimensionIds(S2CDimensionIds payload) {
        WorldMapScreen.dimensionIds = payload.dimensionIds();
    }

    public static void setCompassRequired(S2CCompassGameRule payload) {
        ModUtil.compassRequired = payload.required();
    }

    public static void openWorldMapScreen(IPayloadContext context) {
        context.player().playSound(SoundEvents.BOOK_PAGE_TURN);
        Minecraft.getInstance().setScreen(new WorldMapScreen());
    }
}
