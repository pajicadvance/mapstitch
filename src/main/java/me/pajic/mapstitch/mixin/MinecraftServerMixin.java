package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CMaxAtlasItemsGameRule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

	@Shadow public abstract PlayerList getPlayerList();
	@Shadow @Final private GameRules gameRules;

	@Inject(
			method = "onGameRuleChanged",
			at = @At("TAIL")
	)
	private <T> void sendModGameRuleUpdatesToPlayers(GameRule<T> rule, T value, CallbackInfo ci) {
		if (rule == ModGameRules.REQUIRE_COMPASS_FOR_POS) getPlayerList().getPlayers().forEach(player ->
				MapStitch.xplat().s2c(player, new S2CCompassGameRule((boolean) value))
		);
		if (rule == ModGameRules.MAX_ATLAS_ITEMS) {
			getPlayerList().getPlayers().forEach(player ->
					MapStitch.xplat().s2c(player, new S2CMaxAtlasItemsGameRule((int) value))
			);
			AtlasItem.setMaxSize((int) value);
		}
	}

	@Inject(
			method = "createLevels",
			at = @At("TAIL")
	)
	private void syncMaxAtlasSize(CallbackInfo ci) {
		AtlasItem.setMaxSize(gameRules.get(ModGameRules.MAX_ATLAS_ITEMS));
	}
}
