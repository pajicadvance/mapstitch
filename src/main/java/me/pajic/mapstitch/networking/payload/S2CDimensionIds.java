package me.pajic.mapstitch.networking.payload;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record S2CDimensionIds(List<Identifier> dimensionIds) implements CustomPacketPayload {

	public static final Type<S2CDimensionIds> TYPE = new Type<>(MapStitch.id("dimension_ids"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CDimensionIds> CODEC = CustomPacketPayload.codec(
			S2CDimensionIds::write,
			S2CDimensionIds::new
	);

	public S2CDimensionIds(RegistryFriendlyByteBuf buf) {
		this(buf.readList(FriendlyByteBuf::readIdentifier));
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeCollection(dimensionIds, FriendlyByteBuf::writeIdentifier);
	}

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
