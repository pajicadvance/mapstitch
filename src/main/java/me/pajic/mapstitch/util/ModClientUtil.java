package me.pajic.mapstitch.util;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.AccessoryUtil;
import me.pajic.mapstitch.platform.MultiVersionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//? >=26.1 {
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.item.ItemStackTemplate;
import me.pajic.mapstitch.extension.MapDecorationRenderStateExtension;
//?} else {
/*import net.minecraft.world.level.saveddata.maps.MapDecoration;
*///?}

public class ModClientUtil {

    public static final Set<Holder<MapDecorationType>> DECORS_REQUIRING_COMPASS = Set.of(
            MapDecorationTypes.PLAYER,
            MapDecorationTypes.PLAYER_OFF_MAP,
            MapDecorationTypes.PLAYER_OFF_LIMITS
    );
    public static boolean worldMapOpen = false;

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

    public static boolean hasCompass(Minecraft mc, String context) {
        return hasItem(mc, Items.COMPASS, context, MapStitch.CONFIG.itemRequirements.compass);
    }

    public static boolean hasClock(Minecraft mc, String context) {
        return hasItem(mc, Items.CLOCK, context, MapStitch.CONFIG.itemRequirements.clock);
    }

    public static boolean hasItem(Minecraft mc, Item item, String context, List<String> reqs) {
        if (mc.player == null) return false;
        if (!reqs.contains(context)) return true;
        List<String> locations = MapStitch.CONFIG.itemRequirements.compassAndClockScan;
        if (locations.contains("accessories") && AccessoryUtil.INSTANCE != null && AccessoryUtil.INSTANCE.hasItem(item, mc.player)) return true;
        if (locations.contains("mainHand") && mc.player.getMainHandItem().is(item)) return true;
        Set<BundleContents> bundles = new HashSet<>();
        Set<ItemContainerContents> containers = new HashSet<>();
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                if (locations.contains("offhand") && i == 40) return true;
                if (locations.contains("hotbar") && i < 9) return true;
                if (locations.contains("inventory") && i >= 9 && i < 36) return true;
            }
            if (locations.contains("bundles") && stack.has(DataComponents.BUNDLE_CONTENTS)) {
                bundles.add(stack.get(DataComponents.BUNDLE_CONTENTS));
            }
            if (locations.contains("containerItems") && stack.has(DataComponents.CONTAINER)) {
                containers.add(stack.get(DataComponents.CONTAINER));
            }
        }
        if (!bundles.isEmpty()) for (BundleContents bundle : bundles) {
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate stack : bundle.items()) if (stack.is(item)) return true;
        }
        if (!containers.isEmpty()) for (ItemContainerContents container : containers) {
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate stack : container.nonEmptyItems()) if (stack.is(item)) return true;
        }
        return false;
    }

    public static ItemStack getFirstItem(Minecraft mc, Item item) {
        if (mc.player == null) return ItemStack.EMPTY;
        List<String> locations = MapStitch.CONFIG.itemRequirements.minimapAtlasScan;
        if (locations.contains("accessories") && AccessoryUtil.INSTANCE != null) {
            ItemStack accessory = AccessoryUtil.INSTANCE.getFirstItem(item, mc.player);
            if (accessory.is(item)) return accessory;
        }
        if (locations.contains("mainHand")) {
            ItemStack mainHand = mc.player.getMainHandItem();
            if (mainHand.is(item)) return mainHand;
        }
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.is(item)) {
                if (locations.contains("offhand") && i == 40) return stack;
                if (locations.contains("hotbar") && i < 9) return stack;
                if (locations.contains("inventory") && i >= 9 && i < 36) return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
