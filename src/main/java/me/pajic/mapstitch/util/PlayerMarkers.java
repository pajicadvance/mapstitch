package me.pajic.mapstitch.util;

import me.pajic.mapstitch.platform.MultiVersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.List;

public final class PlayerMarkers {

    private static final Identifier SPRITE = Identifier.withDefaultNamespace("textures/map/decorations/player.png");

    public static List<AbstractClientPlayer> otherPlayers(Minecraft mc) {
        if (mc.level == null || mc.player == null) return List.of();
        PlayerTeam ownTeam = mc.player.getTeam();
        List<AbstractClientPlayer> list = new ArrayList<>();
        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player || p.isRemoved()) continue;
            if (ownTeam != null && !mc.player.isAlliedTo(p)) continue;
            if (p.isSpectator() && !mc.player.isSpectator()) continue;
            if (p.isInvisibleTo(mc.player)) continue;
            list.add(p);
        }
        return list;
    }

    public static float partialTick(Minecraft mc) {
        //~ if <26.1 'getDeltaTracker' -> 'getTimer'
        return mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }

    public static double lerpX(Player p, float pt) {
        return Mth.lerp(pt, p.xo, p.getX());
    }

    public static double lerpZ(Player p, float pt) {
        return Mth.lerp(pt, p.zo, p.getZ());
    }

    public static float lerpYaw(Player p, float pt) {
        return Mth.rotLerp(pt, p.yRotO, p.getYRot());
    }

    public static int color(Player p) {
        int c = MultiVersionUtil.INSTANCE.playerMarkerColor(p);
        return (c >>> 24) == 0 ? c | 0xFF000000 : c;
    }

    public static void draw(GuiGraphicsExtractor graphics, float x, float y, float yaw, int argb) {
        MultiVersionUtil.INSTANCE.pushPose(graphics);
        MultiVersionUtil.INSTANCE.translatePose(graphics, x, y, 0);
        MultiVersionUtil.INSTANCE.rotateDegrees(graphics, yaw + 180.0F);
        MultiVersionUtil.INSTANCE.blitTinted(graphics, SPRITE, -4, -4, 0, 0, 8, 8, 8, 8, argb);
        MultiVersionUtil.INSTANCE.popPose(graphics);
    }
}
