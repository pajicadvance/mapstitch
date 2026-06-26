package me.pajic.mapstitch.platform.fabric;

//? fabric {

import me.pajic.mapstitch.MapStitch;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.networking.S2CCompassGameRulePayload;
import me.pajic.mapstitch.networking.S2CDimensionIdsPayload;
import me.pajic.mapstitch.networking.S2COpenWorldMapScreenSignal;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.sounds.SoundEvents;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		KeyMapping.Category.register(MapStitch.id("keys"));
		KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_WORLD_MAP);
		ClientTickEvents.END_CLIENT_TICK.register(ModKeybinds::onClientTick);
		ClientPlayNetworking.registerGlobalReceiver(S2CDimensionIdsPayload.TYPE, (payload, _) ->
				WorldMapScreen.dimensionIds = payload.dimensionIds()
		);
		ClientPlayNetworking.registerGlobalReceiver(S2CCompassGameRulePayload.TYPE, (payload, _) ->
				ModUtil.compassRequired = payload.required()
		);
		ClientPlayNetworking.registerGlobalReceiver(S2COpenWorldMapScreenSignal.TYPE, (_, context) -> {
			context.player().playSound(SoundEvents.BOOK_PAGE_TURN);
			context.client().setScreenAndShow(new WorldMapScreen());
		});
		HudElementRegistry.attachElementBefore(
				VanillaHudElements.MOB_EFFECTS,
				MapStitch.id("minimap"),
				(context, tickCounter) -> MinimapOverlay.render(context)
		);
	}
}
//?}
