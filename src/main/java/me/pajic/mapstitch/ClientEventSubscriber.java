package me.pajic.mapstitch;

import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.config.ModYACLConfig;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.minimap.MinimapOverlay;
import me.pajic.mapstitch.util.CompatFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
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

@EventBusSubscriber(modid = MapStitch.MOD_ID, value = Dist.CLIENT)
public class ClientEventSubscriber {

	@SubscribeEvent
	private static void onCommonSetup(FMLCommonSetupEvent event) {
		if (CompatFlags.YACL_LOADED) ModLoadingContext.get().registerExtensionPoint(
				IConfigScreenFactory.class,
				() -> (container, parent) -> ModYACLConfig.makeScreen(parent)
		);
		ItemProperties.register(
				ModItems.ATLAS,
				MapStitch.id("atlas_fullness"),
				(stack, level, entity, i) -> stack.getOrDefault(ModDataComponents.ATLAS_FULLNESS, 0)
		);
	}

	@SubscribeEvent
	private static void initKeybinds(RegisterKeyMappingsEvent event) {
		event.register(ModKeybinds.OPEN_WORLD_MAP);
	}

	@SubscribeEvent
	private static void onClientTick(ClientTickEvent.Post event) {
		ModKeybinds.onClientTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	private static void initHudLayers(RegisterGuiLayersEvent event) {
		event.registerBelow(
				VanillaGuiLayers.EFFECTS,
				MapStitch.id("minimap"),
				(context, dt) -> MinimapOverlay.render(context)
		);
	}
}
