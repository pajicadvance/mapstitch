package me.pajic.mapstitch.recipe;

import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipes {
	public static final RecipeSerializer<AtlasRecipe> ATLAS = new RecipeSerializer<>(AtlasRecipe.MAP_CODEC, AtlasRecipe.STREAM_CODEC);

	public static void init() {}
}
