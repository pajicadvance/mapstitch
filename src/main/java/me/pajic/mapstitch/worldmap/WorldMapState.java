package me.pajic.mapstitch.worldmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pajic.mapstitch.MapStitch;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class WorldMapState {

    private File file;
	private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

	public int scale = 0;
	public int zoom = 0;
	public double x = 0;
	public double z = 0;
	public boolean help = false;
	public boolean grid = false;
	public boolean follow = false;

	public static WorldMapState load(File file) {
		WorldMapState state;
		if (file.exists()) try (FileReader reader = new FileReader(file)) {
            state = GSON.fromJson(reader, WorldMapState.class);
            if (state == null) throw new Exception();
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Can't load world map state, using default state");
            state = new WorldMapState();
        } else state = new WorldMapState();
		state.file = file;
		state.writeChanges();
		return state;
	}

	public void writeChanges() {
		File dir = file.getParentFile();
        try (FileWriter writer = new FileWriter(file)) {
            if (dir.exists() || dir.mkdirs()) GSON.toJson(this, writer);
            else throw new IOException("Couldn't create mod config directory");
        } catch (IOException e) {
            MapStitch.LOGGER.warn("World map state could not be saved, using default state on next world map display", e);
        }
	}
}
