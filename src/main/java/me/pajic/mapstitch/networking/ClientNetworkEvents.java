package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2CMaxAtlasItemsGameRule;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public class ClientNetworkEvents {

    public static void setDimensionIds(S2CDimensionIds payload) {
        WorldMapScreen.dimensionIds = payload.dimensionIds();
    }

    public static void setCompassRequired(S2CCompassGameRule payload) {
        ModUtil.compassRequired = payload.required();
    }

	public static void setMaxAtlasSize(S2CMaxAtlasItemsGameRule payload) {
		AtlasItem.setMaxSize(payload.maxSize());
	}

    public static void openWorldMapScreen(Player player) {
        player.playSound(SoundEvents.BOOK_PAGE_TURN);
        Minecraft.getInstance().setScreenAndShow(new WorldMapScreen());
    }
}
