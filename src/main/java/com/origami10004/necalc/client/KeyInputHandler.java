package com.origami10004.necalc.client;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;

import org.lwjgl.input.Keyboard;

import com.origami10004.necalc.necalc;

public class KeyInputHandler {
    @SubscribeEvent
    public void onKeyInput(net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent event) {
        if (KeyBindings.OPEN_CALC_GUI.isPressed()) {
            // Open the GUI here
            necalc.logger.info("Opening calculator GUI key pressed!");
        }
    }

    @SubscribeEvent
    public void onGuiKeyInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (Keyboard.getEventKeyState()) {
            int pressedKey = Keyboard.getEventKey();
            necalc.logger.info("Key pressed in GUI: " + pressedKey);

            if (KeyBindings.OPEN_CALC_GUI.isActiveAndMatches(pressedKey)) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiContainerCreative) {
                    // Open GUI only when the player is in the inventory screen
                    necalc.logger.info("Opening calculator GUI key pressed in GUI!");
                    event.setCanceled(true); 
                }
                
            }
        }
    }
}
