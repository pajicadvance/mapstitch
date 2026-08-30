package me.pajic.mapstitch.mixin.client;

//? >=26.1 {

import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MapRenderState.MapDecorationRenderState.class)
public class MapDecorationRenderStateMixin implements MapDecorationRenderStateExtension {

	@Unique Holder<MapDecorationType> mapstitch$decorationType;

	@Override
	public void mapstitch$setDecorationType(Holder<MapDecorationType> type) {
		mapstitch$decorationType = type;
	}

	@Override
	public Holder<MapDecorationType> mapstitch$getDecorationType() {
		return mapstitch$decorationType;
	}
}
//?}
