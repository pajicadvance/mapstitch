package me.pajic.mapstitch.item;

import it.unimi.dsi.fastutil.Pair;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.enchantment.ModEnchantments;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.mixin.accessor.BundleItemAccessor;
import me.pajic.mapstitch.networking.payload.S2COpenWorldMapScreen;
import me.pajic.mapstitch.platform.MultiLoaderUtil;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

//? >=26.1 {
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
//? fabric {
import me.pajic.mapstitch.util.CompatFlags;
import me.pajic.mapstitch.compat.RemappedCompat;
//?}
//?} else {
/*import net.minecraft.world.InteractionResultHolder;
*///?}

public class AtlasItem extends Item {

	private static final Map<UUID, Set<MapId>> INITIALIZED = new HashMap<>();
	private static final Semaphore MUTEX = new Semaphore(1);
    private boolean initialLoadComplete = false;

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
                //? >=26.1 {
                else {
					BundleItemAccessor.mapstitch$callPlayInsertFailSound(player);
				}
                //?}
				updateAtlas(contents, self, player);
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
				updateAtlas(contents, self, player);
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
        //? >=26.1 {
		if (clickAction == ClickAction.PRIMARY && other.isEmpty()) {
			BundleItem.toggleSelectedItem(self, -1);
		} else {
        //?}
			BundleContents initialContents = self.get(DataComponents.BUNDLE_CONTENTS);
			if (initialContents != null) {
				BundleContents.Mutable contents = new BundleContents.Mutable(initialContents);
				if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && isValidItemForAtlas(other, self, player.level())) {
					if (slot.allowModification(player) && contents.tryInsert(other) > 0) {
						playInsertSound(player);
					}
                    //? >=26.1 {
                    else {
						BundleItemAccessor.mapstitch$callPlayInsertFailSound(player);
					}
                    //?}
					updateAtlas(contents, self, player);
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
					updateAtlas(contents, self, player);
					return true;
				}
                //? >=26.1 {
                else {
					BundleItem.toggleSelectedItem(self, -1);
					return false;
				}
                //?}
			}
        //? >=26.1
		}
		return false;
	}

	@Override @NotNull
    //~ if <26.1 'InteractionResult' -> 'InteractionResultHolder<ItemStack>'
	public InteractionResult use(final @NotNull Level level, final Player player, final @NotNull InteractionHand hand) {
		player.startUsingItem(hand);
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.playSound(SoundEvents.BOOK_PAGE_TURN);
			MultiLoaderUtil.INSTANCE.s2c(serverPlayer, new S2COpenWorldMapScreen(
					player.getItemInHand(hand).getOrDefault(ModDataComponents.ATLAS_SCALE, -1))
			);
		}
        //~ if <26.1 'InteractionResult.SUCCESS' -> 'InteractionResultHolder.success(player.getItemInHand(hand))'
		return InteractionResult.SUCCESS;
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
		return !stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)./*? >=26.1 {*/items()./*?}*/isEmpty();
	}

	@Override
	public int getBarWidth(final ItemStack stack) {
		BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return Math.round(Mth.clampedLerp(getAtlasItemCount(contents) / (getMaxSize().floatValue() * 64), 0, 13));
	}

	@Override
	public int getBarColor(final ItemStack stack) {
		BundleContents contents = stack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		return getAtlasItemCount(contents) == getMaxSize().intValue() * 64 ?
                //~ if <26.1 'ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F)' -> 'Mth.color(1.0F, 0.33F, 0.33F)'
                ARGB.colorFromFloat(1.0F, 1.0F, 0.33F, 0.33F) : BundleItemAccessor.mapstitch$getBarColor();
	}

    @Override
	public void inventoryTick(
			@NotNull ItemStack atlas,
            //~ if <26.1 'ServerLevel' -> 'Level'
			@NotNull ServerLevel level,
			@NotNull Entity owner,
            //? <26.1 {
            /*int slotId,
            boolean isSelected
            *///?} else {
            @Nullable EquipmentSlot slot
            //?}
	) {
        if (!level.isClientSide()) {
            BundleContents contents = atlas.get(DataComponents.BUNDLE_CONTENTS);
            if (contents != null && !contents.isEmpty()) {
                int activeMapId = atlas.getOrDefault(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
                if (!initialLoadComplete) {
                    atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
                    //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
                    for (ItemStackTemplate stack : contents.items()) {
                        if (stack.is(Items.FILLED_MAP)) {
                            MapId mapId = stack.get(DataComponents.MAP_ID);
                            UUID uuid = owner.getUUID();
                            INITIALIZED.putIfAbsent(uuid, new HashSet<>());
                            if (mapId != null) {
                                MapItemSavedData data = MapItem.getSavedData(mapId, level);
                                if (data != null && data.dimension.identifier().equals(level.dimension().identifier())) {
                                    updateMap(owner, stack, level, data, atlas, mapId, uuid, true);
                                }
                            }
                        }
                    }
                    initialLoadComplete = true;
                }
                int posX = owner.getBlockX();
                int posZ = owner.getBlockZ();
                //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
                for (ItemStackTemplate stack : contents.items()) {
                    if (stack.is(Items.FILLED_MAP)) {
                        MapId mapId = stack.get(DataComponents.MAP_ID);
                        UUID uuid = owner.getUUID();
                        INITIALIZED.putIfAbsent(uuid, new HashSet<>());
                        if (mapId != null && (!INITIALIZED.get(uuid).contains(mapId) || activeMapId == mapId.id())) {
                            MapItemSavedData data = MapItem.getSavedData(mapId, level);
                            if (data != null && data.dimension.identifier().equals(level.dimension().identifier())) {
                                updateMap(owner, stack, level, data, atlas, mapId, uuid, false);
                                int distX = Math.abs(data.centerX - posX);
                                int distZ = Math.abs(data.centerZ - posZ);
                                int scale = data.scale + 1;
                                if (distX > 64 * scale || distZ > 64 * scale) {
                                    updateActiveMap(atlas, contents, posX, posZ, level, owner);
                                }
                            }
                        }
                    }
                }
                MapItemSavedData activeMapData = MapItem.getSavedData(new MapId(activeMapId), level);
                if (activeMapData != null && !activeMapData.dimension.identifier().equals(level.dimension().identifier())) {
                    activeMapId = -1;
                }
                if (activeMapId == -1) updateActiveMap(atlas, contents, posX, posZ, level, owner);
            }
        }
	}

    //~ if <26.1 'ItemStackTemplate stack' -> 'ItemStack stack'
    //~ if <26.1 'ServerLevel' -> 'Level'
    private void updateMap(Entity owner, ItemStackTemplate stack, ServerLevel level, MapItemSavedData data, ItemStack atlas, MapId mapId, UUID uuid, boolean force) {
        if (owner instanceof ServerPlayer player) {
            //~ if <26.1 'item().value()' -> 'getItem()'
            if (!data.locked) ((MapItem) stack.item().value()).update(level, owner, data);
            if (force) data.tickCarriedBy(player, stack/*? >=26.1 {*/.create(), null/*?}*/);
            data.tickCarriedBy(player, atlas/*? >=26.1 {*/, null/*?}*/);
            //? >=26.1 && fabric {
            if (CompatFlags.REMAPPED_LOADED) RemappedCompat.sendMapPackets(mapId, data, player);
            else
            //?}
                ModUtil.sendVanillaMapPacket(mapId, data, player, force);
        }
        INITIALIZED.get(uuid).add(mapId);
    }

	@Override
	//? neoforge
	//@SuppressWarnings("deprecation")
	public void onDestroyed(final ItemEntity entity) {
		BundleContents contents = entity.getItem().get(DataComponents.BUNDLE_CONTENTS);
		if (contents != null) {
			entity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            //~ if <26.1 'itemCopyStream()' -> 'itemsCopy()'
			ItemUtils.onContainerDestroyed(entity, contents.itemCopyStream());
		}
	}

	public static Fraction getMaxSize() {
		return Fraction.getFraction(MapStitch.CONFIG.maxAtlasItems.get(), 64);
	}

	public static List<Component> getTooltip(ItemStack atlas) {
		List<Component> lines = new ArrayList<>();
		int scale = atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
		if (scale != -1) lines.add(Component.translatable("mapstitch.gui.worldmap.scale", "1:" + Math.round(Math.pow(2, scale))).withStyle(ChatFormatting.GRAY));
		BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		int filledMapCount = 0;
		int emptyMapCount = 0;
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
		for (ItemStackTemplate map : contents.items()) {
            //~ if <26.1 'count()' -> 'getCount()'
			int count = map.count();
			if (map.is(Items.FILLED_MAP)) filledMapCount += count;
			if (map.is(Items.PAPER) || map.is(Items.MAP)) emptyMapCount += count;
		}
		lines.add(Component.translatable("mapstitch.tooltip.atlas.filled_maps", filledMapCount).withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("mapstitch.tooltip.atlas.empty_maps", emptyMapCount).withStyle(ChatFormatting.GRAY));
		boolean filledMapsFirst = atlas.getOrDefault(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true);
		lines.add(Component.translatable(filledMapsFirst ?
				"mapstitch.tooltip.atlas.filled_maps_first" :
				"mapstitch.tooltip.atlas.empty_maps_first"
        ).withStyle(ChatFormatting.GRAY));
		return lines;
	}

	private void updateActiveMap(ItemStack atlas, BundleContents contents, int posX, int posZ, Level level, Entity owner) {
		int emptyMapIndex = -1;
		Set<Pair<Vector2i, Identifier>> cachedCenters = new HashSet<>();
		for (int i = 0; i < contents.size(); i++) {
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            //~ if <26.1 'items().get(i)' -> 'getItemUnsafe(i)'
			ItemStackTemplate map = contents.items().get(i);
			if (map.is(Items.FILLED_MAP)) {
				MapId mapId = map.get(DataComponents.MAP_ID);
				MapItemSavedData mapData = MapItem.getSavedData(mapId, level);
				if (mapData != null && !ModUtil.isExplorationMap(mapData) && mapData.dimension.identifier().equals(level.dimension().identifier())) {
					int centerX = mapData.centerX;
					int centerZ = mapData.centerZ;
					cachedCenters.add(Pair.of(new Vector2i(centerX, centerZ), mapData.dimension.identifier()));
					int distX = Math.abs(centerX - posX);
					int distZ = Math.abs(centerZ - posZ);
					int scale = Math.toIntExact(Math.round(Math.pow(2, mapData.scale)));
					if (distX <= 64 * scale && distZ <= 64 * scale) {
						atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, mapId.id());
						return;
					}
				}
			} else if (emptyMapIndex == -1 && (map.is(Items.PAPER) || map.is(Items.MAP))) emptyMapIndex = i;
		}
		atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
		if (MUTEX.availablePermits() > 0 && emptyMapIndex != -1) {
			try {
				MUTEX.acquire();
				ItemStack newMap = MapItem.create(
                        /*? >=26.1 {*/(ServerLevel) /*?}*/level, posX, posZ,
						atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, 0).byteValue(),
						true, false
				);
				MapItemSavedData mapData = MapItem.getSavedData(newMap.get(DataComponents.MAP_ID), level);
				if (mapData != null) {
					int centerX = mapData.centerX;
					int centerZ = mapData.centerZ;
					Vector2i center = new Vector2i(centerX, centerZ);
					if (!cachedCenters.contains(Pair.of(center, mapData.dimension.identifier()))) {
						BundleContents.Mutable mutableContents = new BundleContents.Mutable(contents);
						//noinspection DataFlowIssue
						((BundleContentsMutableExtension) mutableContents).mapstitch$removeOneItemAtIndex(emptyMapIndex);
						newMap.set(ModDataComponents.MAP_CENTER, new Vector2i(centerX, centerZ));
                        ((MapItem) newMap.getItem()).update(level, owner, mapData);
						mutableContents.tryInsert(newMap);
						atlas.set(DataComponents.BUNDLE_CONTENTS, mutableContents.toImmutable());
						if (owner instanceof ServerPlayer player) {
							player.awardStat(Stats.ITEM_USED.get(this));
							level.playSound(
									null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT,
									player.getSoundSource(), 1.0F, 1.0F
							);
                            updateAtlas(mutableContents, atlas, player);
						}
					}
				}
			} catch (InterruptedException e) {
				MapStitch.LOGGER.warn("Map creation interrupted", e);
			} finally {
				MUTEX.release();
			}
		}
	}

	public void updateAtlas(BundleContents.Mutable contents, ItemStack atlas, Player player) {
		BundleContents immutableContents = contents.toImmutable();
		atlas.set(DataComponents.BUNDLE_CONTENTS, immutableContents);
		int itemCount = getAtlasItemCount(immutableContents);
		atlas.set(
				ModDataComponents.ATLAS_FULLNESS,
				Math.min(4, itemCount == 0 ? 0 : itemCount / ((getMaxSize().intValue() * 64) / 4) + 1)
		);
		atlas.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
        player.containerMenu.slotsChanged(player.getInventory());
	}

	private int getAtlasItemCount(BundleContents contents) {
		int itemCount = 0;
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
        //~ if <26.1 'count()' -> 'getCount()'
		for (ItemStackTemplate item : contents.items()) itemCount += item.count();
		return itemCount;
	}

	private boolean isValidItemForAtlas(ItemStack stack, ItemStack atlas, Level level) {
		if (ModEnchantments.hasGlobetrotter(atlas, level) ? stack.is(Items.PAPER) : stack.is(Items.MAP)) return true;
		if (stack.is(Items.FILLED_MAP)) {
			MapItemSavedData mapData = MapItem.getSavedData(stack, level);
			int atlasScale = atlas.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
			return mapData != null && (ModUtil.isExplorationMap(mapData) || (atlasScale != -1 && mapData.scale == atlasScale));
		}
		return false;
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
