package me.pajic.mapstitch.minimap;

import net.minecraft.network.chat.Component;

public enum MinimapPosition {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

	public Component getName() {
		return Component.translatable("config.mapstitch.minimap.position." + name());
	}
}
