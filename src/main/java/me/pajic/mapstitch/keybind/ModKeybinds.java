package me.pajic.mapstitch.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

	public static final KeyMapping.Category MOD_KEYS = new KeyMapping.Category(MapStitch.id("keys"));
	public static final KeyMapping.Category MOD_KEYS_WORLD_MAP = new KeyMapping.Category(MapStitch.id("keys_world_map"));

	public static final KeyMapping OPEN_WORLD_MAP = new KeyMapping(
			"mapstitch.key.open_world_map",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_M,
			MOD_KEYS
	);

	public static final KeyMapping SCALE_UP = new KeyMapping(
			"mapstitch.key.scale_up",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_S,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping SCALE_DOWN = new KeyMapping(
			"mapstitch.key.scale_down",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping DIMENSION_UP = new KeyMapping(
			"mapstitch.key.dimension_up",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_D,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping DIMENSION_DOWN = new KeyMapping(
			"mapstitch.key.dimension_down",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping FOLLOW_PLAYER = new KeyMapping(
			"mapstitch.key.follow_player",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping TOGGLE_GRID = new KeyMapping(
			"mapstitch.key.toggle_grid",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping EJECT_MAP = new KeyMapping(
			"mapstitch.key.eject_map",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_Q,
			MOD_KEYS_WORLD_MAP
	);

	public static final KeyMapping TOGGLE_HELP = new KeyMapping(
			"mapstitch.key.toggle_help",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			MOD_KEYS_WORLD_MAP
	);

	public static void onClientTick(Minecraft client) {
		if (client.player != null && client.level != null && OPEN_WORLD_MAP.consumeClick()) {
			client.player.playSound(SoundEvents.BOOK_PAGE_TURN);
			MapStitch.xplat().c2s(new C2SPlaySound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN)));
			client.setScreenAndShow(new WorldMapScreen());
		}
	}
}
