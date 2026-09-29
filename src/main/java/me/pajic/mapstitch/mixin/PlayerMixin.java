package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.compat.ToolPouchCompat;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void tickToolPouchAtlases(CallbackInfo ci) {
        ToolPouchCompat.tick((Player) (Object) this);
    }
}
