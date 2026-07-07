package me.pajic.mapstitch.worldmap;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.OhmegaCompat;
import me.pajic.mapstitch.compat.TrinketsCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.config.ModConfigHolder;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WorldMapScreen extends Screen {
	private final Minecraft MC = Minecraft.getInstance();
	private static final Map<GridPos, MapDataWithId> MAPS = new HashMap<>();
	private static final Identifier PLAYER_MARKER = Identifier.withDefaultNamespace("textures/map/decorations/player.png");

	public static List<Identifier> dimensionIds = List.of();
	private static Identifier dimensionId = Identifier.withDefaultNamespace("overworld");

	private static int posX = 0;
	private static int posY = 0;
	private static int posZ = 0;

	private boolean compass;
	private int mapSize;
	private double mapPixels;
	private double camX, camZ;
	private int zoomLevel;
	private float zoom;
	private int screenW, screenH;
	private int scale;
	private boolean help;
	private boolean grid;
	private boolean follow;
	private int mapsRendered = 0;

	public WorldMapScreen() {
		super(Component.translatable("mapstitch.gui.worldmap.title"));
		WorldMapState state = WorldMapStateHolder.state();
		scale = state.scale;
		zoomLevel = state.zoom;
		camX = state.x;
		camZ = state.z;
		help = state.help;
		grid = state.grid;
		follow = state.follow;
	}

	private record MapDataWithId(MapId id, MapItemSavedData data) {}

	private record GridPos(int gx, int gy, int s) {}
	private GridPos worldToGrid(int worldX, int worldZ, int scale) {
		return new GridPos(
				Math.floorDiv(worldX, 128 << scale),
				Math.floorDiv(worldZ, 128 << scale),
				scale
		);
	}

	private int gridOriginX(int gx) { return gx * mapSize - 64; }
	private int gridOriginZ(int gy) { return gy * mapSize - 64; }

	private double worldToScreenX(double worldX) { return (worldX - camX) * zoom + screenW / 2.0; }
	private double worldToScreenZ(double worldZ) { return (worldZ - camZ) * zoom + screenH / 2.0; }
	private double screenToWorldX(double screenX) { return (screenX - screenW / 2.0) / zoom + camX; }
	private double screenToWorldZ(double screenZ) { return (screenZ - screenH / 2.0) / zoom + camZ; }

	@Override
	public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		mapSize = 128 << scale;
		mapPixels = mapSize * zoom;
		int highlightColor = ModConfigHolder.options().worldMapTextHighlightColor.color;
		int xBoundMin = Math.floorDiv((int)(camX - (screenW/2d)/zoom), mapSize);
		int xBoundMax = Math.floorDiv((int)(camX + (screenW/2d)/zoom), mapSize) + 1;
		int zBoundMin = Math.floorDiv((int)(camZ - (screenH/2d)/zoom), mapSize);
		int zBoundMax = Math.floorDiv((int)(camZ + (screenH/2d)/zoom), mapSize) + 1;
		renderMaps(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
		if (compass) {
			renderPlayerMarker(graphics);
			if (grid) {
				renderGrid(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
				renderPosAtCursor(graphics, mouseX, mouseY, highlightColor);
			}
		}
		renderText(graphics, highlightColor);
		mapsRendered = 0;
	}

	private void renderMaps(GuiGraphicsExtractor graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
		MAPS.clear();
		if (!ModConfigHolder.options().worldMapHelp) help = false;
		if (MC.level == null || MC.player == null) return;
		compass = ModUtil.hasCompass(MC);
		if (compass) {
			posX = MC.player.blockPosition().getX();
			posY = MC.player.blockPosition().getY();
			posZ = MC.player.blockPosition().getZ();
			if (follow) centerMap();
		}
		zoom = (float) Math.pow(2, zoomLevel) / Math.powExact(2, scale);
		List<ItemStack> items = new ArrayList<>(MC.player.getInventory().getNonEquipmentItems());
		if (CompatFlags.TRINKETS_LOADED) items.addAll(TrinketsCompat.getTrinketAtlases(MC.player));
		if (CompatFlags.OHMEGA_LOADED) items.addAll(OhmegaCompat.getOhmegaAtlases(MC.player));
		items.forEach(stack -> {
			if (stack.is(ModItems.ATLAS)) {
				BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				for (ItemStackTemplate map : contents.items()) {
					if (map.is(Items.FILLED_MAP)) prepareMap(map);
				}
			} else if (stack.is(Items.FILLED_MAP)) {
				prepareMap(ItemStackTemplate.fromNonEmptyStack(stack));
			}
		});
		screenW = MC.getWindow().getGuiScaledWidth();
		screenH = MC.getWindow().getGuiScaledHeight();
		for (int gy = zBoundMin; gy <= zBoundMax; gy++) {
			for (int gx = xBoundMin; gx <= xBoundMax; gx++) {
				MapDataWithId map = MAPS.get(new GridPos(gx, gy, scale));
				if (map != null && map.data.scale == scale) {
					double px = worldToScreenX(gridOriginX(gx));
					double py = worldToScreenZ(gridOriginZ(gy));
					Matrix3x2fStack pose = graphics.pose();
					pose.pushMatrix();
					pose.translate((float) px, (float) py);
					pose.scale((float) (mapPixels / 128.0), (float) (mapPixels / 128.0));
					MapRenderState state = new MapRenderState();
					MC.getMapRenderer().extractRenderState(map.id, map.data, state);
					state.decorations.forEach(decor -> {
						Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType();
						if (!ModUtil.DECORS_REQUIRING_COMPASS.contains(type)) decor.renderOnFrame = true;
					});
					graphics.map(state);
					mapsRendered++;
					pose.popMatrix();
				}
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private void prepareMap(ItemStackTemplate map) {
		MapId id = map.get(DataComponents.MAP_ID);
		Vector2i mapCenter = map.get(ModDataComponents.MAP_CENTER);
		if (id != null && mapCenter != null ) {
			MapItemSavedData data = MC.level.getMapData(id);
			if (data != null && !data.isExplorationMap() && !data.locked && dimensionId.equals(data.dimension.identifier())){
				GridPos gridPos = worldToGrid(mapCenter.x, mapCenter.y, data.scale);
				MAPS.put(gridPos, new MapDataWithId(id, data));
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private void renderPlayerMarker(GuiGraphicsExtractor graphics) {
		double sx = worldToScreenX(posX);
		double sz = worldToScreenZ(posZ);
		int topInset  = (grid && compass) ? 12 : 0;
		int leftInset = (grid && compass) ? getVerticalGridBarWidth(MC.font) : 0;
		float px = (float) Mth.clamp(sx, leftInset + 4, screenW - 4);
		float py = (float) Mth.clamp(sz, topInset  + 4, screenH - 4);
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(px, py);
		pose.rotate(Mth.DEG_TO_RAD * (MC.player.getYRot() + 180.0F));
		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				PLAYER_MARKER,
				-4, -4,
				0, 0,
				8, 8,
				8, 8
		);
		pose.popMatrix();
	}

	private void renderText(GuiGraphicsExtractor graphics, int c) {
		textStack(4 + (grid & compass ? 12 : 0), grid & compass ? getVerticalGridBarWidth(MC.font) : 0, false, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.position", white(String.valueOf(posX)), white(String.valueOf(posY)), white(String.valueOf(posZ))).withColor(c), compass),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.dimension", getDimensionDisplayName()), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.scale", white("1:" + Math.powExact(2, scale))).withColor(c), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.zoom", white(zoom * Math.powExact(2, scale) + "x")).withColor(c), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.following"), follow),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.debug.rendered_maps", mapsRendered), MapStitch.xplat().isDebug())
		));
		textStack(screenH - 12, grid & compass ? getVerticalGridBarWidth(MC.font) : 0, true, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.help", Component.translatable("mapstitch.gui.worldmap.help_key").withColor(c)), ModConfigHolder.options().worldMapHelp && !help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.help_control", Component.translatable("mapstitch.gui.worldmap.help_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.exit_control", Component.translatable("mapstitch.gui.worldmap.exit_key").withColor(c), Component.keybind(ModKeybinds.OPEN_WORLD_MAP.getName()).withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.grid_control", Component.translatable("mapstitch.gui.worldmap.grid_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.follow_control", Component.translatable("mapstitch.gui.worldmap.follow_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.scale_control", Component.translatable("mapstitch.gui.worldmap.scale_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.dimension_control", Component.translatable("mapstitch.gui.worldmap.dimension_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.center_control", Component.translatable("mapstitch.gui.worldmap.center_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.zoom_control", Component.translatable("mapstitch.gui.worldmap.zoom_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.move_control", Component.translatable("mapstitch.gui.worldmap.move_key").withColor(c)), help)
		));
	}

	private void textStack(int startY, int x, boolean flipped, GuiGraphicsExtractor graphics, List<Pair<Component, Boolean>> lines) {
		int y = startY;
		for (Pair<Component, Boolean> line : lines) {
			if (line.right()) {
				graphics.fill(2 + x, y - 2, font.width(line.left()) + 5 + x, y + 9, ARGB.color(
						ARGB.as8BitChannel(ModConfigHolder.options().worldMapTextBackgroundOpacity / 100F),
						0, 0, 0
				));
				graphics.text(MC.font, line.left(), 4 + x, y, 0xffffffff);
				y += flipped ? -12 : 12;
			}
		}
	}

	private void renderGrid(GuiGraphicsExtractor graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
		Font font = minecraft.font;
		int verticalBarWidth = getVerticalGridBarWidth(font);
		int step = Math.max(1, (int) Math.ceil(48 / mapPixels));
		List<Supplier<TextRenderData>> textRenderCalls = new ArrayList<>();
		for (int gx = xBoundMin; gx <= xBoundMax; gx++) {
			int worldX = gridOriginX(gx);
			int px = (int) Math.round(worldToScreenX(worldX));
			if (px < 0 || px > screenW) continue;
			boolean labelled = compass && Math.floorMod(gx, step) == 0;
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
			boolean labelled = compass && Math.floorMod(gz, step) == 0;
			graphics.fill(0, pz, screenW, pz + 1, labelled ? 0x80ffffff : 0x40ffffff);
			if (labelled) {
				String s = Integer.toString(worldZ);
				int ty = Mth.clamp(pz - font.lineHeight / 2, 1, screenH - font.lineHeight - 1);
				textRenderCalls.add(() -> new TextRenderData(s, 2, ty));
			}
		}
		if (compass) {
			graphics.fill(0, 0, screenW, 12, 0xc0000000);
			graphics.fill(0, 0, verticalBarWidth, screenH, 0xc0000000);
		}
		textRenderCalls.forEach(d -> graphics.text(font, d.get().s, d.get().i1, d.get().i2, 0xffffffff, false));
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
		Font font = minecraft.font;
		int posX = mouseX + 8;
		int posY = mouseY + 8;
		graphics.fill(posX - 2, posY - 2, posX + font.width(text) + 1, posY + 9, ARGB.color(
				ARGB.as8BitChannel(ModConfigHolder.options().worldMapTextBackgroundOpacity / 100F),
				0, 0, 0
		));
		graphics.text(font, text, posX, posY, 0xffffffff);
	}

	private Component white(String s) {
		return Component.literal(s).withColor(0xffffffff);
	}

	@SuppressWarnings("deprecation")
	private String getDimensionDisplayName() {
		return WordUtils.capitalize(dimensionId.getPath().replace("_", " "));
	}

	private void centerMap() {
		if (compass) {
			camX = posX;
			camZ = posZ;
		} else if (MC.level != null) {
			BlockPos spawn = MC.level.getRespawnData().pos();
			camX = spawn.getX();
			camZ = spawn.getZ();
		}
	}

	@Override
	public boolean mouseDragged(@NotNull MouseButtonEvent event, double dx, double dy) {
		if (event.button() == 0) {
			camX -= dx / zoom;
			camZ -= dy / zoom;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 2) centerMap();
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		zoomLevel = Mth.clamp(zoomLevel + (int) Math.signum(scrollY), -2, 1);
		return true;
	}

	@Override
	public boolean keyPressed(@NotNull KeyEvent event) {
		if (event.isUp() && scale < 4) {
			scale++;
			return true;
		}
		if (event.isDown() && scale > 0) {
			scale--;
			return true;
		}
		if (event.isRight()) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i + 1 > dimensionIds.size() - 1 ? dimensionIds.getFirst() : dimensionIds.get(i + 1);
			return true;
		}
		if (event.isLeft()) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i - 1 < 0 ? dimensionIds.getLast() : dimensionIds.get(i - 1);
			return true;
		}
		if (ModConfigHolder.options().worldMapHelp && event.key() == InputConstants.KEY_H) {
			help = !help;
			return true;
		}
		if (event.key() == InputConstants.KEY_F) {
			follow = !follow;
			return true;
		}
		if (event.key() == InputConstants.KEY_G) {
			grid = !grid;
			return true;
		}
		if (ModKeybinds.OPEN_WORLD_MAP.matches(event)) {
			onClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	@Override
	public void onClose() {
		WorldMapState state = WorldMapStateHolder.state();
		state.scale = scale;
		state.zoom = zoomLevel;
		state.x = camX;
		state.z = camZ;
		state.help = help;
		state.grid = grid;
		state.follow = follow;
		state.writeChanges();
		super.onClose();
	}
}
