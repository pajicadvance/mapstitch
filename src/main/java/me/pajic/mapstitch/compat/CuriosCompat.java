package me.pajic.mapstitch.compat;

import me.pajic.mapstitch.item.AtlasCurioItem;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;

public class CuriosCompat {

    public static boolean hasCompassInCurioSlot(LivingEntity entity) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(entity);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            if (handler.isEquipped(Items.COMPASS)) return true;
            return handler.findCurios(stack -> stack.has(DataComponents.CONTAINER)).stream().anyMatch(slotResult -> {
                for (ItemStack stack : slotResult.stack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems()) {
                    if (stack.is(Items.COMPASS)) return true;
                }
                return false;
            });
        }
        return false;
    }

    public static List<ItemStack> getCurioAtlases(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(itemHandler -> itemHandler
                        .findCurios(stack -> stack.is(ModItems.ATLAS))
                        .stream().map(SlotResult::stack).toList())
                .orElseGet(List::of);
    }

    public static AtlasItem makeCurioAtlas(Item.Properties properties) {
        return new AtlasCurioItem(properties);
    }
}
