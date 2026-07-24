package me.pajic.mapstitch.util;

import me.pajic.mapstitch.MapStitch;

public class CompatFlags {
	public static final boolean YACL_LOADED = MapStitch.xplat().isModLoaded("yet_another_config_lib_v3");
	public static final boolean TRINKETS_LOADED = MapStitch.xplat().isModLoaded("trinkets_updated");
	public static final boolean OHMEGA_LOADED = MapStitch.xplat().isModLoaded("ohmega");
	public static final boolean CURIOS_LOADED = MapStitch.xplat().isModLoaded("curios");
}
