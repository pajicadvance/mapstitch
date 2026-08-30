package me.pajic.mapstitch.extension;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapId;

public interface MapItemSavedDataExtension {
    Packet<?> mapstitch$forceUpdatePacket(MapId id, Player player);
}
