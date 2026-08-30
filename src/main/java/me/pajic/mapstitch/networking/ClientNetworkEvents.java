package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class ClientNetworkEvents {

    public static void setDimensionIds(S2CDimensionIds payload) {
        WorldMapScreen.dimensionIds = payload.dimensionIds();
    }

    public static void openWorldMapScreen(Player player, int scaleOverride) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        //~ if <26.1 'setScreenAndShow' -> 'setScreen'
        Minecraft.getInstance().setScreenAndShow(new WorldMapScreen(Math.max(scaleOverride, -1)));
    }

    public static void syncWorldMapScreen() {
        WorldMapScreen.clearMaps();
    }
}
