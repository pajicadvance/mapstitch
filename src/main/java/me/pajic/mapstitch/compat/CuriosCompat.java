package me.pajic.mapstitch.compat;

import me.pajic.mapstitch.item.AtlasCurioItem;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

public class CuriosCompat {

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
