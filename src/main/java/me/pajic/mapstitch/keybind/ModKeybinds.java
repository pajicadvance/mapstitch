package me.pajic.mapstitch.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

//? >=26.1
import me.pajic.mapstitch.MapStitch;

public class ModKeybinds {

	public static final KeyMapping OPEN_WORLD_MAP = create(GLFW.GLFW_KEY_M, "open_world_map", "");
	public static final KeyMapping SCALE_UP = create(GLFW.GLFW_KEY_S, "scale_up", "world_map");
	public static final KeyMapping SCALE_DOWN = create(GLFW.GLFW_KEY_UNKNOWN, "scale_down", "world_map");
	public static final KeyMapping DIMENSION_UP = create(GLFW.GLFW_KEY_D, "dimension_up", "world_map");
	public static final KeyMapping DIMENSION_DOWN = create(GLFW.GLFW_KEY_UNKNOWN, "dimension_down", "world_map");
	public static final KeyMapping FOLLOW_PLAYER = create(GLFW.GLFW_KEY_F, "follow_player", "world_map");
	public static final KeyMapping TOGGLE_GRID = create(GLFW.GLFW_KEY_G, "toggle_grid", "world_map");
	public static final KeyMapping EJECT_MAP = create(GLFW.GLFW_KEY_Q, "eject_map", "world_map");
	public static final KeyMapping TOGGLE_HELP = create(GLFW.GLFW_KEY_H, "toggle_help", "world_map");

	public static void onClientTick(Minecraft client) {
		if (client.player != null && client.level != null && OPEN_WORLD_MAP.consumeClick()) {
			client.player.playSound(SoundEvents.BOOK_PAGE_TURN);
			MultiLoaderUtil.INSTANCE.c2s(new C2SPlaySound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN)));
			client.setScreenAndShow(new WorldMapScreen(-1));
		}
	}

    private static KeyMapping create(int key, String name, String category) {
        return new KeyMapping(
                "mapstitch.key." + name,
                InputConstants.Type.KEYSYM,
                key,
                //? <26.1 {
                /*"key.category.mapstitch.keys_" + category
                *///?} else {
                new KeyMapping.Category(MapStitch.id("keys_" + category))
                //?}
        );
    }
}
