package me.pajic.mapstitch.compat;

import com.swacky.ohmega.api.AccessoryHelper;
import com.swacky.ohmega.api.IAccessory;
import com.swacky.ohmega.api.event.AccessoryOverrideTypesEvent;
import com.swacky.ohmega.common.accessorytype.AccessoryType;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.AtlasOhmegaItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.Map;

//? >=26.1
import net.minecraft.world.item.ItemStackTemplate;

//? neoforge
//import net.neoforged.bus.api.IEventBus;

public class OhmegaCompat implements AccessoryUtil {

    @Override
    public AtlasItem makeAtlas(Item.Properties properties) {
        return new AtlasOhmegaItem(properties);
    }

    @Override
    public List<ItemStack> getAtlases(LivingEntity entity) {
        if (entity instanceof Player player) {
            //~ if <26.1 'getAccessoryStacks' -> 'getStacks'
            return AccessoryHelper.getAccessoryStacks(player).stream().filter(stack -> stack.is(ModItems.ATLAS)).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasItem(Item item, LivingEntity entity) {
        if (entity instanceof Player player) {
            //~ if <26.1 'getAccessoryStacks' -> 'getStacks'
            NonNullList<ItemStack> items = AccessoryHelper.getAccessoryStacks(player);
            if (items.stream().anyMatch(stack -> stack.is(item))) return true;
            List<ItemStack> list = items.stream().filter(stack -> stack.has(DataComponents.CONTAINER)).toList();
            return list.stream().anyMatch(stack -> {
                ItemContainerContents contents = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
                for (ItemStackTemplate itemStack: contents.nonEmptyItems()) {
                    if (itemStack.is(item)) return true;
                }
                return false;
            });
        }
        return false;
    }

    @Override
    public ItemStack getFirstItem(Item item, LivingEntity entity) {
        if (entity instanceof Player player) {
            //~ if <26.1 'getAccessoryStacks' -> 'getStacks'
            NonNullList<ItemStack> items = AccessoryHelper.getAccessoryStacks(player);
            ItemStack stack = items.stream().filter(stack1 -> stack1.is(item)).findFirst().orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    //? fabric {
    public static void setupAccessories() {
        AccessoryOverrideTypesEvent.EVENT.register(OhmegaCompat::bind);
    }
    //?} else {
    /*public static void setupAccessories(IEventBus modBus) {
        modBus.addListener(AccessoryOverrideTypesEvent.class, event -> bind(event.overrideRemaps));
    }
    *///?}

    private static void bind(Map<Item, AccessoryType> map) {
        if (MapStitch.CONFIG.accessorySlots.compassSlot.get()) {
            AccessoryHelper.bindAccessory(Items.COMPASS, new CompassBinding());
            map.put(Items.COMPASS, AccessoryType.UTILITY.get());
        }
        if (MapStitch.CONFIG.accessorySlots.clockSlot.get()) {
            AccessoryHelper.bindAccessory(Items.CLOCK, new ClockBinding());
            map.put(Items.CLOCK, AccessoryType.UTILITY.get());
        }
    }

    static class CompassBinding implements IAccessory {}
    static class ClockBinding implements IAccessory {}
}
