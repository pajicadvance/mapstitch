package me.pajic.mapstitch.compat;

//? >=26.1 && fabric {

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import dev.worldgen.remapped.color.RemappedColor;
import dev.worldgen.remapped.duck.MapColorDuck;
import dev.worldgen.remapped.duck.MapDataDuck;
import dev.worldgen.remapped.network.BaseMapUpdatePacket;
import dev.worldgen.remapped.util.RemappedUtils;
import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.nethermap.ImprovedNetherMap;
import me.pajic.mapstitch.util.ModUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.List;

public class RemappedCompat {

    public static void sendMapPackets(MapId id, MapItemSavedData data, ServerPlayer player) {
        List<CustomPacketPayload> packets = MapDataDuck.cast(data).getRemappedPackets(id, player);
        packets.forEach((packet) -> ServerPlayNetworking.send(player, packet));
        if (packets.stream().noneMatch(packet -> packet instanceof BaseMapUpdatePacket)) {
            ModUtil.sendVanillaMapPacket(id, data, player, false);
        }
    }

    public static void updateColors(Level level, Entity entity, MapItemSavedData data) {
        if (level.dimension() == data.dimension && entity instanceof Player player) {
            Registry<RemappedColor> registry = level.registryAccess().lookupOrThrow(RemappedColor.REGISTRY_KEY);
            int scale = 1 << data.scale;
            int centerX = data.centerX;
            int centerZ = data.centerZ;
            int playerImgX = Mth.floor(player.getX() - (double)centerX) / scale + 64;
            int playerImgY = Mth.floor(player.getZ() - (double)centerZ) / scale + 64;
            int radius = 128 / scale;
            if (!ImprovedNetherMap.dimensionAllowed(level.dimension()) && level.dimensionType().hasCeiling()) {
                radius /= 2;
            }

            MapItemSavedData.HoldingPlayer holdingPlayer = data.getHoldingPlayer(player);
            holdingPlayer.step++;
            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();
            boolean foundConsecutiveChanges = false;

            for(int imgX = playerImgX - radius; imgX < playerImgX + radius; ++imgX) {
                if ((imgX & 15) == (holdingPlayer.step & 15) || foundConsecutiveChanges) {
                    foundConsecutiveChanges = false;
                    double previousAverageAreaHeight = 0.0F;

                    for(int imgY = playerImgY - radius - 1; imgY < playerImgY + radius; ++imgY) {
                        if (imgX >= 0 && imgY >= -1 && imgX < 128 && imgY < 128) {
                            int distanceToPlayerSqr = Mth.square(imgX - playerImgX) + Mth.square(imgY - playerImgY);
                            boolean ditherBlack = distanceToPlayerSqr > (radius - 2) * (radius - 2);
                            int averagingAreaMinX = (centerX / scale + imgX - 64) * scale;
                            int averagingAreaMinZ = (centerZ / scale + imgY - 64) * scale;
                            Multiset<MapColorDuck> colorCount = LinkedHashMultiset.create();
                            LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(averagingAreaMinX), SectionPos.blockToSectionCoord(averagingAreaMinZ));
                            if (!chunk.isEmpty()) {
                                int waterDepth = 0;
                                double averageAreaHeight = 0.0F;
                                if (!ImprovedNetherMap.dimensionAllowed(level.dimension()) && level.dimensionType().hasCeiling()) {
                                    int ceilingNoise = averagingAreaMinX + averagingAreaMinZ * 231871;
                                    ceilingNoise = ceilingNoise * ceilingNoise * 31287121 + ceilingNoise * 11;
                                    if ((ceilingNoise >> 20 & 1) == 0) {
                                        colorCount.add(RemappedUtils.get(registry, RemappedUtils.DIRT_BROWN).value(), 10);
                                    } else {
                                        colorCount.add(RemappedUtils.get(registry, RemappedUtils.STONE_GRAY).value(), 100);
                                    }

                                    averageAreaHeight = 100.0F;
                                } else {
                                    for(int averagingAreaDeltaX = 0; averagingAreaDeltaX < scale; ++averagingAreaDeltaX) {
                                        for(int averagingAreaDeltaZ = 0; averagingAreaDeltaZ < scale; ++averagingAreaDeltaZ) {
                                            blockPos.set(averagingAreaMinX + averagingAreaDeltaX, 0, averagingAreaMinZ + averagingAreaDeltaZ);
                                            int originalColumnY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX(), blockPos.getZ()) + 1;
                                            int columnY = level.dimensionType().hasCeiling() && ImprovedNetherMap.dimensionAllowed(level.dimension()) ? switch (MapStitch.CONFIG.netherMap.mode.get()) {
                                                case DYNAMIC -> ImprovedNetherMap.getDynamicHeight(level, blockPos, originalColumnY);
                                                case STATIC -> MapStitch.CONFIG.netherMap.staticModeHeight.get();
                                            } : originalColumnY;
                                            BlockState state;
                                            if (columnY <= level.getMinY()) {
                                                state = Blocks.BEDROCK.defaultBlockState();
                                            } else {
                                                do {
                                                    --columnY;
                                                    blockPos.setY(columnY);
                                                    state = chunk.getBlockState(blockPos);
                                                } while(RemappedUtils.getMatchingColor(level, blockPos, state).getColor() == 0 && columnY > level.getMinY());

                                                if (columnY > level.getMinY() && !state.getFluidState().isEmpty()) {
                                                    int solidY = columnY - 1;
                                                    belowPos.set(blockPos);

                                                    BlockState belowBlock;
                                                    do {
                                                        belowPos.setY(solidY--);
                                                        belowBlock = chunk.getBlockState(belowPos);
                                                        ++waterDepth;
                                                    } while(solidY > level.getMinY() && !belowBlock.getFluidState().isEmpty());

                                                    state = getCorrectStateForFluidBlock(level, state, blockPos);
                                                }
                                            }

                                            data.checkBanners(level, blockPos.getX(), blockPos.getZ());
                                            averageAreaHeight += (double)columnY / (double)(scale * scale);
                                            colorCount.add(RemappedUtils.getMatchingColor(level, blockPos, state));
                                        }
                                    }
                                }

                                waterDepth /= scale * scale;
                                MapColorDuck color = Iterables.getFirst(Multisets.copyHighestCountFirst(colorCount), MapColorDuck.empty());
                                MapColor.Brightness brightness;
                                if (color.useDithering()) {
                                    double diff = (double)waterDepth * 0.1 + (double)((imgX + imgY) & 1) * 0.2;
                                    if (diff < (double)0.5F) {
                                        brightness = MapColor.Brightness.HIGH;
                                    } else if (diff > 0.9) {
                                        brightness = MapColor.Brightness.LOW;
                                    } else {
                                        brightness = MapColor.Brightness.NORMAL;
                                    }
                                } else {
                                    double diff = (averageAreaHeight - previousAverageAreaHeight) * (double)4.0F / (double)(scale + 4) + ((double)((imgX + imgY) & 1) - (double)0.5F) * 0.4;
                                    if (diff > 0.6) {
                                        brightness = MapColor.Brightness.HIGH;
                                    } else if (diff < -0.6) {
                                        brightness = MapColor.Brightness.LOW;
                                    } else {
                                        brightness = MapColor.Brightness.NORMAL;
                                    }
                                }

                                previousAverageAreaHeight = averageAreaHeight;
                                if (imgY >= 0 && distanceToPlayerSqr < radius * radius && (!ditherBlack || ((imgX + imgY) & 1) != 0)) {
                                    foundConsecutiveChanges |= RemappedUtils.putColor(data, imgX, imgY, color, brightness.id);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static BlockState getCorrectStateForFluidBlock(Level level, BlockState blockState, BlockPos blockPos) {
        FluidState fluidState = blockState.getFluidState();
        return !fluidState.isEmpty() && !blockState.isFaceSturdy(level, blockPos, Direction.UP) ? fluidState.createLegacyBlock() : blockState;
    }
}
//?}
