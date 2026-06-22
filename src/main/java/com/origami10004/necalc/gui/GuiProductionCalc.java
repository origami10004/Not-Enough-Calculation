package com.origami10004.necalc.gui;

import java.io.IOException;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.ProductionStep;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiProductionCalc extends GuiCommon {
	// GUI textures
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/prod_calc.png");

	// constants for GUI layout
	private static final int GUI_WIDTH			= 184;
	private static final int GUI_HEIGHT			= 302;
	protected static final int SLOT_SIZE			= 18;
	protected static final int SLOTS_PER_ROW		= 8;
	private static final int TARGET_ROWS		= 2;

	protected static final int INDENT_L = 8;
	private static final int INDENT_R = 8;

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
	private CalculatorState calcState;
	private InventoryPlayer playerInv;
	private FakeContainer container;

	// Target scrolling
	protected int targetScrollRow = 0;
	private int prodScrollRow = 0;
	private final RateEditHelper editOverlay = new RateEditHelper(this);

	// Recipe hovering
	private int hoveredRecipeRow = -1;
	private int hoveredRecipeRowTicks = 0;
	private ItemStack hoveredStepStack = ItemStack.EMPTY;

	// Element positions
	private int rateBtnY;
	private int unhideX, unhideY;
	private int invY;
	private int targetGridY;
	private int prodTableY;

	private int gx, gy; // top left corner of the whole GUI

	public GuiProductionCalc(InventoryPlayer playerInv, CalculatorState calcState) {
		super(new FakeContainer(playerInv, true, 9, 248));
		this.playerInv = playerInv;
		this.calcState = calcState;
		this.container = (FakeContainer) inventorySlots;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void initGui() {
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT + TAB_H;
		super.initGui();
		this.gx = guiLeft;
		this.gy = guiTop;
		this.targetGridY = gy + TAB_H + 35;
		this.editOverlay.reInit(gx, this.targetGridY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		// main panel
		this.mc.getTextureManager().bindTexture(BG_TEXTURE);
		drawModalRectWithCustomSizedTexture(this.gx, this.gy + TAB_H, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		drawTabStrip(gx, gy);
		
		int curY = gy + TAB_H;

		// target panel
		this.fontRenderer.drawString(I18n.format("necalc.gui.target" ), this.gx + 8, curY + 6, 0xFF000000);
		curY += 16;
		// Rate buttons
		int width = this.fontRenderer.getStringWidth(I18n.format("necalc.gui.rate"));
		this.fontRenderer.drawString(I18n.format("necalc.gui.rate"), this.gx + INDENT_L + 4, curY + 6, 0xFF000000);
		String [] rateLabels = {I18n.format("necalc.gui.rate.minute"), I18n.format("necalc.gui.rate.second"), I18n.format("necalc.gui.rate.ticks")};
		int selectedRate = this.calcState.getSelectedRate();
		this.rateBtnY = curY + 4;
		for (int i = 0; i < 3; i++) {
			this.drawButton(this.gx + INDENT_L + 4 + width + 4 + i * 34, rateBtnY, 30, 11, rateLabels[i], mouseX, mouseY, (i == selectedRate), true);
		}
		curY += 15;
		// Target table
		this.targetGridY = curY + 4;
		for (int row = 0; row < TARGET_ROWS; row++) {
			int actual = row + this.targetScrollRow;
			for(int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + INDENT_L + 4 + col * SLOT_SIZE;
				this.drawItemSlot(slotX, this.targetGridY + row * SLOT_SIZE);

				ItemStack curTarget = this.calcState.getTargetSlot(actual * SLOTS_PER_ROW + col);
				if (!curTarget.isEmpty()) {
					RenderHelper.enableGUIStandardItemLighting();
					this.itemRender.renderItemAndEffectIntoGUI(curTarget, slotX + 1, this.targetGridY + row * SLOT_SIZE + 1);
					this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, curTarget, slotX + 1, this.targetGridY + row * SLOT_SIZE + 1, Double.toString(this.calcState.getTargetSlotRate(actual * SLOTS_PER_ROW + col)));
					RenderHelper.disableStandardItemLighting();
				}
			}
		}
		//Target scrollbar
		int sbX = this.gx + INDENT_L + 4 + SLOTS_PER_ROW * SLOT_SIZE + 2;
		this.drawTargetScrollBar(sbX, this.targetGridY, TARGET_ROWS * 18, mouseX, mouseY);
		curY += 6 + TARGET_ROWS * SLOT_SIZE;

		// Required rates panel
		this.fontRenderer.drawString(I18n.format("necalc.gui.results"), this.gx + 8, curY + 7, 0xFF000000);
		this.unhideX = this.gx + GUI_WIDTH - INDENT_R - HIDE_SIZE - 2;
		this.unhideY = curY + 4;
		this.drawEyeButton(unhideX, unhideY, mouseX, mouseY, this.calcState.hasHidden(), false);
		// Rates table
		this.prodTableY = curY + 18;
		this.drawProdTable(this.prodTableY, mouseX, mouseY);

		curY += TABLE_VIS_ROWS * TABLE_ROW_H + 20;

		// Player inventory
		this.invY = curY;
		this.fontRenderer.drawString(I18n.format("necalc.gui.inventory"), this.gx + 8, this.invY + 6, 0xFF000000);
		this.drawPlayerInventory(this.gx + 8, this.invY + 16, mouseX, mouseY, this.playerInv);

		// Rate editor overlay
		editOverlay.drawOverlay(mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

		drawTabTooltips(mouseX - this.guiLeft, mouseY - this.guiTop, this.gx - this.guiLeft, this.gy - this.guiTop);

		// Help tooltip
		if (mouseX >= this.gx + 157 && mouseX < this.gx + 171 && mouseY >= this.gy + TAB_H + 18 && mouseY < this.gy + TAB_H + 31) {
			this.drawHoveringText(I18n.format("necalc.gui.target_help"), mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Unhide button tooltip
		if (mouseX >= unhideX && mouseX < unhideX + HIDE_SIZE && mouseY >= unhideY && mouseY < unhideY + HIDE_SIZE) {
			String text;
			if (this.calcState.hasHidden()) {
				text = I18n.format("necalc.gui.show.some", this.calcState.getHiddenCount());
			} else {
				text = I18n.format("necalc.gui.show.none");
			}
			this.drawHoveringText(text, mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Production step recipe tooltip (only show after hovering for a while)
		if (hoveredRecipeRow != -1 && hoveredRecipeRowTicks > 20) {
			List<ProductionStep> visible = this.calcState.getVisibleRecipes();
			if (hoveredRecipeRow < visible.size()) {
				ProductionStep step = visible.get(hoveredRecipeRow);
				Necalc.logger.info("Hovering recipe row " + hoveredRecipeRow);
				//this.drawRecipeTooltip(step, mouseX, mouseY);
			}
		}

		// Production step tooltips
		if (hoveredStepStack != null && !hoveredStepStack.isEmpty()) {
			this.renderToolTip(hoveredStepStack, mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Inventory tooltips
		this.drawPlayerInventoryTooltips(mouseX - this.guiLeft, mouseY - this.guiTop);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		if (hoveredRecipeRow != -1) {
			hoveredRecipeRowTicks++;
		}
		editOverlay.updateCursorCounter();
	}

	private void drawTargetScrollBar(int x, int y, int height, int mouseX, int mouseY) {
		int totalRows = calcState.getTargetNumRows();
		int thumbH, thumbY;
		if (totalRows <= TARGET_ROWS) {
			thumbH = height - 2;
			thumbY = y + 1;
		} else {
			thumbH = Math.max(SB_THUMB_MIN_H, (height - 2) * TARGET_ROWS / totalRows);
			thumbY = y + 1 + (height - thumbH) * this.targetScrollRow / (totalRows - TARGET_ROWS);
		}

		this.drawRectPanelOutdent(x + 1, y + 1, SB_W - 2, thumbH, 0xFFC6C6C6);
	}

	private void drawEyeButton(int x, int y, int mouseX, int mouseY, boolean active, boolean crossed) {
		boolean hovered = mouseX >= x && mouseX < x + HIDE_SIZE && mouseY >= y && mouseY < y + HIDE_SIZE;
		int cx = x + HIDE_SIZE / 2;
		int cy = y + HIDE_SIZE / 2;
		int lineColor = active ? 0xFF000000 : 0xFF8B8B8B;
		int bg = !active ? 0xFF999999 : (hovered ? 0xFFA8B8D8 : 0xFFC6C6C6);

		this.drawButton(x, y, HIDE_SIZE, HIDE_SIZE, "", mouseX, mouseY, false, active);

		drawRect(cx - 2, cy - 2, cx + 2, cy + 3, lineColor);
		drawRect(cx - 3, cy - 1, cx + 3, cy + 2, lineColor);
		drawRect(cx - 4, cy, cx + 4, cy + 1, lineColor);
		drawRect(cx - 2, cy - 1, cx + 2, cy + 2, bg);
		drawRect(cx - 3, cy, cx + 3, cy + 1, bg);
		drawRect(cx - 1, cy, cx + 1, cy + 1, lineColor);
	}

	private void drawProdTable(int y, int mouseX, int mouseY) {
		List<ProductionStep> visible = this.calcState.getVisibleRecipes();
		int maxScroll = Math.max(0, visible.size() - TABLE_VIS_ROWS);
		this.prodScrollRow = Math.max(0, Math.min(this.prodScrollRow, maxScroll));

		int rowX = this.gx + INDENT_L + 1;
		int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
		hoveredStepStack = ItemStack.EMPTY;

		// Scrollbar if needed and adjust row width accordingly
		if (visible.size() > TABLE_VIS_ROWS) {
			rowW -= SB_W + 2;
		}

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
				this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, input, rowX + 2, iconY, String.format("%.1f", this.calcState.getMultiplier() * step.getPrimaryInputRate()));
				RenderHelper.disableStandardItemLighting();
				if (rowHovered) {
					if (mouseX >= rowX + 2 && mouseX < rowX + 2 + 16 && mouseY >= iconY && mouseY < iconY + 16) {
						hoveredStepStack = input;
					}
				}
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
			if (rowHovered) {
				if (mouseX >= machineIconX && mouseX < machineIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
					hoveredStepStack = step.getMachine();
				}
			}

			// Arrow
			drawArrow(rowX + 60, rowY + TABLE_ROW_H / 2 - 1, 0xFFAAAAAA);

			// Primary output and rate (output cannot be empty)
			int outputIconX = rowX + 72;
			ItemStack output = step.getPrimaryOutput();
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemAndEffectIntoGUI(output, outputIconX, iconY);
			this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, output, outputIconX, iconY, String.format("%.1f", this.calcState.getMultiplier() * step.getPrimaryOutputRate()));
			RenderHelper.disableStandardItemLighting();
			if (rowHovered) {
				if (mouseX >= outputIconX && mouseX < outputIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
					hoveredStepStack = output;
				}
			}
			// More outputs
			if (step.getOutputs().size() > 1) {
				this.fontRenderer.drawString("+" + (step.getOutputs().size() - 1), rowX + 82, iconY + 4, 0xFF000000);
			}

			// Hide button
			int eyeX = rowX + rowW - 14;
			int eyeY = rowY + (TABLE_ROW_H - 12) / 2;
			drawEyeButton(eyeX, eyeY, mouseX, mouseY, true, true);
		}
	}

	// Input handling
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		// Edit overlay first
		if (editOverlay.mouseClicked(mouseX, mouseY, mouseButton)) return;

		// Tab handling
		for (int i = 0; i < TAB_COUNT; i++) {
			int tx = gx + 4 + i * (TAB_W + 2);
			if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= gy + 2 && mouseY < gy + 2 + TAB_H) {
				onTabClicked(i);
				return;
			}
		}

		// Rate buttons
		if (mouseButton == 0) {
			int width = this.fontRenderer.getStringWidth(I18n.format("necalc.gui.rate"));
			for (int i = 0; i < 3; i++) {
				int btnX = this.gx + INDENT_L + 4 + width + 4 + i * 34;
				if (mouseX >= btnX && mouseX < btnX + 30 && mouseY >= this.rateBtnY
							&& mouseY < this.rateBtnY + 11) {
					this.calcState.setSelectedRate(i); return;
				}
			}
		}
		
		// Target slot set (left click)
		if (mouseButton == 0) {
			int targetSlot = getTargetSlotAt(mouseX, mouseY);
			if (targetSlot != -1) {
				ItemStack heldItem = this.mc.player.inventory.getItemStack();
				this.calcState.setTargetSlot(targetSlot, heldItem.copy());
				return;
			}
		}
		// Target slot edit (middle click)
		if (mouseButton == 2) {
			int targetSlot = getTargetSlotAt(mouseX, mouseY);
			if (targetSlot != -1 && !this.calcState.getTargetSlot(targetSlot).isEmpty()) {
				int slotScreenX = this.gx + INDENT_L + 4 + (targetSlot % SLOTS_PER_ROW) * SLOT_SIZE;
				int slotScreenY = this.targetGridY + (targetSlot / SLOTS_PER_ROW - this.targetScrollRow) * SLOT_SIZE;
				editOverlay.openSlot(targetSlot, slotScreenX, slotScreenY);
				return;
			}
		}
		// Unhide button (left click)
		if (mouseButton == 0
                && mouseX >= this.unhideX && mouseX < this.unhideX + HIDE_SIZE
                && mouseY >= this.unhideY && mouseY < this.unhideY + HIDE_SIZE) {
            this.calcState.showAllRecipes();
            return;
        }

		// Production step hide/show toggle (left click)
		if (mouseButton == 0) {
			int prodRow = getProdRowAt(mouseX, mouseY);
			if (prodRow != -1) {
				List<ProductionStep> visible = this.calcState.getVisibleRecipes();
				int rowX = gx + INDENT_L + 1;
				int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
				if (visible.size() > TABLE_VIS_ROWS) rowW -= SB_W + 2;
				int rowY = 1 + prodTableY + (prodRow - prodScrollRow) * TABLE_ROW_H;
				int eyeX = rowX + rowW - 14;
				int eyeY = rowY + (TABLE_ROW_H - HIDE_SIZE) / 2;
				if (mouseX >= eyeX && mouseX < eyeX + HIDE_SIZE
						&& mouseY >= eyeY && mouseY < eyeY + HIDE_SIZE) {
					calcState.hideRecipe(prodRow);
					return;
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException{
		if (editOverlay.keyTyped(this, typedChar, keyCode)) return;
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
	}

	// Helper functions
	private void onTabClicked(int index) {
		switch (index) {
			case 0:
				break;
			case 1:
				this.editOverlay.close();
				// mc.displayGuiScreen(new GuiFlowChart(playerInv, container));
				break;
			case 2:
				this.editOverlay.close();
				// mc.displayGuiScreen(new GuiManageRecipes(playerInv, container));
				break;
			case 3:
				this.editOverlay.close();
				// mc.displayGuiScreen(new GuiAddRecipe(playerInv, container));
				break;
			case 4:
				this.editOverlay.close();
				mc.displayGuiScreen(new GuiRecipeEditor(playerInv, this, true));
				break;
		}
	}

	private int getTargetSlotAt(int mouseX, int mouseY) {
		for (int row = 0; row < TARGET_ROWS; row++) {
			int actual = row + this.targetScrollRow;
			for(int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + INDENT_L + 4 + col * SLOT_SIZE;
				if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE
							&& mouseY >= this.targetGridY + row * SLOT_SIZE
							&& mouseY < this.targetGridY + row * SLOT_SIZE + SLOT_SIZE) {
					return actual * SLOTS_PER_ROW + col;
				}
			}
		}
		return -1;
	}
	private int getProdRowAt(int mouseX, int mouseY) {
		int rowX = this.gx + INDENT_L + 1;
		int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
		if (mouseX < rowX || mouseX >= rowX + rowW) return -1;

		int prodY = this.gy + TAB_H + 35 + 6 + TARGET_ROWS * SLOT_SIZE + 18;
		for(int i = 0; i < TABLE_VIS_ROWS; i++) {
			int rowY = prodY + i * TABLE_ROW_H;
			if (mouseY >= rowY && mouseY < rowY + TABLE_ROW_H) {
				return i + this.prodScrollRow;
			}
		}
		return -1;
	}
}
