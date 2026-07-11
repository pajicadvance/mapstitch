package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public abstract class MapItemMixin {

	@Shadow public abstract void update(Level level, Entity player, MapItemSavedData data);

	@SuppressWarnings("DataFlowIssue")
	@Inject(
			method = "inventoryTick",
			at = @At("TAIL")
	)
	private void updateMapsInInventory(ItemStack itemStack, ServerLevel level, Entity owner, EquipmentSlot slot, CallbackInfo ci, @Local(name = "data") MapItemSavedData data) {
		if (data != null && !data.locked) {
			update(level, owner, data);
			if (owner instanceof ServerPlayer serverPlayer) {
				Packet<?> packet = data.getUpdatePacket(itemStack.get(DataComponents.MAP_ID), serverPlayer);
				if (packet != null) serverPlayer.connection.send(packet);
			}
		}
	}
}
