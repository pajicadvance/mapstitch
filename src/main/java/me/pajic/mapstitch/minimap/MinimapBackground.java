package me.pajic.mapstitch.minimap;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum MinimapBackground implements EnumTranslatable {
	CLEAR, TEXTURE, NONE;

    @Override @NotNull
    public String prefix() {
        return "mapstitch.client_config.minimap.background";
    }

	public Component getName() {
		return Component.translatable(prefix() + "." + name());
	}
}
