package me.pajic.mapstitch.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import me.pajic.mapstitch.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

//~ if <26.1 'gamerules.GameRules' -> 'GameRules'
import net.minecraft.world.level.gamerules.GameRules;

//? >=26.1 {
import org.jetbrains.annotations.NotNull;
import com.llamalad7.mixinextras.sugar.Local;
import me.pajic.mapstitch.component.ModDataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        //~ if <26.1 'level' -> 'level, blockPos, f'
        super(level, gameProfile);
    }

    //? >=26.1 {
    @NotNull @Shadow public abstract ServerLevel level();

	@Inject(
			method = "synchronizeSpecialItemUpdates",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;getUpdatePacket(Lnet/minecraft/world/level/saveddata/maps/MapId;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/network/protocol/Packet;"
			)
	)
	private void addMapOriginData(ItemStack itemStack, CallbackInfo ci, @Local MapItemSavedData data) {
		if (!itemStack.has(ModDataComponents.MAP_CENTER)) {
			itemStack.set(ModDataComponents.MAP_CENTER, new Vector2i(data.centerX, data.centerZ));
		}
	}
    //?}

    @WrapMethod(method = "restoreFrom")
    private void restoreItems(ServerPlayer oldPlayer, boolean restoreAll, Operation<Void> original) {
        if (
                !restoreAll && !oldPlayer.isSpectator() &&
                //~ if <26.1 'get(GameRules.KEEP_INVENTORY)' -> 'getBoolean(GameRules.RULE_KEEPINVENTORY)'
                !level().getGameRules().get(GameRules.KEEP_INVENTORY)
        ) {
            if (oldPlayer.getInventory().hasAnyMatching(stack -> stack.is(ModItems.ATLAS))) {
                getInventory().replaceWith(oldPlayer.getInventory());
            }
        }
        original.call(oldPlayer, restoreAll);
    }
}
