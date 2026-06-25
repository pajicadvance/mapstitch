package me.pajic.mapstitch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.pajic.mapstitch.minimap.MinimapBackground;
import me.pajic.mapstitch.minimap.MinimapDisplayCondition;
import me.pajic.mapstitch.minimap.MinimapPosition;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.PRIVATE).create();
	private File file;

	public MinimapDisplayCondition minimapDisplayCondition = MinimapDisplayCondition.HOTBAR;
    public MinimapPosition minimapPosition = MinimapPosition.TOP_RIGHT;
	public int minimapSize = 19;
	public MinimapBackground minimapBackground = MinimapBackground.CLEAR;
	public int minimapBackgroundOpacity = 50;
	public int minimapXOffset = 0;
	public int minimapYOffset = 0;
	public int worldMapTextBackgroundOpacity = 50;
	public boolean worldMapHelp = true;

    public static ModConfig load(File file) {
        ModConfig config;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                config = new ModConfig();
            }
        } else {
            config = new ModConfig();
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
