package me.pajic.mapstitch.recipe;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.enchantment.ModEnchantments;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

//? >=26.1
import net.minecraft.world.item.ItemStackTemplate;

public class AtlasCartographyHandler {

    public static final int TOGGLE_MODE_BUTTON = 0;
    public static final int MODE_COPY = 0;
    public static final int MODE_MERGE = 1;

    public enum Status { OK, NOT_ENOUGH_EMPTY_MAPS, SCALE_MISMATCH, TOO_MANY_ITEMS, NOTHING_TO_COPY }

    public record Result(ItemStack stack, Status status, int amount) {
        static Result ok(ItemStack stack) {
            return new Result(stack, Status.OK, 0);
        }
        static Result fail(Status status, int amount) {
            return new Result(ItemStack.EMPTY, status, amount);
        }
    }

    public static boolean isAtlasRelatedRecipe(ItemStack topAtlas, ItemStack bottomAtlas) {
        return topAtlas.is(ModItems.ATLAS) && bottomAtlas.is(ModItems.ATLAS);
    }

    public static Result assemble(ItemStack top, ItemStack bottom, int mode, Level level) {
        if (!isAtlasRelatedRecipe(top, bottom)) return Result.fail(Status.OK, 0);
        return mode == MODE_MERGE ? merge(top, bottom, level) : copy(top, bottom, level);
    }

    private static Result copy(ItemStack source, ItemStack target, Level level) {
        Integer scale = resultScale(source, target, level);
        if (scale == null) return Result.fail(Status.SCALE_MISMATCH, 0);
        Builder builder = new Builder(level);
        builder.addAll(target, true);
        builder.addAll(source, false);
        int copies = builder.copies();
        if (copies == 0) return Result.fail(Status.NOTHING_TO_COPY, 0);
        int missing = copies - builder.displaced - builder.emptyCount();
        if (missing > 0) return Result.fail(Status.NOT_ENOUGH_EMPTY_MAPS, missing);
        return Result.ok(builder.build(target.copyWithCount(1), scale, copies));
    }

    private static Result merge(ItemStack top, ItemStack bottom, Level level) {
        Integer scale = resultScale(top, bottom, level);
        if (scale == null) return Result.fail(Status.SCALE_MISMATCH, 0);
        int excess = itemCount(top) + itemCount(bottom) - MapStitch.CONFIG.maxAtlasItems.get();
        if (excess > 0) return Result.fail(Status.TOO_MANY_ITEMS, excess);
        Builder builder = new Builder(level);
        builder.addAll(top, true);
        builder.addAll(bottom, true);
        ItemStack base = top.copyWithCount(1);
        mergeEnchantments(base, bottom);
        return Result.ok(builder.build(base, scale, 0));
    }

    private static @Nullable Integer resultScale(ItemStack primary, ItemStack secondary, Level level) {
        int a = primary.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
        int b = secondary.getOrDefault(ModDataComponents.ATLAS_SCALE, -1);
        boolean aMaps = hasRegularMaps(primary, level), bMaps = hasRegularMaps(secondary, level);
        if (aMaps && bMaps) return a == b ? a : null;
        if (aMaps) return a;
        if (bMaps) return b;
        return a != -1 ? a : b;
    }

    private static boolean hasRegularMaps(ItemStack atlas, Level level) {
        for (ItemStack stack : stacks(atlas)) {
            if (stack.has(DataComponents.MAP_ID) && !ModUtil.isExplorationMap(stack, level)) return true;
        }
        return false;
    }

    private static int itemCount(ItemStack atlas) {
        int count = 0;
        for (ItemStack stack : stacks(atlas)) count += stack.getCount();
        return count;
    }

    private static List<ItemStack> stacks(ItemStack atlas) {
        BundleContents contents = atlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        List<ItemStack> list = new ArrayList<>();
        //? >=26.1 {
        for (ItemStackTemplate template : contents.items()) list.add(template.create());
        //?} else {
        /*for (ItemStack stack : contents.items()) list.add(stack.copy());
         *///?}
        return list;
    }

    private static void mergeEnchantments(ItemStack base, ItemStack other) {
        ItemEnchantments extra = other.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (extra.isEmpty()) return;
        EnchantmentHelper.updateEnchantments(base, mutable -> {
            for (Object2IntMap.Entry<Holder<Enchantment>> e : extra.entrySet()) mutable.upgrade(e.getKey(), e.getIntValue());
        });
    }

