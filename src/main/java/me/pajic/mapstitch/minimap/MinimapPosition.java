package me.pajic.mapstitch.minimap;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum MinimapPosition implements EnumTranslatable {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

    @Override @NotNull
    public String prefix() {
        return "mapstitch.client_config.minimap.position";
    }

    public Component getName() {
        return Component.translatable(prefix() + "." + name());
    }
}
