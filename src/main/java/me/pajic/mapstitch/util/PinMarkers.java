package me.pajic.mapstitch.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PinMarkers {

    public static void draw(GuiGraphicsExtractor graphics, int sx, int sy, int color, boolean highlighted) {
        graphics.fill(sx, sy - 4, sx + 1, sy + 1, 0xFF6B6B6B);
        graphics.fill(sx - 3, sy - 10, sx + 4, sy - 3, highlighted ? 0xFFFFFFFF : 0xFF000000);
        graphics.fill(sx - 2, sy - 9, sx + 3, sy - 4, 0xFF000000 | color);
        graphics.fill(sx - 1, sy - 8, sx, sy - 7, 0x80FFFFFF);
    }
}
