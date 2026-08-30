package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

//? <26.1
//import net.minecraft.resources.ResourceKey;

@Mixin(PlayerList.class)
public class PlayerListMixin {

	@Inject(
			method = "placeNewPlayer",
			at = @At("RETURN")
	)
	private void sendModDataToPlayers(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        //~ if <26.1 'keySet().stream().toList()' -> 'listElementIds().map(ResourceKey::location).toList()'
        List<Identifier> dimensionIds = player.registryAccess().lookupOrThrow(Registries.DIMENSION).keySet().stream().toList();
		MultiLoaderUtil.INSTANCE.s2c(player, new S2CDimensionIds(dimensionIds));
        ModUtil.dimensionIds = dimensionIds;
	}

	@Inject(
			method = "remove",
			at = @At("HEAD")
	)
	private void removeModDataFromPlayers(ServerPlayer player, CallbackInfo ci) {
		AtlasItem.clearInitializedMapsForPlayer(player);
	}
}
