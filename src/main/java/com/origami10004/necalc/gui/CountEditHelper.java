package com.origami10004.necalc.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.ingredient.Ingredients;

public class CountEditHelper {
	private static final int PANEL_W  = 100;
	private static final int PANEL_H  = 28;
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/count_editor.png");

	private GuiRecipeEditor parent;
	private int activeSlot = -1;
	private boolean isOpen = false;
	private boolean isInput = false;
	private int panelX = 0;
	private int panelY = 0;
	private GuiTextField countInputField;

	public CountEditHelper(GuiRecipeEditor parent) {
		this.parent = parent;
	}

	public void openInputSlot(int slotIndex, int slotX, int slotY) {
		if (slotIndex < 0 || slotIndex >= RecipeState.getStagedRecipe().getInputs().size()) {
			return;
		}
		this.activeSlot = slotIndex;
		isOpen = true;
		isInput = true;
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		
		int currentCount = (int) RecipeState.getStagedRecipe().getInputs().get(slotIndex).getValue();
		this.countInputField = new GuiTextField(0, parent.mc.fontRenderer, this.panelX + 25, this.panelY + 4, PANEL_W - 29, PANEL_H - 8);
		this.countInputField.setText(Integer.toString(currentCount));
		this.countInputField.setCursorPositionEnd();
		this.countInputField.setFocused(true);
	}

	public void openOutputSlot(int slotIndex, int slotX, int slotY) {
		if (slotIndex < 0 || slotIndex >= RecipeState.getStagedRecipe().getOutputs().size()) {
			return;
		}
		this.activeSlot = slotIndex;
		isOpen = true;
		isInput = false;
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		
		int currentCount = (int) RecipeState.getStagedRecipe().getOutputs().get(slotIndex).getValue();
		this.countInputField = new GuiTextField(0, parent.mc.fontRenderer, this.panelX + 25, this.panelY + 4, PANEL_W - 29, PANEL_H - 8);
		this.countInputField.setText(Integer.toString(currentCount));
		this.countInputField.setCursorPositionEnd();
		this.countInputField.setFocused(true);
	}

	public void close() {
		this.activeSlot = -1;
		isOpen = false;
	}

	public boolean isOpen() {
		return isOpen;
	}

	public boolean hovered(int mouseX, int mouseY) {
		return isOpen && mouseX >= panelX && mouseX < panelX + PANEL_W && mouseY >= panelY && mouseY < panelY + PANEL_H;
	}

	public void reInit(int gx, int tableY) {
		if (!isOpen) return;
		int slotX = gx + 12 + (this.activeSlot % GuiRecipeEditor.SLOTS_PER_ROW) * 18;
		int slotY = tableY;
		if (isInput) {
			slotY += (this.activeSlot / GuiRecipeEditor.SLOTS_PER_ROW - parent.inputScrollRow) * 18;
		} else {
			slotY += (this.activeSlot / GuiRecipeEditor.SLOTS_PER_ROW - parent.outputScrollRow) * 18 + 78;
		}
		this.panelX = slotX - 5;
		this.panelY = slotY - 5;
		this.countInputField.x = this.panelX + 25;
		this.countInputField.y = this.panelY + 4;
	}

	public void drawOverlay(int mouseX, int mouseY) {
		if (!isOpen) return;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 0, 300); // Ensure the overlay is drawn above other GUI elements
		this.parent.mc.getTextureManager().bindTexture(BG_TEXTURE);
		this.parent.drawModalRectWithCustomSizedTexture(panelX, panelY, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
		this.countInputField.drawTextBox();
		GlStateManager.popMatrix();
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!isOpen) return false;
		if (mouseX >= panelX && mouseX < panelX + PANEL_W && mouseY >= panelY && mouseY < panelY + PANEL_H) {
			if (mouseButton == 1) {
				this.countInputField.setText("");
				this.countInputField.setFocused(true);
			} else {
				this.countInputField.mouseClicked(mouseX, mouseY, mouseButton);
			}
			return true;
		}
		return this.countInputField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public boolean keyTyped(char typedChar, int keyCode) {
		if (!isOpen) return false;
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			String text = countInputField.getText();
			if (text == "" || Integer.parseInt(text) <= 0) {
				if (isInput) {
					RecipeState.setInput(activeSlot, Ingredients.EMPTY);
				} else {
					RecipeState.setOutput(activeSlot, Ingredients.EMPTY);
				}
			} else {
				if (isInput) {
					RecipeState.getInput(activeSlot).setValue(Integer.parseInt(text));
				} else {
					RecipeState.getOutput(activeSlot).setValue(Integer.parseInt(text));
				}
			}
			close();
			return true;
		}

		if (keyCode == Keyboard.KEY_ESCAPE) {
			close();
			return true;
		}

		boolean ctrlA = keyCode == Keyboard.KEY_A && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
		boolean ctrlC = keyCode == Keyboard.KEY_C && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
		boolean ctrlV = keyCode == Keyboard.KEY_V && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));
		boolean ctrlX = keyCode == Keyboard.KEY_X && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL));

		if (Character.isDigit(typedChar) || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) {
			this.countInputField.textboxKeyTyped(typedChar, keyCode);
		} else if (ctrlA) {
			this.countInputField.setCursorPositionZero();
			this.countInputField.setSelectionPos(this.countInputField.getText().length());
		} else if (ctrlC) {
			String selectedText = this.countInputField.getSelectedText();
			if (!selectedText.isEmpty()) {
				GuiScreen.setClipboardString(selectedText);
			}
		} else if (ctrlV) {
			String clipboardText = GuiScreen.getClipboardString();
			if (clipboardText != null && !clipboardText.isEmpty()) {
				String sanitised = clipboardText.replaceAll("[^0-9.]", "");
				if (this.countInputField.getText().contains(".")) {
					sanitised = sanitised.replaceAll("\\.", "");
				} else if (sanitised.indexOf(".") != sanitised.lastIndexOf(".")) {
					int first = sanitised.indexOf(".");
					sanitised = sanitised.substring(0, first + 1) + sanitised.substring(first + 1).replaceAll("\\.", "");
				}
				this.countInputField.writeText(sanitised);
			}
		} else if (ctrlX) {
			String selectedText = this.countInputField.getSelectedText();
			if (!selectedText.isEmpty()) {
				GuiScreen.setClipboardString(selectedText);
				this.countInputField.writeText("");
			}
		}
		return true;
	}

	public void updateCursorCounter() {
		if (isOpen) this.countInputField.updateCursorCounter();
	}
}
