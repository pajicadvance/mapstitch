package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.nethermap.ImprovedNetherMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? <26.1 {
/*import me.pajic.mapstitch.component.ModDataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;
*///?}

@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @ModifyExpressionValue(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/dimension/DimensionType;hasCeiling()Z"
            )
    )
    private boolean removeNetherCeilingCheck(boolean original, @Local(argsOnly = true) Level level) {
        return !ImprovedNetherMap.dimensionAllowed(level.dimension()) && original;
    }

    @ModifyExpressionValue(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunk;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"
            )
    )
    private int modifyNetherMapHeight(int original, @Local(argsOnly = true) Level level, @Local(ordinal = 0) BlockPos.MutableBlockPos blockPos) {
        if (ImprovedNetherMap.dimensionAllowed(level.dimension())) {
            return switch (MapStitch.CONFIG.netherMap.mode.get()) {
                case DYNAMIC -> ImprovedNetherMap.getDynamicHeight(level, blockPos, original);
                case STATIC -> MapStitch.CONFIG.netherMap.staticModeHeight.get();
            };
        }
        return original;
    }

    //? <26.1 {
    /*@Inject(
            method = "getUpdatePacket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getUpdatePacket(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/network/protocol/Packet;"
            )
    )
    private void addMapOriginData(ItemStack stack, Level level, Player player, CallbackInfoReturnable<Packet<?>> cir, @Local MapItemSavedData data) {
        if (!stack.has(ModDataComponents.MAP_CENTER)) {
            stack.set(ModDataComponents.MAP_CENTER, new Vector2i(data.centerX, data.centerZ));
        }
    }
    *///?}
}
