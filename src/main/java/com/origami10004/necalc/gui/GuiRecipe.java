package com.origami10004.necalc.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import org.lwjgl.input.Keyboard;

import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.MachineState;

public class GuiRecipe extends GuiCommon {
	private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("necalc", "textures/gui/arrow.png");

	private final ProductionStep step;
	private final GuiProductionCalc parent;

	// instance variables
	private int inputX, inputY;
	private int outputX, outputY;
	private int inputRows, inputCols;
	private int outputRows, outputCols;

	@Override
	protected int getActiveTab() {
		return -1;
	}

	public GuiRecipe(GuiProductionCalc parent, InventoryPlayer playerInventory, ProductionStep step) {
		super(new NecalcContainer(playerInventory, false, 0, 0));
		this.parent = parent;
		this.step = step;
	}

	@Override
	public void initGui() {
		this.inputCols = formatRows(step.getInputs().size());
		this.inputRows = (int) Math.ceil((double) step.getInputs().size() / inputCols);
		this.outputCols = formatRows(step.getOutputs().size());
		this.outputRows = (int) Math.ceil((double) step.getOutputs().size() / outputCols);

		this.xSize = 44 + inputCols * 18 + outputCols * 18;
		this.ySize = Math.max(inputRows, outputRows) * 18 + 100;
		super.initGui();

		this.inputX = guiLeft + 7;
		this.inputY = guiTop + (ySize - inputRows * 18) / 2;
		this.outputX = guiLeft + xSize - 7 - outputCols * 18;
		this.outputY = guiTop + (ySize - outputRows * 18) / 2;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		drawRectPanel(guiLeft, guiTop, xSize, ySize);
		this.fontRenderer.drawString(I18n.format("necalc.gui.recipe.title"), guiLeft + 8, guiTop + 6, 0xFF000000);

		for (int i = 0; i < inputRows; i++) {
			for (int j = 0; j < inputCols; j++) {
				int index = i * inputCols + j;
				if (index >= step.getInputs().size()) break;
				drawItemSlot(inputX + j * 18, inputY + i * 18);
				Ingredients currentInput = step.getInputs().get(index);
				currentInput.renderValue(this, inputX + j * 18 + 1, inputY + i * 18 + 1, currentInput.getValue() / CalculatorState.getMultiplier());
			}
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(ARROW_TEXTURE);
		drawModalRectWithCustomSizedTexture(this.inputX + inputCols * 18 + 4, this.guiTop + (this.ySize - 15) / 2, 0, 0, 22, 15, 22, 15);

		step.getMachine().renderValue(this, this.inputX + inputCols * 18 + 7, this.guiTop + (this.ySize - 15) / 2 - 20, step.getMachineCount());

		for (int i = 0; i < outputRows; i++) {
			for (int j = 0; j < outputCols; j++) {
				int index = i * outputCols + j;
				if (index >= step.getOutputs().size()) break;
				drawItemSlot(outputX + j * 18, outputY + i * 18);
				Ingredients currentOutput = step.getOutputs().get(index);
				currentOutput.renderValue(this, outputX + j * 18 + 1, outputY + i * 18 + 1, currentOutput.getValue() / CalculatorState.getMultiplier());
			}
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		// machine
		if (mouseX >= this.inputX + inputCols * 18 + 7 && mouseX < this.inputX + inputCols * 18 + 7 + 16 &&
				mouseY >= this.guiTop + (this.ySize - 15) / 2 - 20 && mouseY < this.guiTop + (this.ySize - 15) / 2 - 20 + 16) {
			this.drawItemExtraInfoTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, step.getMachine(), I18n.format("necalc.gui.machine_count", step.getMachineCount()));
		}
		// arrow
		if (mouseX >= this.inputX + inputCols * 18 + 4 && mouseX < this.inputX + inputCols * 18 + 4 + 22 &&
				mouseY >= this.guiTop + (this.ySize - 15) / 2 && mouseY < this.guiTop + (this.ySize - 15) / 2 + 15) {
			double time = step.getRecipeTime();
			String secStr = String.format("%.2f", time / 20.0);
			String tickStr = String.format("%.2f", time);
			this.drawHoveringText(I18n.format("necalc.gui.recipe.time", secStr, tickStr, MachineState.getMachineSpeeds().get(step.getMachine())), mouseX - this.guiLeft, mouseY - this.guiTop);
		}

		// inputs
		if (getInputAt(mouseX, mouseY) != -1) {
			int index = getInputAt(mouseX, mouseY);
			Ingredients currentInput = step.getInputs().get(index);
			String text = String.format("%.4f", step.getPrimaryInputRate() / CalculatorState.getMultiplier()) + GuiProductionCalc.rateLabels[CalculatorState.getDisplayRate()];
			this.drawItemExtraInfoTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, currentInput, I18n.format("necalc.gui.target_rate", text));
		}

		// outputs
		if (getOutputAt(mouseX, mouseY) != -1) {
			int index = getOutputAt(mouseX, mouseY);
			Ingredients currentOutput = step.getOutputs().get(index);
			String text = String.format("%.4f", step.getPrimaryInputRate() / CalculatorState.getMultiplier()) + GuiProductionCalc.rateLabels[CalculatorState.getDisplayRate()];
			this.drawItemExtraInfoTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, currentOutput, I18n.format("necalc.gui.target_rate", text));
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE || typedChar == 'e') {
			mc.displayGuiScreen(parent);
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}


	// Helpers
	private int formatRows(int count) {
		if (count <= 3) return 1;
		if (count == 4) return 2;
		if (count <= 9) return 3;
		if (count <= 16) return 4;

		int sqrt = (int) Math.sqrt(count);
		if (sqrt * sqrt == count) return sqrt;
		int range = sqrt / 2;
		for (int i = 1; i < range; i++) {
			if (count % (sqrt - i) == 0) return sqrt - i;
			if (count % (sqrt + i) == 0) return sqrt + i;
		}
		return sqrt;
	}

	private int getInputAt(int mouseX, int mouseY) {
		if (mouseX < inputX || mouseX >= inputX + inputCols * 18 || mouseY < inputY || mouseY >= inputY + inputRows * 18) {
			return -1;
		}
		int col = (mouseX - inputX) / 18;
		int row = (mouseY - inputY) / 18;
		int index = row * inputCols + col;
		if (index >= step.getInputs().size()) return -1;
		return index;
	}

	private int getOutputAt(int mouseX, int mouseY) {
		if (mouseX < outputX || mouseX >= outputX + outputCols * 18 || mouseY < outputY || mouseY >= outputY + outputRows * 18) {
			return -1;
		}
		int col = (mouseX - outputX) / 18;
		int row = (mouseY - outputY) / 18;
		int index = row * outputCols + col;
		if (index >= step.getOutputs().size()) return -1;
		return index;
	}
}
