package me.pajic.mapstitch.datapack;

import me.pajic.mapstitch.MapStitch;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class ModDatapacks {

    private static final Set<String> PACKS = new HashSet<>();

    public static void init() {
        if (MapStitch.CONFIG.globetrotter.enabled.get()) PACKS.add("globetrotter");
        if (MapStitch.CONFIG.accessorySlots.atlasSlot.get()) PACKS.add("atlas_slot");
        if (MapStitch.CONFIG.accessorySlots.clockSlot.get()) PACKS.add("clock_slot");
        if (MapStitch.CONFIG.accessorySlots.compassSlot.get()) PACKS.add("compass_slot");
        if (MapStitch.CONFIG.mapRecipe.cheaperRecipe.get()) {
            //~ if <26.1 'cheaper_maps' -> 'cheaper_maps_old' {
            if (MapStitch.CONFIG.mapRecipe.replaceVanillaRecipe.get()) PACKS.add("cheaper_maps");
            else PACKS.add("cheaper_maps_no_replace");
            //~}
        }
    }

    public static Stream<String> getPacks() {
        return PACKS.stream();
    }
}
