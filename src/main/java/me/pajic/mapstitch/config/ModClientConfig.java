package me.pajic.mapstitch.config;

import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigSection;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedEnum;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.minimap.MinimapBackground;
import me.pajic.mapstitch.minimap.MinimapPosition;
import me.pajic.mapstitch.worldmap.TextHighlightColor;

@Version(version = 1)
public class ModClientConfig extends Config {

    public ModClientConfig() {
        super(MapStitch.id("client_config"));
    }

    public Minimap minimap = new Minimap();
    public WorldMap worldMap = new WorldMap();
    public MinimapInfo minimapInfo = new MinimapInfo();

    public static class Minimap extends ConfigSection {
        public ValidatedEnum<MinimapPosition> position = new ValidatedEnum<>(MinimapPosition.TOP_RIGHT);
        public ValidatedInt size = new ValidatedInt(19, 39, 1);
        public ValidatedEnum<MinimapBackground> background = new ValidatedEnum<>(MinimapBackground.CLEAR);
        public ValidatedInt backgroundOpacity = new ValidatedInt(50, 100, 0);
        public ValidatedBoolean preventEffectOverlap = new ValidatedBoolean();
        public ValidatedInt xOffset = new ValidatedInt(0);
        public ValidatedInt yOffset = new ValidatedInt(0);
    }

    public static class WorldMap extends ConfigSection {
        public ValidatedEnum<TextHighlightColor> textHighlightColor = new ValidatedEnum<>(TextHighlightColor.YELLOW);
        public ValidatedInt textBackgroundOpacity = new ValidatedInt(50, 100, 0);
        public ValidatedBoolean buttons = new ValidatedBoolean();
        public ValidatedBoolean help = new ValidatedBoolean();
    }

    public static class MinimapInfo extends ConfigSection {
        public ValidatedBoolean realTime = new ValidatedBoolean(false);
        public ValidatedEnum<RealTimeFormat> realTimeFormat = new ValidatedEnum<>(RealTimeFormat.H24);
        public ValidatedBoolean gameTime = new ValidatedBoolean(false);
        public ValidatedBoolean coordinates = new ValidatedBoolean(false);
        public ValidatedBoolean biome = new ValidatedBoolean(false);
        public ValidatedBoolean weather = new ValidatedBoolean(false);
    }
}
