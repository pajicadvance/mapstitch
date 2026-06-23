package me.pajic.mapstitch.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

import java.util.List;

public class ModDataComponents {
	public static final DataComponentType<Integer> ATLAS_SCALE = DataComponentType.<Integer>builder()
			.persistent(ExtraCodecs.intRange(0, 4)).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static final DataComponentType<Integer> ATLAS_FULLNESS = DataComponentType.<Integer>builder()
			.persistent(ExtraCodecs.intRange(0, 4)).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static final DataComponentType<Integer> ATLAS_ACTIVE_MAP_ID = DataComponentType.<Integer>builder()
			.persistent(ExtraCodecs.intRange(-1, Integer.MAX_VALUE)).networkSynchronized(ByteBufCodecs.VAR_INT).build();

	public static final DataComponentType<Vector2d> MAP_CENTER = DataComponentType.<Vector2d>builder()
			.persistent(Codec.DOUBLE.listOf().comapFlatMap(
					input -> Util.fixedSize(input, 2).map(floats -> new Vector2d(floats.getFirst(), floats.get(1))),
					vec -> List.of(vec.x, vec.y))
			).networkSynchronized(new StreamCodec<>() {
				@Override
				public void encode(@NotNull RegistryFriendlyByteBuf output, @NotNull Vector2d value) {
					output.writeDouble(value.x);
					output.writeDouble(value.y);
				}
				@Override @NotNull
				public Vector2d decode(@NotNull RegistryFriendlyByteBuf input) {
					return new Vector2d(input.readDouble(), input.readDouble());
				}
			}).cacheEncoding().build();

	public static void init() {}
}
