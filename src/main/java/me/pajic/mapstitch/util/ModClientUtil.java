package me.pajic.mapstitch.util;

import me.pajic.mapstitch.platform.MultiVersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import java.util.ArrayList;
import java.util.List;

//? >=26.1 {
import net.minecraft.client.renderer.state.MapRenderState;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
//?} else {
/*import net.minecraft.world.level.saveddata.maps.MapDecoration;
*///?}

public class ModClientUtil {

    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
    public static void renderDecorations(Minecraft mc, GuiGraphicsExtractor graphics, List<MapRenderState.MapDecorationRenderState> decors, float decorScale) {
        decors.forEach(decor -> {
            byte dx = decor.x/*? <26.1 {*//*()*//*?}*/;
            byte dy = decor.y/*? <26.1 {*//*()*//*?}*/;
            byte dRot = decor.rot/*? <26.1 {*//*()*//*?}*/;
            MultiVersionUtil.INSTANCE.pushPose(graphics);
            MultiVersionUtil.INSTANCE.translatePose(graphics, dx / 2F + 64F, dy / 2F + 64F, -0.02F);
            MultiVersionUtil.INSTANCE.rotateDecor(graphics, dRot);
            MultiVersionUtil.INSTANCE.scalePose(graphics, 4F / decorScale, 4F / decorScale, 3);
            MultiVersionUtil.INSTANCE.translatePose(graphics, -0.125F, 0.125F, 0);
            //~ if <26.1 'decor.atlasSprite' -> 'mc.getMapDecorationTextures().get(decor)'
            MultiVersionUtil.INSTANCE.blitDecorSprite(mc, graphics, decor.atlasSprite);
            MultiVersionUtil.INSTANCE.popPose(graphics);
            Component name = decor.name/*? <26.1 {*//*().orElse(null)*//*?}*/;
            if (name != null) {
                Font font = mc.font;
                float width = font.width(name);
                float scale = Mth.clamp(25F / width, 0.5F, 1) / decorScale;
                MultiVersionUtil.INSTANCE.pushPose(graphics);
                MultiVersionUtil.INSTANCE.translatePose(graphics, dx / 2F + 64F - width * scale / 2F, dy / 2F + 64F + 4F / decorScale, -0.025F);
                MultiVersionUtil.INSTANCE.scalePose(graphics, scale, scale, 1);
                MultiVersionUtil.INSTANCE.translatePose(graphics, 0, 0, -0.1F);
                graphics.text(font, name, 0, 0, -1);
                MultiVersionUtil.INSTANCE.popPose(graphics);
            }
        });
    }

    public static boolean isExplorationMarker(Holder<MapDecorationType> type) {
        return type.value().explorationMapElement() || type.is(MapDecorationTypes.RED_X.unwrapKey().orElseThrow());
    }

    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
    public static boolean isExplorationMap(List<MapRenderState.MapDecorationRenderState> decors) {
        //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
        for (MapRenderState.MapDecorationRenderState decor : decors) {
            //? >=26.1
            Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType();
            //~ if <26.1 'type' -> 'decor.type()'
            if (isExplorationMarker(type)) return true;
        }
        return false;
    }

    //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
    public static List<MapRenderState.MapDecorationRenderState> extractDecors(MapItemSavedData data, MapId id, Minecraft mc, boolean explorationOnly) {
        //~ if <26.1 'MapRenderState.MapDecorationRenderState' -> 'MapDecoration'
        List<MapRenderState.MapDecorationRenderState> decors = new ArrayList<>();
        //? >=26.1 {
        MapRenderState state = new MapRenderState();
        mc.getMapRenderer().extractRenderState(id, data, state);
        state.decorations.forEach(decor -> {
            if (!explorationOnly) decors.add(decor);
            else {
                Holder<MapDecorationType> type = ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType();
                if (isExplorationMarker(type)) decors.add(decor);
            }
        });
        //?} else {
        /*data.getDecorations().forEach(decor -> {
            if (!explorationOnly) decors.add(decor);
            else if (isExplorationMarker(decor.type())) decors.add(decor);
        });
        *///?}
        return decors;
    }
}
