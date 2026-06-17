package com.origami10004.necalc.gui;

public class RateEditHelper {
	private int activeSlot = -1;

	public void openSlot(int slotIndex) {
		this.activeSlot = slotIndex;
	}
	public void close() {
		this.activeSlot = -1;
	}

	public void drawOverlay(GuiProductionCalc parent, int mouseX, int mouseY) {
		// Implementation for drawing rate edit overlay
	}

	public void mouseClicked(GuiProductionCalc parent, int mouseX, int mouseY, int mouseButton) {
		// Implementation for handling mouse clicks on the rate edit overlay
	}
}