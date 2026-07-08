package com.origami10004.necalc.gui;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import org.lwjgl.input.Mouse;

import javax.annotation.ParametersAreNonnullByDefault;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.ingredient.*;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiProductionCalc extends GuiCommon {
	// GUI textures
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/prod_calc.png");

	// constants for GUI layout
	private static final int GUI_WIDTH			= 184;
	private static final int GUI_HEIGHT			= 300;
	protected static final int SLOT_SIZE			= 18;
	public static final int SLOTS_PER_ROW		= 8;
	public static final int TARGET_ROWS		= 2;

	protected static final int INDENT_L = 8;
	private static final int INDENT_R = 8;

	private static final int SB_W = 12;

	private static final int TABLE_ROW_H    = 22;
	private static final int TABLE_VIS_ROWS = 5;

	private static final int HIDE_SIZE = 12;


	// Other constants
	@Override
	protected int getActiveTab() {
		return 0;
	}

	// instance variables
	private InventoryPlayer playerInv;

	// Target scrolling
	protected int targetScrollRow = 0;
	private float targetScrollPercent = 0.0f;
	private boolean draggingTargetScroll = false;
	private int prodScrollRow = 0;
	private float prodScrollPercent = 0.0f;
	private boolean draggingProdScroll = false;
	private final RateEditHelper editOverlay = new RateEditHelper(this);

	// Recipe hovering
	private int hoveredRecipeRow = -1;
	private int hoveredRecipeRowTicks = 0;
	private Ingredients hoveredStepStack = Ingredients.EMPTY;
	private double hoveredStepValue = 0.0;
	private boolean hoverMachine = false;

	// Element positions
	private int rateBtnY;
	private int unhideX, unhideY;
	private int invY;
	private int targetGridY;
	private int prodTableY;

	private int gx, gy; // top left corner of the whole GUI

	public GuiProductionCalc(InventoryPlayer playerInv) {
		super(new FakeContainer(playerInv, true, 9, 246));
		this.playerInv = playerInv;
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
		int selectedRate = CalculatorState.getDisplayRate();
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

				Ingredients curTarget = CalculatorState.getTargetSlot(actual * SLOTS_PER_ROW + col);
				curTarget.renderValue(this, slotX + 1, this.targetGridY + row * SLOT_SIZE + 1, CalculatorState.getTargetSlotRate(actual * SLOTS_PER_ROW + col));
			}
		}
		//Target scrollbar
		this.drawTargetScrollBar();
		curY += 6 + TARGET_ROWS * SLOT_SIZE;

		// Required rates panel
		this.fontRenderer.drawString(I18n.format("necalc.gui.results"), this.gx + 8, curY + 7, 0xFF000000);
		this.unhideX = this.gx + GUI_WIDTH - INDENT_R - HIDE_SIZE - 2;
		this.unhideY = curY + 4;
		this.drawEyeButton(unhideX, unhideY, mouseX, mouseY, CalculatorState.hasHidden(), false);
		// Rates table
		this.prodTableY = curY + 18;
		this.drawProdTable(this.prodTableY, mouseX, mouseY);

		curY += TABLE_VIS_ROWS * TABLE_ROW_H + 18;

		// Player inventory
		this.invY = curY;
		this.fontRenderer.drawString(I18n.format("necalc.gui.inventory"), this.gx + 8, this.invY + 6, 0xFF000000);
		this.drawPlayerInventory(this.gx + 8, this.invY + 16, mouseX, mouseY, this.playerInv);

		// Rate editor overlay
		editOverlay.drawOverlay(mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		if (editOverlay.hovered(mouseX, mouseY)) return;

		drawTabTooltips(mouseX - this.guiLeft, mouseY - this.guiTop, this.gx - this.guiLeft, this.gy - this.guiTop);

		// Help tooltip
		if (mouseX >= this.gx + 157 && mouseX < this.gx + 171 && mouseY >= this.gy + TAB_H + 18 && mouseY < this.gy + TAB_H + 31) {
			this.drawHoveringText(I18n.format("necalc.gui.target_help"), mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Target table tooltips
		int targetSlot = getTargetSlotAt(mouseX, mouseY);
		if (targetSlot != -1) {
			Ingredients ing = CalculatorState.getTargetSlot(targetSlot);
			if (!ing.isEmpty()) {
				double rate = CalculatorState.getTargetSlotRate(targetSlot);
				
				this.drawItemExtraInfoTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, ing,
						I18n.format("necalc.gui.target_rate", rate));
			}
		}

		// Unhide button tooltip
		if (mouseX >= unhideX && mouseX < unhideX + HIDE_SIZE && mouseY >= unhideY && mouseY < unhideY + HIDE_SIZE) {
			String text;
			if (CalculatorState.hasHidden()) {
				text = I18n.format("necalc.gui.show.some", CalculatorState.getHiddenCount());
			} else {
				text = I18n.format("necalc.gui.show.none");
			}
			this.drawHoveringText(text, mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Production step recipe tooltip (only show after hovering for a while)
		if (hoveredRecipeRow != -1 && hoveredRecipeRowTicks > 20) {
			List<ProductionStep> visible = CalculatorState.getVisibleRecipes();
			if (hoveredRecipeRow < visible.size()) {
				ProductionStep step = visible.get(hoveredRecipeRow);
				//this.drawRecipeTooltip(step, mouseX, mouseY);
			}
		}

		// Production step tooltips
		if (hoveredStepStack != null && !hoveredStepStack.isEmpty()) {
			if (hoverMachine) {
				drawItemExtraInfoTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, hoveredStepStack,
						I18n.format("necalc.gui.machine_count", hoveredStepValue));
			} else {
				drawItemExtraInfoTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, hoveredStepStack,
						I18n.format("necalc.gui.target_rate", hoveredStepValue));
			}
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

	private void drawTargetScrollBar() {
		int totalRows = CalculatorState.getTargetNumRows();
		
		int sbX = this.gx + 159;
		int sbY = this.gy + TAB_H + 36;
		int width = 12;
		int height = 34;
		if (totalRows <= TARGET_ROWS) {
			this.targetScrollPercent = 0.0f;
			this.targetScrollRow = 0;
		}
		this.drawScrollbar(sbX, sbY, width, height, this.targetScrollPercent, totalRows > TARGET_ROWS);
	}

	private void drawProductionScrollBar() {
		int sbX = this.gx + 163;
		int sbY = this.gy + TAB_H + 92;
		int width = 12;
		int height = 110;
		this.drawScrollbar(sbX, sbY, width, height, this.prodScrollPercent, true);
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
		List<ProductionStep> visible = CalculatorState.getVisibleRecipes();
		int maxScroll = Math.max(0, visible.size() - TABLE_VIS_ROWS);
		this.prodScrollRow = Math.max(0, Math.min(this.prodScrollRow, maxScroll));

		int rowX = this.gx + INDENT_L + 1;
		int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
		hoveredStepStack = Ingredients.EMPTY;

		// Scrollbar if needed and adjust row width accordingly
		if (visible.size() > TABLE_VIS_ROWS) {
			rowW -= SB_W;
			drawProductionScrollBar();
		} else {
			this.prodScrollRow = 0;
			this.prodScrollPercent = 0.0f;
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

			int rowBg = rowHovered ? 0xFFA8B8D8 : 0xFFACACAC;
			drawRectPanelOutdent(rowX, rowY, rowW, TABLE_ROW_H, rowBg);

			// Primary input and rate
			Ingredients input = step.getPrimaryInput();
			int iconY = rowY + 2;
			if (input != null) {
				input.renderValue(this, rowX + 2, iconY, step.getPrimaryInputRate() / CalculatorState.getMultiplier());
				if (rowHovered) {
					if (mouseX >= rowX + 2 && mouseX < rowX + 2 + 16 && mouseY >= iconY && mouseY < iconY + 16) {
						hoveredStepStack = input;
						hoveredStepValue = step.getPrimaryInputRate() / CalculatorState.getMultiplier();
						hoverMachine = false;
					}
				}
			}
			// More inputs
			if (step.getInputs().size() > 1) {
				this.fontRenderer.drawString("+" + (step.getInputs().size() - 1), rowX + 18, iconY + 4, 0xFF000000);
			}

			// Arrow
			drawArrow(rowX + 28, rowY + TABLE_ROW_H / 2 - 1, 0xFFFFFFFF);

			// Machine (machine cannot be empty)
			int machineIconX = rowX + 40;
			step.getMachine().renderValue(this, machineIconX, iconY);
			if (rowHovered) {
				if (mouseX >= machineIconX && mouseX < machineIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
					hoveredStepStack = step.getMachine();
					hoveredStepValue = step.getMachine().getValue();
					hoverMachine = true;
				}
			}

			// Arrow
			drawArrow(rowX + 60, rowY + TABLE_ROW_H / 2 - 1, 0xFFFFFFFF);

			// Primary output and rate (output cannot be empty)
			int outputIconX = rowX + 72;
			Ingredients output = step.getPrimaryOutput();
			output.renderValue(this, outputIconX, iconY, step.getPrimaryOutputRate() / CalculatorState.getMultiplier());
			if (rowHovered) {
				if (mouseX >= outputIconX && mouseX < outputIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
					hoveredStepStack = output;
					hoveredStepValue = step.getPrimaryOutputRate() / CalculatorState.getMultiplier();
					hoverMachine = false;
				}
			}
			// More outputs
			if (step.getOutputs().size() > 1) {
				this.fontRenderer.drawString("+" + (step.getOutputs().size() - 1), rowX + 88, iconY + 4, 0xFF000000);
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
			int tx = gx + TAB_LEFT_PAD + i * (TAB_W + 2);
			if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= gy + 2 && mouseY < gy + 2 + TAB_H) {
				onTabClicked(i);
				return;
			}
		}

		// Rate buttons
		int width = this.fontRenderer.getStringWidth(I18n.format("necalc.gui.rate"));
		for (int i = 0; i < 3; i++) {
			int btnX = this.gx + INDENT_L + 4 + width + 4 + i * 34;
			if (mouseX >= btnX && mouseX < btnX + 30 && mouseY >= this.rateBtnY
						&& mouseY < this.rateBtnY + 11) {
				if (mouseButton == 0) {
					CalculatorState.setDisplayRate(i);
				}
				return;
			}
		}
		
		// Target slot set
		int targetSlot = getTargetSlotAt(mouseX, mouseY);
		if (targetSlot != -1) {
			if (mouseButton == 0) {
				// left click: set target slot to held item
				ItemStack heldItem = this.mc.player.inventory.getItemStack();
				CalculatorState.setTargetSlot(targetSlot, IngredientManager.of(heldItem));
				if (this.targetScrollRow > CalculatorState.getTargetNumRows() - TARGET_ROWS) {
					this.targetScrollRow = Math.max(0, CalculatorState.getTargetNumRows() - TARGET_ROWS);
					this.targetScrollPercent = (float) this.targetScrollRow / Math.max(1, CalculatorState.getTargetNumRows() - TARGET_ROWS);
				}
			} else if (mouseButton == 2) {
				// middle click: open rate editor
				if (!CalculatorState.getTargetSlot(targetSlot).isEmpty()) {
					int slotScreenX = this.gx + INDENT_L + 4 + (targetSlot % SLOTS_PER_ROW) * SLOT_SIZE;
					int slotScreenY = this.targetGridY + (targetSlot / SLOTS_PER_ROW - this.targetScrollRow) * SLOT_SIZE;
					editOverlay.openSlot(targetSlot, slotScreenX, slotScreenY);
				}
			}
			return;
		}
		
		// Unhide button (left click)
		if (mouseX >= this.unhideX && mouseX < this.unhideX + HIDE_SIZE
					&& mouseY >= this.unhideY && mouseY < this.unhideY + HIDE_SIZE) {
			if (mouseButton == 0) {
				CalculatorState.showAllRecipes();
			}
			return;
		}

		// Production step hide/show toggle (left click)
		int prodRow = getProdRowAt(mouseX, mouseY);
		if (prodRow != -1) {
			if (mouseButton == 0) {
				List<ProductionStep> visible = CalculatorState.getVisibleRecipes();
				int rowX = gx + INDENT_L + 1;
				int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
				if (visible.size() > TABLE_VIS_ROWS) rowW -= SB_W + 2;
				int rowY = 1 + prodTableY + (prodRow - prodScrollRow) * TABLE_ROW_H;
				int eyeX = rowX + rowW - 14;
				int eyeY = rowY + (TABLE_ROW_H - HIDE_SIZE) / 2;
				if (mouseX >= eyeX && mouseX < eyeX + HIDE_SIZE
						&& mouseY >= eyeY && mouseY < eyeY + HIDE_SIZE) {
					CalculatorState.hideRecipe(prodRow);
					this.prodScrollRow = Math.min(this.prodScrollRow, Math.max(0, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS));
					this.prodScrollPercent = (float) this.prodScrollRow / Math.max(1, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS);
					return;
				}
			}
		}

		// target scroll
		if (mouseX >= this.gx + 158 && mouseX < this.gx + 172 && mouseY >= this.gy + TAB_H + 35 && mouseY < this.gy + TAB_H + 71) {
			if (mouseButton == 0) {
				this.draggingTargetScroll = true;
				this.targetScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 35, 36);
				this.targetScrollRow = (int) (this.targetScrollPercent * Math.max(0, CalculatorState.getTargetNumRows() - TARGET_ROWS));
			}
			return;
		}

		// production scroll
		if (CalculatorState.getVisibleRecipes().size() > TABLE_VIS_ROWS &&
				mouseX >= this.gx + 162 && mouseX < this.gx + 176
				&& mouseY >= this.gy + TAB_H + 91 && mouseY < this.gy + TAB_H + 203) {
			if (mouseButton == 0) {
				this.draggingProdScroll = true;
				this.prodScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 91, 112);
				this.prodScrollRow = (int) (this.prodScrollPercent * (CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS));
			}
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (this.draggingTargetScroll) {
			this.targetScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 35, 36);
			this.targetScrollRow = (int) (this.targetScrollPercent * Math.max(0, CalculatorState.getTargetNumRows() - TARGET_ROWS));
			return;
		}
		if (this.draggingProdScroll) {
			this.prodScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 91, 112);
			this.prodScrollRow = (int) (this.prodScrollPercent * Math.max(0, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS));
			return;
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.draggingTargetScroll) {
			this.draggingTargetScroll = false;
			return;
		}
		if (this.draggingProdScroll) {
			this.draggingProdScroll = false;
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException{
		if (editOverlay.keyTyped(typedChar, keyCode)) return;
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			// Target table scroll
			if (!editOverlay.isOpen() && mouseX >= this.gx + INDENT_L && mouseX < this.gx + GUI_WIDTH - INDENT_L
						&& mouseY >= this.gy + TAB_H + 16 && mouseY < this.gy + TAB_H + 75) {
				int scrollRows = Math.max(0, CalculatorState.getTargetNumRows() - TARGET_ROWS);
				if (scrollRows <= 0) return;
				if (scroll > 0) {
					this.targetScrollRow = Math.max(0, this.targetScrollRow - 1);
				} else {
					this.targetScrollRow = Math.min(scrollRows, this.targetScrollRow + 1);
				}
				this.targetScrollPercent = (float) this.targetScrollRow / scrollRows;
				return;
			}
			
			// Production table scroll
			if (mouseX >= this.gx + INDENT_L && mouseX < this.gx + GUI_WIDTH - INDENT_L
						&& mouseY >= this.prodTableY && mouseY < this.prodTableY + TABLE_VIS_ROWS * TABLE_ROW_H) {
				int scrollRows = Math.max(0, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS);
				if (scrollRows <= 0) return;
				if (scroll > 0) {
					this.prodScrollRow = Math.max(0, this.prodScrollRow - 1);
				} else {
					this.prodScrollRow = Math.min(Math.max(0, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS), this.prodScrollRow + 1);
				}
				this.prodScrollPercent = (float) this.prodScrollRow / scrollRows;
				return;
			}
		}
		super.handleMouseInput();
	}

	// Helper functions
	protected void onTabClicked(int index) {
		switch (index) {
			case 0:
				break;
			case 1:
				this.editOverlay.close();
				// mc.displayGuiScreen(new GuiFlowChart(playerInv, container));
				break;
			case 2:
				this.editOverlay.close();
				mc.displayGuiScreen(new GuiManageRecipes(playerInv));
				break;
			case 3:
				this.editOverlay.close();
				mc.displayGuiScreen(new GuiManageMachines(playerInv));
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

	public int getTargetScrollRow() {
		return this.targetScrollRow;
	}

	public Rectangle getTargetSlotArea(int visRow, int col) {
		int x = this.gx + INDENT_L + 4 + col * SLOT_SIZE;
		int y = this.targetGridY + visRow * SLOT_SIZE;
		return new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
	}
}
