package com.origami10004.necalc.gui;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.MachineState;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.ingredient.Ingredients;

import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;

import java.util.List;
import java.util.ArrayList;
import java.awt.Rectangle;

public class RecipeViewHelper {
	private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("necalc", "textures/gui/arrow.png");

	private final GuiProductionCalc parent;
	private boolean isOpen = false;
	private ProductionStep step = null;
	private int inputX, inputY;
	private int outputX, outputY;
	private int inputRows, inputCols;
	private int outputRows, outputCols;
	private int xSize, ySize;
	private int guiLeft, guiTop;

	private int parentX, parentY;

	public RecipeViewHelper(GuiProductionCalc parent) {
		this.parent = parent;
	}

	public void init(int x, int y) {
		this.parentX = x;
		this.parentY = y;

		if (!isOpen()) return;

		this.inputCols = formatRows(step.getInputs().size());
		this.inputRows = (int) Math.ceil((double) step.getInputs().size() / inputCols);
		this.outputCols = formatRows(step.getOutputs().size());
		this.outputRows = (int) Math.ceil((double) step.getOutputs().size() / outputCols);

		this.xSize = 44 + inputCols * 18 + outputCols * 18;
		this.ySize = Math.max(Math.max(inputRows, outputRows) * 18 + 14, 65);

		this.guiLeft = parentX - xSize;
		this.guiTop = parentY + 92;

		this.inputX = guiLeft + 7;
		this.inputY = guiTop + (ySize - inputRows * 18) / 2;
		this.outputX = guiLeft + xSize - 7 - outputCols * 18;
		this.outputY = guiTop + (ySize - outputRows * 18) / 2;
	}

	public void open(ProductionStep step) {
		this.step = step;
		this.isOpen = true;
		init(this.parentX, this.parentY);
	}

	public boolean isOpen() {
		return this.isOpen && this.step != null;
	}
	
	public void drawExtension(int mouseX, int mouseY) {
		if (!isOpen()) return;

		parent.drawRectPanel(guiLeft, guiTop, xSize, ySize);
		
		for (int i = 0; i < inputRows; i++) {
			for (int j = 0; j < inputCols; j++) {
				int index = i * inputCols + j;
				if (index >= step.getInputs().size()) break;
				parent.drawItemSlot(inputX + j * 18, inputY + i * 18);
				Ingredients currentInput = step.getInputs().get(index);
				currentInput.renderValue(parent, inputX + j * 18 + 1, inputY + i * 18 + 1, step.getInputRate(index) / CalculatorState.getMultiplier());
			}
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		parent.mc.getTextureManager().bindTexture(ARROW_TEXTURE);
		GuiProductionCalc.drawModalRectWithCustomSizedTexture(this.inputX + inputCols * 18 + 4, this.guiTop + (this.ySize - 15) / 2, 0, 0, 22, 15, 22, 15);

		step.getMachine().renderValue(parent, this.inputX + inputCols * 18 + 7, this.guiTop + (this.ySize - 15) / 2 - 20, step.getMachineCount());

		for (int i = 0; i < outputRows; i++) {
			for (int j = 0; j < outputCols; j++) {
				int index = i * outputCols + j;
				if (index >= step.getOutputs().size()) break;
				parent.drawItemSlot(outputX + j * 18, outputY + i * 18);
				Ingredients currentOutput = step.getOutputs().get(index);
				currentOutput.renderValue(parent, outputX + j * 18 + 1, outputY + i * 18 + 1, step.getOutputRate(index) / CalculatorState.getMultiplier());
			}
		}
	}

	public void drawTooltip(int mouseX, int mouseY) {
		if (!isOpen()) return;

		// machine
		if (mouseX >= this.inputX + inputCols * 18 + 7 && mouseX < this.inputX + inputCols * 18 + 7 + 16 &&
				mouseY >= this.guiTop + (this.ySize - 15) / 2 - 20 && mouseY < this.guiTop + (this.ySize - 15) / 2 - 20 + 16) {
			parent.drawItemExtraInfoTooltip(mouseX, mouseY, step.getMachine(), I18n.format("necalc.gui.machine_count", step.getMachineCount()));
		}
		// arrow
		if (mouseX >= this.inputX + inputCols * 18 + 4 && mouseX < this.inputX + inputCols * 18 + 4 + 22 &&
				mouseY >= this.guiTop + (this.ySize - 15) / 2 && mouseY < this.guiTop + (this.ySize - 15) / 2 + 15) {
			double time = step.getRecipeTime();
			String secStr = String.format("%.2f", time / 20.0);
			String tickStr = String.format("%.2f", time);
			parent.drawHoveringText(I18n.format("necalc.gui.arrow_time", secStr, tickStr, MachineState.getMachineSpeeds().get(step.getMachine())), mouseX, mouseY);
		}

		// inputs
		if (getInputAt(mouseX, mouseY) != -1) {
			int index = getInputAt(mouseX, mouseY);
			Ingredients currentInput = step.getInputs().get(index);
			String text = String.format("%.4f", step.getPrimaryInputRate() / CalculatorState.getMultiplier()) + GuiProductionCalc.rateLabels[CalculatorState.getDisplayRate()];
			parent.drawItemExtraInfoTooltip(mouseX, mouseY, currentInput, I18n.format("necalc.gui.target_rate", text));
		}

		// outputs
		if (getOutputAt(mouseX, mouseY) != -1) {
			int index = getOutputAt(mouseX, mouseY);
			Ingredients currentOutput = step.getOutputs().get(index);
			String text = String.format("%.4f", step.getPrimaryInputRate() / CalculatorState.getMultiplier()) + GuiProductionCalc.rateLabels[CalculatorState.getDisplayRate()];
			parent.drawItemExtraInfoTooltip(mouseX, mouseY, currentOutput, I18n.format("necalc.gui.target_rate", text));
		}
	}

	public List<Rectangle> area() {
		if (!isOpen()) return null;
		List<Rectangle> res = new ArrayList<>();
		res.add(new Rectangle(guiLeft, guiTop, xSize, ySize));
		return res;
	}

	public Ingredients getHoveredStack(int mouseX, int mouseY) {
		if (!isOpen()) return Ingredients.EMPTY;
		int inputIndex = getInputAt(mouseX, mouseY);
		if (inputIndex != -1) {
			return step.getInputs().get(inputIndex);
		}
		if (mouseX >= this.inputX + inputCols * 18 + 7 && mouseX < this.inputX + inputCols * 18 + 7 + 16 &&
				mouseY >= this.guiTop + (this.ySize - 15) / 2 - 20 && mouseY < this.guiTop + (this.ySize - 15) / 2 - 20 + 16) {
			return step.getMachine();
		}
		int outputIndex = getOutputAt(mouseX, mouseY);
		if (outputIndex != -1) {
			return step.getOutputs().get(outputIndex);
		}
		return Ingredients.EMPTY;
	}

	// Helpers
	public static int formatRows(int count) {
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
