package me.pajic.mapstitch.item;

import com.swacky.ohmega.api.IAccessory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AtlasOhmegaItem extends AtlasItem implements IAccessory {
	public AtlasOhmegaItem(Properties properties) {
		super(properties);
	}

	@Override
	public void tick(@NotNull Player player, @NotNull ItemStack stack) {
		stack.inventoryTick(player.level(), player, null);
	}

	@Override
	public boolean autoSync(@NotNull Player player, @NotNull ItemStack stack) {
		return true;
	}
}
