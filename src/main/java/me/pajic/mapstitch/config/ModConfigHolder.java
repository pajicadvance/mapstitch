package me.pajic.mapstitch.config;

import net.neoforged.fml.loading.FMLPaths;

public class ModConfigHolder {
    private static ModConfig CONFIG;

    public static ModConfig options() {
        if (CONFIG == null) init();
        return CONFIG;
    }

    public static void init() {
        CONFIG = ModConfig.load(FMLPaths.CONFIGDIR.get().resolve("mapstitch.json").toFile());
    }
}
