package me.pajic.mapstitch.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {

	public static final KeyMapping OPEN_WORLD_MAP = new KeyMapping(
			"mapstitch.key.open_world_map",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_M,
			"key.category.mapstitch.keys"
	);

	public static final KeyMapping SCALE_UP = new KeyMapping(
			"mapstitch.key.scale_up",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_S,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping SCALE_DOWN = new KeyMapping(
			"mapstitch.key.scale_down",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping DIMENSION_UP = new KeyMapping(
			"mapstitch.key.dimension_up",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_D,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping DIMENSION_DOWN = new KeyMapping(
			"mapstitch.key.dimension_down",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_UNKNOWN,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping FOLLOW_PLAYER = new KeyMapping(
			"mapstitch.key.follow_player",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping TOGGLE_GRID = new KeyMapping(
			"mapstitch.key.toggle_grid",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_G,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping EJECT_MAP = new KeyMapping(
			"mapstitch.key.eject_map",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_Q,
			"key.category.mapstitch.keys_world_map"
	);

	public static final KeyMapping TOGGLE_HELP = new KeyMapping(
			"mapstitch.key.toggle_help",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			"key.category.mapstitch.keys_world_map"
	);

	public static void onClientTick(Minecraft client) {
		if (client.player != null && client.level != null && OPEN_WORLD_MAP.consumeClick()) {
			client.player.playSound(SoundEvents.BOOK_PAGE_TURN);
			PacketDistributor.sendToServer(new C2SPlaySound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN)));
			client.setScreen(new WorldMapScreen(-1));
		}
	}
}
