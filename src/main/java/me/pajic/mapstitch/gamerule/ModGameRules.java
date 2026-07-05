package me.pajic.mapstitch.gamerule;

import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;

public class ModGameRules {
	public static final GameRules.Key<GameRules.BooleanValue> REQUIRE_COMPASS_FOR_POS = GameRules.register(
			"require_compass_for_pos",
			GameRules.Category.PLAYER,
			GameRules.BooleanValue.create(true, (server, bl) ->
					server.getPlayerList().getPlayers().forEach(player ->
							PacketDistributor.sendToPlayer(player, new S2CCompassGameRule(bl.get()))))
	);

	public static void init() {}
}
