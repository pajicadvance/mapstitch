package me.pajic.mapstitch.platform.fabric;

//? fabric {

import me.pajic.mapstitch.MapStitch;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.ServerNetworkEvents;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import me.pajic.mapstitch.networking.payload.S2CCompassGameRule;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
		ModDataComponents.init();
		ModItems.init();
		ModGameRules.init();
		ModRecipes.init();
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_scale"), ModDataComponents.ATLAS_SCALE);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_fullness"), ModDataComponents.ATLAS_FULLNESS);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_active_map_index"), ModDataComponents.ATLAS_ACTIVE_MAP_ID);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_eject_filled_maps_first"), ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("map_center"), ModDataComponents.MAP_CENTER);
		Registry.register(BuiltInRegistries.GAME_RULE, MapStitch.id("require_compass_for_pos"), ModGameRules.REQUIRE_COMPASS_FOR_POS);
		Registry.register(BuiltInRegistries.ITEM, ModItems.ATLAS_KEY, ModItems.ATLAS);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MapStitch.id("crafting_special_atlas"), ModRecipes.ATLAS);
		PayloadTypeRegistry.clientboundPlay().register(S2CDimensionIds.TYPE, S2CDimensionIds.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CCompassGameRule.TYPE, S2CCompassGameRule.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2COpenWorldMapScreen.TYPE, S2COpenWorldMapScreen.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(C2SPlaySound.TYPE, C2SPlaySound.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(C2SSetEjectMode.TYPE, C2SSetEjectMode.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(C2SEjectMap.TYPE, C2SEjectMap.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(C2SPlaySound.TYPE, (payload, context) ->
				ServerNetworkEvents.playSound(payload, context.player())
		);
		ServerPlayNetworking.registerGlobalReceiver(C2SSetEjectMode.TYPE, (payload, context) ->
				ServerNetworkEvents.setEjectMode(payload, context.player())
		);
		ServerPlayNetworking.registerGlobalReceiver(C2SEjectMap.TYPE, (payload, context) ->
				ServerNetworkEvents.ejectMap(payload, context.player()))
		;
		FabricLoader.getInstance().getModContainer(MapStitch.MOD_ID).ifPresent(container ->
				ResourceLoader.registerBuiltinPack(
						MapStitch.id("cheaper_maps"),
						container,
						Component.translatable("mapstitch.pack.cheaper_maps"),
						PackActivationType.DEFAULT_ENABLED
				)
		);
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			for (int i = 4; i >= 0; i--) {
				ItemStack atlas = new ItemStack(ModItems.ATLAS);
				atlas.set(ModDataComponents.ATLAS_SCALE, i);
				output.insertAfter(Items.MAP, atlas);
			}
		});
	}
}
//?}
