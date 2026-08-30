package me.pajic.mapstitch.platform;

//$ loader_util_import
import me.pajic.mapstitch.platform.fabric.FabricLoaderUtil;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;

public interface MultiLoaderUtil {

    MultiLoaderUtil INSTANCE = /*$ loader_util_inst*/ new FabricLoaderUtil();

    boolean isModLoaded(String modId);
    boolean isDevEnv();
    Path configDir();
    void s2c(ServerPlayer player, CustomPacketPayload payload);
    void c2s(CustomPacketPayload payload);
}
