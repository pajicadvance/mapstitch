package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record S2CMaxAtlasItemsGameRule(int maxSize) implements CustomPacketPayload {
	public static final Type<S2CMaxAtlasItemsGameRule> TYPE = new Type<>(MapStitch.id("max_atlas_items"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CMaxAtlasItemsGameRule> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, S2CMaxAtlasItemsGameRule::maxSize,
			S2CMaxAtlasItemsGameRule::new
	);

	@Override @NotNull
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
