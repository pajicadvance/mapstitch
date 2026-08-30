package me.pajic.mapstitch.util;

import me.pajic.mapstitch.platform.MultiLoaderUtil;

public class CompatFlags {

	public static final boolean TRINKETS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("trinkets_updated") || MultiLoaderUtil.INSTANCE.isModLoaded("trinkets");
	public static final boolean OHMEGA_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("ohmega");
	public static final boolean CURIOS_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("curios");
    public static final boolean REMAPPED_LOADED = MultiLoaderUtil.INSTANCE.isModLoaded("remapped");
}
