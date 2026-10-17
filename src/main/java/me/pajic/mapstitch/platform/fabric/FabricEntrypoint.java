package me.pajic.mapstitch.platform.fabric;

//? fabric {

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.OhmegaCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.datapack.ModDatapacks;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.ServerNetworkEvents;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SPlacePin;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SRemovePin;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import me.pajic.mapstitch.networking.payload.C2SSharePin;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.networking.payload.S2CPlaySound;
import me.pajic.mapstitch.networking.payload.S2CSyncPins;
import me.pajic.mapstitch.networking.payload.S2CSyncWorldMap;
import me.pajic.mapstitch.pin.PinShares;
import me.pajic.mapstitch.recipe.ModRecipes;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

//? <26.1 {
/*import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
*///?} else {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
//?}

@Entrypoint("main")
public class FabricEntrypoint implements ModInitializer {

	@Override
	public void onInitialize() {
        ModDatapacks.init();
        if (CompatFlags.OHMEGA_LOADED) OhmegaCompat.setupAccessories();
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_scale"), ModDataComponents.ATLAS_SCALE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_fullness"), ModDataComponents.ATLAS_FULLNESS);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_active_map_index"), ModDataComponents.ATLAS_ACTIVE_MAP_ID);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("atlas_eject_filled_maps_first"), ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MapStitch.id("map_center"), ModDataComponents.MAP_CENTER);
        Registry.register(BuiltInRegistries.ITEM, ModItems.ATLAS_KEY, ModItems.ATLAS);
        Registry.register(BuiltInRegistries.ITEM, ModItems.ATLAS_PIN_KEY, ModItems.ATLAS_PIN);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MapStitch.id("crafting_special_atlas"), ModRecipes.ATLAS);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, MapStitch.id("crafting_special_atlas_pin_bulk_dye"), ModRecipes.ATLAS_PIN_BULK_DYE);
        PayloadTypeRegistry.clientboundPlay().register(S2CDimensionIds.TYPE, S2CDimensionIds.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2COpenWorldMapScreen.TYPE, S2COpenWorldMapScreen.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CSyncWorldMap.TYPE, S2CSyncWorldMap.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CPlaySound.TYPE, S2CPlaySound.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(S2CSyncPins.TYPE, S2CSyncPins.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SPlaySound.TYPE, C2SPlaySound.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SSetEjectMode.TYPE, C2SSetEjectMode.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SEjectMap.TYPE, C2SEjectMap.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SPlacePin.TYPE, C2SPlacePin.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SRemovePin.TYPE, C2SRemovePin.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(C2SSharePin.TYPE, C2SSharePin.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(C2SPlaySound.TYPE, (payload, context) ->
                ServerNetworkEvents.playSound(payload, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(C2SSetEjectMode.TYPE, (payload, context) ->
                ServerNetworkEvents.setEjectMode(payload, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(C2SEjectMap.TYPE, (payload, context) ->
                ServerNetworkEvents.ejectMap(payload, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(C2SPlacePin.TYPE, (payload, context) ->
                ServerNetworkEvents.placePin(payload, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(C2SRemovePin.TYPE, (payload, context) ->
                ServerNetworkEvents.removePin(payload, context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(C2SSharePin.TYPE, (payload, context) ->
                ServerNetworkEvents.sharePin(payload, context.player())
        );
        CommandRegistrationCallback.EVENT.register((dispatcher, r, e) ->
                PinShares.registerCommands(dispatcher)
        );
        ServerTickEvents.END_SERVER_TICK.register(PinShares::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(PinShares::onServerStopping);
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            //~ if <26.1 'insertAfter' -> 'addAfter'
            output.insertAfter(Items.MAP, ModItems.ATLAS_PIN.getDefaultInstance());
            for (int i = 4; i >= 0; i--) {
                ItemStack atlas = new ItemStack(ModItems.ATLAS);
                atlas.set(ModDataComponents.ATLAS_SCALE, i);
                //~ if <26.1 'insertAfter' -> 'addAfter'
                output.insertAfter(Items.MAP, atlas);
            }
        });
        LootTableEvents.MODIFY.register((key, tableBuilder, s, registries) -> {
            if (ModUtil.isGlobetrotterLootTable(key)) tableBuilder.withPool(ModUtil.getGlobetrotterLootPool(registries/*? <26.1 {*//*.asGetterLookup()*//*?}*/));
        });
        FabricLoader.getInstance().getModContainer(MapStitch.MOD_ID).ifPresent(container ->
                //~ if <26.1 'ResourceLoader.registerBuiltinPack' -> 'ResourceManagerHelper.registerBuiltinResourcePack'
                ModDatapacks.getPacks().forEach(s -> ResourceLoader.registerBuiltinPack(
                        MapStitch.id(s),
                        container,
                        Component.translatable("mapstitch.pack." + s),
                        //~ if <26.1 'PackActivationType' -> 'ResourcePackActivationType'
                        PackActivationType.ALWAYS_ENABLED
                ))
        );
	}
}
//?}
