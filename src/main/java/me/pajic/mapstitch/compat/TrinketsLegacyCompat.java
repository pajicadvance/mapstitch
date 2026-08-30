package me.pajic.mapstitch.compat;

//? fabric && 1.21.1 {

/*import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.AtlasTrinketLegacyItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.Optional;

public class TrinketsLegacyCompat implements AccessoryUtil {

    @Override
    public AtlasItem makeAtlas(Item.Properties properties) {
        AtlasTrinketLegacyItem item = new AtlasTrinketLegacyItem(properties);
        TrinketsApi.registerTrinket(item, item);
        return item;
    }

    @Override
    public List<ItemStack> getAtlases(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity)
                .map(tc -> tc
                        .getEquipped(ModItems.ATLAS)
                        .stream().map(Tuple::getB).toList()
                ).orElseGet(List::of);
    }

    @Override
    public boolean hasItem(Item item, LivingEntity entity) {
        Optional<TrinketComponent> opt = TrinketsApi.getTrinketComponent(entity);
        if (opt.isPresent()) {
            TrinketComponent component = opt.get();
            if (component.isEquipped(item)) return true;
            List<ItemStack> list = component.getEquipped(stack -> stack.has(DataComponents.CONTAINER))
                    .stream().map(Tuple::getB).toList();
            return list.stream().anyMatch(stack -> {
                ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                for (ItemStack itemStack : contents.nonEmptyItems()) {
                    if (itemStack.is(item)) return true;
                }
                return false;
            });
        }
        return false;
    }

    @Override
    public ItemStack getFirstItem(Item item, LivingEntity entity) {
        Optional<TrinketComponent> opt = TrinketsApi.getTrinketComponent(entity);
        if (opt.isPresent()) {
            List<Tuple<SlotReference, ItemStack>> list = opt.get().getEquipped(item);
            if (!list.isEmpty()) return list.getFirst().getB();
        }
        return ItemStack.EMPTY;
    }
}
*///?}
