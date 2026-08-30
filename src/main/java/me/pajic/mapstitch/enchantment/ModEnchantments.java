package me.pajic.mapstitch.enchantment;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> GLOBETROTTER = ResourceKey.create(
            Registries.ENCHANTMENT,
            MapStitch.id("globetrotter")
    );

    //? neoforge
    //@SuppressWarnings("deprecation")
    public static boolean hasGlobetrotter(ItemStack atlas, Level level) {
        return atlas.getEnchantments().getLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(GLOBETROTTER)) > 0;
    }
}
