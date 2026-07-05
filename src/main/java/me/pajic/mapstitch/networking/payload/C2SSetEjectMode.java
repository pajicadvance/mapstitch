package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record C2SSetEjectMode(int slotId) implements CustomPacketPayload {
    public static final Type<C2SSetEjectMode> TYPE = new Type<>(MapStitch.id("eject_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, C2SSetEjectMode> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2SSetEjectMode::slotId,
            C2SSetEjectMode::new
    );

    @Override @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
