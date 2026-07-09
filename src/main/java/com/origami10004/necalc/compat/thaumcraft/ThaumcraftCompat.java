package com.origami10004.necalc.compat.thaumcraft;

import net.minecraftforge.fml.common.Loader;

import com.origami10004.necalc.Necalc;

public class ThaumcraftCompat {
	private static boolean loaded = false;

	public static void init() {
		if (Loader.isModLoaded("thaumcraft")) {
			loaded = true;
			Necalc.logger.info("Thaumcraft detected, essentia support enabled");
		}
	}
	public static boolean isLoaded() {
		return loaded;
	}

	public static boolean isEssentiaStack(Object obj) {
		if (!isLoaded()) return false;
		return obj instanceof thaumcraft.api.aspects.Aspect;
	}
}
