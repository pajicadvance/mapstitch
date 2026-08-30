package me.pajic.mapstitch.compat;

import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.util.CompatFlags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AccessoryUtil {

    @Nullable AccessoryUtil INSTANCE = makeInstance();

    @Nullable static AccessoryUtil makeInstance() {
        //? !(neoforge && 1.21.1) {
        //~ if 1.21.1 'TrinketsCompat' -> 'TrinketsLegacyCompat'
        if (CompatFlags.TRINKETS_LOADED) return new TrinketsCompat();
        //?}
        if (CompatFlags.OHMEGA_LOADED) return new OhmegaCompat();
        //? neoforge
        //if (CompatFlags.CURIOS_LOADED) return new CuriosCompat();
        return null;
    }

    AtlasItem makeAtlas(Item.Properties properties);
    List<ItemStack> getAtlases(LivingEntity entity);
    boolean hasItem(Item item, LivingEntity entity);
    ItemStack getFirstItem(Item item, LivingEntity entity);
}
