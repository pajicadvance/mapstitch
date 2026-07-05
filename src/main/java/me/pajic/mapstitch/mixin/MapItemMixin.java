package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.component.ModDataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItem.class)
public class MapItemMixin {

    @Inject(
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
}
