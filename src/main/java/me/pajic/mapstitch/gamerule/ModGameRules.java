package me.pajic.mapstitch.gamerule;

import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CMaxAtlasItemsGameRule;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;

public class ModGameRules {
	public static final GameRules.Key<GameRules.BooleanValue> REQUIRE_COMPASS_FOR_POS = GameRules.register(
			"require_compass_for_pos",
			GameRules.Category.PLAYER,
			GameRules.BooleanValue.create(true, (server, bl) ->
					server.getPlayerList().getPlayers().forEach(player ->
							PacketDistributor.sendToPlayer(player, new S2CCompassGameRule(bl.get()))
					)
			)
	);

	public static final GameRules.Key<GameRules.IntegerValue> MAX_ATLAS_ITEMS = GameRules.register(
			"max_atlas_items",
			GameRules.Category.PLAYER,
			GameRules.IntegerValue.create(16384, 64, 16384, (server, i) -> {
				server.getPlayerList().getPlayers().forEach(player ->
						PacketDistributor.sendToPlayer(player, new S2CMaxAtlasItemsGameRule(i.get()))
				);
				AtlasItem.setMaxSize(i.get());
			})
	);

	public static void init() {}
}
