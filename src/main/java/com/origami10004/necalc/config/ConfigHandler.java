package com.origami10004.necalc.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public final class ConfigHandler {
	// TODO switch to @Config
	public static boolean enableItem = false;

	private ConfigHandler() {
	}

	public static void load(File configFile) {
		Configuration configuration = new Configuration(configFile);
		configuration.load();

		enableItem = configuration.getBoolean(
			"enableItem",
			Configuration.CATEGORY_GENERAL,
			false,
			"Register the calculator item. Leave this off for client-only installs."
		);

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}
}