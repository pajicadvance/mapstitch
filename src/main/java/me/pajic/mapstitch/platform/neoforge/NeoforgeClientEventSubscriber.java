package me.pajic.mapstitch.platform.neoforge;

//? neoforge {

/*import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.config.ModYACLConfig;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.networking.S2CCompassGameRulePayload;
import me.pajic.mapstitch.networking.S2CDimensionIdsPayload;
import me.pajic.mapstitch.networking.S2COpenWorldMapScreenSignal;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
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
		event.register(ModKeybinds.OPEN_WORLD_MAP);
	}

	@SubscribeEvent
	private static void onClientTick(ClientTickEvent.Post event) {
		ModKeybinds.onClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	private static void initNetworking(RegisterClientPayloadHandlersEvent event) {
		event.register(S2CDimensionIdsPayload.TYPE, (payload, _) ->
				WorldMapScreen.dimensionIds = payload.dimensionIds()
		);
		event.register(S2CCompassGameRulePayload.TYPE, (payload, _) ->
				ModUtil.compassRequired = payload.required()
		);
		event.register(S2COpenWorldMapScreenSignal.TYPE, (_, context) -> {
			context.player().playSound(SoundEvents.BOOK_PAGE_TURN);
			Minecraft.getInstance().setScreenAndShow(new WorldMapScreen());
		});
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
