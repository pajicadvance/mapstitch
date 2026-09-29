package qa;

import java.nio.file.*;
import me.pajic.mapstitch.compat.ToolPouchCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class SmokeQa implements ModInitializer {
    int ticks;
    @Override public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                if (!ToolPouchCompat.getAtlases(null).isEmpty()) throw new AssertionError("unexpected pouch atlas");
                Files.writeString(Path.of("result.txt"), "PASS dedicated server starts without Tool Pouch; optional API remains loadable\n");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> { if (++ticks >= 3) server.halt(false); });
    }
}
