package me.pajic.mapstitch.networking;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.NotNull;

public record C2SPlaySoundPayload(Holder<SoundEvent> sound) implements CustomPacketPayload {
	public static final Type<C2SPlaySoundPayload> TYPE = new Type<>(MapStitch.id("play_sound"));
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SPlaySoundPayload> CODEC = StreamCodec.composite(
			SoundEvent.STREAM_CODEC, C2SPlaySoundPayload::sound,
			C2SPlaySoundPayload::new
	);

	@Override @NotNull
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
