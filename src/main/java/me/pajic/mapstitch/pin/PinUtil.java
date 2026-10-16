package me.pajic.mapstitch.pin;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.extension.BundleContentsMutableExtension;
import me.pajic.mapstitch.item.ModItems;
import me.pajic.mapstitch.util.ModUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemContainerContents;

//? >=26.1
import net.minecraft.world.item.ItemStackTemplate;
//? >26.2
import net.minecraft.util.Prediction;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PinUtil {

    private static final int CONTAINER_SIZE = 27;
    public static final int DEFAULT_COLOR = 0xF9FFFE;
    public static final double SIMILAR_COLOR_THRESHOLD = 0.1;

    public static boolean itemRequired() {
        return MapStitch.CONFIG.atlasPins.item.get();
    }

    public static int colorOf(ItemStack stack) {
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        return dyed != null ? dyed.rgb() & 0xFFFFFF : DEFAULT_COLOR;
    }

    public static boolean isPin(ItemStack stack, int color) {
        return stack.is(ModItems.ATLAS_PIN) && colorOf(stack) == color;
    }

    public static ItemStack createPin(int color) {
        ItemStack stack = new ItemStack(ModItems.ATLAS_PIN);
        setColor(stack, color);
        return stack;
    }

    public static Map<Integer, Integer> collectPinColors(Player player) {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) collect(inv.getItem(i), counts, true);
        return counts;
    }

    public static @Nullable Integer findSimilarPinColor(Player player, int target) {
        Integer best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int color : collectPinColors(player).keySet()) {
            double distance = colorDistance(color, target);
            if (distance <= SIMILAR_COLOR_THRESHOLD && distance < bestDistance) {
                best = color;
                bestDistance = distance;
            }
        }
        return best;
    }

    public static int countSimilarPins(Player player, int target) {
        int total = 0;
        for (Map.Entry<Integer, Integer> e : collectPinColors(player).entrySet()) {
            if (isSimilarColor(e.getKey(), target)) total += e.getValue();
        }
        return total;
    }

    private static void collect(ItemStack stack, Map<Integer, Integer> counts, boolean allowContainers) {
        if (stack.isEmpty() || stack.is(ModItems.ATLAS)) return;
        if (stack.is(ModItems.ATLAS_PIN)) {
            counts.merge(colorOf(stack), stack.getCount(), Integer::sum);
            return;
        }
        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate entry : bundle.items()) {
                ItemStack s = entry/*? >=26.1 {*/.create()/*?}*/;
                if (s.is(ModItems.ATLAS_PIN)) counts.merge(colorOf(s), s.getCount(), Integer::sum);
            }
        }
        if (allowContainers) {
            ItemContainerContents container = stack.get(DataComponents.CONTAINER);
            //~ if <26.1 'nonEmptyItemCopyStream' -> 'nonEmptyStream'
            if (container != null) container.nonEmptyItemCopyStream().forEach(s -> collect(s, counts, false));
        }
    }

    public static int countPins(Player player, int color) {
        return collectPinColors(player).getOrDefault(color, 0);
    }

    public static boolean consumePin(Player player, int color) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isPin(stack, color)) {
                stack.shrink(1);
                inv.setChanged();
                return true;
            }
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (removeFromContainer(inv.getItem(i), color, true)) {
                inv.setChanged();
                return true;
            }
        }
        return false;
    }

    private static boolean removeFromContainer(ItemStack stack, int color, boolean allowContainers) {
        if (stack.isEmpty() || stack.is(ModItems.ATLAS)) return false;

        BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null) {
            int index = -1;
            int i = 0;
            //~ if <26.1 'ItemStackTemplate' -> 'ItemStack'
            for (ItemStackTemplate entry : bundle.items()) {
                if (isPin(entry/*? >=26.1 {*/.create()/*?}*/, color)) {
                    index = i;
                    break;
                }
                i++;
            }
            if (index != -1) {
                BundleContents.Mutable mutable = ModUtil.toMutable(bundle);
                ItemStack removed = ((BundleContentsMutableExtension) mutable).mapstitch$removeOneItemAtIndex(index);
                if (!removed.isEmpty()) {
                    stack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                    return true;
                }
            }
        }

        if (allowContainers) {
            ItemContainerContents container = stack.get(DataComponents.CONTAINER);
            if (container != null) {
                NonNullList<ItemStack> slots = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
                container.copyInto(slots);
                for (ItemStack s : slots) {
                    if (isPin(s, color)) {
                        s.shrink(1);
                        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
                        return true;
                    }
                }
                for (ItemStack s : slots) {
                    if (removeFromContainer(s, color, false)) {
                        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(slots));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void givePin(Player player, int color) {
        ItemStack stack = createPin(color);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false
                    //? >26.2
                    , Prediction.SERVER_ONLY
            );
        }
    }

    public static void setColor(ItemStack stack, int color) {
        color &= 0xFFFFFF;
        if (color  == DEFAULT_COLOR) stack.remove(DataComponents.DYED_COLOR);
        else stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color/*? <1.21.5 {*//*, true*//*?}*/));
    }

    public static int mixColor(ItemStack target, List<DyeColor> dyes) {
        int r = 0, g = 0, b = 0, maxSum = 0, count = 0;
        DyedItemColor existing = target.get(DataComponents.DYED_COLOR);
        if (existing != null) {
            int c = existing.rgb();
            int cr = c >> 16 & 0xFF, cg = c >> 8 & 0xFF, cb = c & 0xFF;
            r += cr; g += cg; b += cb;
            maxSum += Math.max(cr, Math.max(cg, cb));
            count++;
        }
        for (DyeColor dye : dyes) {
            int c = dye.getTextureDiffuseColor();
            int cr = c >> 16 & 0xFF, cg = c >> 8 & 0xFF, cb = c & 0xFF;
            r += cr; g += cg; b += cb;
            maxSum += Math.max(cr, Math.max(cg, cb));
            count++;
        }
        int ar = r / count, ag = g / count, ab = b / count;
        float avgMax = (float) maxSum / count;
        float max = Math.max(ar, Math.max(ag, ab));
        if (max > 0) {
            ar = (int) (ar * avgMax / max);
            ag = (int) (ag * avgMax / max);
            ab = (int) (ab * avgMax / max);
        }
        return ar << 16 | ag << 8 | ab;
    }

    public static boolean isSimilarColor(int a, int b) {
        return colorDistance(a, b) <= SIMILAR_COLOR_THRESHOLD;
    }

    public static double colorDistance(int a, int b) {
        if ((a & 0xFFFFFF) == (b & 0xFFFFFF)) return 0;
        double[] la = toOklab(a), lb = toOklab(b);
        double dl = la[0] - lb[0], da = la[1] - lb[1], db = la[2] - lb[2];
        return Math.sqrt(dl * dl + da * da + db * db);
    }

    private static double[] toOklab(int rgb) {
        double r = srgbToLinear(rgb >> 16 & 0xFF);
        double g = srgbToLinear(rgb >> 8 & 0xFF);
        double b = srgbToLinear(rgb & 0xFF);
        double l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
        double m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
        double s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
        return new double[] {
                0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s,
                1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s,
                0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
        };
    }

    private static double srgbToLinear(int channel) {
        double c = channel / 255.0;
        return c <= 0.04045 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
