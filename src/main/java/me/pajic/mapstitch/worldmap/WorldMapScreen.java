package me.pajic.mapstitch.worldmap;

import com.mojang.blaze3d.platform.InputConstants;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.config.ModConfigHolder;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMapScreen extends Screen {
	public static List<Identifier> dimensionIds = List.of();
	public static final Map<Integer, MapRenderState> RENDER_STATES = new HashMap<>();
	private static final Map<Vec2, MapRenderState> RENDER_LIST = new HashMap<>();
	private static final int WHITE = 0xffffffff;

	private static Identifier dimensionId = Identifier.withDefaultNamespace("overworld");
	private static int scale = 0;
	private static int zoom = 0;
	private static int posX = 0;
	private static int posY = 0;
	private static int posZ = 0;
	private static boolean help = false;
	private static boolean grid = false;

	private final Minecraft MC = Minecraft.getInstance();
	private double mouseDragX;
	private double mouseDragY;

	public WorldMapScreen() {
		super(Component.translatable("mapstitch.gui.worldmap.title"));
		centerMap();
	}

	@Override
	public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		RENDER_LIST.clear();
		if (!ModConfigHolder.options().worldMapHelp) help = false;
		if (MC.level == null || MC.player == null) return;
		int screenX = MC.getWindow().getGuiScaledWidth();
		int screenY = MC.getWindow().getGuiScaledHeight();
		float z = (float) Math.pow(2, zoom);
		int s = Math.powExact(2, scale);
		boolean compass = ModUtil.hasCompass(MC);
		if (compass) {
			posX = MC.player.blockPosition().getX();
			posY = MC.player.blockPosition().getY();
			posZ = MC.player.blockPosition().getZ();
		}
		// prepare maps
		MC.player.getInventory().forEach(stack -> {
			if (stack.is(ModItems.ATLAS)) {
				BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				for (ItemStackTemplate map : contents.items()) {
					if (map.is(Items.FILLED_MAP)) prepareMap(map, z, screenX, screenY, compass);
				}
			} else if (stack.is(Items.FILLED_MAP)) {
				prepareMap(ItemStackTemplate.fromNonEmptyStack(stack), z, screenX, screenY, compass);
			}
		});
		float invertedZoom = 1 / z;

		int mapSize = Mth.floor(128 / invertedZoom);

		// render prepared maps
		Matrix3x2fStack pose = graphics.pose();
		RENDER_LIST.forEach((pos, state) -> {
			pose.pushMatrix();
			pose.translate(screenX / 2F, screenY / 2F);
			pose.scale(z);
			pose.translate(
					(pos.x - screenX / 2F + (float) mouseDragX) - 128,
					(pos.y - screenY / 2F + (float) mouseDragY) - 128
			);
			graphics.map(state);
			pose.popMatrix();
		});
		// render grid
		if (grid) {
			int mapOffset = Mth.floor(mapSize / 2F);
			int dragHorizontalOffset = Mth.floor(mouseDragY / invertedZoom);
			int dragVerticalOffset = Mth.floor(mouseDragX / invertedZoom);
			int horizontalLineOffset = Mth.floor((float) dragHorizontalOffset / mapSize);
			int verticalLineOffset = Mth.floor((float) dragVerticalOffset / mapSize);
			int requiredHorizontalLinesStart = -Mth.floor((float) screenY / mapSize) + horizontalLineOffset;
			int requiredHorizontalLinesEnd = Mth.floor((float) screenY / mapSize) + horizontalLineOffset;
			int requiredVerticalLinesStart = -Mth.floor((float) screenX / mapSize) + verticalLineOffset;
			int requiredVerticalLinesEnd = Mth.floor((float) screenX / mapSize) + verticalLineOffset;
			for (int i = requiredHorizontalLinesStart; i <= requiredHorizontalLinesEnd; i++) {
				float y = (screenY / 2F) - (mapSize) * i + dragHorizontalOffset - mapOffset;
				graphics.horizontalLine(0, screenX, Mth.floor(y), WHITE);
				if (compass) graphics.text(
						MC.font, String.valueOf(-64 - i * (128 * (Math.pow(2, scale)))), 2, Mth.floor(y) + 5, WHITE
				);
			}
			for (int i = requiredVerticalLinesStart; i <= requiredVerticalLinesEnd; i++) {
				float x = (screenX / 2F) - (mapSize) * i + dragVerticalOffset - mapOffset;
				graphics.verticalLine(Mth.floor(x), 0, screenY, WHITE);
				if (compass) graphics.text(
						MC.font, String.valueOf(-64 - i * (128 * (Math.pow(2, scale)))), Mth.floor(x) + 5, 2, WHITE
				);
			}
		}
		// render text lines
		textStack(4, false, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.position", posX, posY, posZ), compass),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.dimension", getDimensionDisplayName()), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.scale", s), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.zoom", z), true),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.debug.rendered_maps", RENDER_LIST.size()), MapStitch.xplat().isDebug())
		));
		textStack(screenY - 12, true, graphics, List.of(
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.help"), ModConfigHolder.options().worldMapHelp && !help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.help_control"), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.exit_control", Component.keybind(ModKeybinds.OPEN_WORLD_MAP.getName()).withColor(0xffffff55)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.grid_control"), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.scale_control"), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.dimension_control"), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.center_control"), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.zoom_control"), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.move_control"), help)
		));
	}

	@SuppressWarnings("DataFlowIssue")
	private void prepareMap(ItemStackTemplate map, float zoomLevel, int screenX, int screenY, boolean compass) {
		MapId mapId = map.get(DataComponents.MAP_ID);
		MapItemSavedData data = MC.level.getMapData(mapId);
		int i = mapId.id();
		if (!data.isExplorationMap() && !data.locked && dimensionId.equals(data.dimension.identifier())) {
			Vector2d mapCenter = map.get(ModDataComponents.MAP_ORIGIN);
			int s = Math.powExact(2, scale);
			int mapCenterX = (int) mapCenter.x;
			int mapCenterY = (int) mapCenter.y;
			float distX = (float) ((Math.abs(mapCenterX + mouseDragX))) * scale;
			float distY = (float) ((Math.abs(mapCenterY + mouseDragY))) * scale;
			float mapSize = 128F / 2;

//			if (distX <= (float) screenX / 2 / zoomLevel + mapSize && distY <= (float) screenY / 2 / zoomLevel + mapSize) {

				int mapSizeScaled = 128 * s;

				float posScreenX = (screenX / 2F) + ((float) (mapCenterX + 64) / mapSizeScaled) * 128;
				float posScreenY = (screenY / 2F) + ((float) (mapCenterY + 64) / mapSizeScaled) * 128;
				MapRenderState state = RENDER_STATES.getOrDefault(i, new MapRenderState());
				MC.getMapRenderer().extractRenderState(new MapId(i), data, state);
				if (data.scale == scale) {
					state.decorations.forEach(decor -> {
						Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType();
						if (type != MapDecorationTypes.PLAYER_OFF_LIMITS && type != MapDecorationTypes.PLAYER_OFF_MAP)
							decor.renderOnFrame = true;
					});
					RENDER_LIST.put(new Vec2(posScreenX, posScreenY), state);
				}
				if (!compass) state.decorations.forEach(decor -> {
					Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType();
					if (type == MapDecorationTypes.PLAYER) decor.renderOnFrame = false;
				});
				if (!RENDER_STATES.containsKey(i)) RENDER_STATES.put(i, state);
//			}
		}
	}

	private void textStack(int startY, boolean flipped, GuiGraphicsExtractor graphics, List<Pair<Component, Boolean>> lines) {
		int y = startY;
		for (Pair<Component, Boolean> line : lines) {
			if (line.right()) {
				graphics.fill(2, y - 2, font.width(line.left()) + 5, y + 9, ARGB.color(
						ARGB.as8BitChannel(ModConfigHolder.options().worldMapTextBackgroundOpacity / 100F),
						0, 0, 0
				));
				graphics.text(MC.font, line.left(), 4, y, WHITE);
				y += flipped ? -12 : 12;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private String getDimensionDisplayName() {
		return WordUtils.capitalize(dimensionId.getPath().replace("_", " "));
	}

	private void centerMap() {
		int s = (int) Math.pow(2, (scale));
		int s2 = (int) (Math.pow(2, -(scale)) * 64);
		mouseDragX = (double) -posX / s + 64 - s2;
		mouseDragY = (double) -posZ / s + 64 - s2;
	}

	@Override
	public boolean mouseDragged(@NotNull MouseButtonEvent event, double dx, double dy) {
		if (event.button() == 0) {
			mouseDragX += dx / (Math.pow(2, zoom));
			mouseDragY += dy / (Math.pow(2, zoom));
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 2) {
			centerMap();
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		zoom = Mth.clamp(zoom + (int) Math.signum(scrollY), -2, 1);
		return true;
	}

	@Override
	public boolean keyPressed(@NotNull KeyEvent event) {
		if (event.isUp() && scale < 4) {
			scale++;
			centerMap();
			return true;
		}
		if (event.isDown() && scale > 0) {
			scale--;
			centerMap();
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
	public void resize(int width, int height) {
		RENDER_STATES.clear();
		super.resize(width, height);
	}

	@Override
	public void onClose() {
		RENDER_STATES.clear();
		super.onClose();
	}
}
