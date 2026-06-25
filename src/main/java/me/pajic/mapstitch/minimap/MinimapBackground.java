package me.pajic.mapstitch.minimap;

import net.minecraft.network.chat.Component;

public enum MinimapBackground {
	CLEAR, TEXTURE, NONE;

	public Component getName() {
		return Component.translatable("config.mapstitch.minimap.background." + name());
	}
}
