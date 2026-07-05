package me.pajic.mapstitch.platform.neoforge;

//? neoforge {

/*import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.C2SPlaySoundPayload;
import me.pajic.mapstitch.networking.S2CCompassGameRulePayload;
import me.pajic.mapstitch.networking.S2CDimensionIdsPayload;
import me.pajic.mapstitch.networking.S2COpenWorldMapScreenSignal;
import me.pajic.mapstitch.recipe.ModRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MapStitch.MOD_ID)
@EventBusSubscriber(modid = MapStitch.MOD_ID)
public class NeoforgeEntrypoint {

	@SubscribeEvent
	private static void register(RegisterEvent event) {
		ModDataComponents.init();
		ModItems.init();
		ModGameRules.init();
		ModRecipes.init();
		event.register(Registries.DATA_COMPONENT_TYPE, registry -> {
			registry.register(MapStitch.id("atlas_scale"), ModDataComponents.ATLAS_SCALE);
			registry.register(MapStitch.id("atlas_fullness"), ModDataComponents.ATLAS_FULLNESS);
			registry.register(MapStitch.id("atlas_active_map_index"), ModDataComponents.ATLAS_ACTIVE_MAP_ID);
			registry.register(MapStitch.id("atlas_eject_filled_maps_first"), ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST);
			registry.register(MapStitch.id("map_center"), ModDataComponents.MAP_CENTER);
		});
		event.register(Registries.GAME_RULE, registry ->
				registry.register(MapStitch.id("require_compass_for_pos"), ModGameRules.REQUIRE_COMPASS_FOR_POS)
		);
		event.register(Registries.ITEM, registry ->
				registry.register(ModItems.ATLAS_KEY, ModItems.ATLAS)
		);
		event.register(Registries.RECIPE_SERIALIZER, registry ->
				registry.register(MapStitch.id("crafting_special_atlas"), ModRecipes.ATLAS)
		);
	}

	@SubscribeEvent
	private static void initNetworking(RegisterPayloadHandlersEvent event) {
		PayloadRegistrar registrar = event.registrar("1");
		registrar.playToClient(S2CDimensionIdsPayload.TYPE, S2CDimensionIdsPayload.CODEC);
		registrar.playToClient(S2CCompassGameRulePayload.TYPE, S2CCompassGameRulePayload.CODEC);
		registrar.playToClient(S2COpenWorldMapScreenSignal.TYPE, S2COpenWorldMapScreenSignal.CODEC);
		registrar.playToServer(C2SPlaySoundPayload.TYPE, C2SPlaySoundPayload.CODEC, (payload, context) ->
				context.player().playSound(payload.sound().value())
		);
	}

	@SubscribeEvent
	private static void initPacks(AddPackFindersEvent event) {
		event.addPackFinders(
				MapStitch.id("resourcepacks/cheaper_maps"),
				PackType.SERVER_DATA,
				Component.translatable("mapstitch.pack.cheaper_maps"),
				PackSource.BUILT_IN,
				false,
				Pack.Position.TOP
		);
	}

	@SubscribeEvent
	private static void initCreativeTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			for (int i = 4; i >= 0; i--) {
				ItemStack atlas = new ItemStack(ModItems.ATLAS);
				atlas.set(ModDataComponents.ATLAS_SCALE, i);
				event.insertAfter(Items.MAP.getDefaultInstance(), atlas, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
			}
		}
	}
}
*///?}
