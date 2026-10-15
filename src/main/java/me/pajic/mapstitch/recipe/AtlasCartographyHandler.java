package me.pajic.mapstitch.recipe;

import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.enchantment.ModEnchantments;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

//? >=26.1
import net.minecraft.world.item.ItemStackTemplate;

public class AtlasCartographyHandler {

    public static boolean isAtlasRelatedRecipe(ItemStack topAtlas, ItemStack bottomAtlas) {
        return topAtlas.is(ModItems.ATLAS) && bottomAtlas.is(ModItems.ATLAS);
    }

    public static boolean isCopyRecipe(ItemStack sourceAtlas, ItemStack targetAtlas) {
        if (isAtlasRelatedRecipe(sourceAtlas, targetAtlas) && sourceAtlas.has(ModDataComponents.ATLAS_SCALE)) {
            BundleContents sourceContents = sourceAtlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            BundleContents targetContents = targetAtlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
            int sourceFilledMapCount = 0;
            int targetEmptyMapCount = 0;
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate map : targetContents.items()) {
                //~ if <26.1 'count()' -> 'getCount()'
                int count = map.count();
                if (map.get(DataComponents.MAP_ID) != null) return false;
                if (map.is(Items.PAPER) || map.is(Items.MAP)) targetEmptyMapCount += count;
            }
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate map : sourceContents.items()) {
                //~ if <26.1 'count()' -> 'getCount()'
                int count = map.count();
                if (map.get(DataComponents.MAP_ID) != null) sourceFilledMapCount += count;
            }
            return targetEmptyMapCount >= sourceFilledMapCount;
        }
        return false;
    }

    public static ItemStack createCopy(ItemStack sourceAtlas, ItemStack targetAtlas, Level level) {
        BundleContents sourceContents = sourceAtlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
        List<ItemStackTemplate> filledMaps = new ArrayList<>();
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
        for (ItemStackTemplate map : sourceContents.items()) {
            if (map.get(DataComponents.MAP_ID) != null) filledMaps.add(map);
        }
        int targetEmptyMapCount = 0;
        BundleContents targetContents = targetAtlas.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
        for (ItemStackTemplate map : targetContents.items()) {
            //~ if <26.1 'count()' -> 'getCount()'
            int count = map.count();
            if (map.is(Items.PAPER) || map.is(Items.MAP)) targetEmptyMapCount += count;
        }
        BundleContents.Mutable contents = new BundleContents.Mutable(/*? <26.3 {*//*BundleContents.EMPTY*//*?}*/);
        ((BundleContentsMutableExtension) contents).mapstitch$setIsAtlas();
        filledMaps.forEach(stack -> contents.tryInsert(stack/*? >=26.1 {*/.create()/*?}*/));
        int emptyMapCount = targetEmptyMapCount - filledMaps.size();
        Item itemToAdd = ModEnchantments.hasGlobetrotter(sourceAtlas, level) ? Items.PAPER : Items.MAP;
        int maxStackSize = itemToAdd.getDefaultMaxStackSize();
        while (emptyMapCount > maxStackSize) {
            contents.tryInsert(new ItemStack(itemToAdd, maxStackSize));
            emptyMapCount -= maxStackSize;
        }
        contents.tryInsert(new ItemStack(itemToAdd, emptyMapCount));
        ItemStack atlas = new ItemStack(ModItems.ATLAS);
        atlas.set(ModDataComponents.ATLAS_SCALE, sourceAtlas.get(ModDataComponents.ATLAS_SCALE));
        atlas.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
        return atlas;
    }
}
