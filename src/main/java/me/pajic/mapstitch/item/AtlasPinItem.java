package me.pajic.mapstitch.item;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

public class AtlasPinItem extends Item {

    public AtlasPinItem() {
        super(new Item.Properties()/*? >=26.1 {*/.setId(ModItems.ATLAS_PIN_KEY)/*?}*/);
    }

    @Override
    public boolean isEnabled(@NotNull FeatureFlagSet enabledFeatures) {
        return MapStitch.CONFIG.atlasPins.item.get();
    }
}
