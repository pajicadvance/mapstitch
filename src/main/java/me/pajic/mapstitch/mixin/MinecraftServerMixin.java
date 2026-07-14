package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.gamerule.ModGameRules;
import me.pajic.mapstitch.item.AtlasItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow @Final protected WorldData worldData;

    @Inject(
            method = "createLevels",
            at = @At("TAIL")
    )
    private void syncMaxAtlasSize(CallbackInfo ci) {
        AtlasItem.setMaxSize(worldData.getGameRules().getInt(ModGameRules.MAX_ATLAS_ITEMS));
    }
}
