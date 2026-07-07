package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record S2CCompassGameRule(boolean required) implements CustomPacketPayload {
	public static final Type<S2CCompassGameRule> TYPE = new Type<>(MapStitch.id("compass_gamerule"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CCompassGameRule> CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, S2CCompassGameRule::required,
			S2CCompassGameRule::new
	);

	@Override @NotNull
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
