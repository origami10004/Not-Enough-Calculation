package com.origami10004.necalc.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.input.Keyboard;

public class KeyBindings {

	private static final String CATEGORY = "necalc.key.category";

	public static final KeyBinding OPEN_CALC_GUI = new KeyBinding("necalc.key.open_calc_gui", Keyboard.KEY_EQUALS, CATEGORY);

	public static void registerKeyBindings() {
		// Open calculator GUI
		ClientRegistry.registerKeyBinding(OPEN_CALC_GUI);
	}
}
