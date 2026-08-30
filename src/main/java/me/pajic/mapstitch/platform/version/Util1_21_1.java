package me.pajic.mapstitch.platform.version;

//? 1.21.1 {

/*import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class Util1_21_1 implements MultiVersionUtil {

    @Override
    public void pushPose(GuiGraphicsExtractor graphics) {
        graphics.pose().pushPose();
    }

    @Override
    public void popPose(GuiGraphicsExtractor graphics) {
        graphics.pose().popPose();
    }

    @Override
    public void translatePose(GuiGraphicsExtractor graphics, float x, float y, float z) {
        graphics.pose().translate(x, y, z);
    }

    @Override
    public void scalePose(GuiGraphicsExtractor graphics, float f, float g, float h) {
        graphics.pose().scale(f, g, h);
    }

    @Override
    public void rotateDecor(GuiGraphicsExtractor graphics, byte rot) {
        graphics.pose().mulPose(Axis.ZP.rotationDegrees((float)(rot * 360) / 16.0F));
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void rotatePlayerMarker(Minecraft mc, GuiGraphicsExtractor graphics) {
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(mc.player.getYRot() + 180.0F));
    }

    @Override
    public int color(int a, int r, int g, int b) {
        return FastColor.ARGB32.color(a, r, g, b);
    }

    @Override
    public int as8BitChannel(float f) {
        return FastColor.as8BitChannel(f);
    }

    @Override
    public void renderMap(Minecraft mc, GuiGraphicsExtractor graphics, MapId id, MapItemSavedData data, @Nullable WorldMapScreen.GridPos gp, boolean minimap)  {
        if (!minimap) data.getDecorations().forEach(decor -> WorldMapScreen.prepareDecoration(decor, gp));
        mc.gameRenderer.getMapRenderer().render(graphics.pose(), graphics.bufferSource(), id, data, true, 15728880);
    }

    @Override
    public void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int w, int h, int tw, int th) {
        graphics.blit(texture, x, y, u, v, w, h, tw, th);
    }

    @Override
    public void blitSprite(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int w, int h) {
        graphics.blitSprite(texture, x, y, w, h);
    }

    @Override
    public void blitDecorSprite(Minecraft mc, GuiGraphicsExtractor graphics, TextureAtlasSprite spr) {
        VertexConsumer vc = graphics.bufferSource().getBuffer(RenderType.text(spr.atlasLocation()));
        Matrix4f m4f = graphics.pose().last().pose();
        vc.addVertex(m4f, -1.0F, 1.0F, -0.001F).setColor(-1).setUv(spr.getU0(), spr.getV0()).setLight(15728880);
        vc.addVertex(m4f, 1.0F, 1.0F, -0.001F).setColor(-1).setUv(spr.getU1(), spr.getV0()).setLight(15728880);
        vc.addVertex(m4f, 1.0F, -1.0F, -0.001F).setColor(-1).setUv(spr.getU1(), spr.getV1()).setLight(15728880);
        vc.addVertex(m4f, -1.0F, -1.0F, -0.001F).setColor(-1).setUv(spr.getU0(), spr.getV1()).setLight(15728880);
    }
}
*///?}
