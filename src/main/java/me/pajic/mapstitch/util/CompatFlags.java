package me.pajic.mapstitch.util;

import me.pajic.mapstitch.MapStitch;

public class CompatFlags {
	public static final boolean TRINKETS_LOADED = MapStitch.xplat().isModLoaded("trinkets_updated");
	public static final boolean OHMEGA_LOADED = MapStitch.xplat().isModLoaded("ohmega");
}
