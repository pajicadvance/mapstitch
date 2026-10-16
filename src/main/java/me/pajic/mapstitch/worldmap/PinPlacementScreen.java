package me.pajic.mapstitch.worldmap;

import com.mojang.blaze3d.platform.InputConstants;
import me.pajic.mapstitch.networking.payload.C2SPlacePin;
import me.pajic.mapstitch.pin.ClientPins;
import me.pajic.mapstitch.pin.Pin;
import me.pajic.mapstitch.pin.PinUtil;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//? >=26.1 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
*///?}

public class PinPlacementScreen extends Screen {

    private static final int SWATCH = 16, GAP = 2, COLS = 8, VISIBLE_ROWS = 3, CELL = SWATCH + GAP;
    private static final int PANEL_W = COLS * CELL - GAP;
    private static final int PALETTE_H = VISIBLE_ROWS * CELL - GAP;

    private static final Comparator<Integer> PALETTE_ORDER = Comparator
            .comparingInt((Integer c) -> isGrayish(c) ? 1 : 0)
            .thenComparingDouble(c -> isGrayish(c) ? -luminance(c) : hue(c))
            .thenComparingInt(c -> c);

    private final Minecraft MC = Minecraft.getInstance();
    private final WorldMapScreen parent;
    private final Identifier dimension;
    private final int x, z;
    private final boolean showCoords;

    private Map<Integer, Integer> counts = Map.of();
    private List<Integer> colors = List.of();
    private @Nullable Integer selected;
    private int scrollRow;
    private int left, top, paletteY;
    private boolean itemRequired;

    private EditBox nameBox;
    private Button confirmButton;

    public PinPlacementScreen(WorldMapScreen parent, Identifier dimension, int x, int z, boolean showCoords) {
        super(Component.translatable("mapstitch.gui.pin.title"));
        this.parent = parent;
        this.dimension = dimension;
        this.x = x;
        this.z = z;
        this.showCoords = showCoords;
    }

    @Override
    protected void init() {
        left = width / 2 - PANEL_W / 2;
        top = height / 2 - 70;
        paletteY = top + 64;

        String previousName = nameBox != null ? nameBox.getValue() : "";
        nameBox = new EditBox(font, left, top + 30, PANEL_W, 18, Component.translatable("mapstitch.gui.pin.name"));
        nameBox.setMaxLength(Pin.MAX_NAME_LENGTH);
        nameBox.setValue(previousName);
        addRenderableWidget(nameBox);
        setInitialFocus(nameBox);

        itemRequired = PinUtil.itemRequired();
        counts = itemRequired && MC.player != null ? PinUtil.collectPinColors(MC.player) : Map.of();
        List<Integer> sorted = new ArrayList<>(itemRequired ? counts.keySet() : freeColors());
        sorted.sort(PALETTE_ORDER);
        colors = sorted;

        if (selected == null || !colors.contains(selected)) {
            selected = colors.contains(PinUtil.DEFAULT_COLOR) ? Integer.valueOf(PinUtil.DEFAULT_COLOR)
                    : colors.isEmpty() ? null : colors.getFirst();
        }
        scrollRow = Mth.clamp(scrollRow, 0, maxScroll());

        int buttonsY = paletteY + PALETTE_H + 18;
        confirmButton = addRenderableWidget(Button.builder(
                Component.translatable("mapstitch.gui.pin.confirm"), b -> confirm()
        ).pos(left, buttonsY).size(PANEL_W / 2 - 1, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("mapstitch.gui.pin.cancel"), b -> onClose()
        ).pos(left + PANEL_W / 2 + 1, buttonsY).size(PANEL_W / 2 - 1, 20).build());
        confirmButton.active = selected != null;
    }

    private int totalRows() { return (colors.size() + COLS - 1) / COLS; }
    private int maxScroll() { return Math.max(0, totalRows() - VISIBLE_ROWS); }

    private boolean inPalette(double mx, double my) {
        return mx >= left && mx < left + PANEL_W + 6 && my >= paletteY && my < paletteY + PALETTE_H;
    }

    private int colorIndexAt(double mx, double my) {
        double rx = mx - left, ry = my - paletteY;
        if (rx < 0 || ry < 0) return -1;
        int col = (int) (rx / CELL), row = (int) (ry / CELL);
        if (col >= COLS || row >= VISIBLE_ROWS || rx % CELL >= SWATCH || ry % CELL >= SWATCH) return -1;
        int index = (scrollRow + row) * COLS + col;
        return index < colors.size() ? index : -1;
    }

    private void confirm() {
        if (MC.player == null || selected == null) return;
        if (itemRequired && PinUtil.countPins(MC.player, selected) <= 0) return;
        MultiLoaderUtil.INSTANCE.c2s(new C2SPlacePin(nameBox.getValue().strip(), selected, dimension, x, z));
        MC.player.playSound(SoundEvents.BUNDLE_INSERT);
        onClose();
    }

