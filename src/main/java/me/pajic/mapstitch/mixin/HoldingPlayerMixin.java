package me.pajic.mapstitch.mixin;

import me.pajic.mapstitch.extension.HoldingPlayerExtension;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(MapItemSavedData.HoldingPlayer.class)
public abstract class HoldingPlayerMixin implements HoldingPlayerExtension {

    @Shadow protected abstract MapItemSavedData.MapPatch createPatch();
    //~ if <26.1 && fabric 'this$0' -> 'field_132' {
    @Shadow @Final MapItemSavedData this$0;

    @Override
    public Packet<?> mapstitch$forceUpdatePacket(MapId id) {
        MapItemSavedData.MapPatch patch = createPatch();
        List<MapDecoration> decorations = new ArrayList<>();
        this$0.getDecorations().forEach(decorations::add);
        return new ClientboundMapItemDataPacket(id, this$0.scale, this$0.locked, decorations, patch);
    }
    //~}
}
