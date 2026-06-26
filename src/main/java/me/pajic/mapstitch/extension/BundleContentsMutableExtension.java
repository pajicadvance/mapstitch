package me.pajic.mapstitch.extension;

import net.minecraft.world.item.ItemStack;

public interface BundleContentsMutableExtension {
	void mapstitch$removeOneItemAtIndex(int index);
	ItemStack mapstitch$removeOneStackOrdered(boolean filledMapsFirst);
}
