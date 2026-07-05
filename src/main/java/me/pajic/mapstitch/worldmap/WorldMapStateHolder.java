package me.pajic.mapstitch.worldmap;

import net.neoforged.fml.loading.FMLPaths;

public class WorldMapStateHolder {
	private static WorldMapState STATE;

	public static WorldMapState state() {
		if (STATE == null) init();
		return STATE;
	}

	public static void init() {
		STATE = WorldMapState.load(FMLPaths.CONFIGDIR.get().resolve("mapstitch_state").toFile());
	}
}
