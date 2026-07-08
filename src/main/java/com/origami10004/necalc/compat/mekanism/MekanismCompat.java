package com.origami10004.necalc.compat.mekanism;

import net.minecraftforge.fml.common.Loader;

import com.origami10004.necalc.Necalc;

public class MekanismCompat {
	private static boolean loaded = false;

	public static void init() {
		if (Loader.isModLoaded("mekanism")) {
			loaded = true;
			Necalc.logger.info("Mekanism detected, gas support enabled");
		}
	}

	public static boolean isLoaded() {
		return loaded;
	}
}
