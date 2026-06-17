package com.origami10004.necalc.config;

import com.origami10004.necalc.Necalc;

import net.minecraftforge.common.config.Config;

@Config(modid = Necalc.MODID, name = Necalc.MODID)
public final class ConfigHandler {

	@Config.Comment("Enable this if the mod is installed only on the client side.")
	@Config.Name("Client-side only: ")
	@Config.RequiresMcRestart
	public static boolean clientOnly = true;
}