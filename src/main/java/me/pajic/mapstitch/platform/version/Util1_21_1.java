package me.pajic.mapstitch.platform.version;

//? 1.21.1 {

/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import me.pajic.mapstitch.util.ModClientUtil;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.*;

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

    @Override
    public void rotateDegrees(GuiGraphicsExtractor graphics, float degrees) {
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
    }

    @Override
    public int color(int a, int r, int g, int b) {
        return FastColor.ARGB32.color(a, r, g, b);
    }

    @Override
    public int playerMarkerColor(Player player) {
        int color = FastColor.ARGB32.color(255, player.getUUID().hashCode());
        float[] hsb = Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), null);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], 0.9F);
        return (FastColor.ARGB32.alpha(color) << 24) | (rgb & 0x00FFFFFF);
    }

    @Override
    public int as8BitChannel(float f) {
        return FastColor.as8BitChannel(f);
    }

    @Override
    public void renderMap(Minecraft mc, GuiGraphicsExtractor graphics, MapId id, MapItemSavedData data, @Nullable WorldMapScreen.GridPos gp, boolean minimap) {
        if (!minimap) data.getDecorations().forEach(decor -> WorldMapScreen.prepareDecoration(decor, gp));
        ModClientUtil.renderingMinimap = minimap;
        try {
            mc.gameRenderer.getMapRenderer().render(graphics.pose(), graphics.bufferSource(), id, data, true, 15728880);
        } finally {
            ModClientUtil.renderingMinimap = false;
        }
    }

    @Override
    public void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int w, int h, int tw, int th) {
        graphics.blit(texture, x, y, u, v, w, h, tw, th);
    }

    @Override
    public void blitTinted(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int w, int h, int tw, int th, int argb) {
        RenderSystem.setShaderColor((argb >> 16 & 0xFF) / 255F, (argb >> 8 & 0xFF) / 255F, (argb & 0xFF) / 255F, (argb >>> 24) / 255F);
        graphics.blit(texture, x, y, u, v, w, h, tw, th);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
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
