package me.pajic.mapstitch.worldmap;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.CuriosCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.config.ModConfigHolder;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WorldMapScreen extends Screen {
	private final Minecraft MC = Minecraft.getInstance();
	private static final Map<GridPos, MapDataWithId> MAPS = new HashMap<>();
	private static final ResourceLocation PLAYER_MARKER = ResourceLocation.withDefaultNamespace("textures/map/decorations/player.png");

	public static List<ResourceLocation> dimensionIds = List.of();
	private static ResourceLocation dimensionId = ResourceLocation.withDefaultNamespace("overworld");

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
		ModUtil.worldMapOpen = true;
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
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
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

	private void renderMaps(GuiGraphics graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
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
		zoom = (float) Math.pow(2, zoomLevel) / (float) Math.pow(2, scale);
		List<ItemStack> items = new ArrayList<>(MC.player.getInventory().items);
		if (CompatFlags.CURIOS_LOADED) items.addAll(CuriosCompat.getCurioAtlases(MC.player));
		items.forEach(stack -> {
			if (stack.is(ModItems.ATLAS)) {
				BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				for (ItemStack map : contents.items()) {
					if (map.is(Items.FILLED_MAP)) prepareMap(map);
				}
			} else if (stack.is(Items.FILLED_MAP)) {
				prepareMap(stack);
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
					PoseStack pose = graphics.pose();
					pose.pushPose();
					pose.translate((float) px, (float) py, 0);
					pose.scale((float) (mapPixels / 128.0), (float) (mapPixels / 128.0), 0);
					MC.gameRenderer.getMapRenderer().render(graphics.pose(), graphics.bufferSource(), map.id, map.data, true, 15728880);
					mapsRendered++;
					pose.popPose();
				}
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private void prepareMap(ItemStack map) {
		MapId id = map.get(DataComponents.MAP_ID);
		Vector2i mapCenter = map.get(ModDataComponents.MAP_CENTER);
		if (id != null && mapCenter != null ) {
			MapItemSavedData data = MC.level.getMapData(id);
			if (data != null && !data.isExplorationMap() && !data.locked && dimensionId.equals(data.dimension.location())){
				GridPos gridPos = worldToGrid(mapCenter.x, mapCenter.y, data.scale);
				MAPS.put(gridPos, new MapDataWithId(id, data));
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private void renderPlayerMarker(GuiGraphics graphics) {
		double sx = worldToScreenX(posX);
		double sz = worldToScreenZ(posZ);
		int topInset  = (grid && compass) ? 12 : 0;
		int leftInset = (grid && compass) ? getVerticalGridBarWidth(MC.font) : 0;
		float px = (float) Mth.clamp(sx, leftInset + 4, screenW - 4);
		float py = (float) Mth.clamp(sz, topInset  + 4, screenH - 4);
		graphics.flush();
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(px, py, 0);
		pose.mulPose(Axis.ZP.rotationDegrees(MC.player.getYRot() + 180.0F));
		graphics.blit(
				PLAYER_MARKER,
				-4, -4,
				0, 0,
				8, 8,
				8, 8
		);
		pose.popPose();
		graphics.flush();
	}

	private void renderText(GuiGraphics graphics, int c) {
		textStack(4 + (grid & compass ? 12 : 0), grid & compass ? getVerticalGridBarWidth(MC.font) : 0, false, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.position", white(String.valueOf(posX)), white(String.valueOf(posY)), white(String.valueOf(posZ))).withColor(c), compass),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.dimension", getDimensionDisplayName()), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.scale", white("1:" + Math.round(Math.pow(2, scale)))).withColor(c), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.zoom", white(zoom * Math.round(Math.pow(2, scale)) + "x")).withColor(c), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.following"), follow),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.debug.rendered_maps", mapsRendered), MapStitch.isDebug())
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

	private void textStack(int startY, int x, boolean flipped, GuiGraphics graphics, List<Pair<Component, Boolean>> lines) {
		int y = startY;
		for (Pair<Component, Boolean> line : lines) {
			if (line.right()) {
				graphics.fill(2 + x, y - 2, font.width(line.left()) + 5 + x, y + 9, FastColor.ARGB32.color(
						FastColor.as8BitChannel(ModConfigHolder.options().worldMapTextBackgroundOpacity / 100F),
						0, 0, 0
				));
				graphics.drawString(MC.font, line.left(), 4 + x, y, 0xffffffff);
				y += flipped ? -12 : 12;
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
    private void renderGrid(GuiGraphics graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
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
		textRenderCalls.forEach(d -> graphics.drawString(font, d.get().s, d.get().i1, d.get().i2, 0xffffffff, false));
	}

	private record TextRenderData(String s, int i1, int i2) {}

	private int getVerticalGridBarWidth(Font font) {
		int wMax = font.width(Integer.toString((int) screenToWorldZ(0)));
		int wMin = font.width(Integer.toString((int) screenToWorldZ(screenH)));
		return Math.max(wMax, wMin) + 4;
	}

	@SuppressWarnings("DataFlowIssue")
    private void renderPosAtCursor(GuiGraphics graphics, int mouseX, int mouseY, int c) {
		int wx = (int) Math.floor(screenToWorldX(mouseX));
		int wz = (int) Math.floor(screenToWorldZ(mouseY));
		Component text = Component.translatable("mapstitch.gui.worldmap.cursor_position", white(String.valueOf(wx)), white(String.valueOf(wz))).withColor(c);
		Font font = minecraft.font;
		int posX = mouseX + 8;
		int posY = mouseY + 8;
		graphics.fill(posX - 2, posY - 2, posX + font.width(text) + 1, posY + 9, FastColor.ARGB32.color(
				FastColor.as8BitChannel(ModConfigHolder.options().worldMapTextBackgroundOpacity / 100F),
				0, 0, 0
		));
		graphics.drawString(font, text, posX, posY, 0xffffffff);
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
			BlockPos spawn = MC.level.getLevelData().getSpawnPos();
			camX = spawn.getX();
			camZ = spawn.getZ();
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0) {
			camX -= dragX / zoom;
			camZ -= dragY / zoom;
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 2) centerMap();
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		zoomLevel = Mth.clamp(zoomLevel + (int) Math.signum(scrollY), -2, 1);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_UP && scale < 4) {
			scale++;
			return true;
		}
		if (keyCode == InputConstants.KEY_DOWN && scale > 0) {
			scale--;
			return true;
		}
		if (keyCode == InputConstants.KEY_RIGHT) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i + 1 > dimensionIds.size() - 1 ? dimensionIds.getFirst() : dimensionIds.get(i + 1);
			return true;
		}
		if (keyCode == InputConstants.KEY_LEFT) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i - 1 < 0 ? dimensionIds.getLast() : dimensionIds.get(i - 1);
			return true;
		}
		if (ModConfigHolder.options().worldMapHelp && keyCode == InputConstants.KEY_H) {
			help = !help;
			return true;
		}
		if (keyCode == InputConstants.KEY_F) {
			follow = !follow;
			return true;
		}
		if (keyCode == InputConstants.KEY_G) {
			grid = !grid;
			return true;
		}
		if (ModKeybinds.OPEN_WORLD_MAP.matches(keyCode, scanCode)) {
			onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
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
		ModUtil.worldMapOpen = false;
		super.onClose();
	}
}
