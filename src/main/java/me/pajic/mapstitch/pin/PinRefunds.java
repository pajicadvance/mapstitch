package me.pajic.mapstitch.pin;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.pajic.mapstitch.MapStitch;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PinRefunds {

    private static final Codec<List<Integer>> CODEC = Codec.INT.listOf();
    private static boolean lastItemRequired = true;

    public static boolean refund(MinecraftServer server, UUID playerId, int color) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        if (player != null && PinUtil.itemRequired()) {
            PinUtil.givePin(player, color);
            return true;
        }
        Path path = file(server, playerId);
        List<Integer> refunds = read(path);
        if (refunds == null) {
            backupBroken(path);
            refunds = List.of();
        }
        List<Integer> updated = new ArrayList<>(refunds);
        updated.add(color);
        write(path, updated);
        return false;
    }

    public static void deliver(ServerPlayer player) {
        if (!PinUtil.itemRequired()) return;
        Path path = file(player.level().getServer(), player.getUUID());
        if (!Files.exists(path)) return;
        List<Integer> refunds = read(path);
        if (refunds == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't delete {}", path, e);
            return;
        }
        if (refunds.isEmpty()) return;
        refunds.forEach(color -> PinUtil.givePin(player, color));
        player.sendSystemMessage(Component.translatable(
                "mapstitch.chat.pin.refunds_delivered", refunds.size()
        ).withStyle(ChatFormatting.GRAY));
    }

    public static void tick(MinecraftServer server) {
        boolean required = PinUtil.itemRequired();
        if (required && !lastItemRequired) server.getPlayerList().getPlayers().forEach(PinRefunds::deliver);
        lastItemRequired = required;
    }

    private static Path file(MinecraftServer server, UUID player) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(MapStitch.MOD_ID).resolve("pins").resolve("refunds")
                .resolve(player + ".json");
    }

    private static @Nullable List<Integer> read(Path path) {
        if (!Files.exists(path)) return List.of();
        try (Reader reader = Files.newBufferedReader(path)) {
            return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .resultOrPartial(MapStitch.LOGGER::warn).orElse(null);
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't read pin refunds from {}", path, e);
            return null;
        }
    }

    private static void write(Path path, List<Integer> refunds) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                writer.write(CODEC.encodeStart(JsonOps.INSTANCE, refunds).getOrThrow().toString());
            }
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't save pin refunds to {}", path, e);
        }
    }

    private static void backupBroken(Path path) {
        try {
            Files.move(path, path.resolveSibling(path.getFileName() + ".broken"), StandardCopyOption.REPLACE_EXISTING);
            MapStitch.LOGGER.warn("Moved unreadable pin refund file to {}.broken", path);
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't back up unreadable pin refund file {}", path, e);
        }
    }
}
