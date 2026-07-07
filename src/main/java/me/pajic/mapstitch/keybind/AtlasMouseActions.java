package me.pajic.mapstitch.keybind;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.networking.payload.C2SSetEjectMode;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AtlasMouseActions implements ItemSlotMouseAction {

	public AtlasMouseActions() {}

	@Override
	public boolean matches(@NotNull Slot slot) {
		return slot.getItem().is(ModItems.ATLAS);
	}

	@Override
	public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, @NotNull ItemStack itemStack) {
		if (scrollY != 0) MapStitch.xplat().c2s(new C2SSetEjectMode(slotIndex));
		return true;
	}

	@Override
	public void onStopHovering(@NotNull Slot hoveredSlot) {}

	@Override
	public void onSlotClicked(@NotNull Slot slot, @NotNull ContainerInput containerInput) {}
}
