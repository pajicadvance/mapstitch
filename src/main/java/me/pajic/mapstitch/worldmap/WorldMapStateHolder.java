package me.pajic.mapstitch.worldmap;

import me.pajic.mapstitch.platform.MultiLoaderUtil;

public class WorldMapStateHolder {

	private static WorldMapState STATE;

	public static WorldMapState state() {
		if (STATE == null) init();
		return STATE;
	}

	public static void init() {
		STATE = WorldMapState.load(MultiLoaderUtil.INSTANCE.configDir().resolve("world_map_state.json").toFile());
	}
}
