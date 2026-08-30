package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record S2CSyncWorldMap() implements CustomPacketPayload {
    public static final Type<S2CSyncWorldMap> TYPE = new Type<>(MapStitch.id("sync_world_map"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncWorldMap> CODEC = StreamCodec.unit(new S2CSyncWorldMap());

    @Override @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
