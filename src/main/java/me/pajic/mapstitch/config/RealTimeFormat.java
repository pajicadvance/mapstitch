package me.pajic.mapstitch.config;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum RealTimeFormat implements EnumTranslatable {
    H12, H24;

    @Override @NotNull
    public String prefix() {
        return "mapstitch.client_config.minimapInfo.realTimeFormat";
    }

    public Component getName() {
        return Component.translatable(prefix() + "." + name());
    }
}
