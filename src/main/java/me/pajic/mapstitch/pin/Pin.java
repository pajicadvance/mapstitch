package me.pajic.mapstitch.pin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Optional;
import java.util.UUID;

public record Pin(UUID id, String name, int color, Identifier dimension, int x, int z, int paidColor) {

    public static final int MAX_NAME_LENGTH = 32;
    public static final int UNPAID = -1;

    private static final StreamCodec<ByteBuf, String> NAME_STREAM_CODEC = ByteBufCodecs.stringUtf8(MAX_NAME_LENGTH);

    public static final Codec<Pin> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(Pin::id),
            Codec.STRING.fieldOf("name").forGetter(Pin::name),
            Codec.INT.fieldOf("color").forGetter(Pin::color),
            Identifier.CODEC.fieldOf("dimension").forGetter(Pin::dimension),
            Codec.INT.fieldOf("x").forGetter(Pin::x),
            Codec.INT.fieldOf("z").forGetter(Pin::z),
            Codec.INT.optionalFieldOf("paid_color").forGetter(p -> Optional.of(p.paidColor()))
    ).apply(i, (id, name, color, dimension, x, z, paid) ->
            new Pin(id, name, color, dimension, x, z, paid.orElse(color))
    ));

    public static final StreamCodec<ByteBuf, Pin> STREAM_CODEC = StreamCodec.of(
            (buf, pin) -> {
                UUIDUtil.STREAM_CODEC.encode(buf, pin.id());
                NAME_STREAM_CODEC.encode(buf, pin.name());
                ByteBufCodecs.INT.encode(buf, pin.color());
                Identifier.STREAM_CODEC.encode(buf, pin.dimension());
                ByteBufCodecs.VAR_INT.encode(buf, pin.x());
                ByteBufCodecs.VAR_INT.encode(buf, pin.z());
                ByteBufCodecs.INT.encode(buf, pin.paidColor());
            },
            buf -> new Pin(
                    UUIDUtil.STREAM_CODEC.decode(buf),
                    NAME_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.INT.decode(buf),
                    Identifier.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.INT.decode(buf)
            )
    );

    public boolean isPaid() {
        return paidColor != UNPAID;
    }
}
