package me.pajic.mapstitch.platform.fabric;

//? fabric {

import me.pajic.mapstitch.MapStitch;
import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.S2CCompassGameRulePayload;
import me.pajic.mapstitch.networking.S2CDimensionIdsPayload;
import me.pajic.mapstitch.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

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
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("map_center"), ModDataComponents.MAP_CENTER);
		Registry.register(BuiltInRegistries.GAME_RULE, MapStitch.id("require_compass_for_pos"), ModGameRules.REQUIRE_COMPASS_FOR_POS);
		Registry.register(BuiltInRegistries.ITEM, ModItems.ATLAS_KEY, ModItems.ATLAS);
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MapStitch.id("crafting_special_atlas"), ModRecipes.ATLAS);
		PayloadTypeRegistry.clientboundPlay().register(S2CDimensionIdsPayload.TYPE, S2CDimensionIdsPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(S2CCompassGameRulePayload.TYPE, S2CCompassGameRulePayload.CODEC);
		FabricLoader.getInstance().getModContainer(MapStitch.MOD_ID).ifPresent(container ->
				ResourceLoader.registerBuiltinPack(
						MapStitch.id("cheaper_maps"),
						container,
						Component.translatable("mapstitch.pack.cheaper_maps"),
						PackActivationType.DEFAULT_ENABLED
				)
		);
	}
}
//?}