    public static @Nullable Component describe(int status, int amount) {
        Status[] values = Status.values();
        if (status <= 0 || status >= values.length) return null;
        String key = "mapstitch.gui.cartography.atlas." + values[status].name().toLowerCase(Locale.ROOT);
        return Component.translatable(key, amount).withStyle(ChatFormatting.RED);
    }

    private static final class Builder {

        private record Entry(ItemStack map, boolean owned) {}
        private record Area(ResourceKey<Level> dimension, int x, int z, int scale) {}

        private final Level level;
        private final List<Entry> filled = new ArrayList<>();
        private final Map<MapId, Integer> byId = new HashMap<>();
        private final Map<Area, Integer> byArea = new HashMap<>();
        private final List<ItemStack> others = new ArrayList<>();
        private int maps, paper;
        private int displaced;

        Builder(Level level) {
            this.level = level;
        }

        void addAll(ItemStack atlas, boolean owned) {
            for (ItemStack stack : stacks(atlas)) {
                if (stack.has(DataComponents.MAP_ID)) addMap(stack, owned);
                else if (!owned) continue;
                else if (stack.is(Items.MAP)) maps += stack.getCount();
                else if (stack.is(Items.PAPER)) paper += stack.getCount();
                else others.add(stack);
            }
        }

        private void addMap(ItemStack stack, boolean owned) {
            if (owned) displaced += stack.getCount() - 1;
            MapId id = stack.get(DataComponents.MAP_ID);
            Entry entry = new Entry(stack.copyWithCount(1), owned);
            if (byId.containsKey(id)) {
                if (owned) displaced++;
                return;
            }
            Area area = area(id, entry.map());
            Integer index = area != null ? byArea.get(area) : null;
            if (index == null) {
                byId.put(id, filled.size());
                if (area != null) byArea.put(area, filled.size());
                filled.add(entry);
                return;
            }
            Entry old = filled.get(index);
            MapId oldId = old.map().get(DataComponents.MAP_ID);
            if (explored(id) > explored(oldId)) {
                byId.remove(oldId);
                byId.put(id, index);
                filled.set(index, entry);
                if (old.owned()) displaced++;
            } else if (owned) {
                displaced++;
            }
        }

        private @Nullable Area area(MapId id, ItemStack map) {
            MapItemSavedData data = MapItem.getSavedData(id, level);
            if (data == null || ModUtil.isExplorationMap(map, level)) return null;
            return new Area(data.dimension, data.centerX, data.centerZ, data.scale);
        }

        private int explored(@Nullable MapId id) {
            MapItemSavedData data = MapItem.getSavedData(id, level);
            if (data == null) return 0;
            int count = 0;
            for (byte color : data.colors) if (color != 0) count++;
            return count;
        }

        int copies() {
            return (int) filled.stream().filter(e -> !e.owned()).count();
        }

        int emptyCount() {
            return maps + paper;
        }

        ItemStack build(ItemStack base, int scale, int cost) {
            int change = displaced - cost;
            if (ModEnchantments.hasGlobetrotter(base, level)) {
                paper += change;
                if (paper < 0) { maps += paper; paper = 0; }
            } else {
                maps += change;
                if (maps < 0) { paper += maps; maps = 0; }
            }
            int total = filled.size() + maps + paper + others.stream().mapToInt(ItemStack::getCount).sum();

            BundleContents.Mutable contents = new BundleContents.Mutable(/*? <26.3 {*//*BundleContents.EMPTY*//*?}*/);
            ((BundleContentsMutableExtension) contents).mapstitch$setIsAtlas();
            filled.forEach(e -> contents.tryInsert(e.map()));
            others.forEach(contents::tryInsert);
            insertEmpty(contents, Items.MAP, maps);
            insertEmpty(contents, Items.PAPER, paper);

            base.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
            if (scale >= 0) base.set(ModDataComponents.ATLAS_SCALE, scale);
            else base.remove(ModDataComponents.ATLAS_SCALE);
            base.set(ModDataComponents.ATLAS_FULLNESS, AtlasItem.fullness(total));
            base.set(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1);
            return base;
        }

        private static void insertEmpty(BundleContents.Mutable contents, Item item, int count) {
            int max = item.getDefaultMaxStackSize();
            while (count > 0) {
                int n = Math.min(count, max);
                contents.tryInsert(new ItemStack(item, n));
                count -= n;
            }
        }
    }
}
