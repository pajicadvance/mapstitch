package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.pajic.mapstitch.extension.HoldingPlayerExtension;
import me.pajic.mapstitch.extension.MapItemSavedDataExtension;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(MapItemSavedData.class)
public abstract class MapItemSavedDataMixin implements MapItemSavedDataExtension {

    @Shadow @Final private Map<Player, MapItemSavedData.HoldingPlayer> carriedByPlayers;

    @ModifyExpressionValue(
            //~ if <26.1 'calculateRotation' -> 'addDecoration'
            method = "calculateRotation",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;dimension:Lnet/minecraft/resources/ResourceKey;",
                    opcode = Opcodes.GETFIELD
            )
    )
    private ResourceKey<Level> preventPlayerMarkerSpin(ResourceKey<Level> original) {
        return Level.OVERWORLD;
    }

    @Override
    public Packet<?> mapstitch$forceUpdatePacket(MapId id, Player player) {
        MapItemSavedData.HoldingPlayer holdingPlayer = carriedByPlayers.get(player);
        return ((HoldingPlayerExtension) holdingPlayer).mapstitch$forceUpdatePacket(id);
    }
}
