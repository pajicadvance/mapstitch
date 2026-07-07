package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Inject(
			method = "placeNewPlayer",
			at = @At("RETURN")
	)
	private void sendModDataToPlayers(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		MapStitch.xplat().s2c(player, new S2CDimensionIds(
				player.registryAccess().lookupOrThrow(Registries.DIMENSION).keySet().stream().toList()
		));
		MapStitch.xplat().s2c(player, new S2CCompassGameRule(
				player.level().getGameRules().get(ModGameRules.REQUIRE_COMPASS_FOR_POS)
		));
	}

	@Inject(
			method = "remove",
			at = @At("HEAD")
	)
	private void removeModDataFromPlayers(ServerPlayer player, CallbackInfo ci) {
		AtlasItem.clearInitializedMapsForPlayer(player);
	}
}
