package me.pajic.mapstitch.extension;

import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapId;

public interface HoldingPlayerExtension {
    Packet<?> mapstitch$forceUpdatePacket(MapId id);
}
