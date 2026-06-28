package com.origami10004.necalc.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lwjgl.input.Keyboard;

import com.origami10004.necalc.data.MachineKey;
import com.origami10004.necalc.data.MachineState;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class SpeedEditHelper {
	private static final int PANEL_W  = 100;
	private static final int PANEL_H  = 28;
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/speed_editor.png");

	private GuiManageMachines parent;
	private int activeSlot = -1;
	private boolean isOpen = false;
	private int panelX = 0;
	private int panelY = 0;
	private GuiTextField speedInputField;
	private MachineKey machine;

	public SpeedEditHelper(GuiManageMachines parent) {
		this.parent = parent;
	}

	public void openSlot(int slotIndex, int slotX, int slotY) {
		this.activeSlot = slotIndex;
		isOpen = true;
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		List<Map.Entry<MachineKey, Integer>> entries = new ArrayList<>(MachineState.getMachineSpeeds().entrySet());
		this.machine = entries.get(slotIndex).getKey();
		int currentSpeed = entries.get(slotIndex).getValue();
		this.speedInputField = new GuiTextField(0, parent.mc.fontRenderer, this.panelX + 25, this.panelY + 4, PANEL_W - 29, PANEL_H - 8);
		this.speedInputField.setText(Integer.toString(currentSpeed));
		this.speedInputField.setCursorPositionEnd();
		this.speedInputField.setFocused(true);
	}

	public void close() {
		this.activeSlot = -1;
		isOpen = false;
	}
	public int getActiveSlot() {
		return activeSlot;
	}
	public void reInit(int gx, int tableY) {
		if (!isOpen) return;
		int slotX = gx + 8 + (this.activeSlot % GuiManageMachines.COLS) * 18;
		int slotY = tableY + (this.activeSlot / GuiManageMachines.COLS) * 18;
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		this.speedInputField.x = this.panelX + 25;
		this.speedInputField.y = this.panelY + 4;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public boolean hovered(int mouseX, int mouseY) {
		return isOpen && mouseX >= panelX && mouseX < panelX + PANEL_W && mouseY >= panelY && mouseY < panelY + PANEL_H;
	}

	public void drawOverlay(int mouseX, int mouseY) {
		if (!isOpen) return;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 300); // Ensure the overlay is drawn above other GUI elements
		this.parent.mc.getTextureManager().bindTexture(BG_TEXTURE);
		this.parent.drawModalRectWithCustomSizedTexture(panelX, panelY, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
		this.speedInputField.drawTextBox();
		GlStateManager.popMatrix();
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isOpen) return false;
		if (mouseX >= panelX && mouseX < panelX + PANEL_W && mouseY >= panelY && mouseY < panelY + PANEL_H) {
			this.speedInputField.mouseClicked(mouseX, mouseY, mouseButton);
			return true;
		}
		return this.speedInputField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public boolean keyTyped(char typedChar, int keyCode) {
		if (!isOpen) return false;
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			MachineState.setMachineSpeed(machine, Integer.parseInt(speedInputField.getText()));
			close();
			return true;
		}

		if (keyCode == Keyboard.KEY_ESCAPE) {
			close();
			return true;
		}

		if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
			this.speedInputField.textboxKeyTyped(typedChar, keyCode);
		}
		return true;
	}

	public void updateCursorCounter() {
		if (isOpen) {
			this.speedInputField.updateCursorCounter();
		}
	}

}
