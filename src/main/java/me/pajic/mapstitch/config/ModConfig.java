package me.pajic.mapstitch.config;

import me.fzzyhmstrs.fzzy_config.annotations.Action;
import me.fzzyhmstrs.fzzy_config.annotations.RequiresAction;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.util.AllowableIdentifiers;
import me.fzzyhmstrs.fzzy_config.util.AllowableStrings;
import me.fzzyhmstrs.fzzy_config.util.ValidationResult;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedChoiceList;
import me.fzzyhmstrs.fzzy_config.validation.collection.ValidatedSet;
import me.fzzyhmstrs.fzzy_config.validation.minecraft.ValidatedIdentifier;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.nethermap.NetherMapMode;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

@Version(version = 1)
public class ModConfig extends Config {

    private static final List<String> COMPASS_REQ = List.of("minimap", "playerMarker", "grid", "coordinates", "biome");
    private static final List<String> CLOCK_REQ = List.of("time", "weather");
    private static final List<String> ITEM_SCAN_LOCATIONS = List.of("mainHand", "offhand", "hotbar", "inventory", "bundles", "containerItems", "shulkerBoxes", "accessories");
    private static final List<String> ATLAS_SCAN_LOCATIONS = List.of("mainHand", "offhand", "hotbar", "inventory", "accessories");

    private static final ValidatedString compassReqEntry = new ValidatedString("", new AllowableStrings(COMPASS_REQ::contains, () -> COMPASS_REQ));
    private static final ValidatedString clockReqEntry = new ValidatedString("", new AllowableStrings(CLOCK_REQ::contains, () -> CLOCK_REQ));
    private static final ValidatedString itemScanEntry = new ValidatedString("", new AllowableStrings(ITEM_SCAN_LOCATIONS::contains, () -> ITEM_SCAN_LOCATIONS));
    private static final ValidatedString atlasScanEntry = new ValidatedString("", new AllowableStrings(ATLAS_SCAN_LOCATIONS::contains, () -> ATLAS_SCAN_LOCATIONS));

    public ModConfig() {
		super(MapStitch.id("config"));
	}

    public ValidatedInt maxAtlasItems = new ValidatedInt(16384, 16384, 1, ValidatedNumber.WidgetType.TEXTBOX);
    public ItemRequirements itemRequirements = new ItemRequirements();
    public MapRecipe mapRecipe = new MapRecipe();
    public NetherMap netherMap = new NetherMap();
    public Globetrotter globetrotter = new Globetrotter();
    public AccessorySlots accessorySlots = new AccessorySlots();
    public MinimapInfo minimapInfo = new MinimapInfo();
    public ValidatedBoolean keepAtlasOnDeath = new ValidatedBoolean(false);

    public static class ItemRequirements extends ConfigSection {
        public ValidatedChoiceList<String> compass = compassReqEntry.toList(COMPASS_REQ).toChoiceList(COMPASS_REQ, ValidatedChoiceList.WidgetType.POPUP, (s, s2) -> Component.translatable("mapstitch.config.reqEntry." + s));
        public ValidatedChoiceList<String> clock = clockReqEntry.toList(CLOCK_REQ).toChoiceList(CLOCK_REQ, ValidatedChoiceList.WidgetType.POPUP, (s, s2) -> Component.translatable("mapstitch.config.reqEntry." + s));
        public ValidatedChoiceList<String> compassAndClockScan = itemScanEntry.toList(ITEM_SCAN_LOCATIONS).toChoiceList(ITEM_SCAN_LOCATIONS, ValidatedChoiceList.WidgetType.POPUP, (s, s2) -> Component.translatable("mapstitch.config.scanEntry." + s));
        public ValidatedChoiceList<String> worldMapAtlasScan = atlasScanEntry.toList(ATLAS_SCAN_LOCATIONS).toChoiceList(ATLAS_SCAN_LOCATIONS, ValidatedChoiceList.WidgetType.POPUP, (s, s2) -> Component.translatable("mapstitch.config.scanEntry." + s));
        public ValidatedChoiceList<String> minimapAtlasScan = atlasScanEntry.toList(ATLAS_SCAN_LOCATIONS).toChoiceList(List.of("mainHand", "offhand", "hotbar", "accessories"), ValidatedChoiceList.WidgetType.POPUP, (s, s2) -> Component.translatable("mapstitch.config.scanEntry." + s));
    }

    @RequiresAction(action = Action.RESTART)
    public static class MapRecipe extends ConfigSection {
        public ValidatedBoolean cheaperRecipe = new ValidatedBoolean();
        public ValidatedBoolean replaceVanillaRecipe = new ValidatedBoolean();
    }

    public static class NetherMap extends ConfigSection {
        public ValidatedEnum<NetherMapMode> mode = new ValidatedEnum<>(NetherMapMode.DYNAMIC);
        public ValidatedInt staticModeHeight = new ValidatedInt(40, 256, 0);
        public ValidatedInt dynamicModeCeilingOffset = new ValidatedInt(32, 128, 0);
        public ValidatedSet<Identifier> allowedDimensions = new ValidatedIdentifier(
                Identifier.withDefaultNamespace("the_nether"),
                new AllowableIdentifiers(ModUtil.dimensionIds::contains, () -> ModUtil.dimensionIds),
                (identifier, vt) -> ValidationResult.Companion.success(identifier)
        ).toSet(Identifier.withDefaultNamespace("the_nether"));
    }

    @RequiresAction(action = Action.RESTART)
    public static class Globetrotter extends ConfigSection {
        public ValidatedBoolean enabled = new ValidatedBoolean();
        public ValidatedInt chance = new ValidatedInt(50, 100, 0);
    }

    @RequiresAction(action = Action.RESTART)
    public static class AccessorySlots extends ConfigSection {
        public ValidatedBoolean atlasSlot = new ValidatedBoolean();
        public ValidatedBoolean compassSlot = new ValidatedBoolean();
        public ValidatedBoolean clockSlot = new ValidatedBoolean(false);
    }

    public static class MinimapInfo extends ConfigSection {
        public ValidatedBoolean allowRealTime = new ValidatedBoolean(true);
        public ValidatedBoolean allowGameTime = new ValidatedBoolean(false);
        public ValidatedBoolean allowCoordinates = new ValidatedBoolean(false);
        public ValidatedBoolean allowBiome = new ValidatedBoolean(false);
        public ValidatedBoolean allowWeather = new ValidatedBoolean(false);
    }
}
