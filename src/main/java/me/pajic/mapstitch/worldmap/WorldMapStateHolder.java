package me.pajic.mapstitch.worldmap;

import me.pajic.mapstitch.MapStitch;

public class WorldMapStateHolder {
	private static WorldMapState STATE;

	public static WorldMapState state() {
		if (STATE == null) init();
		return STATE;
	}

	public static void init() {
		STATE = WorldMapState.load(MapStitch.xplat().configDir().resolve("mapstitch_state").toFile());
	}
}
