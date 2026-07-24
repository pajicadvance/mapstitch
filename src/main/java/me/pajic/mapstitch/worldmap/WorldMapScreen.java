package me.pajic.mapstitch.worldmap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.CuriosCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.config.ModConfigHolder;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.keybind.ModKeybinds;
import me.pajic.mapstitch.networking.payload.C2SEjectMap;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class WorldMapScreen extends Screen {
	private final Minecraft MC = Minecraft.getInstance();
	private static final Map<GridPos, MapDataWithId> MAPS = new HashMap<>();
	private static final Map<GridPos, List<MapDecoration>> DECORATIONS = new HashMap<>();
	private static final ResourceLocation PLAYER_MARKER = ResourceLocation.withDefaultNamespace("textures/map/decorations/player.png");
	private static final ResourceLocation ATLAS_CRAFTING = MapStitch.id("textures/gui/atlas_crafting.png");

	public static List<ResourceLocation> dimensionIds = List.of();
	private static ResourceLocation dimensionId = ResourceLocation.withDefaultNamespace("overworld");

	private static int posX = 0;
	private static int posY = 0;
	private static int posZ = 0;
	private static int zoomLevel;

	private boolean compass;
	private int mapSize;
	private double mapPixels;
	private double camX, camZ;
	private int mouseX, mouseY;
	private float zoom;
	private int screenW, screenH;
	private int scale;
	private boolean help;
	private boolean grid;
	private boolean follow;
	private int mapsRendered = 0;

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
		ModUtil.worldMapOpen = true;
		dimensionId = MC.level.dimension().location();
	}

	@Override
	protected void init() {
		if (ModConfigHolder.options().worldMapButtons) {
			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.zoom_in"),
					b -> zoomLevel = Mth.clamp(zoomLevel + 1, -2, 1)
			).pos(width - 20, height / 2 - 72).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.zoom_in.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.zoom_out"),
					b -> zoomLevel = Mth.clamp(zoomLevel - 1, -2, 1)
			).pos(width - 20, height / 2 - 54).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.zoom_out.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.dimension"), b -> {
						int i = dimensionIds.indexOf(dimensionId);
						dimensionId = i + 1 > dimensionIds.size() - 1 ? dimensionIds.getFirst() : dimensionIds.get(i + 1);
					}
			).pos(width - 20, height / 2 - 36).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.dimension.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.scale"),
					b -> {
						if (scale < 4) scale++;
						else scale = 0;
					}
			).pos(width - 20, height / 2 - 18).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.scale.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.center"),
					b -> centerMap()
			).pos(width - 20, height / 2).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.center.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.grid"),
					b -> grid = !grid
			).pos(width - 20, height / 2 + 18).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.grid.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.follow"),
					b -> follow = !follow
			).pos(width - 20, height / 2 + 36).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.follow.tooltip"))
			).build());

			addRenderableWidget(Button.builder(
					Component.translatable("mapstitch.gui.worldmap.button.help"),
					b -> help = !help
			).pos(width - 20, height / 2 + 54).size(16, 16).tooltip(
					Tooltip.create(Component.translatable("mapstitch.gui.worldmap.button.help.tooltip"))
			).build());
		}
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
	private GridPos screenToGrid(double screenX, double screenY, int scale) {
		return new GridPos(
				Math.floorDiv(Mth.floor(screenToWorldX(screenX)) + 64, 128 << scale),
				Math.floorDiv(Mth.floor(screenToWorldZ(screenY)) + 64, 128 << scale),
				scale
		);
	}

	private int gridOriginX(int gx) { return gx * mapSize - 64; }
	private int gridOriginZ(int gy) { return gy * mapSize - 64; }

	private double worldToScreenX(double worldX) { return (worldX - camX) * zoom + screenW / 2.0; }
	private double worldToScreenZ(double worldZ) { return (worldZ - camZ) * zoom + screenH / 2.0; }
	private double screenToWorldX(double screenX) { return (screenX - screenW / 2.0) / zoom + camX; }
	private double screenToWorldZ(double screenY) { return (screenY - screenH / 2.0) / zoom + camZ; }

	@SuppressWarnings("DataFlowIssue")
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(graphics, mouseX, mouseY, partialTick);
		mapSize = 128 << scale;
		mapPixels = mapSize * zoom;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		int highlightColor = ModConfigHolder.options().worldMapTextHighlightColor.color;
		int xBoundMin = Math.floorDiv((int)(camX - (screenW/2d)/zoom), mapSize);
		int xBoundMax = Math.floorDiv((int)(camX + (screenW/2d)/zoom), mapSize) + 1;
		int zBoundMin = Math.floorDiv((int)(camZ - (screenH/2d)/zoom), mapSize);
		int zBoundMax = Math.floorDiv((int)(camZ + (screenH/2d)/zoom), mapSize) + 1;
		renderMaps(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
		renderDecorations(graphics);
		if (compass) {
			if (MC.level.dimension().location().equals(dimensionId)) renderPlayerMarker(graphics);
			if (grid) {
				renderGrid(graphics, xBoundMin, xBoundMax, zBoundMin, zBoundMax);
				renderPosAtCursor(graphics, mouseX, mouseY, highlightColor);
			}
		}
		renderText(graphics, highlightColor);
		mapsRendered = 0;
		for (Renderable renderable : renderables) {
			renderable.render(graphics, mouseX, mouseY, partialTick);
		}
	}

	private void renderMaps(GuiGraphics graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
		if (!ModConfigHolder.options().worldMapHelp) help = false;
		if (MC.level == null || MC.player == null) return;
		boolean hasAnyMapSources = false;
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
		for (ItemStack stack : items) {
			if (stack.is(ModItems.ATLAS)) {
				BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
				for (ItemStack map : contents.items()) {
					if (map.is(Items.FILLED_MAP)) {
						prepareMap(map);
						hasAnyMapSources = true;
					}
				}
			} else if (stack.is(Items.FILLED_MAP)) {
				prepareMap(stack);
				hasAnyMapSources = true;
			}
		}
		screenW = MC.getWindow().getGuiScaledWidth();
		screenH = MC.getWindow().getGuiScaledHeight();
		PoseStack pose = graphics.pose();
		if (hasAnyMapSources) {
			if (MAPS.isEmpty()) {
				compass = false;
				Component text = Component.translatable("mapstitch.gui.worldmap.no_maps_rendered");
				graphics.drawString(MC.font, text, width / 2 - font.width(text) / 2, height / 2, 0xffffffff);
			}
			else for (int gy = zBoundMin; gy <= zBoundMax; gy++) {
				for (int gx = xBoundMin; gx <= xBoundMax; gx++) {
					GridPos gp = new GridPos(gx, gy, scale);
					MapDataWithId map = MAPS.get(gp);
					if (map != null && map.data.scale == scale) {
						double px = worldToScreenX(gridOriginX(gx));
						double py = worldToScreenZ(gridOriginZ(gy));
						pose.pushPose();
						pose.translate((float) px, (float) py, 0);
						pose.scale((float) (mapPixels / 128), (float) (mapPixels / 128), 0);
						map.data.getDecorations().forEach(decor -> prepareDecoration(decor, gp));
						MC.gameRenderer.getMapRenderer().render(graphics.pose(), graphics.bufferSource(), map.id, map.data, true, 15728880);
						mapsRendered++;
						pose.popPose();
					}
				}
			}
		} else {
			compass = false;
			pose.pushPose();
			pose.translate(width / 2F, height / 2F - 52, 0);
			Component text1 = Component.translatable("mapstitch.gui.worldmap.no_map_sources")
					.withColor(ModConfigHolder.options().worldMapTextHighlightColor.color);
			Component text2 = Component.translatable("mapstitch.gui.worldmap.no_map_sources_info_1");
			Component text3 = Component.translatable("mapstitch.gui.worldmap.no_map_sources_info_2");
			graphics.drawString(MC.font, text1, -font.width(text1) / 2, 0, 0xffffffff);
			graphics.drawString(MC.font, text2, -font.width(text2) / 2, 12, 0xffffffff);
			graphics.drawString(MC.font, text3, -font.width(text3) / 2, 24, 0xffffffff);
			graphics.blit(
					ATLAS_CRAFTING,
					-60, 36,
					0, 0,
					120, 68,
					120, 68
			);
			pose.popPose();
		}
		MAPS.clear();
	}

	private void renderDecorations(GuiGraphics graphics) {
		PoseStack pose = graphics.pose();
		DECORATIONS.forEach((gp, decors) -> {
			double px = worldToScreenX(gridOriginX(gp.gx));
			double py = worldToScreenZ(gridOriginZ(gp.gy));
			float s = (float) Math.pow(2, zoomLevel);
			pose.pushPose();
			pose.translate((float) px, (float) py, 0);
			pose.scale((float) (mapPixels / 128.0), (float) (mapPixels / 128.0), 0);
			decors.forEach(decor -> {
				pose.pushPose();
				pose.translate(decor.x() / 2F + 64F, decor.y() / 2F + 64F, -0.02F);
				pose.mulPose(Axis.ZP.rotationDegrees((float)(decor.rot() * 360) / 16.0F));
				pose.scale(4F / s, 4F / s, 3F);
				pose.translate(-0.125F, 0.125F, 0);
				TextureAtlasSprite spr = MC.getMapDecorationTextures().get(decor);
				VertexConsumer vc = graphics.bufferSource().getBuffer(RenderType.text(spr.atlasLocation()));
				Matrix4f m4f = pose.last().pose();
				vc.addVertex(m4f, -1.0F, 1.0F, -0.001F).setColor(-1).setUv(spr.getU0(), spr.getV0()).setLight(15728880);
				vc.addVertex(m4f, 1.0F, 1.0F, -0.001F).setColor(-1).setUv(spr.getU1(), spr.getV0()).setLight(15728880);
				vc.addVertex(m4f, 1.0F, -1.0F, -0.001F).setColor(-1).setUv(spr.getU1(), spr.getV1()).setLight(15728880);
				vc.addVertex(m4f, -1.0F, -1.0F, -0.001F).setColor(-1).setUv(spr.getU0(), spr.getV1()).setLight(15728880);
				pose.popPose();
				if (decor.name().isPresent()) {
					Font font = MC.font;
					Component name = decor.name().get();
					float width = font.width(name);
					float scale = Mth.clamp(25F / width, 0.5F, 1) / s;
					pose.pushPose();
					pose.translate(
							decor.x() / 2F + 64F - width * scale / 2F,
							decor.y() / 2F + 64F + 4F / s,
							-0.025F
					);
					pose.scale(scale, scale, 1);
					pose.translate(0, 0, -0.1F);
					graphics.drawString(font, name, 0, 0, -1);
					pose.popPose();
				}
			});
			pose.popPose();
		});
		DECORATIONS.clear();
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

	private void prepareDecoration(MapDecoration decor, GridPos gp) {
		if (!ModUtil.DECORS_REQUIRING_COMPASS.contains(decor.type())) {
			DECORATIONS.putIfAbsent(gp, new ArrayList<>());
			DECORATIONS.get(gp).add(decor);
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
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.help", Component.keybind(ModKeybinds.TOGGLE_HELP.getName()).withColor(c)), ModConfigHolder.options().worldMapHelp && !help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.help_control", Component.keybind(ModKeybinds.TOGGLE_HELP.getName()).withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.exit_control", Component.translatable("mapstitch.gui.worldmap.exit_key").withColor(c), Component.keybind(ModKeybinds.OPEN_WORLD_MAP.getName()).withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.eject_control", Component.translatable("mapstitch.gui.worldmap.eject_key", Component.keybind(ModKeybinds.EJECT_MAP.getName())).withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.grid_control", Component.keybind(ModKeybinds.TOGGLE_GRID.getName()).withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.follow_control", Component.keybind(ModKeybinds.FOLLOW_PLAYER.getName()).withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.scale_control", getMultiKeyTranslation(ModKeybinds.SCALE_UP, ModKeybinds.SCALE_DOWN, c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.dimension_control", getMultiKeyTranslation(ModKeybinds.DIMENSION_UP, ModKeybinds.DIMENSION_DOWN, c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.center_control", Component.translatable("mapstitch.gui.worldmap.center_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.zoom_control", Component.translatable("mapstitch.gui.worldmap.zoom_key").withColor(c)), help),
				new ObjectBooleanImmutablePair<>(Component.translatable("mapstitch.gui.worldmap.move_control", Component.translatable("mapstitch.gui.worldmap.move_key").withColor(c)), help)
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

    private void renderGrid(GuiGraphics graphics, int xBoundMin, int xBoundMax, int zBoundMin, int zBoundMax) {
		Font font = MC.font;
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
		textRenderCalls.forEach(d -> graphics.drawString(
				font, d.get().s, d.get().i1, d.get().i2, 0xffffffff, false)
		);
	}

	private record TextRenderData(String s, int i1, int i2) {}

	private int getVerticalGridBarWidth(Font font) {
		int wMax = font.width(Integer.toString((int) screenToWorldZ(0)));
		int wMin = font.width(Integer.toString((int) screenToWorldZ(screenH)));
		return Math.max(wMax, wMin) + 4;
	}

    private void renderPosAtCursor(GuiGraphics graphics, int mouseX, int mouseY, int c) {
		int wx = (int) Math.floor(screenToWorldX(mouseX));
		int wz = (int) Math.floor(screenToWorldZ(mouseY));
		Component text = Component.translatable("mapstitch.gui.worldmap.cursor_position", white(String.valueOf(wx)), white(String.valueOf(wz))).withColor(c);
		Font font = MC.font;
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

	@SuppressWarnings("DataFlowIssue")
	private void ejectMapAtCursor() {
		MapDataWithId mapDataWithId = MAPS.get(screenToGrid(mouseX, mouseY, scale));
		if (mapDataWithId != null) {
			PacketDistributor.sendToServer(new C2SEjectMap(mapDataWithId.id));
			MC.player.playSound(SoundEvents.BUNDLE_REMOVE_ONE);
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
		if (ModKeybinds.SCALE_UP.matches(keyCode, scanCode)) {
			if (scale < 4) scale++;
			else scale = 0;
			return true;
		}
		if (ModKeybinds.SCALE_DOWN.matches(keyCode, scanCode)) {
			if (scale > 0) scale--;
			else scale = 4;
			return true;
		}
		if (ModKeybinds.DIMENSION_UP.matches(keyCode, scanCode)) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i + 1 > dimensionIds.size() - 1 ? dimensionIds.getFirst() : dimensionIds.get(i + 1);
			return true;
		}
		if (ModKeybinds.DIMENSION_DOWN.matches(keyCode, scanCode)) {
			int i = dimensionIds.indexOf(dimensionId);
			dimensionId = i - 1 < 0 ? dimensionIds.getLast() : dimensionIds.get(i - 1);
			return true;
		}
		if (ModConfigHolder.options().worldMapHelp && ModKeybinds.TOGGLE_HELP.matches(keyCode, scanCode)) {
			help = !help;
			return true;
		}
		if (ModKeybinds.FOLLOW_PLAYER.matches(keyCode, scanCode)) {
			follow = !follow;
			return true;
		}
		if (ModKeybinds.TOGGLE_GRID.matches(keyCode, scanCode)) {
			grid = !grid;
			return true;
		}
		if (ModKeybinds.OPEN_WORLD_MAP.matches(keyCode, scanCode)) {
			onClose();
			return true;
		}
		if (modifiers == 2 && ModKeybinds.EJECT_MAP.matches(keyCode, scanCode)) {
			ejectMapAtCursor();
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
