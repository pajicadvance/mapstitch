package me.pajic.mapstitch.minimap;

import net.minecraft.network.chat.Component;

public enum MinimapDisplayCondition {
	HANDS, HOTBAR, INVENTORY;

	public Component getName() {
		return Component.translatable("config.mapstitch.minimap.display_condition." + name());
	}
}
