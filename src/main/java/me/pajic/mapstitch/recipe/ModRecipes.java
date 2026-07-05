package me.pajic.mapstitch.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class ModRecipes {
	public static RecipeSerializer<AtlasRecipe> ATLAS = new SimpleCraftingRecipeSerializer<>(AtlasRecipe::new);

	public static void init() {}
}
