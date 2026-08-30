package me.pajic.mapstitch.platform;

//$ version_util_import
import me.pajic.mapstitch.platform.version.Util26_2;
import me.pajic.mapstitch.worldmap.WorldMapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public interface MultiVersionUtil {

    MultiVersionUtil INSTANCE = /*$ version_util_inst*/ new Util26_2();

    void pushPose(GuiGraphicsExtractor graphics);
    void popPose(GuiGraphicsExtractor graphics);
    void translatePose(GuiGraphicsExtractor graphics, float x, float y, float z);
    void scalePose(GuiGraphicsExtractor graphics, float f, float g, float h);
    void rotateDecor(GuiGraphicsExtractor graphics, byte rot);
    void rotatePlayerMarker(Minecraft mc, GuiGraphicsExtractor graphics);
    int color(int a, int r, int g, int b);
    int as8BitChannel(float f);
    void renderMap(Minecraft mc, GuiGraphicsExtractor graphics, MapId id, MapItemSavedData data, @Nullable WorldMapScreen.GridPos gridPos, boolean minimap);
    void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v, int w, int h, int tw, int th);
    void blitSprite(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int w, int h);
    void blitDecorSprite(Minecraft mc, GuiGraphicsExtractor graphics, TextureAtlasSprite spr);
}
