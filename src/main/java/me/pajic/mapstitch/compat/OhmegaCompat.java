package me.pajic.mapstitch.compat;

import com.swacky.ohmega.api.AccessoryHelper;
import me.pajic.mapstitch.item.AtlasItem;
import me.pajic.mapstitch.item.AtlasOhmegaItem;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class OhmegaCompat {

	public static List<ItemStack> getOhmegaAtlases(LivingEntity entity) {
		if (entity instanceof Player player) {
			return AccessoryHelper.getAccessoryStacks(player).stream().filter(stack -> stack.is(ModItems.ATLAS)).toList();
		}
		return List.of();
	}

	public static AtlasItem makeOhmegaAtlas(Item.Properties properties) {
		return new AtlasOhmegaItem(properties);
	}
}
