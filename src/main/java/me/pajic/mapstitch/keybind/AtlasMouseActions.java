package me.pajic.mapstitch.keybind;

import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class AtlasMouseActions implements ItemSlotMouseAction {
	private final ScrollWheelHandler scrollWheelHandler;

	public AtlasMouseActions() {
		this.scrollWheelHandler = new ScrollWheelHandler();
	}

	@Override
	public boolean matches(@NotNull Slot slot) {
		return slot.getItem().is(ModItems.ATLAS);
	}

	@SuppressWarnings("DataFlowIssue")
	@Override
	public boolean onMouseScrolled(double scrollX, double scrollY, int slotIndex, @NotNull ItemStack itemStack) {
		Vector2i wheelXY = scrollWheelHandler.onMouseScroll(scrollX, scrollY);
		int wheel = wheelXY.y == 0 ? -wheelXY.x : wheelXY.y;
		if (wheel != 0 && itemStack.has(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST)) {
			boolean current = itemStack.get(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST);
			itemStack.set(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, !current);
		}
		return true;
	}

	@Override
	public void onStopHovering(@NotNull Slot hoveredSlot) {}

	@Override
	public void onSlotClicked(@NotNull Slot slot, @NotNull ContainerInput containerInput) {}
}
