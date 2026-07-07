package me.pajic.mapstitch.platform.fabric;

//? fabric {

import me.pajic.mapstitch.MapStitch;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.networking.ClientNetworkEvents;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		KeyMapping.Category.register(MapStitch.id("keys"));
		KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_WORLD_MAP);
		ClientTickEvents.END_CLIENT_TICK.register(ModKeybinds::onClientTick);
		ClientPlayNetworking.registerGlobalReceiver(S2CDimensionIds.TYPE, (payload, _) ->
				ClientNetworkEvents.setDimensionIds(payload)
		);
		ClientPlayNetworking.registerGlobalReceiver(S2CCompassGameRule.TYPE, (payload, _) ->
				ClientNetworkEvents.setCompassRequired(payload)
		);
		ClientPlayNetworking.registerGlobalReceiver(S2COpenWorldMapScreen.TYPE, (_, context) ->
				ClientNetworkEvents.openWorldMapScreen(context.player())
		);
		HudElementRegistry.attachElementBefore(
				VanillaHudElements.MOB_EFFECTS,
				MapStitch.id("minimap"),
				(context, tickCounter) -> MinimapOverlay.render(context)
		);
	}
}
//?}
