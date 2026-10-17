package me.pajic.mapstitch.worldmap;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.MapStitchClient;
import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.networking.payload.C2SRemovePin;
import me.pajic.mapstitch.pin.ClientPins;
import me.pajic.mapstitch.pin.Pin;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import me.pajic.mapstitch.util.ModClientUtil;
import me.pajic.mapstitch.util.PinMarkers;
import me.pajic.mapstitch.util.PlayerMarkers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

//? <26.1 {
/*import me.pajic.mapstitch.mixin.accessor.ScreenAccessor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.client.gui.components.Renderable;
*///?} else {
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
//?}

public class WorldMapScreen extends Screen {

	private final Minecraft MC = Minecraft.getInstance();
	private static final Map<GridPos, MapDataWithId> MAPS = new HashMap<>();
    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration' {
	private static final Map<GridPos, List<MapRenderState.MapDecorationRenderState>> DECORATIONS = new HashMap<>();
    private static final Map<GridPos, MapDecorsWithId> EXPLORATION_MARKERS = new HashMap<>();
    //~}
	private static final Identifier ATLAS_CRAFTING = MapStitch.id("textures/gui/atlas_crafting.png");

	public static List<Identifier> dimensionIds = List.of();
	private static Identifier dimensionId = Identifier.withDefaultNamespace("overworld");

	private static int posX = 0;
	private static int posY = 0;
	private static int posZ = 0;
	private static int zoomLevel;
    private static float updateTimer = 1;

	private boolean hasCompass;
    private boolean coordinatesAllowed;
	private int mapSize;
	private double mapPixels;
	private double camX, camZ;
	private int mouseX, mouseY;
	private float zoom;
	private int screenW, screenH;
	private int scale;
	private boolean help;
	private boolean grid;
    private boolean gridAllowed;
	private boolean follow;
	private int mapsRendered = 0;

    private @Nullable AbstractClientPlayer hoveredPlayer;
    private float hoveredPlayerX, hoveredPlayerY;

    private boolean renderingAsBackground;
    private int previewX, previewZ;
    private @Nullable Integer previewColor;

    private boolean pinMode;
    private boolean mapSourcesPresent;
    private Pin hoveredPin;
    private boolean pinPressActive;
    private double pinPressX, pinPressY, pinDragDistance;
    private @Nullable Pin pressedPin;

