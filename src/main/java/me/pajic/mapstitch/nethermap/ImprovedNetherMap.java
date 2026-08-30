package me.pajic.mapstitch.nethermap;

import me.pajic.mapstitch.MapStitch;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ImprovedNetherMap {

    @SuppressWarnings("deprecation")
    public static int getDynamicHeight(Level level, BlockPos.MutableBlockPos blockPos, int originalValue) {
        int originalY = blockPos.getY();
        int top = (level.getHeight() == 256 ? 128 : level.getHeight()) - MapStitch.CONFIG.netherMap.dynamicModeCeilingOffset.get();
        int j = -1;
        for (int i = top; i > 4; i--) {
            blockPos.setY(i);
            if (level.getBlockState(blockPos).isAir()) {
                j = i;
                break;
            }
        }
        if (j != -1) for (int y = j; y > 4; y--) {
            blockPos.setY(y);
            BlockState state = level.getBlockState(blockPos);
            if (state.isSolid() || state.liquid()) {
                blockPos.setY(originalY);
                return j;
            }
        }
        return originalValue;
    }

    public static boolean dimensionAllowed(ResourceKey<Level> key) {
        return MapStitch.CONFIG.netherMap.allowedDimensions.get().contains(key.identifier());
    }
}
