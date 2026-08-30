package me.pajic.mapstitch.compat;

//? neoforge {

/*import me.pajic.mapstitch.item.AtlasCurioItem;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Optional;

//? >=26.1
//import net.minecraft.world.item.ItemStackTemplate;

public class CuriosCompat implements AccessoryUtil {

    @Override
    public AtlasItem makeAtlas(Item.Properties properties) {
        return new AtlasCurioItem(properties);
    }

    @Override
    public List<ItemStack> getAtlases(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(itemHandler -> itemHandler
                        .findCurios(stack -> stack.is(ModItems.ATLAS))
                        .stream().map(SlotResult::stack).toList())
                .orElseGet(List::of);
    }

    @Override
    public boolean hasItem(Item item, LivingEntity entity) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(entity);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            if (handler.isEquipped(item)) return true;
            return handler.findCurios(stack -> stack.has(DataComponents.CONTAINER)).stream().anyMatch(slotResult -> {
                //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
                for (ItemStack stack : slotResult.stack().getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItems()) {
                    if (stack.is(item)) return true;
                }
                return false;
            });
        }
        return false;
    }

    @Override
    public ItemStack getFirstItem(Item item, LivingEntity entity) {
        Optional<ICuriosItemHandler> opt = CuriosApi.getCuriosInventory(entity);
        if (opt.isPresent()) {
            ICuriosItemHandler handler = opt.get();
            Optional<SlotResult> res = handler.findFirstCurio(item);
            if (res.isPresent()) return res.get().stack();
        }
        return ItemStack.EMPTY;
    }
}
*///?}
