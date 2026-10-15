package me.pajic.mapstitch.platform.version;

//? 26.1.2 {

/*import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import me.pajic.mapstitch.util.ModClientUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class Util26_1_2 implements MultiVersionUtil {

    private static final MapRenderState STATE = new MapRenderState();

    @Override
    public void pushPose(GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
    }

    @Override
    public void popPose(GuiGraphicsExtractor graphics) {
        graphics.pose().popMatrix();
    }

    @Override
    public void translatePose(GuiGraphicsExtractor graphics, float x, float y, float z) {
        graphics.pose().translate(x, y);
    }

    @Override
    public void scalePose(GuiGraphicsExtractor graphics, float f, float g, float h) {
        graphics.pose().scale(f, g);
    }

    @Override
    public void rotateDecor(GuiGraphicsExtractor graphics, byte rot) {
        graphics.pose().rotate((float) (Math.PI / 180.0) * rot * 360F / 16F);
    }

    @Override
    public void rotateDegrees(GuiGraphicsExtractor graphics, float degrees) {
        graphics.pose().rotate(Mth.DEG_TO_RAD * degrees);
    }

    @Override
    public int color(int a, int r, int g, int b) {
        return ARGB.color(a, r, g, b);
    }

    @Override
    public int playerMarkerColor(Player player) {
        return ARGB.setBrightness(ARGB.color(255, player.getUUID().hashCode()), 0.9F);
    }

    @Override
    public int as8BitChannel(float f) {
        return ARGB.as8BitChannel(f);
    }

    @Override
    public void renderMap(Minecraft mc, GuiGraphicsExtractor graphics, MapId id, MapItemSavedData data, @Nullable WorldMapScreen.GridPos gp, boolean minimap) {
        MapRenderState state = minimap ? STATE : new MapRenderState();
        mc.getMapRenderer().extractRenderState(id, data, state);
        if (minimap) state.decorations.removeIf(decor -> ModClientUtil.DECORS_REQUIRING_COMPASS.contains(
                ((MapDecorationRenderStateExtension) decor).mapstitch$getDecorationType()
        ));
        state.decorations.forEach(decor -> {
            if (minimap) decor.renderOnFrame = true;
            else if (gp != null) WorldMapScreen.prepareDecoration(decor, gp);
        });
        graphics.map(state);
    }

    @Override
    public void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int w, int h, int tw, int th) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, tw, th);
    }

    @Override
    public void blitTinted(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int w, int h, int tw, int th, int argb) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, w, h, tw, th, argb);
    }

    @Override
    public void blitSprite(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int w, int h) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, w, h);
    }

    @Override
    public void blitDecorSprite(Minecraft mc, GuiGraphicsExtractor graphics, TextureAtlasSprite spr) {
        if (spr != null) {
            AbstractTexture tex = mc.getTextureManager().getTexture(spr.atlasLocation());
            graphics.blit(
                    tex.getTextureView(), tex.getSampler(),
                    -1, -1,
                    1, 1,
                    spr.getU0(), spr.getU1(),
                    spr.getV1(), spr.getV0()
            );
        }
    }
}
*///?}
