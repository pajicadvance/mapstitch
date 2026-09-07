package me.pajic.mapstitch.minimap;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.MapStitchClient;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import me.pajic.mapstitch.util.ModClientUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//? >=26.1 {
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.client.renderer.state.MapRenderState;
//?} else {
/*import net.minecraft.world.level.saveddata.maps.MapDecoration;
*///?}

public class MinimapOverlay {

	private static final Minecraft MC = Minecraft.getInstance();
	private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("container/cartography_table/map");
    private static final Set<MinimapExplorationMarker> EXPLORATION_MARKERS = new HashSet<>();
    private static final Map<MapId, Vector2i> CACHED_CENTERS = new HashMap<>();
    private static ItemStack lastAtlas = ItemStack.EMPTY;
	private static int noMapTextTimer = 100;
    private static boolean toggle = true;

	public static void render(GuiGraphicsExtractor graphics) {
		if (
				toggle && MC.player != null && MC.level != null && !MC.gui.hud.isHidden() && ModClientUtil.hasCompass(MC, "minimap")
				&& !MC.gui.hud.getDebugOverlay().showDebugScreen() && !(MC.gui.screen() instanceof WorldMapScreen)
		) {
			ItemStack atlas = ModClientUtil.getFirstItem(MC, ModItems.ATLAS);
			if (!atlas.isEmpty()) {
                if (!ItemStack.isSameItemSameComponents(atlas, lastAtlas)) {
                    lastAtlas = atlas.copy();
                    updateMarkers(atlas);
                }
				int width = MC.getWindow().getGuiScaledWidth();
				int height = MC.getWindow().getGuiScaledHeight();
				int offsetX = MapStitchClient.CONFIG.minimap.xOffset.get();
				int offsetY = MapStitchClient.CONFIG.minimap.yOffset.get();
				float scale = 0.05F + (MapStitchClient.CONFIG.minimap.size.get() * 0.05F);
				int offset = Math.round(scale * switch (MapStitchClient.CONFIG.minimap.background.get()) {
					case TEXTURE -> 12;
					case CLEAR -> 6;
					case NONE -> 4;
				});
				int scaleOffset = Math.round(128 * scale);
                IntIntImmutablePair position = switch (MapStitchClient.CONFIG.minimap.position.get()) {
					case TOP_RIGHT -> new IntIntImmutablePair(
							width - offset - scaleOffset - offsetX,
							offset + offsetY + getEffectOffset()
					);
					case BOTTOM_LEFT -> new IntIntImmutablePair(
							offset + offsetX,
							height - offset - scaleOffset - offsetY
					);
					case BOTTOM_RIGHT -> new IntIntImmutablePair(
							width - offset - scaleOffset - offsetX,
							height - offset - scaleOffset - offsetY
					);
					default -> new IntIntImmutablePair(
							offset + offsetX,
							offset + offsetY
					);
				};
				int x = position.leftInt();
				int y = position.rightInt();
                //? <26.1
                //graphics.flush();
                MultiVersionUtil.INSTANCE.pushPose(graphics);
				MultiVersionUtil.INSTANCE.translatePose(graphics, x, y, 0);
                MultiVersionUtil.INSTANCE.scalePose(graphics, scale, scale, 0);
				switch (MapStitchClient.CONFIG.minimap.background.get()) {
					case TEXTURE -> MultiVersionUtil.INSTANCE.blitSprite(graphics, BACKGROUND_TEXTURE, -8, -8, 144, 144);
					case CLEAR -> graphics.fill(-4, -4, 132, 132, MultiVersionUtil.INSTANCE.color(
							MultiVersionUtil.INSTANCE.as8BitChannel(MapStitchClient.CONFIG.minimap.backgroundOpacity.get() / 100F),
							0, 0, 0
					));
				}
				int id = atlas.getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
				if (id != -1) {
					noMapTextTimer = 100;
					MapId mapId = new MapId(id);
					MapItemSavedData mapData = MapItem.getSavedData(mapId, MC.level);
                    BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
                    Vector2i activeMapCenter = CACHED_CENTERS.get(mapId);
                    //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
                    if (activeMapCenter == null) for (ItemStackTemplate map : contents.items()) {
                        MapId activeId = map.get(DataComponents.MAP_ID);
                        if (activeId != null && id == activeId.id()) {
                            activeMapCenter = map.get(ModDataComponents.MAP_CENTER);
                            CACHED_CENTERS.put(mapId, activeMapCenter);
                            break;
                        }
                    }
					if (mapData != null && activeMapCenter != null) {
                        MultiVersionUtil.INSTANCE.renderMap(MC, graphics, mapId, mapData, null, true);
                        //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
                        List<MapRenderState.MapDecorationRenderState> decors = new ArrayList<>();
                        int s = 1 << mapData.scale;
                        for (MinimapExplorationMarker marker : EXPLORATION_MARKERS) {
                            float xd = (float) (marker.worldX() - activeMapCenter.x) / s;
                            float yd = (float) (marker.worldZ() - activeMapCenter.y) / s;
                            if (xd >= -63.0F && yd >= -63.0F && xd <= 63.0F && yd <= 63.0F) {
                                //? >=26.1 {
                                marker.decor().x = (byte) Math.round(xd * 2.0F);
                                marker.decor().y = (byte) Math.round(yd * 2.0F);
                                //?}
                                //~ if <26.1 'marker.decor()' -> 'new MapDecoration(marker.decor().type(), (byte) Math.round(xd * 2.0F), (byte) Math.round(yd * 2.0F), marker.decor().rot(), marker.decor().name())'
                                decors.add(marker.decor());
                            }
                        }
                        ModClientUtil.renderDecorations(MC, graphics, decors, 1);
                    }
				} else {
					if (noMapTextTimer == 0) {
						Component c1 = Component.translatable("mapstitch.gui.minimap.no_map_1");
						Component c2 = Component.translatable("mapstitch.gui.minimap.no_map_2");
						graphics.text(MC.font, c1, 64 - MC.font.width(c1) / 2, 64 - 8, -1);
						graphics.text(MC.font, c2, 64 - MC.font.width(c2) / 2, 64 - 8 + 12, -1);
					} else noMapTextTimer--;
				}
                int i = 124;
                BlockPos blockPos = MC.player.blockPosition();
                if (MapStitch.CONFIG.minimapInfo.allowRealTime.get() && MapStitchClient.CONFIG.minimapInfo.realTime.get()) {
                    Component c = Component.translatable(
                            "mapstitch.gui.minimap.real_time",
                            LocalTime.now().format(DateTimeFormatter.ofPattern(switch (MapStitchClient.CONFIG.minimapInfo.realTimeFormat.get()) {
                                case H12 -> "hh:mm a";
                                case H24 -> "HH:mm";
                            }))
                    );
                    graphics.text(MC.font, c, 64 - MC.font.width(c) / 2, i+=12, -1);
                }
                if (MapStitch.CONFIG.minimapInfo.allowGameTime.get() && !MC.showOnlyReducedInfo() && MapStitchClient.CONFIG.minimapInfo.gameTime.get() && ModClientUtil.hasClock(MC, "time")) {
                    //~ if <26.1 'getOverworldClockTime()' -> 'getDayTime()'
                    long time = MC.level.getOverworldClockTime();
                    long timeOffset = (time + 6000) % 24000;
                    Component c = Component.translatable(
                            "mapstitch.gui.minimap.time",
                            (time / 24000L) + 1,
                            timeOffset / 1000,
                            String.format("%02d", (int) ((double) (timeOffset / 10 % 100) / 100 * 60))
                    );
                    graphics.text(MC.font, c, 64 - MC.font.width(c) / 2, i+=12, -1);
                }
                if (MapStitch.CONFIG.minimapInfo.allowCoordinates.get() && !MC.showOnlyReducedInfo() && MapStitchClient.CONFIG.minimapInfo.coordinates.get() && ModClientUtil.hasCompass(MC, "coordinates")) {
                    Component c = Component.translatable(
                            "mapstitch.gui.minimap.coordinates",
                            blockPos.getX(), blockPos.getY(), blockPos.getZ()
                    );
                    graphics.text(MC.font, c, 64 - MC.font.width(c) / 2, i+=12, -1);
                }
                if (MapStitch.CONFIG.minimapInfo.allowBiome.get() && !MC.showOnlyReducedInfo() && MapStitchClient.CONFIG.minimapInfo.biome.get() && ModClientUtil.hasCompass(MC, "biome")) {
                    ResourceKey<Biome> key = MC.player.level().getBiome(blockPos).unwrapKey().orElse(null);
                    Component c;
                    if (key != null) {
                        Identifier biomeId = key.identifier();
                        c = Component.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath());
                    } else c = Component.translatable("mapstitch.gui.minimap.biome.unknown");
                    graphics.text(MC.font, c, 64 - MC.font.width(c) / 2, i+=12, -1);
                }
                if (MapStitch.CONFIG.minimapInfo.allowWeather.get() && !MC.showOnlyReducedInfo() && MapStitchClient.CONFIG.minimapInfo.weather.get() && ModClientUtil.hasClock(MC, "weather")) {
                    Component c;
                    if (MC.level.isThundering()) c = Component.translatable("mapstitch.gui.minimap.weather.thundering");
                    else if (MC.level.isRaining()) c = switch (MC.level.getBiome(blockPos).value().getPrecipitationAt(blockPos/*? >=26.1 {*/, (int) MC.player.getY()/*?}*/)) {
                        case RAIN -> Component.translatable("mapstitch.gui.minimap.weather.raining");
                        case SNOW -> Component.translatable("mapstitch.gui.minimap.weather.snowing");
                        default -> Component.translatable("mapstitch.gui.minimap.weather.cloudy");
                    };
                    else c = Component.translatable("mapstitch.gui.minimap.weather.clear");
                    graphics.text(MC.font, c, 64 - MC.font.width(c) / 2, i + 12, -1);
                }
				MultiVersionUtil.INSTANCE.popPose(graphics);
                //? <26.1
                //graphics.flush();
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private static int getEffectOffset() {
		if (MapStitchClient.CONFIG.minimap.preventEffectOverlap.get()) {
			Set<MobEffectInstance> effects = new HashSet<>(MC.player.getActiveEffects());
			if (!effects.isEmpty()) {
				for (MobEffectInstance effect : effects) {
					if (!effect.getEffect().value().isBeneficial()) return 51;
				}
				return 25;
			}
		}
		return 0;
	}

    @SuppressWarnings("DataFlowIssue")
    private static void updateMarkers(ItemStack atlas) {
        EXPLORATION_MARKERS.clear();
        BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
        for (ItemStackTemplate map : contents.items()) {
            if (map.is(Items.FILLED_MAP)) {
                MapId id = map.get(DataComponents.MAP_ID);
                Vector2i mapCenter = map.get(ModDataComponents.MAP_CENTER);
                if (id != null && mapCenter != null) {
                    MapItemSavedData data = MC.level.getMapData(id);
                    if (data != null) {
                        int scaleFactor = 1 << data.scale;
                        //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
                        List<MapRenderState.MapDecorationRenderState> explorationDecors = ModClientUtil.extractDecors(data, id, MC, true);
                        explorationDecors.forEach(decor -> {
                            int worldX = mapCenter.x + Math.round((decor.x/*? <26.1 {*//*()*//*?}*/ / 2.0F) * scaleFactor);
                            int worldZ = mapCenter.y + Math.round((decor.y/*? <26.1 {*//*()*//*?}*/ / 2.0F) * scaleFactor);
                            EXPLORATION_MARKERS.add(new MinimapExplorationMarker(worldX, worldZ, decor));
                        });
                    }
                }
            }
        }
    }

    public static void toggle() {
        toggle = !toggle;
    }

    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
    private record MinimapExplorationMarker(int worldX, int worldZ, MapRenderState.MapDecorationRenderState decor) {}
}
