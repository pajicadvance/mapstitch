package me.pajic.mapstitch.item;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.mixin.accessor.BundleItemAccessor;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;
import java.util.concurrent.Semaphore;

public class AtlasItem extends Item {
	public static final Fraction MAX_SIZE = Fraction.getFraction(256, 1);
	private static final Map<UUID, Set<MapId>> INITIALIZED = new HashMap<>();
	private static final Semaphore MUTEX = new Semaphore(1);

	public AtlasItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean overrideStackedOnOther(
			final ItemStack self,
			@NotNull final Slot slot,
			@NotNull final ClickAction clickAction,
			@NotNull final Player player
	) {
		BundleContents initialContents = self.get(DataComponents.BUNDLE_CONTENTS);
		if (initialContents != null) {
			ItemStack other = slot.getItem();
			BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);
			if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && isValidItemForAtlas(other, self, player.level())) {
				if (contents.tryTransfer(slot, player) > 0) {
					playInsertSound(player);
				}
				updateAtlas(contents, self);
				broadcastChangesOnContainerMenu(player);
				return true;
			} else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
				boolean filledMapsFirst = self.getOrDefault(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true);
				ItemStack itemStack = ((BundleContentsMutableExtension) contents).mapstitch$removeOneStackOrdered(filledMapsFirst);
				if (itemStack != null) {
					ItemStack remainder = slot.safeInsert(itemStack);
					if (remainder.getCount() > 0 && isValidItemForAtlas(remainder, self, player.level())) {
						contents.tryInsert(remainder);
					} else {
						playRemoveOneSound(player);
					}
				}
				updateAtlas(contents, self);
				broadcastChangesOnContainerMenu(player);
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean overrideOtherStackedOnMe(
			final @NotNull ItemStack self,
			final @NotNull ItemStack other,
			final @NotNull Slot slot,
			final @NotNull ClickAction clickAction,
			final @NotNull Player player,
			final @NotNull SlotAccess carriedItem
	) {
		BundleContents initialContents = self.get(DataComponents.BUNDLE_CONTENTS);
		if (initialContents != null) {
			BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);
			if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && isValidItemForAtlas(other, self, player.level())) {
				if (slot.allowModification(player) && contents.tryInsert(other) > 0) {
					playInsertSound(player);
				}
				updateAtlas(contents, self);
				broadcastChangesOnContainerMenu(player);
				return true;
			} else if (clickAction == ClickAction.SECONDARY && other.isEmpty()) {
				if (slot.allowModification(player)) {
					boolean filledMapsFirst = self.getOrDefault(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true);
					ItemStack removed = ((BundleContentsMutableExtension) contents).mapstitch$removeOneStackOrdered(filledMapsFirst);
					if (removed != null) {
						playRemoveOneSound(player);
						carriedItem.set(removed);
					}
				}
				updateAtlas(contents, self);
				broadcastChangesOnContainerMenu(player);
				return true;
			}
		}
		return false;
	}

	@Override @NotNull
	public InteractionResultHolder<ItemStack> use(final @NotNull Level level, final Player player, final @NotNull InteractionHand hand) {
		player.startUsingItem(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.playSound(SoundEvents.BOOK_PAGE_TURN);
			PacketDistributor.sendToPlayer(serverPlayer, new S2COpenWorldMapScreen());
		}
		return InteractionResultHolder.success(player.getItemInHand(hand));
	}

	@Override @NotNull
	public InteractionResult useOn(@NotNull UseOnContext context) {
		BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
		if (clicked.is(BlockTags.BANNERS)) {
			if (!context.getLevel().isClientSide()) {
				int activeMapId = context.getItemInHand().getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
				MapItemSavedData data = context.getLevel().getMapData(new MapId(activeMapId));
				if (data != null && !data.toggleBanner(context.getLevel(), context.getClickedPos())) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.SUCCESS;
		} else {
			return super.useOn(context);
		}
	}

	@Override
	public boolean isBarVisible(final ItemStack stack) {
		return !stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY).isEmpty();
	}

	@Override
	public int getBarWidth(final ItemStack stack) {
		BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return Math.round(Mth.clampedLerp(getAtlasItemCount(contents) / (MAX_SIZE.intValue() * 64F), 0, 13));
	}

	@Override
	public int getBarColor(final ItemStack stack) {
		BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return getAtlasItemCount(contents) == MAX_SIZE.intValue() * 64 ?
				Mth.color(1.0F, 0.33F, 0.33F) : BundleItemAccessor.mapstitch$getBarColor();
	}

	@Override
	public void inventoryTick(
			@NotNull ItemStack atlas,
			@NotNull Level level,
			@NotNull Entity owner,
			int slotId,
			boolean isSelected
	) {
		if (level instanceof ServerLevel serverLevel) {
			BundleContents contents = atlas.get(DataComponents.BUNDLE_CONTENTS);
			int activeMapId = atlas.getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
			if (contents != null && !contents.isEmpty() && owner instanceof ServerPlayer player) {
				int posX = player.getBlockX();
				int posZ = player.getBlockZ();
				for (ItemStack stack : contents.items()) {
					if (stack.is(Items.FILLED_MAP)) {
						MapId mapId = stack.get(DataComponents.MAP_ID);
						UUID uuid = player.getUUID();
						INITIALIZED.putIfAbsent(uuid, new HashSet<>());
						if (mapId != null && (!INITIALIZED.get(uuid).contains(mapId) || activeMapId == mapId.id())) {
							MapItemSavedData data = MapItem.getSavedData(mapId, serverLevel);
							if (data != null) {
								data.tickCarriedBy(player, atlas);
								if (!data.locked) {
									((MapItem) stack.getItem()).update(serverLevel, player, data);
									Packet<?> packet = data.getUpdatePacket(mapId, player);
									if (packet != null) player.connection.send(packet);
								}
								int distX = Math.abs(data.centerX - posX);
								int distZ = Math.abs(data.centerZ - posZ);
								int scale = data.scale + 1;
								if (distX > 64 * scale || distZ > 64 * scale) {
									updateActiveMap(atlas, contents, posX, posZ, serverLevel, player);
								}
							}
							INITIALIZED.get(uuid).add(mapId);
						}
					}
				}
				if (activeMapId == -1) updateActiveMap(atlas, contents, posX, posZ, serverLevel, player);
			}
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onDestroyed(final ItemEntity entity) {
		BundleContents contents = entity.getItem().get(DataComponents.BUNDLE_CONTENTS);
		if (contents != null) {
			entity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
			ItemUtils.onContainerDestroyed(entity, contents.itemsCopy());
		}
	}

	public static List<Component> getTooltip(ItemStack atlas) {
		List<Component> lines = new ArrayList<>();
		int scale = atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
		if (scale != -1) lines.add(Component.translatable("mapstitch.gui.worldmap.scale", "1:" + Math.round(Math.pow(2, scale))).withStyle(ChatFormatting.GRAY));
		BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		int filledMapCount = 0;
		int emptyMapCount = 0;
		for (ItemStack map : contents.items()) {
			int count = map.getCount();
			if (map.is(Items.FILLED_MAP)) filledMapCount += count;
			if (map.is(Items.MAP)) emptyMapCount += count;
		}
		lines.add(Component.translatable("mapstitch.tooltip.atlas.filled_maps", filledMapCount).withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("mapstitch.tooltip.atlas.empty_maps", emptyMapCount).withStyle(ChatFormatting.GRAY));
		boolean filledMapsFirst = atlas.getOrDefault(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true);
		lines.add(Component.translatable(filledMapsFirst ?
				"mapstitch.tooltip.atlas.filled_maps_first" :
				"mapstitch.tooltip.atlas.empty_maps_first")
				.withStyle(ChatFormatting.GRAY));
		return lines;
	}

	private void updateActiveMap(ItemStack atlas, BundleContents contents, int posX, int posZ, ServerLevel level, Entity owner) {
		int emptyMapIndex = -1;
		boolean hasAnyFilledMaps = false;
		for (int i = 0; i < contents.size(); i++) {
			ItemStack map = contents.getItemUnsafe(i);
			if (map.is(Items.FILLED_MAP)) {
				hasAnyFilledMaps = true;
				MapId mapId = map.get(DataComponents.MAP_ID);
				MapItemSavedData mapData = MapItem.getSavedData(mapId, level);
				if (mapData != null) {
					int distX = Math.abs(mapData.centerX - posX);
					int distZ = Math.abs(mapData.centerZ - posZ);
					int scale = Math.toIntExact(Math.round(Math.pow(2, mapData.scale)));
					if (distX < 64 * scale && distZ < 64 * scale) {
						atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, mapId.id());
						return;
					}
				}
			} else if (emptyMapIndex == -1 && map.is(Items.MAP)) emptyMapIndex = i;
		}
		atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
		if (MUTEX.availablePermits() > 0 && emptyMapIndex != -1 && hasAnyFilledMaps) {
			try {
				MUTEX.acquire();
				ItemStack newMap = MapItem.create(level, posX, posZ, atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, 0).byteValue(), true, false);
				BundleContents.Mutable mutableContents = new BundleContents.Mutable(contents);
				//noinspection DataFlowIssue
				((BundleContentsMutableExtension) mutableContents).mapstitch$removeOneItemAtIndex(emptyMapIndex);
				MapItemSavedData mapData = MapItem.getSavedData(newMap.get(DataComponents.MAP_ID), level);
				newMap.set(ModDataComponents.MAP_CENTER, new Vector2i(mapData.centerX, mapData.centerZ));
				newMap.inventoryTick(level, owner, 0, true);
				mutableContents.tryInsert(newMap);
				atlas.set(DataComponents.BUNDLE_CONTENTS, mutableContents.toImmutable());
				if (owner instanceof ServerPlayer player) {
					player.awardStat(Stats.ITEM_USED.get(this));
					level.playSound(null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
				}
			} catch (InterruptedException e) {
				MapStitch.LOGGER.warn("Map creation interrupted", e);
			} finally {
				MUTEX.release();
			}
		}
	}

	public void updateAtlas(BundleContents.Mutable contents, ItemStack atlas) {
		BundleContents immutableContents = contents.toImmutable();
		atlas.set(DataComponents.BUNDLE_CONTENTS, immutableContents);
		int itemCount = getAtlasItemCount(immutableContents);
		atlas.set(ModDataComponents.ATLAS_FULLNESS, Math.min(4, itemCount == 0 ? 0 : itemCount / ((MAX_SIZE.intValue() * 64) / 4) + 1));
		atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
	}

	private int getAtlasItemCount(BundleContents contents) {
		int itemCount = 0;
		for (ItemStack item : contents.items()) itemCount += item.getCount();
		return itemCount;
	}

	private boolean isValidItemForAtlas(ItemStack map, ItemStack atlas, Level level) {
		if (map.is(Items.MAP)) return true;
		if (map.is(Items.FILLED_MAP)) {
			MapItemSavedData mapData = MapItem.getSavedData(map, level);
			int atlasScale = atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
			return mapData != null && atlasScale != -1 && mapData.scale == atlasScale;
		}
		return false;
	}

	private void broadcastChangesOnContainerMenu(final Player player) {
		player.containerMenu.slotsChanged(player.getInventory());
	}

	private void playInsertSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	private void playRemoveOneSound(Entity entity) {
		entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
	}

	public static void clearInitializedMapsForPlayer(ServerPlayer player) {
		INITIALIZED.remove(player.getUUID());
	}
}
