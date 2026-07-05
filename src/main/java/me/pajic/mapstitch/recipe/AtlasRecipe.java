package me.pajic.mapstitch.recipe;

import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

public class AtlasRecipe extends CustomRecipe {
	private ItemStack map = ItemStack.EMPTY;
	private int scale = -1;

	public AtlasRecipe(CraftingBookCategory category) {
		super(category);
	}

	@Override
	public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
		if (input.size() == 2) {
			boolean hasBook = false;
			boolean hasMap = false;
			for (int i = 0; i < input.size(); i++) {
				ItemStack itemStack = input.getItem(i);
				if (!itemStack.isEmpty()) {
					if (itemStack.is(Items.BOOK)) {
						if (hasBook) return false;
						hasBook = true;
					} else if (itemStack.is(Items.FILLED_MAP)) {
						if (hasMap) return false;
						hasMap = true;
						map = itemStack.copy();
						MapItemSavedData data = MapItem.getSavedData(itemStack, level);
						if (data != null) scale = data.scale;
					}
				}
			}
			return hasBook && hasMap;
		}
		return false;
	}

	@Override @NotNull
	public ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider registries) {
		ItemStack itemStack = ItemStack.EMPTY;
		if (scale != -1) {
			itemStack = new ItemStack(ModItems.ATLAS);
			itemStack.set(ModDataComponents.ATLAS_SCALE, scale);
			itemStack.set(ModDataComponents.ATLAS_FULLNESS, 1);
			BundleContents.Mutable contents = new BundleContents.Mutable(itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY));
			contents.tryInsert(map);
			itemStack.set(DataComponents.BUNDLE_CONTENTS, contents.toImmutable());
		}
		return itemStack;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override @NotNull
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return ModRecipes.ATLAS;
	}
}
