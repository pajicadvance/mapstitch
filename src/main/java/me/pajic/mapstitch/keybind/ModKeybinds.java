package me.pajic.mapstitch.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.minimap.MinimapOverlay;import me.pajic.mapstitch.networking.payload.C2SPlaySound;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

//? >=26.1
import java.util.Map;

public class ModKeybinds {

    public static final Identifier KEYS_ID = MapStitch.id("keys");
    public static final Identifier KEYS_WORLD_MAP_ID = MapStitch.id("keys_world_map");

    //? >=26.1 {
    private static final Map<Identifier, KeyMapping.Category> CATEGORIES = Map.of(
            KEYS_ID, new KeyMapping.Category(KEYS_ID),
            KEYS_WORLD_MAP_ID, new KeyMapping.Category(KEYS_WORLD_MAP_ID)
    );
    //?}

	public static final KeyMapping OPEN_WORLD_MAP = create(GLFW.GLFW_KEY_M, "open_world_map", KEYS_ID);
    public static final KeyMapping TOGGLE_MINIMAP = create(GLFW.GLFW_KEY_UNKNOWN, "toggle_minimap", KEYS_ID);
	public static final KeyMapping SCALE_UP = create(GLFW.GLFW_KEY_S, "scale_up", KEYS_WORLD_MAP_ID);
	public static final KeyMapping SCALE_DOWN = create(GLFW.GLFW_KEY_UNKNOWN, "scale_down", KEYS_WORLD_MAP_ID);
	public static final KeyMapping DIMENSION_UP = create(GLFW.GLFW_KEY_D, "dimension_up", KEYS_WORLD_MAP_ID);
	public static final KeyMapping DIMENSION_DOWN = create(GLFW.GLFW_KEY_UNKNOWN, "dimension_down", KEYS_WORLD_MAP_ID);
	public static final KeyMapping FOLLOW_PLAYER = create(GLFW.GLFW_KEY_F, "follow_player", KEYS_WORLD_MAP_ID);
	public static final KeyMapping TOGGLE_GRID = create(GLFW.GLFW_KEY_G, "toggle_grid", KEYS_WORLD_MAP_ID);
	public static final KeyMapping EJECT_MAP = create(GLFW.GLFW_KEY_Q, "eject_map", KEYS_WORLD_MAP_ID);
	public static final KeyMapping TOGGLE_HELP = create(GLFW.GLFW_KEY_H, "toggle_help", KEYS_WORLD_MAP_ID);

	public static void onClientTick(Minecraft client) {
		if (client.player != null && client.level != null) {
            if (OPEN_WORLD_MAP.consumeClick()) {
                client.player.playSound(SoundEvents.BOOK_PAGE_TURN);
                MultiLoaderUtil.INSTANCE.c2s(new C2SPlaySound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BOOK_PAGE_TURN)));
                client.setScreenAndShow(new WorldMapScreen(-1));
            }
            if (TOGGLE_MINIMAP.consumeClick()) MinimapOverlay.toggle();
		}
	}

    private static KeyMapping create(int key, String name, Identifier category) {
        return new KeyMapping(
                "mapstitch.key." + name,
                InputConstants.Type.KEYSYM,
                key,
                //? <26.1 {
                /*"key.category." + category.toLanguageKey()
                *///?} else {
                CATEGORIES.get(category)
                //?}
        );
    }
}
