package me.pajic.mapstitch.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.networking.ClientNetworkEvents;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.networking.payload.S2CSyncWorldMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

//? >=26.1 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}

//~ if <26.1 'keymapping.v1.KeyMappingHelper' -> 'keybinding.v1.KeyBindingHelper'
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;

@Entrypoint("client")
public class FabricClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MapStitch.onInitializeClient();
        //? >=26.1 {
        KeyMapping.Category.register(ModKeybinds.KEYS_ID);
        KeyMapping.Category.register(ModKeybinds.KEYS_WORLD_MAP_ID);
        //?}
        //~ if <26.1 'KeyMappingHelper.registerKeyMapping' -> 'KeyBindingHelper.registerKeyBinding' {
        KeyMappingHelper.registerKeyMapping(ModKeybinds.OPEN_WORLD_MAP);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.TOGGLE_MINIMAP);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.SCALE_UP);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.SCALE_DOWN);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.DIMENSION_UP);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.DIMENSION_DOWN);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.FOLLOW_PLAYER);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.TOGGLE_GRID);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.EJECT_MAP);
        KeyMappingHelper.registerKeyMapping(ModKeybinds.TOGGLE_HELP);
        //~}
        ClientTickEvents.END_CLIENT_TICK.register(ModKeybinds::onClientTick);
        ClientPlayNetworking.registerGlobalReceiver(S2CDimensionIds.TYPE, (payload, context) ->
                ClientNetworkEvents.setDimensionIds(payload)
        );
        ClientPlayNetworking.registerGlobalReceiver(S2COpenWorldMapScreen.TYPE, (payload, context) ->
                ClientNetworkEvents.openWorldMapScreen(context.player(), payload.scaleOverride())
        );
        ClientPlayNetworking.registerGlobalReceiver(S2CSyncWorldMap.TYPE, (payload, context) ->
                ClientNetworkEvents.syncWorldMapScreen()
        );
        //? >=26.1 {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.MOB_EFFECTS,
                MapStitch.id("minimap"),
                (context, tickCounter) -> MinimapOverlay.render(context)
        );
        //?}
	}
}
//?}
