package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.component.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(MapItem.class)
public abstract class MapItemMixin {

    @Shadow public abstract void update(Level level, Entity viewer, MapItemSavedData data);

    @Shadow @Nullable
    public static MapItemSavedData getSavedData(ItemStack stack, Level level) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

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

    @Inject(
            method = "inventoryTick",
            at = @At("TAIL")
    )
    private void updateMapsInInventory(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected, CallbackInfo ci) {
        if (!level.isClientSide) {
            MapItemSavedData data = getSavedData(stack, level);
            if (data != null && !data.locked) {
                update(level, entity, data);
                if (entity instanceof ServerPlayer serverPlayer) {
                    Packet<?> packet = data.getUpdatePacket(stack.get(DataComponents.MAP_ID), serverPlayer);
                    if (packet != null) serverPlayer.connection.send(packet);
                }
            }
        }
    }
}
