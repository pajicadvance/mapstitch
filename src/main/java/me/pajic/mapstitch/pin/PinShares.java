package me.pajic.mapstitch.pin;

import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import me.pajic.mapstitch.MapStitch;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PinShares {

    public static final int TIMEOUT_SECONDS = 60;
    public static final int MAX_PENDING_PER_SENDER = 10;
    private static final Codec<List<Integer>> REFUNDS_CODEC = Codec.INT.listOf();

    private record PendingShare(
            UUID id, UUID sender, Component senderName, UUID target,
            Component targetName, Pin pin, int paidColor, long expiresAt
    ) {}

    private static final Map<UUID, PendingShare> PENDING = new LinkedHashMap<>();

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MapStitch.MOD_ID)
                .then(Commands.literal("acceptpin")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .executes(ctx -> accept(
                                        ctx.getSource().getPlayerOrException(),
                                        UuidArgument.getUuid(ctx, "id")
                                )))));
    }

    public static void share(ServerPlayer sender, UUID pinId, UUID targetId) {
        MinecraftServer server = sender.level().getServer();
        if (targetId.equals(sender.getUUID())) return;

        ServerPlayer target = server.getPlayerList().getPlayer(targetId);
        if (target == null) {
            sender.sendSystemMessage(error("mapstitch.chat.pin_share.offline"));
            return;
        }
        if (sender.getTeam() != null && !target.isAlliedTo(sender)) return;

        Pin pin = PinStorage.get(sender).stream().filter(p -> p.id().equals(pinId)).findFirst().orElse(null);
        if (pin == null) return;

        long pending = PENDING.values().stream().filter(s -> s.sender().equals(sender.getUUID())).count();
        if (pending >= MAX_PENDING_PER_SENDER) {
            sender.sendSystemMessage(error("mapstitch.chat.pin_share.too_many"));
            return;
        }
        if (PENDING.values().stream().anyMatch(s -> s.sender().equals(sender.getUUID())
                && s.target().equals(targetId) && s.pin().id().equals(pinId))) {
            sender.sendSystemMessage(error("mapstitch.chat.pin_share.duplicate"));
            return;
        }
        Integer paidColor = PinUtil.findSimilarPinColor(sender, pin.color());
        if (paidColor == null || !PinUtil.consumePin(sender, paidColor)) {
            sender.sendSystemMessage(error("mapstitch.chat.pin_share.no_pin"));
            return;
        }

        PendingShare share = new PendingShare(
                UUID.randomUUID(), sender.getUUID(), sender.getDisplayName(),
                targetId, target.getDisplayName(), pin, paidColor,
                System.currentTimeMillis() + TIMEOUT_SECONDS * 1000L
        );
        PENDING.put(share.id(), share);

        target.sendSystemMessage(Component.translatable(
                "mapstitch.chat.pin_share.request", share.senderName(), pinName(pin), acceptLink(share.id())
        ));
        sender.sendSystemMessage(Component.translatable(
                "mapstitch.chat.pin_share.sent", pinName(pin), share.targetName()
        ));
    }

    private static int accept(ServerPlayer player, UUID shareId) {
        PendingShare share = PENDING.get(shareId);
        if (share == null || !share.target().equals(player.getUUID())
                || System.currentTimeMillis() >= share.expiresAt()) {
            player.sendSystemMessage(error("mapstitch.chat.pin_share.invalid"));
            return 0;
        }
        if (PinStorage.get(player).size() >= PinStorage.MAX_PINS) {
            player.sendSystemMessage(error("mapstitch.chat.pin_share.full", PinStorage.MAX_PINS));
            return 0;
        }
        PENDING.remove(shareId);
        Pin p = share.pin();
        PinStorage.add(player, new Pin(UUID.randomUUID(), p.name(), p.color(), p.dimension(), p.x(), p.z()));
        PinStorage.sync(player);
        player.sendSystemMessage(Component.translatable(
                "mapstitch.chat.pin_share.accepted", pinName(p), share.senderName()
        ));

        ServerPlayer sender = player.level().getServer().getPlayerList().getPlayer(share.sender());
        if (sender != null) sender.sendSystemMessage(Component.translatable(
                "mapstitch.chat.pin_share.accepted_sender", share.targetName(), pinName(p)
        ));
        return 1;
    }

    public static void tick(MinecraftServer server) {
        if (PENDING.isEmpty()) return;
        long now = System.currentTimeMillis();
        Iterator<PendingShare> it = PENDING.values().iterator();
        while (it.hasNext()) {
            PendingShare share = it.next();
            if (now < share.expiresAt()) continue;
            it.remove();
            refund(server, share);
            ServerPlayer target = server.getPlayerList().getPlayer(share.target());
            if (target != null) {
                target.sendSystemMessage(Component.translatable(
                        "mapstitch.chat.pin_share.expired",
                        share.senderName()
                ).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        PENDING.values().forEach(share -> refund(server, share));
        PENDING.clear();
    }

    private static void refund(MinecraftServer server, PendingShare share) {
        ServerPlayer sender = server.getPlayerList().getPlayer(share.sender());
        if (sender != null) {
            PinUtil.givePin(sender, share.paidColor());
            sender.sendSystemMessage(Component.translatable(
                    "mapstitch.chat.pin_share.expired_sender", share.targetName(), pinName(share.pin())
            ).withStyle(ChatFormatting.GRAY));
        } else {
            List<Integer> refunds = new ArrayList<>(readRefunds(refundFile(server, share.sender())));
            refunds.add(share.paidColor());
            writeRefunds(refundFile(server, share.sender()), refunds);
        }
    }

    public static void deliverRefunds(ServerPlayer player) {
        Path path = refundFile(player.level().getServer(), player.getUUID());
        if (!Files.exists(path)) return;
        List<Integer> refunds = readRefunds(path);
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't delete {}", path, e);
        }
        if (refunds.isEmpty()) return;
        refunds.forEach(color -> PinUtil.givePin(player, color));
        player.sendSystemMessage(Component.translatable(
                "mapstitch.chat.pin_share.refunded_offline",
                refunds.size()
        ).withStyle(ChatFormatting.GRAY));
    }

    private static Path refundFile(MinecraftServer server, UUID player) {
        return server.getWorldPath(LevelResource.ROOT)
                .resolve("data").resolve(MapStitch.MOD_ID).resolve("pins").resolve("refunds")
                .resolve(player + ".json");
    }

    private static List<Integer> readRefunds(Path path) {
        if (!Files.exists(path)) return List.of();
        try (Reader reader = Files.newBufferedReader(path)) {
            return REFUNDS_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                    .resultOrPartial(MapStitch.LOGGER::warn).orElse(List.of());
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't read pin refunds from {}", path, e);
            return List.of();
        }
    }

    private static void writeRefunds(Path path, List<Integer> refunds) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                writer.write(REFUNDS_CODEC.encodeStart(JsonOps.INSTANCE, refunds).getOrThrow().toString());
            }
        } catch (Exception e) {
            MapStitch.LOGGER.warn("Couldn't save pin refunds to {}", path, e);
        }
    }

    private static Component pinName(Pin pin) {
        MutableComponent name = pin.name().isEmpty()
                ? Component.translatable("mapstitch.gui.worldmap.pin.unnamed")
                : Component.literal(pin.name());
        return Component.literal("■ ").withColor(pin.color() & 0xFFFFFF).append(name.withStyle(ChatFormatting.WHITE));
    }

    private static Component acceptLink(UUID shareId) {
        String command = "/" + MapStitch.MOD_ID + " acceptpin " + shareId;
        Component hover = Component.translatable("mapstitch.chat.pin_share.accept.hover", TIMEOUT_SECONDS);
        return Component.translatable("mapstitch.chat.pin_share.accept").withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true)
                .withClickEvent(clickEvent(command))
                .withHoverEvent(hoverEvent(hover)));
    }

    private static ClickEvent clickEvent(String command) {
        //? >=1.21.5 {
        return new ClickEvent.RunCommand(command);
        //?} else {
        /*return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        *///?}
    }

    private static HoverEvent hoverEvent(Component text) {
        //? >=1.21.5 {
        return new HoverEvent.ShowText(text);
        //?} else {
        /*return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        *///?}
    }

    private static Component error(String key, Object... args) {
        return Component.translatable(key, args).withStyle(ChatFormatting.RED);
    }
}
