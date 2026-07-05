package me.pajic.mapstitch.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

import java.util.HashSet;
import java.util.Set;

public class ModUtil {
	public static boolean compassRequired = true;
	public static boolean worldMapOpen = false;
	public static final Set<Holder<MapDecorationType>> DECORS_REQUIRING_COMPASS = Set.of(
			MapDecorationTypes.PLAYER,
			MapDecorationTypes.PLAYER_OFF_MAP,
			MapDecorationTypes.PLAYER_OFF_LIMITS
	);

	@SuppressWarnings("DataFlowIssue")
	public static boolean hasCompass(Minecraft mc) {
		if (!compassRequired) return true;
		Set<BundleContents> bundles = new HashSet<>();
		Set<ItemContainerContents> containers = new HashSet<>();
		for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
			ItemStack stack = mc.player.getInventory().getItem(i);
			if (stack.is(Items.COMPASS)) return true;
			if (stack.has(DataComponents.BUNDLE_CONTENTS)) bundles.add(stack.get(DataComponents.BUNDLE_CONTENTS));
			if (stack.has(DataComponents.CONTAINER)) containers.add(stack.get(DataComponents.CONTAINER));
		}
		if (!bundles.isEmpty()) for (BundleContents bundle : bundles) {
			for (ItemStack stack : bundle.items()) if (stack.is(Items.COMPASS)) return true;
		}
		if (!containers.isEmpty()) for (ItemContainerContents container : containers) {
			for (ItemStack stack : container.nonEmptyItems()) if (stack.is(Items.COMPASS)) return true;
		}
		return false;
	}
}
