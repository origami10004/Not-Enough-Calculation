package com.origami10004.necalc.compat.thaumcraft;

import net.minecraftforge.fml.common.Loader;

import com.origami10004.necalc.Necalc;

public class ThaumcraftCompat {
	private static boolean loaded = false;

	public static void init() {
		if (Loader.isModLoaded("thaumicenergistics")) {
			loaded = true;
			Necalc.logger.info("Thaumic Energistics detected, essentia support enabled");
		}
	}
	public static boolean isLoaded() {
		return loaded;
	}
}
