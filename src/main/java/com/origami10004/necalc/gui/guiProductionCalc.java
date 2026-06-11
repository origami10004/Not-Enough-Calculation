package com.origami10004.necalc.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

public class guiProductionCalc extends guiCommon {
	// constants for GUI layout
	private static final int GUI_WIDTH			= 184;
	private static final int SLOT_SIZE			= 18;
	private static final int SLOTS_PER_ROW		= 8;
	private static final int TARGET_ROWS		= 2;

	private static final int INDENT_L = 8;
	private static final int INDENT_R = 8;
	private static final int INDENT_H = 59;

	private static final int TABLE_ROW_H    = 12;
	private static final int TABLE_VIS_ROWS = 7;
	private static final int TABLE_HDR_H    = 12;

	private static final int SB_W = 14;
	private static final int SB_THUMB_MIN_H = 10;

	// Other constants
	@Override
	protected int getActiveTab() {
		return 0;
	}

	// instance variables
	private containerProductionCalc container;
	private InventoryPlayer playerInv;

	// Target scrolling
	private int targetScrollRow = 0;
	private boolean sbDragging = false;
	private int sbDragStartY = 0;
	private int sbDragStartRow = 0;
	private final rateEditHelper editOverlay = new rateEditHelper();

	private int gx, gy; // top left corner of the whole GUI
	private int gui_height;

	public guiProductionCalc(InventoryPlayer playerInv) {
		this.container = new containerProductionCalc(playerInv);
		this.playerInv = playerInv;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		int targetH	= 4 + TARGET_ROWS * SLOT_SIZE + 16;
		int tableH	= TABLE_HDR_H + TABLE_VIS_ROWS * TABLE_ROW_H + 4;
		int invH	= 10 + 3 * SLOT_SIZE + 4 + SLOT_SIZE + 4;
		this.gui_height = TAB_H + 4 + targetH + 6 + tableH + 6 + invH + 24;

		this.gx = (this.width - GUI_WIDTH) / 2;
		this.gy = (this.height - gui_height) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		int curY = this.gy;

		// main panel
		this.drawRectPanel(this.gx, curY + TAB_H, GUI_WIDTH, this.gui_height - TAB_H);
		this.drawTabStrip(this.gx, curY);
		curY += TAB_H;
		
		// Target panel
		this.fontRenderer.drawString(I18n.format("necalc.gui.target" ), this.gx + 8, curY + 6, 0xFF000000);
		this.drawRectPanelIndent(this.gx + INDENT_L, curY + 16, GUI_WIDTH - INDENT_L - INDENT_R, INDENT_H, 0xFF8B8B8B);
		curY += 16;
		// Rate buttons
		int width = this.fontRenderer.getStringWidth(I18n.format("necalc.gui.rate"));
		this.fontRenderer.drawString(I18n.format("necalc.gui.rate"), this.gx + INDENT_L + 4, curY + 6, 0xFF000000);
		String [] rateLabels = {I18n.format("necalc.gui.rate.minute"), I18n.format("necalc.gui.rate.second"), I18n.format("necalc.gui.rate.ticks")};
		int selectedRate = this.container.getSelectedRate();
		for (int i = 0; i < 3; i++) {
			int color = (i == selectedRate) ? 0xFF6396c6 : 0xFFC6C6C6;
			int accent = (i == selectedRate) ? 0xFF6396c6 : 0xFFFFFFFF;
			this.drawButton(this.gx + INDENT_L + 4 + width + 4 + i * 34, curY + 4, 30, 11, rateLabels[i], color, accent);
		}
		curY += 15;
		// Target table
		int slotY = curY + 4;
		for (int row = 0; row < TARGET_ROWS; row++) {
			int actual = row + this.targetScrollRow;
			for(int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + INDENT_L + 4 + col * SLOT_SIZE;
				this.drawRectPanelIndent(slotX, slotY + row * SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, 0xFF8B8B8B);

				ItemStack curTarget = this.container.getTargetSlot(actual * SLOTS_PER_ROW + col);
				if (!curTarget.isEmpty()) {
					RenderHelper.enableGUIStandardItemLighting();
					this.itemRender.renderItemAndEffectIntoGUI(curTarget, slotX + 1, slotY + 1);
					this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, curTarget, slotX + 1, slotY + 1, Double.toString(this.container.getTargetSlotRate(actual * SLOTS_PER_ROW + col)));
					RenderHelper.disableStandardItemLighting();
				}
			}
		}
		//Target scrollbar
		int sbX = this.gx + INDENT_L + 4 + SLOTS_PER_ROW * SLOT_SIZE + 2;
		this.drawTargetScrollBar(sbX, slotY, TARGET_ROWS * 18, mouseX, mouseY);
		curY += 6 + TARGET_ROWS * SLOT_SIZE;

		// Required rates panel
		this.fontRenderer.drawString(I18n.format("necalc.gui.results"), this.gx + 8, curY + 6, 0xFF000000);
		this.drawRectPanelIndent(this.gx + INDENT_L, curY + 16, GUI_WIDTH - INDENT_L - INDENT_R, TABLE_HDR_H + TABLE_VIS_ROWS * TABLE_ROW_H + 4, 0xFF8B8B8B);

		curY += TABLE_HDR_H + TABLE_VIS_ROWS * TABLE_ROW_H + 18;

		// Player inventory
		int invY = curY;
		this.fontRenderer.drawString(I18n.format("necalc.gui.inventory"), this.gx + 8, invY + 6, 0xFF000000);
		this.drawPlayerInventory(this.gx + 8, invY + 16, this.playerInv);

		// Tooltips
		// Tabs
		curY = this.gy;
		drawTabTooltips(mouseX, mouseY, this.gx, curY);
		
		// Rate editor
		editOverlay.drawOverlay(this, mouseX, mouseY);

		// Inventory tooltips
		this.drawPlayerInventoryTooltips(this.gx + 8, invY + 16, mouseX, mouseY, this.playerInv);
	}

	private void drawTargetScrollBar(int x, int y, int height, int mouseX, int mouseY) {
		this.drawRectPanelIndent(x, y, SB_W, height, 0xFF8B8B8B);
		int totalRows = container.getTargetNumRows();
		int thumbH, thumbY;
		if (totalRows <= TARGET_ROWS) {
			thumbH = height - 1;
			thumbY = y + 1;
		} else {
			thumbH = Math.max(SB_THUMB_MIN_H, (height - 2) * TARGET_ROWS / totalRows);
			thumbY = y + 1 + (height - thumbH) * this.targetScrollRow / (totalRows - TARGET_ROWS);
		}

		drawRect(x + 1, y + 1, x + SB_W - 2, y + thumbH - 1, 0xFFFFFFFF);
		drawRect(x + 2, y + 2, x + SB_W - 1, y + thumbH, 0xFF555555);
		drawRect(x + 2, y + 2, x + SB_W - 2, y + thumbH - 1, 0xFFC6C6C6);
	}
}
