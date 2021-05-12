package me.superckl.upm.util;

import java.util.HashSet;
import java.util.Set;

import me.superckl.upm.LogHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class OneTimeWarnings {

	private static final Set<ResourceLocation> INJECTION_CHECK = new HashSet<>();

	public static void injectionCheckActual(final TileEntity entity) {
		if(!OneTimeWarnings.INJECTION_CHECK.contains(entity.getType().getRegistryName())) {
			LogHelper.warn(String.format("Error performing injection checking! Tile entity %s at"
					+ " %s accepted/provided energy but the storage change did not match! This"
					+ " tile entity requires special handling -- please report this issue to the UPM issue tracker.",
					entity.getType().getRegistryName(), entity.getBlockPos()));
			OneTimeWarnings.INJECTION_CHECK.add(entity.getType().getRegistryName());
		}
	}

}
