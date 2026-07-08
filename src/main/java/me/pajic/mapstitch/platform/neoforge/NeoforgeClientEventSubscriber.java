package me.pajic.mapstitch.platform.neoforge;

//? neoforge {

/*import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.config.ModYACLConfig;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.networking.ClientNetworkEvents;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.util.CompatFlags;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(modid = MapStitch.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {

	@SubscribeEvent
	private static void onCommonSetup(FMLCommonSetupEvent event) {
		if (CompatFlags.YACL_LOADED) ModLoadingContext.get().registerExtensionPoint(
				IConfigScreenFactory.class,
				() -> (_, parent) -> ModYACLConfig.makeScreen(parent)
		);
	}

	@SubscribeEvent
	private static void initKeybinds(RegisterKeyMappingsEvent event) {
		event.registerCategory(ModKeybinds.MOD_KEYS);
		event.registerCategory(ModKeybinds.MOD_KEYS_WORLD_MAP);
		event.register(ModKeybinds.OPEN_WORLD_MAP);
		event.register(ModKeybinds.SCALE_UP);
		event.register(ModKeybinds.SCALE_DOWN);
		event.register(ModKeybinds.DIMENSION_UP);
		event.register(ModKeybinds.DIMENSION_DOWN);
		event.register(ModKeybinds.FOLLOW_PLAYER);
		event.register(ModKeybinds.TOGGLE_GRID);
		event.register(ModKeybinds.EJECT_MAP);
		event.register(ModKeybinds.TOGGLE_HELP);
	}

	@SubscribeEvent
	private static void onClientTick(ClientTickEvent.Post event) {
		ModKeybinds.onClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	private static void initNetworking(RegisterClientPayloadHandlersEvent event) {
		event.register(S2CDimensionIds.TYPE, (payload, _) ->
				ClientNetworkEvents.setDimensionIds(payload)
		);
		event.register(S2CCompassGameRule.TYPE, (payload, _) ->
				ClientNetworkEvents.setCompassRequired(payload)
		);
		event.register(S2COpenWorldMapScreen.TYPE, (_, context) ->
				ClientNetworkEvents.openWorldMapScreen(context.player())
		);
	}

	@SubscribeEvent
	private static void initHudLayers(RegisterGuiLayersEvent event) {
		event.registerBelow(
				VanillaGuiLayers.EFFECTS,
				MapStitch.id("minimap"),
				(context, _) -> MinimapOverlay.render(context)
		);
	}
}
*///?}
