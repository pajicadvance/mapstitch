package me.pajic.mapstitch.platform.neoforge;

//? neoforge {

/*import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.OhmegaCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.datapack.ModDatapacks;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.ServerNetworkEvents;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import me.pajic.mapstitch.networking.payload.S2CDimensionIds;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.networking.payload.S2CSyncWorldMap;
import me.pajic.mapstitch.recipe.ModRecipes;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;

//? <26.1 {
import me.pajic.mapstitch.networking.ClientNetworkEvents;
//?}

@Mod(MapStitch.MOD_ID)
@EventBusSubscriber(modid = MapStitch.MOD_ID)
public class NeoforgeEntrypoint {

    public NeoforgeEntrypoint(IEventBus modBus) {
        if (CompatFlags.OHMEGA_LOADED) OhmegaCompat.setupAccessories(modBus);
    }

    @SubscribeEvent
    private static void register(RegisterEvent event) {
        event.register(Registries.DATA_COMPONENT_TYPE, registry -> {
            registry.register(MapStitch.id("atlas_scale"), ModDataComponents.ATLAS_SCALE);
            registry.register(MapStitch.id("atlas_fullness"), ModDataComponents.ATLAS_FULLNESS);
            registry.register(MapStitch.id("atlas_active_map_index"), ModDataComponents.ATLAS_ACTIVE_MAP_ID);
            registry.register(MapStitch.id("atlas_eject_filled_maps_first"), ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST);
            registry.register(MapStitch.id("map_center"), ModDataComponents.MAP_CENTER);
        });
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
        registrar.playToClient(S2CDimensionIds.TYPE, S2CDimensionIds.CODEC
                /^? <26.1 {^/, (payload, context) -> ClientNetworkEvents.setDimensionIds(payload)/^?}^/
        );
        registrar.playToClient(S2COpenWorldMapScreen.TYPE, S2COpenWorldMapScreen.CODEC
                /^? <26.1 {^/, (payload, context) -> ClientNetworkEvents.openWorldMapScreen(context.player(), payload.scaleOverride())/^?}^/
        );
        registrar.playToClient(S2CSyncWorldMap.TYPE, S2CSyncWorldMap.CODEC
                /^? <26.1 {^/, (payload, context) -> ClientNetworkEvents.syncWorldMapScreen()/^?}^/
        );
        registrar.playToServer(C2SPlaySound.TYPE, C2SPlaySound.CODEC, (payload, context) ->
                ServerNetworkEvents.playSound(payload, context.player())
        );
        registrar.playToServer(C2SSetEjectMode.TYPE, C2SSetEjectMode.CODEC, (payload, context) ->
                ServerNetworkEvents.setEjectMode(payload, context.player())
        );
        registrar.playToServer(C2SEjectMap.TYPE, C2SEjectMap.CODEC, (payload, context) ->
                ServerNetworkEvents.ejectMap(payload, context.player())
        );
    }

    @SubscribeEvent
    private static void initLoot(LootTableLoadEvent event) {
        if (ModUtil.isGlobetrotterLootTable(event.getKey())) event.getTable().addPool(ModUtil.getGlobetrotterLootPool(event.getRegistries()).build());
    }

    @SubscribeEvent
    private static void initPacks(AddPackFindersEvent event) {
        ModDatapacks.init();
        ModDatapacks.getPacks().forEach(s -> event.addPackFinders(
                MapStitch.id("resourcepacks/" + s),
                PackType.SERVER_DATA,
                Component.translatable("mapstitch.pack." + s),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP
        ));
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
