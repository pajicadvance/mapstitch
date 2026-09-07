package me.pajic.mapstitch.platform.neoforge;

//? neoforge {

/*import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

//? >=26.1 {
/^import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import me.pajic.mapstitch.networking.ClientNetworkEvents;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.networking.payload.S2CSyncWorldMap;
^///?} else {
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
//?}

@EventBusSubscriber(modid = MapStitch.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEventSubscriber {

	@SubscribeEvent
	public static void onClientSetup(final FMLCommonSetupEvent event) {
        MapStitch.onInitializeClient();
        //? <26.1 {
        ItemProperties.register(
                ModItems.ATLAS,
                MapStitch.id("atlas_fullness"),
                (stack, level, entity, i) -> stack.getOrDefault(ModDataComponents.ATLAS_FULLNESS, 0)
        );
        //?}
	}

    @SubscribeEvent
    private static void initKeybinds(RegisterKeyMappingsEvent event) {
        //? >=26.1 {
        /^event.registerCategory(new KeyMapping.Category(ModKeybinds.KEYS_ID));
        event.registerCategory(new KeyMapping.Category(ModKeybinds.KEYS_WORLD_MAP_ID));
        ^///?}
        event.register(ModKeybinds.OPEN_WORLD_MAP);
        event.register(ModKeybinds.TOGGLE_MINIMAP);
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

    //? >=26.1 {
    /^@SubscribeEvent
    private static void initNetworking(RegisterClientPayloadHandlersEvent event) {
        event.register(S2CDimensionIds.TYPE, (payload, context) ->
                ClientNetworkEvents.setDimensionIds(payload)
        );
        event.register(S2COpenWorldMapScreen.TYPE, (payload, context) ->
                ClientNetworkEvents.openWorldMapScreen(context.player(), payload.scaleOverride())
        );
        event.register(S2CSyncWorldMap.TYPE, (payload, context) ->
                ClientNetworkEvents.syncWorldMapScreen()
        );
    }
    ^///?}

    @SubscribeEvent
    private static void initHudLayers(RegisterGuiLayersEvent event) {
        event.registerBelow(
                VanillaGuiLayers.EFFECTS,
                MapStitch.id("minimap"),
                (context, dt) -> MinimapOverlay.render(context)
        );
    }
}
*///?}
