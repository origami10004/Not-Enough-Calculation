package com.origami10004.necalc.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

import com.origami10004.necalc.data.ProductionStep;

import java.util.List;

public class GuiProductionCalc extends GuiCommon {
	// constants for GUI layout
	private static final int GUI_WIDTH			= 184;
	private static final int SLOT_SIZE			= 18;
	private static final int SLOTS_PER_ROW		= 8;
	private static final int TARGET_ROWS		= 2;

	private static final int INDENT_L = 8;
	private static final int INDENT_R = 8;
	private static final int INDENT_H = 59;

	private static final int SB_W = 14;
	private static final int SB_THUMB_MIN_H = 10;

	private static final int TABLE_ROW_H    = 22;
	private static final int TABLE_VIS_ROWS = 5;

	private static final int HIDE_SIZE = 12;

	// Other constants
	@Override
	protected int getActiveTab() {
		return 0;
	}

	// instance variables
	private ContainerProductionCalc container;
	private InventoryPlayer playerInv;

	// Target scrolling
	private int targetScrollRow = 0;
	private int prodScrollRow = 0;
	private final RateEditHelper editOverlay = new RateEditHelper();

	// Recipe hovering
	private int hoveredRecipeRow = -1;
	private int hoveredRecipeRowTicks = 0;

	private int gx, gy; // top left corner of the whole GUI
	private int gui_height;

	public GuiProductionCalc(InventoryPlayer playerInv) {
		this.container = new ContainerProductionCalc(playerInv);
		this.playerInv = playerInv;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		int targetH	= 4 + TARGET_ROWS * SLOT_SIZE + 16;
		int tableH	= TABLE_VIS_ROWS * TABLE_ROW_H + 4;
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
		this.fontRenderer.drawString(I18n.format("necalc.gui.results"), this.gx + 8, curY + 7, 0xFF000000);
		int unhideX = this.gx + GUI_WIDTH - INDENT_R - HIDE_SIZE - 2;
		int unhideY = curY + 4;
		this.drawEyeButton(unhideX, unhideY, false);
		// Rates table
		int prodY = curY + 18;
		this.drawProdTable(prodY, mouseX, mouseY);




		curY += TABLE_VIS_ROWS * TABLE_ROW_H + 20;

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

		// Unhide tooltip
		if (mouseX >= unhideX && mouseX < unhideX + HIDE_SIZE && mouseY >= unhideY && mouseY < unhideY + HIDE_SIZE) {
			String text;
			if (this.container.hasHidden()) {
				text = I18n.format("necalc.gui.show.some", this.container.getHiddenCount());
			} else {
				text = I18n.format("necalc.gui.show.none");
			}
			this.drawHoveringText(text, mouseX, mouseY);
		}

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

		this.drawRectPanelOutdent(x + 1, y + 1, SB_W - 2, thumbH, 0xFFC6C6C6);
	}

	private void drawEyeButton(int x, int y, boolean crossed) {
		int cx = x + HIDE_SIZE / 2;
		int cy = y + HIDE_SIZE / 2;

		int bg = this.container.hasHidden() ? 0xFFC6C6C6 : 0xFF999999;
		int lineColor = this.container.hasHidden() ? 0xFF000000 : 0xFF8B8B8B;

		this.drawRectPanelOutdent(x, y, HIDE_SIZE, HIDE_SIZE, bg);

		drawRect(cx - 2, cy - 2, cx + 2, cy + 3, lineColor);
		drawRect(cx - 3, cy - 1, cx + 3, cy + 2, lineColor);
		drawRect(cx - 4, cy, cx + 4, cy + 1, lineColor);
		drawRect(cx - 2, cy - 1, cx + 2, cy + 2, bg);
		drawRect(cx - 3, cy, cx + 3, cy + 1, bg);
		drawRect(cx - 1, cy, cx + 1, cy + 1, lineColor);
	}

