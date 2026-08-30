package me.pajic.mapstitch.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;

public class ModDataComponents {

	public static final DataComponentType<Integer> ATLAS_SCALE = DataComponentType.<Integer>builder()
			.persistent(ExtraCodecs.intRange(0, 4)).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static final DataComponentType<Integer> ATLAS_FULLNESS = DataComponentType.<Integer>builder()
			.persistent(ExtraCodecs.intRange(0, 4)).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static final DataComponentType<Integer> ATLAS_ACTIVE_MAP_ID = DataComponentType.<Integer>builder()
			.persistent(ExtraCodecs.intRange(-1, Integer.MAX_VALUE)).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static final DataComponentType<Boolean> ATLAS_EJECT_FILLED_MAPS_FIRST = DataComponentType.<Boolean>builder()
			.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();

	public static final DataComponentType<Vector2i> MAP_CENTER = DataComponentType.<Vector2i>builder()
			.persistent(Codec.INT.listOf().comapFlatMap(
					input -> Util.fixedSize(input, 2).map(ints -> new Vector2i(ints.getFirst(), ints.get(1))),
					vec -> List.of(vec.x, vec.y))
			).networkSynchronized(new StreamCodec<>() {
				@Override
				public void encode(@NotNull RegistryFriendlyByteBuf output, @NotNull Vector2i value) {
					output.writeInt(value.x);
					output.writeInt(value.y);
				}
				@Override @NotNull
				public Vector2i decode(@NotNull RegistryFriendlyByteBuf input) {
					return new Vector2i(input.readInt(), input.readInt());
				}
			}).cacheEncoding().build();
}
