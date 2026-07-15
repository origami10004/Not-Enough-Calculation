package com.origami10004.necalc.gui;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Mouse;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.ingredient.*;
import com.origami10004.necalc.calc.Solver;
import com.origami10004.necalc.proxy.ClientProxy;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GuiProductionCalc extends GuiCommon {
	// GUI textures
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/prod_calc.png");
	private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("necalc", "textures/gui/button_icons.png");

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
	protected static final String [] rateLabels = {I18n.format("necalc.gui.rate.minute"), I18n.format("necalc.gui.rate.second"), I18n.format("necalc.gui.rate.ticks")};


	// Other constants
	@Override
	protected int getActiveTab() {
		return 0;
	}

	// instance variables
	private InventoryPlayer playerInv;

	// scrolling
	protected int targetScrollRow = 0;
	private float targetScrollPercent = 0.0f;
	private boolean draggingTargetScroll = false;
	private int prodScrollRow = 0;
	private float prodScrollPercent = 0.0f;
	private boolean draggingProdScroll = false;
	private boolean showProd = true;
	private int inputScrollRow = 0;
	private float inputScrollPercent = 0.0f;
	private boolean draggingInputScroll = false;
	private final RateEditHelper editOverlay = new RateEditHelper(this);
	private final RecipeViewHelper recipeViewHelper = new RecipeViewHelper(this);

	// Recipe hovering
	private Ingredients hoveredStepStack = Ingredients.EMPTY;
	private String hoveredStepValue = "";
	private boolean hoverMachine = false;

	// Element positions
	private int rateBtnY;
	private int unhideX, unhideY;
	private int invY;
	private int targetGridY;
	private int prodTableY;

	private int gx, gy; // top left corner of the whole GUI

	public GuiProductionCalc(InventoryPlayer playerInv) {
		super(new NecalcContainer(playerInv, true, 9, 246));
		this.playerInv = playerInv;
		ClientProxy.lastOpenedGui = this;
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
		this.recipeViewHelper.init(this.gx, this.gy + TAB_H);
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
		if (showProd) {
			this.fontRenderer.drawString(I18n.format("necalc.gui.results"), this.gx + 8, curY + 7, 0xFF000000);
		} else {
			this.fontRenderer.drawString(I18n.format("necalc.gui.inputs"), this.gx + 8, curY + 7, 0xFF000000);
		}
		this.unhideX = this.gx + GUI_WIDTH - INDENT_R - HIDE_SIZE - 2;
		this.unhideY = curY + 4;
		this.drawEyeButton(unhideX, unhideY, mouseX, mouseY, CalculatorState.hasHidden(showProd), false);

		this.drawButton(this.gx + 147, this.gy + TAB_H + 77, 12, 12, "", mouseX, mouseY, false, true);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BUTTON_TEXTURE);
		drawModalRectWithCustomSizedTexture(this.gx + 148, this.gy + TAB_H + 78, 0, 0, 10, 10, 30, 10);

		// Production/input table
		this.prodTableY = curY + 18;
		if (showProd) {
			this.drawProdTable(this.prodTableY, mouseX, mouseY);
		} else {
			this.drawInputTable(this.prodTableY, mouseX, mouseY);
		}

		curY += TABLE_VIS_ROWS * TABLE_ROW_H + 18;

		// Player inventory
		this.invY = curY;
		this.fontRenderer.drawString(I18n.format("necalc.gui.inventory"), this.gx + 8, this.invY + 6, 0xFF000000);
		this.drawPlayerInventory(this.gx + 8, this.invY + 16, mouseX, mouseY, this.playerInv);

		this.recipeViewHelper.drawExtension(mouseX, mouseY);

		// Rate editor overlay
		editOverlay.drawOverlay(mouseX, mouseY);
	}

	@Override
	public void renderHoveredToolTip(int mouseX, int mouseY) {
		if (editOverlay.hovered(mouseX, mouseY)) return;

		drawTabTooltips(mouseX, mouseY, this.gx, this.gy);

		// Help tooltip
		if (mouseX >= this.gx + 157 && mouseX < this.gx + 171 && mouseY >= this.gy + TAB_H + 18 && mouseY < this.gy + TAB_H + 31) {
			this.drawHoveringText(I18n.format("necalc.gui.target_help"), mouseX, mouseY);
		}

		// Target table tooltips
		int targetSlot = getTargetSlotAt(mouseX, mouseY);
		if (targetSlot != -1) {
			Ingredients ing = CalculatorState.getTargetSlot(targetSlot);
			if (!ing.isEmpty()) {
				String rate = String.format("%.4f", CalculatorState.getTargetSlotRate(targetSlot));
				
				this.drawItemExtraInfoTooltip(mouseX, mouseY, ing,
						I18n.format("necalc.gui.target_rate", rate));
			}
		}

		// Unhide button tooltip
		if (mouseX >= unhideX && mouseX < unhideX + HIDE_SIZE && mouseY >= unhideY && mouseY < unhideY + HIDE_SIZE) {
			String text;
			if (CalculatorState.hasHidden(showProd)) {
				if (showProd) {
					text = I18n.format("necalc.gui.show_recipe.some", CalculatorState.getHiddenCount(showProd));
				} else {
					text = I18n.format("necalc.gui.show_input.some", CalculatorState.getHiddenCount(showProd));
				}
			} else {
				if (showProd) {
					text = I18n.format("necalc.gui.show_recipe.none");
				} else {
					text = I18n.format("necalc.gui.show_input.none");
				}
			}
			this.drawHoveringText(text, mouseX, mouseY);
		}

		// Production step tooltips
		if (hoveredStepStack != null && !hoveredStepStack.isEmpty()) {
			if (showProd) {
				if (hoverMachine) {
					drawItemExtraInfoTooltip(mouseX, mouseY, hoveredStepStack,
							I18n.format("necalc.gui.machine_count", hoveredStepValue));
				} else {
					drawItemExtraInfoTooltip(mouseX, mouseY, hoveredStepStack,
							I18n.format("necalc.gui.target_rate", hoveredStepValue));
				}
			} else {
				drawHoveringText(hoveredStepStack.getTooltip(this.mc), mouseX, mouseY);
			}
		}

		this.recipeViewHelper.drawTooltip(mouseX, mouseY);
		super.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
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

	private void drawInputScrollBar() {
		int sbX = this.gx + 163;
		int sbY = this.gy + TAB_H + 92;
		int width = 12;
		int height = 110;
		this.drawScrollbar(sbX, sbY, width, height, this.inputScrollPercent, true);
	}

	private void drawEyeButton(int x, int y, int mouseX, int mouseY, boolean active, boolean crossed) {
		this.drawButton(x, y, HIDE_SIZE, HIDE_SIZE, "", mouseX, mouseY, false, active);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BUTTON_TEXTURE);
		drawModalRectWithCustomSizedTexture(x + 1, y + 1, active ? 10 : 20, 0, 10, 10, 30, 10);
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

			int rowBg = rowHovered ? 0xFFA8B8D8 : 0xFFACACAC;
			drawRectPanelOutdent(rowX, rowY, rowW, TABLE_ROW_H, rowBg);

			int curX = rowX + 2;

			// Primary input and rate
			Ingredients input = step.getPrimaryInput();
			int iconY = rowY + 2;
			if (input != null && !input.isEmpty()) {
				input.renderValue(this, curX, iconY, step.getPrimaryInputRate() / CalculatorState.getMultiplier());
				if (rowHovered) {
					if (mouseX >= curX && mouseX < curX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
						hoveredStepStack = input;
						hoveredStepValue = String.format("%.4f", step.getPrimaryInputRate() / CalculatorState.getMultiplier()) + rateLabels[CalculatorState.getDisplayRate()];
						hoverMachine = false;
					}
				}
			}
			curX += 18;
			// More inputs
			int width = 11;
			if (step.getInputs().size() > 1) {
				String text = "+" + (step.getInputs().size() - 1);
				this.fontRenderer.drawString(text, curX, iconY + 4, 0xFF000000);
				width = Math.max(width, this.fontRenderer.getStringWidth(text));
			}
			curX += width + 2;

			// Arrow
			drawArrow(curX, rowY + TABLE_ROW_H / 2 - 2, 0xFFFFFFFF);
			curX += 7;

			// Machine (machine cannot be empty)
			int machineIconX = curX;
			step.getMachine().renderValue(this, machineIconX, iconY, step.getMachineCount());
			if (rowHovered) {
				if (mouseX >= machineIconX && mouseX < machineIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
					hoveredStepStack = step.getMachine();
					hoveredStepValue = String.format("%.4f", step.getMachineCount());
					hoverMachine = true;
				}
			}
			curX += 18;

			// Arrow
			drawArrow(curX, rowY + TABLE_ROW_H / 2 - 2, 0xFFFFFFFF);
			curX += 7;

			// Primary output and rate (output cannot be empty)
			int outputIconX = curX;
			Ingredients output = step.getPrimaryOutput();
			output.renderValue(this, outputIconX, iconY, step.getPrimaryOutputRate() / CalculatorState.getMultiplier());
			if (rowHovered) {
				if (mouseX >= outputIconX && mouseX < outputIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
					hoveredStepStack = output;
					hoveredStepValue = String.format("%.4f", step.getPrimaryOutputRate() / CalculatorState.getMultiplier()) + rateLabels[CalculatorState.getDisplayRate()];
					hoverMachine = false;
				}
			}
			curX += 18;
			// More outputs
			if (step.getOutputs().size() > 1) {
				this.fontRenderer.drawString("+" + (step.getOutputs().size() - 1), curX, iconY + 4, 0xFF000000);
			}

			// Hide button
			int eyeX = rowX + rowW - 14;
			int eyeY = rowY + (TABLE_ROW_H - 12) / 2;
			drawEyeButton(eyeX, eyeY, mouseX, mouseY, true, true);
		}
	}

	private void drawInputTable(int y, int mouseX, int mouseY) {
		List<Map.Entry<Ingredients, Solver.Input>> visible = new ArrayList<>(CalculatorState.getVisibleInputs().entrySet());
		int maxScroll = Math.max(0, visible.size() - TABLE_VIS_ROWS);
		this.inputScrollRow = Math.max(0, Math.min(this.inputScrollRow, maxScroll));

		int rowX = this.gx + INDENT_L + 1;
		int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
		hoveredStepStack = Ingredients.EMPTY;

		if (visible.size() > TABLE_VIS_ROWS) {
			rowW -= SB_W;
			drawInputScrollBar();
		} else {
			this.inputScrollRow = 0;
			this.inputScrollPercent = 0.0f;
		}

		// Input rates
		for (int i = 0; i < TABLE_VIS_ROWS; i++) {
			int idx = i + this.inputScrollRow;
			int rowY = 1 + y + i * TABLE_ROW_H;
			
			if (idx >= visible.size()) break; // no more steps to show

			Ingredients input = visible.get(idx).getKey();
			Solver.Input inputData = visible.get(idx).getValue();
			boolean rowHovered = mouseX >= rowX && mouseX < rowX + rowW && mouseY >= rowY && mouseY < rowY + TABLE_ROW_H;
			
			int rowBg = rowHovered ? 0xFFA8B8D8 : 0xFFACACAC;
			drawRectPanelOutdent(rowX, rowY, rowW, TABLE_ROW_H, rowBg);

			input.renderValue(this, rowX + 2, rowY + 2, inputData.rate / CalculatorState.getMultiplier());
			if (rowHovered) {
				if (mouseX >= rowX + 2 && mouseX < rowX + 18 && mouseY >= rowY + 2 && mouseY < rowY + 18) {
					hoveredStepStack = input;
					hoveredStepValue = "";
					hoverMachine = false;
				}
			}
			this.fontRenderer.drawString(String.format("%.4f", inputData.rate / CalculatorState.getMultiplier()) + rateLabels[CalculatorState.getDisplayRate()], rowX + 20, rowY + 7, 0xFF000000);

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
				if (showProd) {
					CalculatorState.showAllRecipes();
				} else {
					CalculatorState.showAllInputs();
				}
			}
			return;
		}

		// Production step hide/show toggle (left click)
		int prodRow = getProdRowAt(mouseX, mouseY);
		if (prodRow != -1) {
			if (mouseButton == 0) {
				if (showProd) {
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

					// Open recipe view (also left click)
					if (CalculatorState.getVisibleRecipes().size() <= TABLE_VIS_ROWS || mouseY < this.gy + TAB_H + 35) {
						// open recipe view
						this.recipeViewHelper.open(visible.get(prodRow));
						return;
					}
				} else {
					List<Map.Entry<Ingredients, Solver.Input>> visible = new ArrayList<>(CalculatorState.getVisibleInputs().entrySet());
					int rowX = gx + INDENT_L + 1;
					int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
					if (visible.size() > TABLE_VIS_ROWS) rowW -= SB_W + 2;
					int rowY = 1 + prodTableY + (prodRow - inputScrollRow) * TABLE_ROW_H;
					int eyeX = rowX + rowW - 14;
					int eyeY = rowY + (TABLE_ROW_H - HIDE_SIZE) / 2;
					if (mouseX >= eyeX && mouseX < eyeX + HIDE_SIZE
							&& mouseY >= eyeY && mouseY < eyeY + HIDE_SIZE) {
						CalculatorState.hideInput(visible.get(prodRow).getKey());
						this.inputScrollRow = Math.min(this.inputScrollRow, Math.max(0, CalculatorState.getVisibleInputs().size() - TABLE_VIS_ROWS));
						this.inputScrollPercent = (float) this.inputScrollRow / Math.max(1, CalculatorState.getVisibleInputs().size() - TABLE_VIS_ROWS);
						return;
					}
				}
			}
		}

		// Change view mode button
		if (mouseX >= this.gx + 147 && mouseX < this.gx + 159 && mouseY >= this.gy + TAB_H + 77 && mouseY < this.gy + TAB_H + 89) {
			if (mouseButton == 0) {
				showProd = !showProd;
			}
			return;
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
				if (showProd) {
					this.draggingProdScroll = true;
					this.prodScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 91, 112);
					this.prodScrollRow = (int) (this.prodScrollPercent * (CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS));
				} else {
					this.draggingInputScroll = true;
					this.inputScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 91, 112);
					this.inputScrollRow = (int) (this.inputScrollPercent * (CalculatorState.getVisibleInputs().size() - TABLE_VIS_ROWS));
				}
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
		if (this.draggingInputScroll) {
			this.inputScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 91, 112);
			this.inputScrollRow = (int) (this.inputScrollPercent * Math.max(0, CalculatorState.getVisibleInputs().size() - TABLE_VIS_ROWS));
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
		if (this.draggingInputScroll) {
			this.draggingInputScroll = false;
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
				if (showProd) {
					int scrollRows = Math.max(0, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS);
					if (scrollRows <= 0) return;
					if (scroll > 0) {
						this.prodScrollRow = Math.max(0, this.prodScrollRow - 1);
					} else {
						this.prodScrollRow = Math.min(Math.max(0, CalculatorState.getVisibleRecipes().size() - TABLE_VIS_ROWS), this.prodScrollRow + 1);
					}
					this.prodScrollPercent = (float) this.prodScrollRow / scrollRows;
					return;
				} else {
					int scrollRows = Math.max(0, CalculatorState.getVisibleInputs().size() - TABLE_VIS_ROWS);
					if (scrollRows <= 0) return;
					if (scroll > 0) {
						this.inputScrollRow = Math.max(0, this.inputScrollRow - 1);
					} else {
						this.inputScrollRow = Math.min(Math.max(0, CalculatorState.getVisibleInputs().size() - TABLE_VIS_ROWS), this.inputScrollRow + 1);
					}
					this.inputScrollPercent = (float) this.inputScrollRow / scrollRows;
					return;
				}
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
				RecipeState.reset();
				mc.displayGuiScreen(new GuiRecipeEditor(playerInv, this, true));
				break;
		}
	}

	private int getTargetSlotAt(int mouseX, int mouseY) {
		int row = this.targetScrollRow + (mouseY - this.targetGridY) / SLOT_SIZE;
		int col = (mouseX - (this.gx + INDENT_L + 4)) / SLOT_SIZE;
		if (row < 0 || row >= TARGET_ROWS) return -1;
		if (col < 0 || col >= SLOTS_PER_ROW) return -1;
		return row * SLOTS_PER_ROW + col;
	}
	private int getProdRowAt(int mouseX, int mouseY) {
		int rowX = this.gx + INDENT_L + 1;
		int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
		if (mouseX < rowX || mouseX >= rowX + rowW) return -1;

		int prodY = this.gy + TAB_H + 35 + 6 + TARGET_ROWS * SLOT_SIZE + 18;
		if (mouseY < prodY || mouseY >= prodY + TABLE_VIS_ROWS * TABLE_ROW_H) return -1;

		return this.prodScrollRow + (mouseY - (prodY)) / TABLE_ROW_H;
	}

	public int getTargetScrollRow() {
		return this.targetScrollRow;
	}

	public Rectangle getTargetSlotArea(int visRow, int col) {
		int x = this.gx + INDENT_L + 4 + col * SLOT_SIZE;
		int y = this.targetGridY + visRow * SLOT_SIZE;
		return new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
	}

	@Override
	public Ingredients getHoveredStack(int mouseX, int mouseY) {
		if (mouseX < this.gx) {
			return recipeViewHelper.getHoveredStack(mouseX, mouseY);
		} else if (mouseX >= this.gx + GUI_WIDTH || mouseY < this.gy || mouseY >= this.gy + GUI_HEIGHT + TAB_H) {
			return Ingredients.EMPTY;
		}
		int targetSlot = getTargetSlotAt(mouseX, mouseY);
		if (targetSlot != -1) {
			return CalculatorState.getTargetSlot(targetSlot);
		}
		int prodRow = getProdRowAt(mouseX, mouseY);
		if (prodRow != -1) {
			if (showProd) {
				List<ProductionStep> visible = CalculatorState.getVisibleRecipes();
				if (prodRow < visible.size()) {
					ProductionStep step = visible.get(prodRow);
					int rowX = this.gx + INDENT_L + 1;
					int rowW = GUI_WIDTH - INDENT_L - INDENT_R - 2;
					if (visible.size() > TABLE_VIS_ROWS) rowW -= SB_W + 2;
					int rowY = 1 + prodTableY + (prodRow - prodScrollRow) * TABLE_ROW_H;
					int iconY = rowY + 2;

					// Primary input
					Ingredients input = step.getPrimaryInput();
					if (input != null) {
						int iconX = rowX + 2;
						if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
							return input;
						}
					}

					// Machine
					int machineIconX = rowX + 40;
					if (mouseX >= machineIconX && mouseX < machineIconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
						return step.getMachine();
					}
					
					// None of the other icons were hovered, default to output item
					return step.getPrimaryOutput();
				}
			} else {
				List<Map.Entry<Ingredients, Solver.Input>> visible = new ArrayList<>(CalculatorState.getVisibleInputs().entrySet());
				if (prodRow < visible.size()) {
					return visible.get(prodRow).getKey();
				}
			}
		}
		return Ingredients.EMPTY;
	}

	@Override
	public List<Rectangle> getExtraGuiArea() {
		return recipeViewHelper.area();
	}
}
