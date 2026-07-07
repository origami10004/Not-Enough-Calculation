package com.origami10004.necalc.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.ingredient.*;

// This class is used for both the recipe editor and the new recipe GUI as they share very similar layout
public class GuiRecipeEditor extends GuiCommon {
	// GUI textures
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/edit_recipe.png");
	private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("necalc", "textures/gui/recipe_buttons.png");

	// constants for gui layout
	private static final int GUI_WIDTH = 184;
	private static final int GUI_HEIGHT = 266;
	private static final int IO_ROWS = 2;
	private static final int SLOTS_PER_ROW = 8;
	private static final int SLOT_SIZE = 18;
	private static final int FIELD_WIDTH = 50;
	private static final int FIELD_HEIGHT = 18;
	private static final int BUTTON_SIZE = 20;


	@Override
	public int getActiveTab() {
		return 4;
	}

	// instance variables
	private final GuiCommon parent;
	private final InventoryPlayer playerInv;
	private final boolean isNewRecipe;
	private int inputGrid;
	private int outputGrid;
	private int machineY;
	private int buttonY;
	private GuiTextField timeInputField;

	private int inputScrollRow = 0;
	private float inputScrollPercent = 0.0f;
	private boolean draggingInputScroll = false;
	private int outputScrollRow = 0;
	private float outputScrollPercent = 0.0f;
	private boolean draggingOutputScroll = false;

	private int gx, gy;

	public GuiRecipeEditor(InventoryPlayer playerInv, GuiCommon parent, boolean isNewRecipe) {
		super(new FakeContainer(playerInv, true, 9, 213));
		this.parent = parent;
		this.playerInv = playerInv;
		this.isNewRecipe = isNewRecipe;
		if (isNewRecipe) {
			RecipeState.reset();
		}
	}
	
