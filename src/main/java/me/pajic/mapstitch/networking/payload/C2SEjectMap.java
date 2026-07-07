package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.jetbrains.annotations.NotNull;

public record C2SEjectMap(MapId mapId) implements CustomPacketPayload {
	public static final Type<C2SEjectMap> TYPE = new Type<>(MapStitch.id("eject_map"));
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SEjectMap> CODEC = StreamCodec.composite(
			MapId.STREAM_CODEC, C2SEjectMap::mapId,
			C2SEjectMap::new
	);

	@Override @NotNull
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
