package com.origami10004.necalc.client;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;

import org.lwjgl.input.Keyboard;

import com.origami10004.necalc.gui.GuiProductionCalc;
import com.origami10004.necalc.proxy.ClientProxy;

public class KeyInputHandler {
	@SubscribeEvent
	public void onKeyInput(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
		if (KeyBindings.OPEN_CALC_GUI.isPressed()) {
			// Open the GUI here
			Minecraft mc = Minecraft.getMinecraft();
			if (ClientProxy.lastOpenedGui != null) {
				mc.displayGuiScreen(ClientProxy.lastOpenedGui);
			} else {
				mc.displayGuiScreen(new GuiProductionCalc(mc.player.inventory));
			}
}
	}

	@SubscribeEvent
	public void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
		if (Keyboard.getEventKeyState()) {
			int pressedKey = Keyboard.getEventKey();

			if (KeyBindings.OPEN_CALC_GUI.isActiveAndMatches(pressedKey)) {
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative) {
					// Open GUI only when the player is in the inventory screen
					if (ClientProxy.lastOpenedGui != null) {
						mc.displayGuiScreen(ClientProxy.lastOpenedGui);
					} else {
						mc.displayGuiScreen(new GuiProductionCalc(mc.player.inventory));
					}
					
					event.setCanceled(true); 
				}
			}
		}
	}
}