	@Override
	public void initGui() {
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT + TAB_H;
		super.initGui();
		this.gx = guiLeft;
		this.gy = guiTop;
		if (this.timeInputField == null) {
			this.timeInputField = new GuiTextField(0, this.fontRenderer, this.gx + GUI_WIDTH / 2, this.gy + 106, FIELD_WIDTH, FIELD_HEIGHT);
			this.timeInputField.setText(String.valueOf(RecipeState.getTime()));
			this.timeInputField.setCursorPositionEnd();
			this.timeInputField.setFocused(false);
		} else {
			this.timeInputField.x = this.gx + GUI_WIDTH / 2;
			this.timeInputField.y = this.gy + 106;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		this.mc.getTextureManager().bindTexture(BG_TEXTURE);
		drawModalRectWithCustomSizedTexture(this.gx, this.gy + TAB_H, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		drawTabStrip(gx, gy);

		int curY = gy + TAB_H;

		if (isNewRecipe) {
			this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.add"), this.gx + 8, curY + 6, 0xFF000000);
		} else {
			this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.edit"), this.gx + 8, curY + 6, 0xFF000000);
		}
		curY += 13;

		// inputs
		this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.input"), this.gx + 12, curY + 6, 0xFF000000);
		this.inputGrid = curY + 16;
		for (int row = 0; row < IO_ROWS; row++) {
			int actual = row + this.inputScrollRow;
			for (int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + 12 + col * SLOT_SIZE;
				int slotY = this.inputGrid + row * SLOT_SIZE;
				this.drawItemSlot(slotX, slotY);

				Ingredients curIng = RecipeState.getInput(actual * SLOTS_PER_ROW + col);
				curIng.renderValue(this, slotX + 1, slotY + 1);
			}
		}
		drawInputScrollBar();
		curY += IO_ROWS * SLOT_SIZE + 18;

		// machine
		this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.machine"), this.gx + 12, curY, 0xFF000000);
		int machineX = this.gx + 12;
		this.machineY = curY + 10;
		this.drawItemSlot(machineX, machineY);
		RecipeState.getMachine().render(this, machineX + 1, machineY + 1);

		// time entry
		this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.time"), this.gx + GUI_WIDTH / 2, curY, 0xFF000000);
		this.timeInputField.drawTextBox();


		curY += SLOT_SIZE + 12;

		// Outputs
		this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.output"), this.gx + 12, curY, 0xFF000000);
		this.outputGrid = curY + 10;
		for (int row = 0; row < IO_ROWS; row++) {
			int actual = row + this.outputScrollRow;
			for (int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + 12 + col * SLOT_SIZE;
				int slotY = this.outputGrid + row * SLOT_SIZE;
				this.drawItemSlot(slotX, slotY);

				Ingredients curIng = RecipeState.getOutput(actual * SLOTS_PER_ROW + col);
				curIng.renderValue(this, slotX + 1, slotY + 1);
			}
		}
		drawOutputScrollBar();
		curY += IO_ROWS * SLOT_SIZE + 14;

		// Buttons
		this.buttonY = curY;
		drawButton(this.gx + 12, curY, BUTTON_SIZE, BUTTON_SIZE, "", mouseX, mouseY, false, true);
		drawButton(this.gx + 32, curY, BUTTON_SIZE, BUTTON_SIZE, "", mouseX, mouseY, false, true);
		this.mc.getTextureManager().bindTexture(BUTTON_TEXTURE);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		drawModalRectWithCustomSizedTexture(this.gx + 14, curY + 2, 0, 0, 16, 16, 48, 16);
		drawModalRectWithCustomSizedTexture(this.gx + 34, curY + 2, 16, 0, 16, 16, 48, 16);
		if (!isNewRecipe) {
			drawButton(this.gx + 52, curY, BUTTON_SIZE, BUTTON_SIZE, "", mouseX, mouseY, false, true);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			drawModalRectWithCustomSizedTexture(this.gx + 54, curY + 2, 32, 0, 16, 16, 48, 16);
		}
		curY += 26;
		
		this.fontRenderer.drawString(I18n.format("necalc.gui.inventory"), this.gx + 8, curY, 0xFF000000);
		drawPlayerInventory(this.gx + 8, curY + 11, mouseX, mouseY, this.playerInv);
	}

	@Override
	public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

		drawTabTooltips(mouseX - this.guiLeft, mouseY - this.guiTop, this.gx - this.guiLeft, this.gy - this.guiTop);

		// Inputs
		int hoverSlot = getInputSlotAt(mouseX, mouseY);
		if (hoverSlot != -1) {
			Ingredients ing = RecipeState.getInput(hoverSlot);
			this.drawHoveringText(ing.getTooltip(this.mc), mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Machine
		if (mouseX >= this.gx + 12 && mouseX < this.gx + 12 + SLOT_SIZE
					&& mouseY >= this.machineY && mouseY < this.machineY + SLOT_SIZE) {
			Ingredients ing = RecipeState.getMachine();
			this.drawHoveringText(ing.getTooltip(this.mc), mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// Outputs
		hoverSlot = getOutputSlotAt(mouseX, mouseY);
		if (hoverSlot != -1) {
			Ingredients ing = RecipeState.getOutput(hoverSlot);
			this.drawHoveringText(ing.getTooltip(this.mc), mouseX - this.guiLeft, mouseY - this.guiTop);
			return;
		}

		// Buttons
		if (mouseX >= this.gx + 12 && mouseX < this.gx + 12 + BUTTON_SIZE
					&& mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_SIZE) {
			this.drawHoveringText(I18n.format("necalc.gui.recipe.confirm"), mouseX - this.guiLeft, mouseY - this.guiTop);
			return;
		}
		if (mouseX >= this.gx + 32 && mouseX < this.gx + 32 + BUTTON_SIZE
					&& mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_SIZE) {
			this.drawHoveringText(I18n.format("necalc.gui.recipe.cancel"), mouseX - this.guiLeft, mouseY - this.guiTop);
			return;
		}
		if (!this.isNewRecipe && mouseX >= this.gx + 52 && mouseX < this.gx + 52 + BUTTON_SIZE
					&& mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_SIZE) {
			this.drawHoveringText(I18n.format("necalc.gui.recipe.delete"), mouseX - this.guiLeft, mouseY - this.guiTop);
			return;
		}

		// Inventory
		this.drawPlayerInventoryTooltips(mouseX - this.guiLeft, mouseY - this.guiTop);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		this.timeInputField.updateCursorCounter();
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT || keyCode == Keyboard.KEY_ESCAPE) {
			if (this.timeInputField.textboxKeyTyped(typedChar, keyCode)) return;
		}
		if (keyCode == Keyboard.KEY_ESCAPE) {
			mc.displayGuiScreen(parent);
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void handleMouseInput() throws IOException {
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			boolean shiftPressed = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
			boolean ctrlPressed = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

			int inputSlot = getInputSlotAt(mouseX, mouseY);
			int outputSlot = getOutputSlotAt(mouseX, mouseY);

			if (inputSlot != -1) {
				if (shiftPressed && ctrlPressed) {
					if (scroll > 0) {
						RecipeState.alterInput(inputSlot, Ingredients.EMPTY, 0, 2);
					} else {
						RecipeState.alterInput(inputSlot, Ingredients.EMPTY, 0, 0.5);
					}
					return;
				} else if (shiftPressed) {
					if (scroll > 0) {
						RecipeState.alterInput(inputSlot, Ingredients.EMPTY, 1, 1);
					} else {
						RecipeState.alterInput(inputSlot, Ingredients.EMPTY, -1, 1);
					}
					return;
				}
			}
			if (outputSlot != -1) {
				if (shiftPressed && ctrlPressed) {
					if (scroll > 0) {
						RecipeState.alterOutput(outputSlot, Ingredients.EMPTY, 0, 2);
					} else {
						RecipeState.alterOutput(outputSlot, Ingredients.EMPTY, 0, 0.5);
					}
					return;
				} else if (shiftPressed) {
					if (scroll > 0) {
						RecipeState.alterOutput(outputSlot, Ingredients.EMPTY, 1, 1);
					} else {
						RecipeState.alterOutput(outputSlot, Ingredients.EMPTY, -1, 1);
					}
					return;
				}
			}

			if (mouseX >= this.gx + 8 && mouseX < this.gx + GUI_WIDTH - 8
					&& mouseY >= this.gy + TAB_H + 16 && mouseY < this.gy + TAB_H + 65) {
				int scrollRows = Math.max(0, RecipeState.getInputRows() - IO_ROWS);
				if (scrollRows <= 0) return;
				if (scroll > 0) {
					this.inputScrollRow = Math.max(0, this.inputScrollRow - 1);
				} else {
					this.inputScrollRow = Math.min(this.inputScrollRow + 1, Math.max(0, RecipeState.getInputRows() - IO_ROWS));
				}
				this.inputScrollPercent = (float) this.inputScrollRow / (RecipeState.getInputRows() - IO_ROWS);
				return;
			}
			if (mouseX >= this.gx + 8 && mouseX < this.gx + GUI_WIDTH - 8
					&& mouseY >= this.gy + TAB_H + 94 && mouseY < this.gy + TAB_H + 143) {
				int scrollRows = Math.max(0, RecipeState.getOutputRows() - IO_ROWS);
				if (scrollRows <= 0) return;
				if (scroll > 0) {
					this.outputScrollRow = Math.max(0, this.outputScrollRow - 1);
				} else {
					this.outputScrollRow = Math.min(this.outputScrollRow + 1, Math.max(0, RecipeState.getOutputRows() - IO_ROWS));
				}
				this.outputScrollPercent = (float) this.outputScrollRow / (RecipeState.getOutputRows() - IO_ROWS);
				return;
			}
		}
		super.handleMouseInput();
	}

	private void drawInputScrollBar() {
		int totalRows = RecipeState.getInputRows();
		int sbX = this.gx + 159;
		int sbY = this.gy + TAB_H + 30;
		int width = 12;
		int height = 34;
		if (totalRows <= IO_ROWS) {
			this.inputScrollRow = 0;
			this.inputScrollPercent = 0.0f;
		}
		drawScrollbar(sbX, sbY, width, height, this.inputScrollPercent, totalRows > IO_ROWS);
	}

	private void drawOutputScrollBar() {
		int totalRows = RecipeState.getOutputRows();
		int sbX = this.gx + 159;
		int sbY = this.gy + TAB_H + 108;
		int width = 12;
		int height = 34;
		if (totalRows <= IO_ROWS) {
			this.outputScrollRow = 0;
			this.outputScrollPercent = 0.0f;
		}
		drawScrollbar(sbX, sbY, width, height, this.outputScrollPercent, totalRows > IO_ROWS);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (int i = 0; i < TAB_COUNT; i++) {
			int tx = gx + TAB_LEFT_PAD + i * (TAB_W + 2);
			if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= gy + 2 && mouseY < gy + 2 + TAB_H) {
				onTabClicked(i);
				return;
			}
		}

		if (mouseX >= timeInputField.x && mouseX < timeInputField.x + timeInputField.width
				&& mouseY >= timeInputField.y && mouseY < timeInputField.y + timeInputField.height) {
			if (mouseButton == 1) {
				this.timeInputField.setText("");
				this.timeInputField.setFocused(true);
			} else {
				this.timeInputField.mouseClicked(mouseX, mouseY, mouseButton);
			}
			return;
		}
		if (this.timeInputField.mouseClicked(mouseX, mouseY, mouseButton)) return;

		// Inputs
		int inputSlot = getInputSlotAt(mouseX, mouseY);
		if (inputSlot != -1) {
			Ingredients heldItem = IngredientManager.of(mc.player.inventory.getItemStack());
			if (mouseButton == 0) {
				RecipeState.setInput(inputSlot, heldItem);
			} else if (mouseButton == 1) {
				Ingredients currentIng = RecipeState.getInput(inputSlot);
				if (!heldItem.isEmpty() && 
						(currentIng.isEmpty() || currentIng.equals(heldItem))) {
					RecipeState.alterInput(inputSlot, heldItem, 1, 1);
				}
			}
			return;
		}

		// Machine
		if (mouseX >= this.gx + 12 && mouseX < this.gx + 12 + SLOT_SIZE
					&& mouseY >= this.machineY && mouseY < this.machineY + SLOT_SIZE) {
			Ingredients heldItem = IngredientManager.of(mc.player.inventory.getItemStack());
			if (mouseButton == 0) {
				RecipeState.setMachine(heldItem);
			}
			return;
		}

		// Outputs
		int outputSlot = getOutputSlotAt(mouseX, mouseY);
		if (outputSlot != -1) {
			Ingredients heldItem = IngredientManager.of(mc.player.inventory.getItemStack());
			if (mouseButton == 0) {
				RecipeState.setOutput(outputSlot, heldItem);
			} else if (mouseButton == 1) {
				Ingredients currentIng = RecipeState.getOutput(outputSlot);
				if (!heldItem.isEmpty() && 
						(currentIng.isEmpty() || currentIng.equals(heldItem))) {
					RecipeState.alterOutput(outputSlot, heldItem, 1, 1);
				}
			}
			return;
		}

		// Buttons
		if (mouseX >= this.gx + 12 && mouseX < this.gx + 12 + BUTTON_SIZE
					&& mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_SIZE) {
			RecipeState.confirmRecipe(this.timeInputField.getText().isEmpty() ? 1 : Integer.parseInt(this.timeInputField.getText()));
			mc.displayGuiScreen(parent);
		}
		if (mouseX >= this.gx + 32 && mouseX < this.gx + 32 + BUTTON_SIZE
					&& mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_SIZE) {
			mc.displayGuiScreen(parent);
			return;
		}
		if (!this.isNewRecipe && mouseX >= this.gx + 52 && mouseX < this.gx + 52 + BUTTON_SIZE
					&& mouseY >= this.buttonY && mouseY < this.buttonY + BUTTON_SIZE) {
			RecipeState.deleteRecipe();
			mc.displayGuiScreen(parent);
			return;
		}

		// input scroll
		if (mouseX >= this.gx + 158 && mouseX < this.gx + 172
					&& mouseY >= this.gy + TAB_H + 29 && mouseY < this.gy + TAB_H + 65) {
			if (mouseButton == 0) {
				this.draggingInputScroll = true;
				this.inputScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 29, 36);
				this.inputScrollRow = (int) (this.inputScrollPercent * Math.max(0, RecipeState.getInputRows() - IO_ROWS));
			}
			return;
		}

		// output scroll
		if (mouseX >= this.gx + 158 && mouseX < this.gx + 172
					&& mouseY >= this.gy + TAB_H + 107 && mouseY < this.gy + TAB_H + 143) {
			if (mouseButton == 0) {
				this.draggingOutputScroll = true;
				this.outputScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 107, 36);
				this.outputScrollRow = (int) (this.outputScrollPercent * Math.max(0, RecipeState.getOutputRows() - IO_ROWS));
			}
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (this.draggingInputScroll) {
			this.inputScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 29, 36);
			this.inputScrollRow = (int) (this.inputScrollPercent * Math.max(0, RecipeState.getInputRows() - IO_ROWS));
			return;
		}
		if (this.draggingOutputScroll) {
			this.outputScrollPercent = updateScroll(mouseY, this.gy + TAB_H + 107, 36);
			this.outputScrollRow = (int) (this.outputScrollPercent * Math.max(0, RecipeState.getOutputRows() - IO_ROWS));
			return;
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.draggingInputScroll) {
			this.draggingInputScroll = false;
			return;
		}
		if (this.draggingOutputScroll) {
			this.draggingOutputScroll = false;
			return;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}


	// Helper functions
	private void onTabClicked(int index) {
		switch (index) {
			case 0:
				mc.displayGuiScreen(new GuiProductionCalc(this.playerInv));
				break;
			case 1:
				// mc.displayGuiScreen(new GuiFlowChart(playerInv, container));
				break;
			case 2:
				mc.displayGuiScreen(new GuiManageRecipes(this.playerInv));
				break;
			case 3:
				mc.displayGuiScreen(new GuiManageMachines(this.playerInv));
				break;
			case 4:
				break;
		}
	}

	private int getInputSlotAt(int mouseX, int mouseY) {
		for (int row = 0; row < IO_ROWS; row++) {
			int actual = row + this.inputScrollRow;
			for(int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + 12 + col * SLOT_SIZE;
				if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE
							&& mouseY >= this.inputGrid + row * SLOT_SIZE
							&& mouseY < this.inputGrid + row * SLOT_SIZE + SLOT_SIZE) {
					return actual * SLOTS_PER_ROW + col;
				}
			}
		}
		return -1;
	}

	private int getOutputSlotAt(int mouseX, int mouseY) {
		for (int row = 0; row < IO_ROWS; row++) {
			int actual = row + this.outputScrollRow;
			for(int col = 0; col < SLOTS_PER_ROW; col++) {
				int slotX = this.gx + 12 + col * SLOT_SIZE;
				if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE
							&& mouseY >= this.outputGrid + row * SLOT_SIZE
							&& mouseY < this.outputGrid + row * SLOT_SIZE + SLOT_SIZE) {
					return actual * SLOTS_PER_ROW + col;
				}
			}
		}
		return -1;
	}
}
