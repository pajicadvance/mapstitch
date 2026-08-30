package me.pajic.mapstitch;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.pajic.mapstitch.config.ModConfig;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class MapStitch {

    public static final String MOD_ID = /*$ mod_id*/ "mapstitch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ModConfig CONFIG = ConfigApiJava.registerAndLoadConfig(ModConfig::new);

    public static void onInitializeClient() {
        MapStitchClient.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void debugLog(String message, Level level, Object ... args) {
        if (MultiLoaderUtil.INSTANCE.isDevEnv()) LOGGER.atLevel(level).log(message, args);
    }
}
