package me.pajic.mapstitch.item;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.compat.OhmegaCompat;
import me.pajic.mapstitch.compat.TrinketsCompat;
import me.pajic.mapstitch.component.ModDataComponents;
import me.pajic.mapstitch.extension.BundleContentsExtension;
import me.pajic.mapstitch.util.CompatFlags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BundleContents;

public class ModItems {
	public static final ResourceKey<Item> ATLAS_KEY = ResourceKey.create(Registries.ITEM, MapStitch.id("atlas"));
	public static final Item ATLAS = makeAtlas(createAtlasProperties());

	private static Item.Properties createAtlasProperties() {
		BundleContents contents = BundleContents.EMPTY;
		((BundleContentsExtension) (Object) contents).mapstitch$setIsAtlas();
		return new Item.Properties()
				.component(DataComponents.BUNDLE_CONTENTS, contents)
				.component(ModDataComponents.ATLAS_FULLNESS, 0)
				.component(ModDataComponents.ATLAS_ACTIVE_MAP_ID, -1)
				.component(ModDataComponents.ATLAS_EJECT_FILLED_MAPS_FIRST, true)
				.stacksTo(1)
				.setId(ModItems.ATLAS_KEY);
	}

	private static AtlasItem makeAtlas(Item.Properties properties) {
		if (CompatFlags.TRINKETS_LOADED) return TrinketsCompat.makeTrinketAtlas(properties);
		if (CompatFlags.OHMEGA_LOADED) return OhmegaCompat.makeOhmegaAtlas(properties);
		return new AtlasItem(properties);
	}

	public static void init() {}
}
