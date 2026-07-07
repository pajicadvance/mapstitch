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

	public static final KeyMapping OPEN_WORLD_MAP = new KeyMapping(
			"mapstitch.key.open_world_map",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_M,
			MOD_KEYS
	);

	public static void onClientTick(Minecraft client) {
		if (client.player != null && client.level != null && OPEN_WORLD_MAP.consumeClick()) {
			client.player.playSound(SoundEvents.BOOK_PAGE_TURN);
			MapStitch.xplat().c2s(new C2SPlaySound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN)));
			client.setScreenAndShow(new WorldMapScreen());
		}
	}
}
