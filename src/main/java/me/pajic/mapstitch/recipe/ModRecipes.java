package me.pajic.mapstitch.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;
//? <26.1
//import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public class ModRecipes {

	public static final RecipeSerializer<AtlasRecipe> ATLAS = new
            //? <26.1 {
            /*SimpleCraftingRecipeSerializer<>(AtlasRecipe::new);
            *///?} else {
            RecipeSerializer<>(AtlasRecipe.MAP_CODEC, AtlasRecipe.STREAM_CODEC);
            //?}
}
