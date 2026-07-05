package me.pajic.mapstitch;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapStitch {
    public static final String MOD_ID = /*$ mod_id*/ "mapstitch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static boolean isDebug() {
        return !FMLLoader.isProduction();
    }
}
