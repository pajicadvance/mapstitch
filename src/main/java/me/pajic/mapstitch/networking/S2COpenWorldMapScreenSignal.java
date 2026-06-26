package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record S2COpenWorldMapScreenSignal() implements CustomPacketPayload {
	public static final Type<S2COpenWorldMapScreenSignal> TYPE = new Type<>(MapStitch.id("open_world_map_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2COpenWorldMapScreenSignal> CODEC = StreamCodec.unit(
			new S2COpenWorldMapScreenSignal()
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
