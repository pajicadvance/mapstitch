package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record S2COpenWorldMapScreen() implements CustomPacketPayload {
	public static final Type<S2COpenWorldMapScreen> TYPE = new Type<>(MapStitch.id("open_world_map_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2COpenWorldMapScreen> CODEC = StreamCodec.unit(
			new S2COpenWorldMapScreen()
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