    private final Button zoomIn = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.zoom_in"),
            b -> zoomLevel = Mth.clamp(zoomLevel + 1, -2, 1)
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.zoom_in.tooltip"))
    ).build();

    private final Button zoomOut = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.zoom_out"),
            b -> zoomLevel = Mth.clamp(zoomLevel - 1, -2, 1)
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.zoom_out.tooltip"))
    ).build();

    private final Button nextDimension = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.dimension"), b -> {
                int i = dimensionIds.indexOf(dimensionId);
                dimensionId = i + 1 > dimensionIds.size() - 1 ? dimensionIds.getFirst() : dimensionIds.get(i + 1);
                clearMaps();
            }
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.dimension.tooltip"))
    ).build();

    private final Button nextScale = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.scale"),
            b -> {
                if (scale < 4) scale++;
                else scale = 0;
            }
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.scale.tooltip"))
    ).build();

    private final Button centerToPlayer = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.center"),
            b -> centerMap()
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.center.tooltip"))
    ).build();

    private final Button toggleGrid = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.grid"),
            b -> grid = !grid
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.grid.tooltip"))
    ).build();

    private final Button togglePlayerFollow = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.follow"),
            b -> follow = !follow
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.follow.tooltip"))
    ).build();

    private final Button togglePinPlacement = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.pin"),
            b -> pinMode = !pinMode
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.pin.tooltip"))
    ).build();

    private final Button toggleHelp = Button.builder(
            Component.translatable("mapstitch.gui.worldmap.button.help"),
            b -> help = !help
    ).size(16, 16).tooltip(
            Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.help.tooltip"))
    ).build();

    @SuppressWarnings("DataFlowIssue")
	public WorldMapScreen(int scaleOverride) {
		super(Component.translatable("mapstitch.gui.worldmap.title"));
		WorldMapState state = WorldMapStateHolder.state();
		scale = scaleOverride > -1 ? scaleOverride : state.scale;
		zoomLevel = state.zoom;
		camX = state.x;
		camZ = state.z;
		help = state.help;
		grid = state.grid;
		follow = state.follow;
		ModClientUtil.worldMapOpen = true;
		dimensionId = MC.level.dimension().identifier();
	}

	@Override
	protected void init() {
		if (MapStitchClient.CONFIG.worldMap.buttons.get()) {
            List<Button> buttons = List.of(
                    zoomIn, zoomOut, nextDimension, nextScale, centerToPlayer,
                    toggleGrid, togglePlayerFollow, togglePinPlacement, toggleHelp
            );
            int x = width - 20;
            int y = height / 2 - (buttons.size() * 18) / 2;
            for (Button button : buttons) {
                button.setPosition(x, y);
                addRenderableWidget(button);
                y += 18;
            }
            addRenderableWidget(zoomIn);
            addRenderableWidget(zoomOut);
            addRenderableWidget(nextDimension);
            addRenderableWidget(nextScale);
            addRenderableWidget(centerToPlayer);
            addRenderableWidget(toggleGrid);
            addRenderableWidget(togglePlayerFollow);
            addRenderableWidget(togglePinPlacement);
            addRenderableWidget(toggleHelp);
		}
	}

	private record MapDataWithId(MapId id, MapItemSavedData data) {}

    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
    private record MapDecorsWithId(MapId id, List<MapRenderState.MapDecorationRenderState> decors) {}

	public record GridPos(int gx, int gy, int scale) {}

    public GridPos worldToGrid(int worldX, int worldZ, int scale) {
		return new GridPos(
				Math.floorDiv(worldX, 128 << scale),
				Math.floorDiv(worldZ, 128 << scale),
				scale
		);
	}
    public GridPos screenToGrid(double screenX, double screenY, int scale) {
		return new GridPos(
				Math.floorDiv(Mth.floor(screenToWorldX(screenX)) + 64, 128 << scale),
				Math.floorDiv(Mth.floor(screenToWorldZ(screenY)) + 64, 128 << scale),
				scale
		);
	}

    public int gridOriginX(int gx) { return gx * mapSize - 64; }
    public int gridOriginZ(int gy) { return gy * mapSize - 64; }

    private int originX(int gx, int scale) { return gx * (128 << scale) - 64; }
    private int originZ(int gy, int scale) { return gy * (128 << scale) - 64; }

    private double screenOffsetX() { return Math.round(screenW / 2.0 - camX * zoom); }
    private double screenOffsetZ() { return Math.round(screenH / 2.0 - camZ * zoom); }

    public double worldToScreenX(double worldX) { return worldX * zoom + screenOffsetX(); }
    public double worldToScreenZ(double worldZ) { return worldZ * zoom + screenOffsetZ(); }
    public double screenToWorldX(double screenX) { return (screenX - screenOffsetX()) / zoom; }
    public double screenToWorldZ(double screenY) { return (screenY - screenOffsetZ()) / zoom; }

    private int pinScreenX(Pin pin) { return (int) Math.round(worldToScreenX(pin.x() + 0.5)); }
    private int pinScreenY(Pin pin) { return (int) Math.round(worldToScreenZ(pin.z() + 0.5)); }

    private int cellEdgeX(int gx) { return (int) Math.round(worldToScreenX(gridOriginX(gx))); }
    private int cellEdgeY(int gy) { return (int) Math.round(worldToScreenZ(gridOriginZ(gy))); }

	@SuppressWarnings("DataFlowIssue")
	@Override
    //~ if <26.1 'extractRenderState' -> 'render'
	public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (updateTimer <= 0) {
            //? <26.1 {
            /*renderBackground(graphics, mouseX, mouseY, a);
             *///?}
            mapSize = 128 << scale;
            mapPixels = mapSize * zoom;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            int highlightColor = MapStitchClient.CONFIG.worldMap.textHighlightColor.get().color;
            int xBoundMin = Math.floorDiv((int)(camX - (screenW/2d)/zoom), mapSize);
            int xBoundMax = Math.floorDiv((int)(camX + (screenW/2d)/zoom), mapSize) + 1;
            int zBoundMin = Math.floorDiv((int)(camZ - (screenH/2d)/zoom), mapSize);
            int zBoundMax = Math.floorDiv((int)(camZ + (screenH/2d)/zoom), mapSize) + 1;
            renderMaps(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
            renderDecorations(graphics);
            renderExplorationMarkers(graphics);
            if (hasCompass && MC.level.dimension().identifier().equals(dimensionId)) renderPlayerMarkers(graphics);
            else hoveredPlayer = null;
            renderPins(graphics);
            if (gridAllowed && grid) {
                renderGrid(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
                if (!renderingAsBackground) renderPosAtCursor(graphics, mouseX, mouseY, highlightColor);
            }
            renderPlayerLabel(graphics, highlightColor);
            renderText(graphics, highlightColor);
            mapsRendered = 0;
            //? <26.1 {
            /*for (Renderable renderable : ((ScreenAccessor) this).mapstitch$getRenderables()) {
                renderable.render(graphics, mouseX, mouseY, a);
            }
            *///?} else {
            super.extractRenderState(graphics, mouseX, mouseY, a);
            //?}
        }
        else updateTimer -= a;
	}

    public void extractAsBackground(GuiGraphicsExtractor graphics, float a) {
        renderingAsBackground = true;
        //~ if <26.1 'extractRenderState' -> 'render'
        extractRenderState(graphics, -10000, -10000, a);
        renderingAsBackground = false;
    }

    public void setPinPreview(int x, int z, @Nullable Integer color) {
        previewX = x;
        previewZ = z;
        previewColor = color;
    }

	private void renderMaps(GuiGraphicsExtractor graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
		if (!MapStitchClient.CONFIG.worldMap.help.get()) help = false;
		if (MC.level == null || MC.player == null) return;
		boolean hasAnyMapSources = false;
		hasCompass = ModClientUtil.hasCompass(MC, "playerMarker");
        coordinatesAllowed = ModClientUtil.hasCompass(MC, "coordinates") && !MC.showOnlyReducedInfo();
        gridAllowed = ModClientUtil.hasCompass(MC, "grid") && coordinatesAllowed;
        togglePlayerFollow.active = centerToPlayer.active = hasCompass;
        toggleGrid.active = gridAllowed;
		if (hasCompass) {
			posX = MC.player.blockPosition().getX();
			posY = MC.player.blockPosition().getY();
			posZ = MC.player.blockPosition().getZ();
			if (follow) centerMap();
		}
		zoom = (float) Math.pow(2, zoomLevel) / (float) Math.pow(2, scale);
        List<String> locations = MapStitch.CONFIG.itemRequirements.worldMapAtlasScan;
		List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < MC.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = MC.player.getInventory().getItem(i);
            if (stack.is(ModItems.ATLAS)) {
                if (locations.contains("offhand") && i == 40) items.add(stack);
                if (locations.contains("hotbar") && i < 9) items.add(stack);
                if (locations.contains("inventory") && i >= 9 && i < 36) items.add(stack);
            }
        }
        if (locations.contains("accessories") && AccessoryUtil.INSTANCE != null) {
            items.addAll(AccessoryUtil.INSTANCE.getAtlases(MC.player));
        }
		for (ItemStack stack : items) if (stack.is(ModItems.ATLAS)) hasAnyMapSources = prepareAtlas(stack);
		screenW = MC.getWindow().getGuiScaledWidth();
		screenH = MC.getWindow().getGuiScaledHeight();
		if (hasAnyMapSources) {
			if (MAPS.isEmpty()) {
				hasCompass = false;
                gridAllowed = false;
				Component text = Component.translatable("mapstitch.gui.worldmap.no_maps_rendered");
				graphics.text(MC.font, text, width / 2 - font.width(text) / 2, height / 2, -1);
			} else {
                if (MapStitchClient.CONFIG.worldMap.checkerboard.get()) {
                    renderCheckerboard(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
                }
                for (int gy = zBoundMin; gy <= zBoundMax; gy++) {
                    for (int gx = xBoundMin; gx <= xBoundMax; gx++) {
                        GridPos gp = new GridPos(gx, gy, scale);
                        MapDataWithId map = MAPS.get(gp);
                        double px = worldToScreenX(gridOriginX(gx));
                        double py = worldToScreenZ(gridOriginZ(gy));
                        MultiVersionUtil.INSTANCE.pushPose(graphics);
                        MultiVersionUtil.INSTANCE.translatePose(graphics, (float) px, (float) py, 0);
                        MultiVersionUtil.INSTANCE.scalePose(graphics, (float) (mapPixels / 128), (float) (mapPixels / 128), 0);
                        if (map != null && map.data.scale == scale) {
                            MultiVersionUtil.INSTANCE.renderMap(MC, graphics, map.id, map.data, gp, false);
                            mapsRendered++;
                        }
                        MultiVersionUtil.INSTANCE.popPose(graphics);
                    }
                }
            }
		} else {
			hasCompass = false;
            gridAllowed = false;
            MultiVersionUtil.INSTANCE.pushPose(graphics);
            MultiVersionUtil.INSTANCE.translatePose(graphics, width / 2F, height / 2F - 46, 0);
			Component text1 = Component.translatable("mapstitch.gui.worldmap.no_map_sources")
					.withColor(MapStitchClient.CONFIG.worldMap.textHighlightColor.get().color);
			Component text2 = Component.translatable("mapstitch.gui.worldmap.no_map_sources_info");
			graphics.text(MC.font, text1, -font.width(text1) / 2, 0, -1);
			graphics.text(MC.font, text2, -font.width(text2) / 2, 12, -1);
            MultiVersionUtil.INSTANCE.blit(
                    graphics, ATLAS_CRAFTING,
                    -60, 24,
                    0, 0,
                    120, 68,
                    120, 68
            );
            MultiVersionUtil.INSTANCE.popPose(graphics);
		}
        mapSourcesPresent = hasAnyMapSources;
	}

    private void renderCheckerboard(GuiGraphicsExtractor graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
        for (int gy = zBoundMin; gy <= zBoundMax; gy++) {
            int y0 = Math.max(0, cellEdgeY(gy));
            int y1 = Math.min(screenH, cellEdgeY(gy + 1));
            if (y0 >= y1) continue;
            for (int gx = xBoundMin + ((xBoundMin + gy) & 1); gx <= xBoundMax; gx += 2) {
                int x0 = Math.max(0, cellEdgeX(gx));
                int x1 = Math.min(screenW, cellEdgeX(gx + 1));
                if (x0 >= x1) continue;
                graphics.fill(x0, y0, x1, y1, 0x10FFFFFF);
            }
        }
    }

	private void renderDecorations(GuiGraphicsExtractor graphics) {
		DECORATIONS.forEach((gp, decors) -> {
			double px = worldToScreenX(gridOriginX(gp.gx));
			double py = worldToScreenZ(gridOriginZ(gp.gy));
			float s = (float) Math.pow(2, zoomLevel);
            MultiVersionUtil.INSTANCE.pushPose(graphics);
            MultiVersionUtil.INSTANCE.translatePose(graphics, (float) px, (float) py, 0);
            MultiVersionUtil.INSTANCE.scalePose(graphics, (float) (mapPixels / 128), (float) (mapPixels / 128), 0);
            ModClientUtil.renderDecorations(MC, graphics, decors, s);
            MultiVersionUtil.INSTANCE.popPose(graphics);
		});
	}

	@SuppressWarnings("DataFlowIssue")
	private boolean prepareAtlas(ItemStack atlas) {
        BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        boolean hasAnyMaps = false;
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
        for (ItemStackTemplate map : contents.items()) {
            if (map.get(DataComponents.MAP_ID) != null) {
                hasAnyMaps = true;
                MapId id = map.get(DataComponents.MAP_ID);
                Vector2i mapCenter = map.get(ModDataComponents.MAP_CENTER);
                if (id != null && mapCenter != null) {
                    MapItemSavedData data = MC.level.getMapData(id);
                    if (data != null && dimensionId.equals(data.dimension.identifier())) {
                        //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
                        List<MapRenderState.MapDecorationRenderState> decors = ModClientUtil.extractDecors(data, id, MC);
                        if (ModClientUtil.isExplorationMap(decors, map/*? >=26.1 {*/.create()/*?}*/)) {
                            EXPLORATION_MARKERS.put(worldToGrid(mapCenter.x, mapCenter.y, data.scale), new MapDecorsWithId(id, decors));
                        }
                        else MAPS.put(worldToGrid(mapCenter.x, mapCenter.y, data.scale), new MapDataWithId(id, data));
                    }
                }
            }
        }
        return hasAnyMaps;
	}

    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
	public static void prepareDecoration(MapRenderState.MapDecorationRenderState decor, GridPos gp) {
        //? >=26.1 {
		decor.renderOnFrame = false;
		Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType();
        //?}
        //~ if <26.1 'type' -> 'decor.type()'
		if (!ModClientUtil.DECORS_REQUIRING_COMPASS.contains(type)) {
			DECORATIONS.putIfAbsent(gp, new ArrayList<>());
			if (DECORATIONS.get(gp).stream().noneMatch(d ->
                    d.x/*? <26.1 {*//*()*//*?}*/ == decor.x/*? <26.1 {*//*()*//*?}*/ && d.y/*? <26.1 {*//*()*//*?}*/ == decor.y/*? <26.1 {*//*()*//*?}*/)
            ) DECORATIONS.get(gp).add(decor);
		}
	}

    private void renderExplorationMarkers(GuiGraphicsExtractor graphics) {
        int margin = 4;
        int topInset  = grid ? 12 : 0;
        int leftInset = grid ? getVerticalGridBarWidth(MC.font) : 0;

        EXPLORATION_MARKERS.forEach((gp, decorsWithId) -> {
            int markerMapSize = 128 << gp.scale;
            double markerMapPixels = markerMapSize * zoom;
            double posScale = markerMapPixels / 128.0;
            double tileOriginX = worldToScreenX(originX(gp.gx(), gp.scale));
            double tileOriginY = worldToScreenZ(originZ(gp.gy(), gp.scale));
            float decorSize = (float) Math.pow(2, zoomLevel - scale + gp.scale);

            //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
            List<MapRenderState.MapDecorationRenderState> onScreen = new ArrayList<>();

            decorsWithId.decors().forEach(decor -> {
                double absX = tileOriginX + (decor.x/*? <26.1 {*//*()*//*?}*/ / 2.0 + 64.0) * posScale;
                double absY = tileOriginY + (decor.y/*? <26.1 {*//*()*//*?}*/ / 2.0 + 64.0) * posScale;
                double clampedX = Mth.clamp(absX, leftInset + margin, screenW - margin);
                double clampedY = Mth.clamp(absY, topInset + margin, screenH - margin);

                if (clampedX == absX && clampedY == absY) {
                    onScreen.add(decor);
                    return;
                }

                //? >=26.1 {
                byte origX = decor.x, origY = decor.y;
                decor.x = (byte) -128;
                decor.y = (byte) -128;
                //?}
                MultiVersionUtil.INSTANCE.pushPose(graphics);
                MultiVersionUtil.INSTANCE.translatePose(graphics, (float) clampedX, (float) clampedY, 0);
                MultiVersionUtil.INSTANCE.scalePose(graphics, (float) posScale, (float) posScale, 0);
                //~ if <26.1 'List.of(decor)' -> 'List.of(new MapDecoration(decor.type(), (byte) -128, (byte) -128, decor.rot(), decor.name()))'
                ModClientUtil.renderDecorations(MC, graphics, List.of(decor), decorSize);
                MultiVersionUtil.INSTANCE.popPose(graphics);
                //? >=26.1 {
                decor.x = origX;
                decor.y = origY;
                //?}
            });

            if (!onScreen.isEmpty()) {
                MultiVersionUtil.INSTANCE.pushPose(graphics);
                MultiVersionUtil.INSTANCE.translatePose(graphics, (float) tileOriginX, (float) tileOriginY, 0);
                MultiVersionUtil.INSTANCE.scalePose(graphics, (float) posScale, (float) posScale, 0);
                ModClientUtil.renderDecorations(MC, graphics, onScreen, decorSize);
                MultiVersionUtil.INSTANCE.popPose(graphics);
            }
        });
    }

    private void renderPins(GuiGraphicsExtractor graphics) {
        hoveredPin = null;
        if (!mapSourcesPresent) return;
        List<Pin> visible = new ArrayList<>();
        for (Pin pin : ClientPins.get()) {
            if (!pin.dimension().equals(dimensionId)) continue;
            int sx = pinScreenX(pin), sy = pinScreenY(pin);
            if (sx < -4 || sx > screenW + 4 || sy < 0 || sy > screenH + 11) continue;
            visible.add(pin);
            if (mouseX >= sx - 3 && mouseX < sx + 4 && mouseY >= sy - 10 && mouseY <= sy) hoveredPin = pin;
        }
        for (Pin pin : visible) {
            if (pin != hoveredPin) PinMarkers.draw(graphics, pinScreenX(pin), pinScreenY(pin), pin.color(), false);
        }
        if (!renderingAsBackground && hoveredPin != null) {
            int sx = pinScreenX(hoveredPin), sy = pinScreenY(hoveredPin);
            PinMarkers.draw(graphics, sx, sy, hoveredPin.color(), true);
            renderPinLabel(graphics, hoveredPin, sx, sy);
        }
        if (previewColor != null && renderingAsBackground) {
            int sx = (int) Math.round(worldToScreenX(previewX + 0.5));
            int sy = (int) Math.round(worldToScreenZ(previewZ + 0.5));
            PinMarkers.draw(graphics, sx, sy, previewColor, true);
        }
    }

    private void renderPinLabel(GuiGraphicsExtractor graphics, Pin pin, int sx, int sy) {
        List<Component> lines = new ArrayList<>();
        lines.add(pin.name().isEmpty() ?
                Component.translatable("mapstitch.gui.worldmap.pin.unnamed") :
                Component.literal(pin.name())
        );
        if (pinMode) {
            lines.add(Component.translatable("mapstitch.gui.worldmap.pin.remove_hint").withColor(0xFFAAAAAA));
            if (pin.isPaid()) lines.add(Component.translatable("mapstitch.gui.worldmap.pin.remove_hint_paid").withColor(0xFFAAAAAA));
        } else if (canSharePins()) {
            lines.add(Component.translatable("mapstitch.gui.worldmap.pin.share_hint").withColor(0xFFAAAAAA));
        }
        int w = lines.stream().mapToInt(font::width).max().orElse(0);
        int h = lines.size() * 10;
        int x = Mth.clamp(sx - w / 2, 2, screenW - w - 2);
        int y = Math.max(2, sy - 13 - h);
        graphics.fill(x - 2, y - 2, x + w + 2, y + h, MultiVersionUtil.INSTANCE.color(
                MultiVersionUtil.INSTANCE.as8BitChannel(MapStitchClient.CONFIG.worldMap.textBackgroundOpacity.get() / 100F),
                0, 0, 0
        ));
        for (int i = 0; i < lines.size(); i++) graphics.text(MC.font, lines.get(i), x, y + i * 10, -1);
    }

    @SuppressWarnings("DataFlowIssue")
    private void removeHoveredPin() {
        if (hoveredPin == null) return;
        MultiLoaderUtil.INSTANCE.c2s(new C2SRemovePin(hoveredPin.id()));
        MC.player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
        hoveredPin = null;
    }

    private void openPinPlacement(double sx, double sy) {
        if (!mapSourcesPresent) return;
        if (gridAllowed && grid && (sy < 12 || sx < getVerticalGridBarWidth(MC.font))) return;
        int wx = Mth.floor(screenToWorldX(sx));
        int wz = Mth.floor(screenToWorldZ(sy));
        //~ if <26.1 'setScreenAndShow' -> 'setScreen'
        MC.setScreenAndShow(new PinPlacementScreen(this, dimensionId, wx, wz, coordinatesAllowed));
    }

    private void openPinShare(Pin pin) {
        //~ if <26.1 'setScreenAndShow' -> 'setScreen'
        MC.setScreenAndShow(new PinShareScreen(this, pin));
    }

    @SuppressWarnings("DataFlowIssue")
	private void renderPlayerMarkers(GuiGraphicsExtractor graphics) {
        float pt = PlayerMarkers.partialTick(MC);
        int topInset  = grid ? 12 : 0;
        int leftInset = grid ? getVerticalGridBarWidth(MC.font) : 0;
        hoveredPlayer = null;
        double bestDistSq = 5 * 5;
        //? <26.1
        //graphics.flush();

        for (AbstractClientPlayer other : PlayerMarkers.otherPlayers(MC)) {
            float px = (float) Mth.clamp(worldToScreenX(PlayerMarkers.lerpX(other, pt)), leftInset + 4, screenW - 4);
            float py = (float) Mth.clamp(worldToScreenZ(PlayerMarkers.lerpZ(other, pt)), topInset + 4, screenH - 4);
            PlayerMarkers.draw(graphics, px, py, PlayerMarkers.lerpYaw(other, pt), PlayerMarkers.color(other));
            if (!renderingAsBackground) {
                double dx = mouseX - px, dy = mouseY - py, distSq = dx * dx + dy * dy;
                if (distSq <= bestDistSq) {
                    bestDistSq = distSq;
                    hoveredPlayer = other;
                    hoveredPlayerX = px;
                    hoveredPlayerY = py;
                }
            }
        }

        float px = (float) Mth.clamp(worldToScreenX(posX), leftInset + 4, screenW - 4);
        float py = (float) Mth.clamp(worldToScreenZ(posZ), topInset + 4, screenH - 4);
        PlayerMarkers.draw(graphics, px, py, PlayerMarkers.lerpYaw(MC.player, pt), PlayerMarkers.color(MC.player));
        //? <26.1
        //graphics.flush();
	}

    @SuppressWarnings("DataFlowIssue")
    private void renderPlayerLabel(GuiGraphicsExtractor graphics, int c) {
        if (hoveredPlayer == null) return;
        String name = hoveredPlayer.getDisplayName().getString();
        Component text = coordinatesAllowed ?
                Component.translatable("mapstitch.gui.worldmap.player_label",
                        white(name), white(String.valueOf(Mth.floor(MC.player.distanceTo(hoveredPlayer))))
                ).withColor(c) : white(name);
        int w = font.width(text);
        int x = Mth.clamp((int) hoveredPlayerX - w / 2, 2, screenW - w - 2);
        int y = Math.max(2, (int) hoveredPlayerY - 16);
        graphics.fill(x - 2, y - 2, x + w + 1, y + 9, MultiVersionUtil.INSTANCE.color(
                MultiVersionUtil.INSTANCE.as8BitChannel(MapStitchClient.CONFIG.worldMap.textBackgroundOpacity.get() / 100F),
                0, 0, 0
        ));
        graphics.text(font, text, x, y, -1);
    }

	private void renderText(GuiGraphicsExtractor graphics, int c) {
		textStack(4 + (gridAllowed && grid ? 12 : 0), gridAllowed && grid ? getVerticalGridBarWidth(MC.font) : 0, false, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.position",
						white(String.valueOf(posX)), white(String.valueOf(posY)), white(String.valueOf(posZ))
				).withColor(c), coordinatesAllowed),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.dimension",
						getDimensionDisplayName()
				), true),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.scale",
						white("1:" + Math.round(Math.pow(2, scale)))
				).withColor(c), true),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.zoom",
						white(zoom * Math.round(Math.pow(2, scale)) + "x")
				).withColor(c), true),
                new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.pin_mode").withColor(c), pinMode),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.following"
				), follow),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.debug.rendered_maps",
						mapsRendered
				), MultiLoaderUtil.INSTANCE.isDevEnv())
		));
		textStack(screenH - 12, gridAllowed && grid ? getVerticalGridBarWidth(MC.font) : 0, true, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.help",
						Component.keybind(ModKeybinds.TOGGLE_HELP.getName()).withColor(c)
				), MapStitchClient.CONFIG.worldMap.help.get() && !help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.help_control",
						Component.keybind(ModKeybinds.TOGGLE_HELP.getName()).withColor(c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.exit_control",
						Component.translatable("mapstitch.gui.worldmap.exit_key").withColor(c),
						Component.keybind(ModKeybinds.OPEN_WORLD_MAP.getName()).withColor(c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.eject_control",
						Component.translatable(
								"mapstitch.gui.worldmap.eject_key",
								Component.keybind(ModKeybinds.EJECT_MAP.getName())
						).withColor(c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.grid_control",
						Component.keybind(ModKeybinds.TOGGLE_GRID.getName()).withColor(c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.follow_control",
						Component.keybind(ModKeybinds.FOLLOW_PLAYER.getName()).withColor(c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.scale_control",
						getMultiKeyTranslation(ModKeybinds.SCALE_UP, ModKeybinds.SCALE_DOWN, c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.dimension_control",
						getMultiKeyTranslation(ModKeybinds.DIMENSION_UP, ModKeybinds.DIMENSION_DOWN, c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.center_control",
						Component.translatable("mapstitch.gui.worldmap.center_key").withColor(c)
				), help),
                new ObjectBooleanImmutablePair<>(Component.translatable(
                        "mapstitch.gui.worldmap.pin_control",
                        Component.keybind(ModKeybinds.TOGGLE_PIN_MODE.getName()).withColor(c)
                ), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.zoom_control",
						Component.translatable("mapstitch.gui.worldmap.zoom_key").withColor(c)
				), help),
				new ObjectBooleanImmutablePair<>(Component.translatable(
						"mapstitch.gui.worldmap.move_control",
						Component.translatable("mapstitch.gui.worldmap.move_key").withColor(c)
				), help)
		));
	}

	private MutableComponent getMultiKeyTranslation(KeyMapping k1, KeyMapping k2, int c) {
		boolean bl1 = !k1.isUnbound();
		boolean bl2 = !k2.isUnbound();
		MutableComponent key;
		if (bl1 && bl2) {
			key = Component.translatable("mapstitch.gui.worldmap.key_multi", Component.keybind(k1.getName()), Component.keybind(k2.getName()));
		} else if (bl1) {
			key = Component.translatable("mapstitch.gui.worldmap.key_single", Component.keybind(k1.getName()));
		} else {
			key = Component.translatable("mapstitch.gui.worldmap.key_single", Component.keybind(k2.getName()));
		}
		return key.withColor(c);
	}

	private void textStack(int startY, int x, boolean flipped, GuiGraphicsExtractor graphics, List<Pair<Component, Boolean>> lines) {
		int y = startY;
		for (Pair<Component, Boolean> line : lines) {
			if (line.right()) {
				graphics.fill(2 + x, y - 2, font.width(line.left()) + 5 + x, y + 9, MultiVersionUtil.INSTANCE.color(
                        MultiVersionUtil.INSTANCE.as8BitChannel(MapStitchClient.CONFIG.worldMap.textBackgroundOpacity.get() / 100F),
						0, 0, 0
				));
				graphics.text(MC.font, line.left(), 4 + x, y, -1);
				y += flipped ? -12 : 12;
			}
		}
	}

	private void renderGrid(GuiGraphicsExtractor graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
		Font font = MC.font;
		int verticalBarWidth = getVerticalGridBarWidth(font);
		int step = Math.max(1, (int) Math.ceil(48 / mapPixels));
		List<Supplier<TextRenderData>> textRenderCalls = new ArrayList<>();
		for (int gx = xBoundMin; gx <= xBoundMax; gx++) {
			int worldX = gridOriginX(gx);
			int px = (int) Math.round(worldToScreenX(worldX));
			if (px < 0 || px > screenW) continue;
			boolean labelled = Math.floorMod(gx, step) == 0;
			graphics.fill(px, 0, px + 1, screenH, labelled ? 0x80ffffff : 0x40ffffff);
			if (labelled) {
				String s = Integer.toString(worldX);
				int tw = font.width(s);
				int tx = Mth.clamp(px - tw / 2, 1, screenW - tw - 1);
				textRenderCalls.add(() -> new TextRenderData(s, tx, 2));
			}
		}
		for (int gz = zBoundMin; gz <= zBoundMax; gz++) {
			int worldZ = gridOriginZ(gz);
			int pz = (int) Math.round(worldToScreenZ(worldZ));
			if (pz < 0 || pz > screenH) continue;
			boolean labelled = Math.floorMod(gz, step) == 0;
			graphics.fill(0, pz, screenW, pz + 1, labelled ? 0x80ffffff : 0x40ffffff);
			if (labelled) {
				String s = Integer.toString(worldZ);
				int ty = Mth.clamp(pz - font.lineHeight / 2, 1, screenH - font.lineHeight - 1);
				textRenderCalls.add(() -> new TextRenderData(s, 2, ty));
			}
		}
        graphics.fill(0, 0, screenW, 12, 0xc0000000);
        graphics.fill(0, 0, verticalBarWidth, screenH, 0xc0000000);
		textRenderCalls.forEach(d -> graphics.text(
				font, d.get().s, d.get().i1, d.get().i2, -1, false
		));
	}

	private record TextRenderData(String s, int i1, int i2) {}

	private int getVerticalGridBarWidth(Font font) {
		int wMax = font.width(Integer.toString((int) screenToWorldZ(0)));
		int wMin = font.width(Integer.toString((int) screenToWorldZ(screenH)));
		return Math.max(wMax, wMin) + 4;
	}

	private void renderPosAtCursor(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int c) {
		int wx = (int) Math.floor(screenToWorldX(mouseX));
		int wz = (int) Math.floor(screenToWorldZ(mouseY));
		Component text = Component.translatable("mapstitch.gui.worldmap.cursor_position", white(String.valueOf(wx)), white(String.valueOf(wz))).withColor(c);
		Font font = MC.font;
		int posX = mouseX + 8;
		int posY = mouseY + 8;
		graphics.fill(posX - 2, posY - 2, posX + font.width(text) + 1, posY + 9, MultiVersionUtil.INSTANCE.color(
                MultiVersionUtil.INSTANCE.as8BitChannel(MapStitchClient.CONFIG.worldMap.textBackgroundOpacity.get() / 100F),
				0, 0, 0
		));
		graphics.text(font, text, posX, posY, -1);
	}

	private Component white(String s) {
		return Component.literal(s).withColor(-1);
	}

	@SuppressWarnings("deprecation")
	private String getDimensionDisplayName() {
		return WordUtils.capitalize(dimensionId.getPath().replace("_", " "));
	}

	private void centerMap() {
		if (hasCompass) {
			camX = posX;
			camZ = posZ;
		} else if (MC.level != null) {
            //~ if <26.1 'getRespawnData().pos()' -> 'getLevelData().getSpawnPos()'
			BlockPos spawn = MC.level.getRespawnData().pos();
			camX = spawn.getX();
			camZ = spawn.getZ();
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private void ejectMapAtCursor() {
        MapId mapToRemove = null;
        for (int i = 0; i < 5; i++) {
            MapDecorsWithId decors = EXPLORATION_MARKERS.remove(screenToGrid(mouseX, mouseY, i));
            if (decors != null) {
                mapToRemove = decors.id;
                break;
            }
        }
        if (mapToRemove == null) {
            MapDataWithId map = MAPS.remove(screenToGrid(mouseX, mouseY, scale));
            if (map != null) mapToRemove = map.id;
        }
        if (mapToRemove != null) {
            MultiLoaderUtil.INSTANCE.c2s(new C2SEjectMap(mapToRemove));
            MC.player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
            clearMaps();
        }
	}

    public static void clearMaps() {
        updateTimer = 1;
        MAPS.clear();
        DECORATIONS.clear();
        EXPLORATION_MARKERS.clear();
    }

    public static boolean canSharePins() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        return server == null || server.isPublished();
    }

	@Override
    //~ if <26.1 '@NotNull MouseButtonEvent event' -> 'double mouseX, double mouseY, int button'
	public boolean mouseDragged(@NotNull MouseButtonEvent event, double dx, double dy) {
        pinDragDistance += Math.abs(dx) + Math.abs(dy);
        if (pinDragDistance > 3) pinPressActive = false;
        //~ if <26.1 'event.button()' -> 'button'
        //~ if >26.2 '0' -> '1'
		if (event.button() == 1) {
			camX -= dx / zoom;
			camZ -= dy / zoom;
			return true;
		}
		return false;
	}

	@Override
    //~ if <26.1 'MouseButtonEvent event, boolean doubleClick' -> 'double mouseX, double mouseY, int button'
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        //~ if <26.1 'event.button()' -> 'button'
        if (event.button() == 2 && hasCompass) centerMap();
        //~ if <26.1 'event, doubleClick' -> 'mouseX, mouseY, button'
        boolean handled = super.mouseClicked(event, doubleClick);
        //~ if <26.1 'event.button()' -> 'button'
        //~ if >26.2 '0' -> '1'
        pinPressActive = !handled && event.button() == 1 && (pinMode || (hoveredPin != null && canSharePins()));
        if (pinPressActive) {
            pressedPin = hoveredPin;
            //~ if <26.1 'event.x()' -> 'mouseX'
            pinPressX = event.x();
            //~ if <26.1 'event.y()' -> 'mouseY'
            pinPressY = event.y();
            pinDragDistance = 0;
        }
        return handled || pinPressActive;
	}

    @Override
    //~ if <26.1 'MouseButtonEvent event' -> 'double mouseX, double mouseY, int button'
    public boolean mouseReleased(@NotNull MouseButtonEvent event) {
        //~ if <26.1 'event.button()' -> 'button'
        //~ if >26.2 '0' -> '1'
        if (pinPressActive && event.button() == 1) {
            pinPressActive = false;
            if (pinMode) {
                //~ if <26.1 'event.hasControlDown()' -> 'hasControlDown()'
                if (event.hasControlDown()) removeHoveredPin();
                else openPinPlacement(pinPressX, pinPressY);
            } else if (pressedPin != null && canSharePins()) openPinShare(pressedPin);
            pressedPin = null;
            return true;
        }
        //~ if <26.1 'event' -> 'mouseX, mouseY, button'
        return super.mouseReleased(event);
    }

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		zoomLevel = Mth.clamp(zoomLevel + (int) Math.signum(scrollY), -2, 1);
		return true;
	}

	@Override
    //~ if <26.1 '@NotNull KeyEvent event' -> 'int keyCode, int scanCode, int modifiers'
	public boolean keyPressed(@NotNull KeyEvent event) {
        //~ if <26.1 'event' -> 'keyCode, scanCode' {
		if (ModKeybinds.SCALE_UP.matches(event)) {
			if (scale < 4) scale++;
			else scale = 0;
			return true;
		}
		if (ModKeybinds.SCALE_DOWN.matches(event)) {
			if (scale > 0) scale--;
			else scale = 4;
			return true;
		}
		if (ModKeybinds.DIMENSION_UP.matches(event)) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i + 1 > dimensionIds.size() - 1 ? dimensionIds.getFirst() : dimensionIds.get(i + 1);
            clearMaps();
			return true;
		}
		if (ModKeybinds.DIMENSION_DOWN.matches(event)) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i - 1 < 0 ? dimensionIds.getLast() : dimensionIds.get(i - 1);
            clearMaps();
			return true;
		}
		if (MapStitchClient.CONFIG.worldMap.help.get() && ModKeybinds.TOGGLE_HELP.matches(event)) {
			help = !help;
			return true;
		}
		if (ModKeybinds.FOLLOW_PLAYER.matches(event) && hasCompass) {
			follow = !follow;
			return true;
		}
		if (ModKeybinds.TOGGLE_GRID.matches(event) && gridAllowed) {
			grid = !grid;
			return true;
		}
		if (ModKeybinds.OPEN_WORLD_MAP.matches(event)) {
			onClose();
			return true;
		}
        if (ModKeybinds.TOGGLE_PIN_MODE.matches(event)) {
            pinMode = !pinMode;
            return true;
        }
        //~ if <26.1 'event.hasControlDown()' -> 'modifiers == 2'
		if (event.hasControlDown() && ModKeybinds.EJECT_MAP.matches(event)) {
			ejectMapAtCursor();
			return true;
		}
        //~}
        //~ if <26.1 'event' -> 'keyCode, scanCode, modifiers'
		return super.keyPressed(event);
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

    private void cleanup() {
        WorldMapState state = WorldMapStateHolder.state();
        state.scale = scale;
        state.zoom = zoomLevel;
        state.x = camX;
        state.z = camZ;
        state.help = help;
        state.grid = grid;
        state.follow = follow;
        state.writeChanges();
        ModClientUtil.worldMapOpen = false;
        clearMaps();
    }

	@Override
	public void onClose() {
		cleanup();
		super.onClose();
	}

    @Override
    public void removed() {
        cleanup();
        super.removed();
    }
}
