package me.pajic.mapstitch.worldmap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class WorldMapState {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.PRIVATE).create();
	private File file;

	public int scale = 0;
	public int zoom = 0;
	public double x = 0;
	public double z = 0;
	public boolean help = false;
	public boolean grid = false;
	public boolean follow = false;

	public static WorldMapState load(File file) {
		WorldMapState config;

		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				config = GSON.fromJson(reader, WorldMapState.class);
			} catch (Exception e) {
				config = new WorldMapState();
			}
		} else {
			config = new WorldMapState();
		}

		config.file = file;
		config.writeChanges();

		return config;
	}

	public void writeChanges() {
		File dir = file.getParentFile();

		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new RuntimeException("Could not create parent directories");
			}
		} else if (!dir.isDirectory()) {
			throw new RuntimeException("The parent file is not a directory");
		}

		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(this, writer);
		} catch (IOException e) {
			throw new RuntimeException("Could not save configuration file", e);
		}
	}
}
