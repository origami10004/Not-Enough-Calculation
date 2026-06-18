package com.origami10004.necalc.gui;

import org.lwjgl.input.Keyboard;

import com.origami10004.necalc.proxy.ClientProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class RateEditHelper {
	private static final int PANEL_W  = 100;
	private static final int PANEL_H  = 28;
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/rate_editor.png");

	private GuiProductionCalc parent;
	private int activeSlot = -1;
	private boolean isOpen = false;
	private double currentRate = 0.0;
	private int panelX = 0;
	private int panelY = 0;
	private GuiTextField rateInputField;

	public RateEditHelper(GuiProductionCalc parent) {
		this.parent = parent;
	}

	public void openSlot(int slotIndex, int slotX, int slotY) {
		this.activeSlot = slotIndex;
		isOpen = true;
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		this.currentRate = ClientProxy.calcState.getTargetSlotRate(slotIndex);
		Minecraft mc = parent.mc;
		this.rateInputField = new GuiTextField(0, mc.fontRenderer, this.panelX + 25, this.panelY + 4, PANEL_W - 29, PANEL_H - 8);
		this.rateInputField.setText(Double.toString(this.currentRate));
		this.rateInputField.setCursorPositionEnd();
		
	}
	public void close() {
		this.activeSlot = -1;
		isOpen = false;
	}
	public boolean isOpen() {
		return isOpen;
	}
	public int getActiveSlot() {
		return activeSlot;
	}
	public double getNewRate() {
		return currentRate;
	}

	public void reInit(int gx, int targetY) {
		if (!isOpen) return;
		int slotX = gx + GuiProductionCalc.INDENT_L + 4 + (this.activeSlot % GuiProductionCalc.SLOTS_PER_ROW) * GuiProductionCalc.SLOT_SIZE;
		int slotY = targetY + (this.activeSlot / GuiProductionCalc.SLOTS_PER_ROW - parent.targetScrollRow) * GuiProductionCalc.SLOT_SIZE;
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		Minecraft mc = parent.mc;
		this.rateInputField = new GuiTextField(0, mc.fontRenderer, this.panelX + 25, this.panelY + 4, PANEL_W - 29, PANEL_H - 8);
		this.rateInputField.setText(Double.toString(this.currentRate));
		this.rateInputField.setCursorPositionEnd();
		this.rateInputField.setFocused(true);
	}

	public void drawOverlay(int mouseX, int mouseY) {
		if (!isOpen) return;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.parent.mc.getTextureManager().bindTexture(BG_TEXTURE);
		this.parent.drawModalRectWithCustomSizedTexture(panelX, panelY, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
		this.rateInputField.drawTextBox();
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isOpen) return false;
		if (mouseX >= panelX && mouseX < panelX + PANEL_W && mouseY >= panelY && mouseY < panelY + PANEL_H) {
			this.rateInputField.mouseClicked(mouseX, mouseY, mouseButton);
			return true;
		}
		return false;
	}

	public boolean keyTyped(GuiProductionCalc parent, char typedChar, int keyCode) {
		if (!isOpen) return false;
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			ClientProxy.calcState.setTargetSlotRate(activeSlot, Double.parseDouble(rateInputField.getText()) / ClientProxy.calcState.getMultiplier());
			close();
			return true;
		}

		if (keyCode == Keyboard.KEY_ESCAPE) {
			close();
			return true;
		}

		if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
			this.rateInputField.textboxKeyTyped(typedChar, keyCode);
		} else if (typedChar == '.' && !rateInputField.getText().contains(".")) {
			this.rateInputField.textboxKeyTyped(typedChar, keyCode);
		}
		return true;
	}

	public void updateCursorCounter() {
		if (isOpen) this.rateInputField.updateCursorCounter();
	}
}