    //~ if <26.1 'graphics.nextStratum()' -> 'graphics.flush()' {
    @Override
    //~ if <26.1 'extractBackground' -> 'renderBackground'
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        parent.setPinPreview(x, z, selected);
        //? >=26.1 {
        parent.extractBackground(graphics, -10000, -10000, a);
        graphics.nextStratum();
        //?}
        parent.extractAsBackground(graphics, a);
        graphics.nextStratum();
        //? <26.1
        //RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        graphics.fill(0, 0, width, height, 0x60000000);
        int bottom = confirmButton.getY() + 28;
        graphics.fill(left - 9, top - 9, left + PANEL_W + 9, bottom + 1, 0xFF000000);
        graphics.fill(left - 8, top - 8, left + PANEL_W + 8, bottom, 0xE0202020);
        //? <26.1
        //graphics.nextStratum();
    }
    //~}

    @Override
    //~ if <26.1 'extractRenderState' -> 'render'
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        //~ if <26.1 'extractRenderState' -> 'render'
        super.extractRenderState(graphics, mouseX, mouseY, a);

        graphics.text(font, title, width / 2 - font.width(title) / 2, top, -1);
        if (showCoords) {
            Component loc = Component.translatable("mapstitch.gui.pin.location", x, z);
            graphics.text(font, loc, width / 2 - font.width(loc) / 2, top + 12, 0xFFA0A0A0);
        }
        graphics.text(font, Component.translatable("mapstitch.gui.pin.color"), left, top + 53, -1);

        graphics.fill(left - 2, paletteY - 2, left + PANEL_W + 2, paletteY + PALETTE_H + 2, 0x80000000);

        if (colors.isEmpty()) {
            Component none = Component.translatable("mapstitch.gui.pin.no_pins");
            graphics.text(font, none, width / 2 - font.width(none) / 2, paletteY + PALETTE_H / 2 - 4, 0xFFFF5555);
            return;
        }

        int hoveredIndex = colorIndexAt(mouseX, mouseY);
        int first = scrollRow * COLS;
        int last = Math.min(colors.size(), first + VISIBLE_ROWS * COLS);
        for (int i = first; i < last; i++) {
            int color = colors.get(i);
            int sx = left + ((i - first) % COLS) * CELL;
            int sy = paletteY + ((i - first) / COLS) * CELL;
            int border = Objects.equals(selected, color) ? 0xFFFFFFFF : i == hoveredIndex ? 0xFFA0A0A0 : 0xFF000000;
            graphics.fill(sx, sy, sx + SWATCH, sy + SWATCH, border);
            graphics.fill(sx + 2, sy + 2, sx + SWATCH - 2, sy + SWATCH - 2, 0xFF000000 | color);
        }

        if (maxScroll() > 0) {
            int barX = left + PANEL_W + 3;
            int thumbH = Math.max(6, PALETTE_H * VISIBLE_ROWS / totalRows());
            int thumbY = paletteY + (PALETTE_H - thumbH) * scrollRow / maxScroll();
            graphics.fill(barX, paletteY, barX + 3, paletteY + PALETTE_H, 0xFF202020);
            graphics.fill(barX, thumbY, barX + 3, thumbY + thumbH, 0xFFA0A0A0);
        }

        Integer info = hoveredIndex >= 0 ? colors.get(hoveredIndex) : selected;
        if (info != null) {
            Component hex = Component.literal(String.format("#%06X", info)).withColor(info);
            Component text = itemRequired
                    ? Component.translatable("mapstitch.gui.pin.color_info", hex, counts.getOrDefault(info, 0))
                    : hex;
            graphics.text(font, text, width / 2 - font.width(text) / 2, paletteY + PALETTE_H + 5, -1);
        }
    }

    @Override
    //~ if <26.1 'MouseButtonEvent event, boolean doubleClick' -> 'double mouseX, double mouseY, int button'
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        //~ if <26.1 'event.button()' -> 'button'
        //~ if >26.2 '0' -> '1'
        if (event.button() == 1) {
            //~ if <26.1 'event.x(), event.y()' -> 'mouseX, mouseY'
            int index = colorIndexAt(event.x(), event.y());
            if (index >= 0) {
                selected = colors.get(index);
                confirmButton.active = true;
                MC.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        //~ if <26.1 'event, doubleClick' -> 'mouseX, mouseY, button'
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll() > 0 && inPalette(mouseX, mouseY)) {
            scrollRow = Mth.clamp(scrollRow - (int) Math.signum(scrollY), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    //~ if <26.1 '@NotNull KeyEvent event' -> 'int keyCode, int scanCode, int modifiers'
    public boolean keyPressed(@NotNull KeyEvent event) {
        //~ if <26.1 'event.key()' -> 'keyCode'
        int key = event.key();
        if ((key == InputConstants.KEY_RETURN || key == InputConstants.KEY_NUMPADENTER) && confirmButton.active) {
            confirm();
            return true;
        }
        //~ if <26.1 'event' -> 'keyCode, scanCode, modifiers'
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        parent.setPinPreview(0, 0, null);
        //~ if <26.1 'setScreenAndShow' -> 'setScreen'
        MC.setScreenAndShow(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    //? >=26.1 {
    @Override
    public boolean isInGameUi() {
        return true;
    }
    //?}

    private static Set<Integer> freeColors() {
        Set<Integer> set = new LinkedHashSet<>();
        for (DyeColor dye : DyeColor.values()) set.add(dye.getTextureDiffuseColor() & 0xFFFFFF);
        for (Pin pin : ClientPins.get()) set.add(pin.color() & 0xFFFFFF);
        return set;
    }

    private static boolean isGrayish(int c) {
        int r = c >> 16 & 0xFF, g = c >> 8 & 0xFF, b = c & 0xFF;
        return Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b)) < 24;
    }

    private static double luminance(int c) {
        return 0.299 * (c >> 16 & 0xFF) + 0.587 * (c >> 8 & 0xFF) + 0.114 * (c & 0xFF);
    }

    private static double hue(int c) {
        double r = (c >> 16 & 0xFF) / 255.0, g = (c >> 8 & 0xFF) / 255.0, b = (c & 0xFF) / 255.0;
        double max = Math.max(r, Math.max(g, b)), d = max - Math.min(r, Math.min(g, b));
        if (d == 0) return 0;
        double h = max == r ? ((g - b) / d) % 6 : max == g ? (b - r) / d + 2 : (r - g) / d + 4;
        return h < 0 ? h + 6 : h;
    }
}