	private void drawProdTable(int y, int mouseX, int mouseY) {
		List<ProductionStep> visible = this.container.getVisibleRecipes();
		int maxScroll = Math.max(0, visible.size() - TABLE_VIS_ROWS);
		this.prodScrollRow = Math.max(0, Math.min(this.prodScrollRow, maxScroll));

		int rowX = this.gx + INDENT_L + 1;
		int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;

		// Scrollbar if needed and adjust row width accordingly
		if (visible.size() > TABLE_VIS_ROWS) {
			rowW -= SB_W + 2;
		}
		
		this.drawRectPanelIndent(this.gx + INDENT_L, y, GUI_WIDTH - INDENT_L - INDENT_R, TABLE_VIS_ROWS * TABLE_ROW_H + 4, 0xFF8B8B8B);

		// Production steps
		for(int i = 0; i < TABLE_VIS_ROWS; i++) {
			int idx = i + this.prodScrollRow;
			int rowY = 1 + y + i * TABLE_ROW_H;

			if (idx >= visible.size()) break; // no more steps to show

			ProductionStep step = visible.get(idx);
			boolean rowHovered = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + TABLE_ROW_H;
			if (rowHovered) {
				// Update hovered row for tooltip timer
				if (hoveredRecipeRow != idx) {
					hoveredRecipeRow = idx;
					hoveredRecipeRowTicks = 0;
				}
			} else {
				if (hoveredRecipeRow == idx) {
					hoveredRecipeRow = -1;
					hoveredRecipeRowTicks = 0;
				}
			}

			int rowBg = rowHovered ? 0xFFA8B8D8 : (step.isUnknown() ? 0xFFDCCE36 : 0xFFC6C6C6);
			drawRectPanelOutdent(rowX, rowY, rowW, TABLE_ROW_H, rowBg);

			// Primary input and rate
			ItemStack input = step.getPrimaryInput();
			int iconY = rowY + 2;
			if (input != null) {
				RenderHelper.enableGUIStandardItemLighting();
				this.itemRender.renderItemAndEffectIntoGUI(input, rowX + 2, iconY);
				this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, input, rowX + 2, iconY, String.format("%.1f", this.container.getMultiplier() * step.getPrimaryInputRate()));
				RenderHelper.disableStandardItemLighting();
			}
			// More inputs
			if (step.getInputs().size() > 1) {
				this.fontRenderer.drawString("+" + (step.getInputs().size() - 1), rowX + 18, iconY + 4, 0xFF000000);
			}

			// Arrow
			drawArrow(rowX + 28, rowY + TABLE_ROW_H / 2 - 1, 0xFFAAAAAA);

			// Machine (machine cannot be empty)
			int machineIconX = rowX + 40;
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemAndEffectIntoGUI(step.getMachine(), machineIconX, iconY);
			this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, step.getMachine(), machineIconX, iconY, String.format("%.1f", step.getMachineCount()));
			RenderHelper.disableStandardItemLighting();
			
			// Arrow
			drawArrow(rowX + 60, rowY + TABLE_ROW_H / 2 - 1, 0xFFAAAAAA);

			// Primary output and rate (output cannot be empty)
			int outputIconX = rowX + 72;
			ItemStack output = step.getPrimaryOutput();
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemAndEffectIntoGUI(output, outputIconX, iconY);
			this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, output, outputIconX, iconY, String.format("%.1f", this.container.getMultiplier() * step.getPrimaryOutputRate()));
			RenderHelper.disableStandardItemLighting();
			// More outputs
			if (step.getOutputs().size() > 1) {
				this.fontRenderer.drawString("+" + (step.getOutputs().size() - 1), rowX + 82, iconY + 4, 0xFF000000);
			}

			// Hide button
			int eyeX = rowX + rowW - 14;
			int eyeY = rowY + (TABLE_ROW_H - 12) / 2;
			drawEyeButton(eyeX, eyeY, true);
		}
	}
}
