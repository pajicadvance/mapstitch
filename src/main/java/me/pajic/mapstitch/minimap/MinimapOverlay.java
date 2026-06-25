package me.pajic.mapstitch.minimap;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import me.pajic.mapstitch.compat.OhmegaCompat;
import me.pajic.mapstitch.compat.TrinketsCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.config.ModConfigHolder;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.util.ModUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

public class MinimapOverlay {
	private static final Minecraft MC = Minecraft.getInstance();
	private static final MapRenderState STATE = new MapRenderState();
	private static final Identifier BACKGROUND_TEXTURE = Identifier.withDefaultNamespace("container/cartography_table/map");
	private static int noMapTextTimer = 100;

	public static void render(GuiGraphicsExtractor graphics) {
		if (
				MC.player != null && MC.level != null && !MC.gui.hud.isHidden() && ModUtil.hasCompass(MC)
				&& !MC.gui.hud.getDebugOverlay().showDebugScreen() && !(MC.gui.screen() instanceof WorldMapScreen)
		) {
			ItemStack atlas = getAtlas();
			if (!atlas.isEmpty()) {
				int width = MC.getWindow().getGuiScaledWidth();
				int height = MC.getWindow().getGuiScaledHeight();
				int offsetX = ModConfigHolder.options().minimapXOffset;
				int offsetY = ModConfigHolder.options().minimapYOffset;
				float scale = 0.05F + (ModConfigHolder.options().minimapSize * 0.05F);
				int offset = Math.round(scale * switch (ModConfigHolder.options().minimapBackground) {
					case TEXTURE -> 12;
					case CLEAR -> 6;
					case NONE -> 4;
				});
				IntIntImmutablePair position;
				int scaleOffset = Math.round(128 * scale);
				switch (ModConfigHolder.options().minimapPosition) {
					case TOP_RIGHT -> position = new IntIntImmutablePair(
							width - offset - scaleOffset - offsetX,
							offset + offsetY + (MC.player.getActiveEffects().isEmpty() ? 0 : 51)
					);
					case BOTTOM_LEFT -> position = new IntIntImmutablePair(
							offset + offsetX,
							height - offset - scaleOffset - offsetY
					);
					case BOTTOM_RIGHT -> position = new IntIntImmutablePair(
							width - offset - scaleOffset - offsetX,
							height - offset - scaleOffset - offsetY
					);
					default -> position = new IntIntImmutablePair(
							offset + offsetX,
							offset + offsetY
					);
				}
				int x = position.leftInt();
				int y = position.rightInt();
				graphics.pose().pushMatrix();
				graphics.pose().translate(x, y);
				graphics.pose().scale(scale, scale);
				switch (ModConfigHolder.options().minimapBackground) {
					case TEXTURE -> graphics.blitSprite(
							RenderPipelines.GUI_TEXTURED,
							BACKGROUND_TEXTURE,
							-8, -8, 144, 144
					);
					case CLEAR -> graphics.fill(-4, -4, 132, 132, ARGB.color(
							ARGB.as8BitChannel(ModConfigHolder.options().minimapBackgroundOpacity / 100F),
							0, 0, 0
					));
				}
				int id = atlas.getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
				if (id != -1) {
					noMapTextTimer = 100;
					MapId mapId = new MapId(id);
					MapItemSavedData mapData = MapItem.getSavedData(mapId, MC.level);
					if (mapData != null) {
						MC.getMapRenderer().extractRenderState(mapId, mapData, STATE);
						STATE.decorations.forEach(decor -> decor.renderOnFrame = true);
						graphics.map(STATE);
					}
				} else {
					if (noMapTextTimer == 0) {
						Component c1 = Component.translatable("mapstitch.gui.minimap.no_map_1");
						Component c2 = Component.translatable("mapstitch.gui.minimap.no_map_2");
						graphics.text(MC.font, c1, 64 - MC.font.width(c1) / 2, 64 - 8, 0xffffffff);
						graphics.text(MC.font, c2, 64 - MC.font.width(c2) / 2, 64 - 8 + 12, 0xffffffff);
					} else noMapTextTimer--;
				}
				graphics.pose().popMatrix();
			}
		}
	}

	@SuppressWarnings("DataFlowIssue")
	private static ItemStack getAtlas() {
		ItemStack trinketAtlas = ItemStack.EMPTY;
		if (CompatFlags.TRINKETS_LOADED) {
			List<ItemStack> list = TrinketsCompat.getTrinketAtlases(MC.player);
			if (!list.isEmpty()) trinketAtlas = list.getFirst();
		}
		else if (CompatFlags.OHMEGA_LOADED) {
			List<ItemStack> list = OhmegaCompat.getOhmegaAtlases(MC.player);
			if (!list.isEmpty()) trinketAtlas = list.getFirst();
		}
		return !trinketAtlas.isEmpty() ? trinketAtlas : switch (ModConfigHolder.options().minimapDisplayCondition) {
			case HANDS -> checkHands();
			case HOTBAR -> {
				ItemStack handAtlas = checkHands();
				yield handAtlas.isEmpty() ? checkHotbar() : handAtlas;
			}
			case INVENTORY -> {
				ItemStack handAtlas = checkHands();
				ItemStack atlas = handAtlas.isEmpty() ? checkHotbar() : handAtlas;
				if (atlas.isEmpty()) for (ItemStack stack : MC.player.getInventory().getNonEquipmentItems()) {
					if (stack.is(ModItems.ATLAS)) yield stack;
				} else yield atlas;
				yield ItemStack.EMPTY;
			}
		};
	}

	@SuppressWarnings("DataFlowIssue")
	private static ItemStack checkHands() {
		ItemStack mainhand = MC.player.getMainHandItem();
		ItemStack offhand = MC.player.getOffhandItem();
		if (mainhand.is(ModItems.ATLAS)) return mainhand;
		if (offhand.is(ModItems.ATLAS)) return offhand;
		return ItemStack.EMPTY;
	}

	@SuppressWarnings("DataFlowIssue")
	private static ItemStack checkHotbar() {
		for (int i = 0; i < 9; i++) {
			ItemStack stack = MC.player.getInventory().getItem(i);
			if (stack.is(ModItems.ATLAS)) return stack;
		}
		return ItemStack.EMPTY;
	}
}
