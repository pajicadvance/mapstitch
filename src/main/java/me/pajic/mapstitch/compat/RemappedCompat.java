package me.pajic.mapstitch.compat;

//? >=26.1 && fabric {

import dev.worldgen.remapped.duck.MapDataDuck;
import dev.worldgen.remapped.network.BaseMapUpdatePacket;
import me.pajic.mapstitch.util.ModUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

public class RemappedCompat {

    public static void sendMapPackets(MapId id, MapItemSavedData data, ServerPlayer player) {
        List<CustomPacketPayload> packets = MapDataDuck.cast(data).getRemappedPackets(id, player);
        packets.forEach((packet) -> ServerPlayNetworking.send(player, packet));
        if (packets.stream().noneMatch(packet -> packet instanceof BaseMapUpdatePacket)) {
            ModUtil.sendVanillaMapPacket(id, data, player, false);
        }
    }
}
//?}
