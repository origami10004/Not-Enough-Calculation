package com.origami10004.necalc.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;

public class guiProductionCalc extends guiCommon {
	// constants for GUI layout
	private static final int GUI_WIDTH			= 194;
	private static final int SLOT_SIZE			= 18;
	private static final int SLOTS_PER_ROW		= 9;

	private static final int PANEL_X_PAD		= 8;   // left margin inside panel
	private static final int RATE_BTN_H			= 10;
	private static final int RATE_BTN_W			= 36;

	// Relative to panel top (gy)
	private static final int TARGET_SEC_Y   = TAB_H + 4;
	private static final int SLOT_GRID_REL  = TARGET_SEC_Y + RATE_BTN_H + 4;

	private static final int TABLE_ROW_H    = 12;
	private static final int TABLE_VIS_ROWS = 7;
	private static final int TABLE_HDR_H    = 12;

	// Other constants
	@Override
	protected int getActiveTab() {
		return 0;
	}


	// instance variables
	private containerProductionCalc calc;
	private InventoryPlayer playerInv;

	private int gx, gy; // top left corner of the whole GUI
	private int gui_height;

	public guiProductionCalc(InventoryPlayer playerInv) {
		this.calc      = new containerProductionCalc(playerInv);
		this.playerInv = playerInv;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		int targetH	= RATE_BTN_H + 4 + calc.getTargetNumRows() * SLOT_SIZE + 16;
		int tableH	= TABLE_HDR_H + TABLE_VIS_ROWS * TABLE_ROW_H + 4;
		int invH	= 10 + 3 * SLOT_SIZE + 4 + SLOT_SIZE + 4;
		this.gui_height = TAB_H + 4 + targetH + 6 + tableH + 6 + invH + 8;

		this.gx = (this.width - GUI_WIDTH) / 2;
		this.gy = (this.height - gui_height) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		
		// main panel
		this.drawRectPanel(this.gx, this.gy + TAB_H, GUI_WIDTH, this.gui_height - TAB_H);

		this.drawTabStrip(this.gx + 4, this.gy);
	}
}
