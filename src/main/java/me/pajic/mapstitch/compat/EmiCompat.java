package me.pajic.mapstitch.compat;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

@EmiEntrypoint
public class EmiCompat implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        for (int i = 4; i >= 0; i--) {
            ItemStack atlas = new ItemStack(ModItems.ATLAS);
            atlas.set(ModDataComponents.ATLAS_SCALE, i);
            registry.addRecipe(new EmiCraftingRecipe(List.of(EmiIngredient.of(Ingredient.of(Items.BOOK)), EmiIngredient.of(Ingredient.of(Items.FILLED_MAP))), EmiStack.of(atlas), MapStitch.id("/atlas_recipe_" + i)));
        }
    }
}
