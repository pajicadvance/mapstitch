package me.pajic.mapstitch.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface Platform {

	boolean isModLoaded(String modId);

	boolean isDevelopmentEnvironment();

	default boolean isDebug() {
		return isDevelopmentEnvironment();
	}

	Path configDir();

	void s2c(ServerPlayer player, CustomPacketPayload payload);

	void c2s(CustomPacketPayload payload);
}
