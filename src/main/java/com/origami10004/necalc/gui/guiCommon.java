package com.origami10004.necalc.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

abstract class guiCommon extends GuiScreen {
	protected static final int TAB_H			= 28;
	protected static final int TAB_W			= 28;
	protected static final int TAB_COUNT		= 4;

	private static final ResourceLocation TAB_ICONS = new ResourceLocation("necalc", "textures/gui/tab_icons.png");
	private static final String[] TAB_LABELS = {"tab.main", "tab.flow", "tab.recipes", "tab.add"};
	
	protected abstract int getActiveTab();

	protected void drawRectPanel(int x, int y, int w, int h) {
		assert w > 6 && h > 6 : "Panel too small for borders";
		// Black border
		drawRect(x + 2, y, x + w - 2, y + h, 0xFF000000);
		drawRect(x, y + 2, x + w, y + h - 2, 0xFF000000);
		drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF000000);

		// Lighter top left
		drawRect(x + 1, y + 2, x + 4, y + h - 3, 0xFFFFFFFF);
		drawRect(x + 2, y + 1, x + w - 3, y + 4, 0xFFFFFFFF);

		// Darker bottom right
		drawRect(x + w - 4, y + 3, x + w - 1, y + h - 2, 0xFF555555);
		drawRect(x + 3, y + h - 4, x + w - 2, y + h - 1, 0xFF555555);

		// Fill
		drawRect(x + 3, y + 4, x + w - 4, y + h - 3, 0xFFC6C6C6);
		drawRect(x + 4, y + 3, x + w - 3, y + h - 4, 0xFFC6C6C6);
	}

	protected void drawTabStrip(int gx, int gy) {
		for (int i = 0; i < TAB_COUNT; i++) {
			int tabX = gx + i * (TAB_W + 1);
			drawTab(tabX, gy, i == getActiveTab(), i);
		}
	}

	private void drawTab(int x, int y, boolean active, int iconIndex) {
		int gy = y;
		int color;
		if (!active) {
			gy += 2;
			color = 0xFF8B8B8B;
		} else {
			color = 0xFFC6C6C6;
		}

		// Black border
		drawRect(x, gy + 2, x + TAB_W, y + TAB_H, 0xFF000000);
		drawRect(x + 1, gy + 1, x + TAB_W - 1, gy + 2, 0xFF000000);
		drawRect(x + 2, gy, x + TAB_W - 2, gy + 1, 0xFF000000);

		// Lighter top left
		drawRect(x + 1, gy + 2, x + 4, y + TAB_H, 0xFFFFFFFF);
		drawRect(x + 2, gy + 1, x + TAB_W - 3, gy + 4, 0xFFFFFFFF);

		// Darker right
		drawRect(x + TAB_W - 3, gy + 3, x + TAB_W - 1, y + TAB_H, 0xFF555555);

		// Fill
		drawRect(x + 3, gy + 4, x + TAB_W - 3, y + TAB_H, color);
		drawRect(x + 4, gy + 3, x + TAB_W - 3, y + TAB_H, color);

		if (active) {
			// Spillover
			drawRect(x + 1, gy + 2, x + 3, y + TAB_H + 3, 0xFFFFFFFF);

			drawRect(x + TAB_W - 3, gy + 3, x + TAB_W - 2, y + TAB_H + 3, 0xFF555555);
			drawRect(x + TAB_W - 2, gy + 3, x + TAB_W - 1, y + TAB_H + 2, 0xFF555555);

			drawRect(x + 3, gy + 3, x + TAB_W - 3, y + TAB_H + 3, color);
		}

		this.mc.getTextureManager().bindTexture(TAB_ICONS);
		drawTexturedModalRect(x + 5, y + 6, iconIndex * 16, 0, 16, 16);
	}
}
