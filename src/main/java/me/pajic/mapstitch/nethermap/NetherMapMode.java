package me.pajic.mapstitch.nethermap;

import me.fzzyhmstrs.fzzy_config.util.EnumTranslatable;
import org.jetbrains.annotations.NotNull;

public enum NetherMapMode implements EnumTranslatable {
    DYNAMIC, STATIC;

    @Override @NotNull
    public String prefix() {
        return "mapstitch.config.netherMap.mode";
    }
}
