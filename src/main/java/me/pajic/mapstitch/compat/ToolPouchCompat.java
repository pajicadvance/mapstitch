package me.pajic.mapstitch.compat;

import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.util.CompatFlags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

//? >=26.1 {
import me.pajic.toolpouch.menu.ToolPouchMenu;
import me.pajic.toolpouch.util.ToolPouchUtil;
import net.minecraft.world.item.ItemStackTemplate;
//?}

/** Uses Tool Pouch's active-pouch selection, including accessories and attached leggings. */
public class ToolPouchCompat {

    public static boolean hasItem(Player player, Item item) {
        //? >=26.1 {
        if (CompatFlags.TOOL_POUCH_LOADED) {
            return ToolPouchUtil.toolPouchHasItem(player, stack -> stack.is(item));
        }
        //?}
        return false;
    }

    public static List<ItemStack> getAtlases(Player player) {
        //? >=26.1 {
        // The open menu owns a separate mutable inventory until it closes. Do not
        // read or mutate the stored snapshot while the player is editing that menu.
        if (CompatFlags.TOOL_POUCH_LOADED && !(player.containerMenu instanceof ToolPouchMenu)) {
            return ToolPouchUtil.getItemsFromToolPouch(player, stack -> stack.is(ModItems.ATLAS))
                    .stream().map(ItemStackTemplate::create).toList();
        }
        //?}
        return List.of();
    }

    public static void saveAtlas(Player player, ItemStack atlas, int index) {
        //? >=26.1 {
        if (CompatFlags.TOOL_POUCH_LOADED && !(player.containerMenu instanceof ToolPouchMenu)) {
            ToolPouchUtil.replaceItemInToolPouch(player, atlas, stack -> stack.is(ModItems.ATLAS), index);
        }
        //?}
    }

    public static void tick(Player player) {
        //? >=26.1 {
        if (player.level().isClientSide()) return;
        List<ItemStack> atlases = getAtlases(player);
        for (int i = 0; i < atlases.size(); i++) {
            ItemStack atlas = atlases.get(i);
            ItemStack before = atlas.copy();
            atlas.inventoryTick(player.level(), player, null);
            if (!ItemStack.isSameItemSameComponents(before, atlas)) saveAtlas(player, atlas, i);
        }
        //?}
    }
}
