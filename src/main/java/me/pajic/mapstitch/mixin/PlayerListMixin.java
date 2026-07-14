package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2CMaxAtlasItemsGameRule;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.gamerules.GameRules;
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
		GameRules rules = player.level().getGameRules();
		MapStitch.xplat().s2c(player, new S2CDimensionIds(
				player.registryAccess().lookupOrThrow(Registries.DIMENSION).keySet().stream().toList()
		));
		MapStitch.xplat().s2c(player, new S2CCompassGameRule(rules.get(ModGameRules.REQUIRE_COMPASS_FOR_POS)));
		MapStitch.xplat().s2c(player, new S2CMaxAtlasItemsGameRule(rules.get(ModGameRules.MAX_ATLAS_ITEMS)));
	}

	@Inject(
			method = "remove",
			at = @At("HEAD")
	)
	private void removeModDataFromPlayers(ServerPlayer player, CallbackInfo ci) {
		AtlasItem.clearInitializedMapsForPlayer(player);
	}
}
