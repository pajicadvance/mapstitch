package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.component.ModDataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

	@Inject(
			method = "synchronizeSpecialItemUpdates",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getUpdatePacket(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/network/protocol/Packet;"
			)
	)
	private void addMapOriginData(ItemStack itemStack, CallbackInfo ci, @Local(name = "data") MapItemSavedData data) {
		if (!itemStack.has(ModDataComponents.MAP_CENTER)) {
			itemStack.set(ModDataComponents.MAP_CENTER, new Vector2d(data.centerX, data.centerZ));
		}
	}
}
