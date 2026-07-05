package me.pajic.mapstitch.util;

import net.neoforged.fml.ModList;

public class CompatFlags {
	public static final boolean YACL_LOADED = ModList.get().isLoaded("yet_another_config_lib_v3");
	public static final boolean CURIOS_LOADED = ModList.get().isLoaded("curios");
}
