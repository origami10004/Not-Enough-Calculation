package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.gui.GuiFlowChart;
import com.origami10004.necalc.gui.GuiProductionCalc;
import com.origami10004.necalc.gui.RecipeViewHelper;
import com.origami10004.necalc.data.ingredient.Ingredients;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class FlowRecipeNode extends FlowNode {
	private static final ResourceLocation ARROW_TEXTURE = new ResourceLocation("necalc", "textures/gui/arrow.png");

	private ProductionStep step;
	private int inputCols;
	private int inputRows;
	private int outputCols;
	private int outputRows;
	private int xSize;
	private int ySize;
	private int inputX;
	private int inputY;
	private int outputX;
	private int outputY;

	public FlowRecipeNode(ProductionStep step, int x, int y) {
		super(x, y);
		this.step = step;

		this.inputCols = RecipeViewHelper.formatRows(step.getInputs().size());
		this.inputRows = (int) Math.ceil((double) step.getInputs().size() / inputCols);
		this.outputCols = RecipeViewHelper.formatRows(step.getOutputs().size());
		this.outputRows = (int) Math.ceil((double) step.getOutputs().size() / outputCols);

		this.xSize = 44 + inputCols * 18 + outputCols * 18;
		this.ySize = Math.max(Math.max(inputRows, outputRows) * 18 + 14, 65);
		super.setSize(xSize, ySize);

		this.inputX = x + 7;
		this.inputY = y + (ySize - inputRows * 18) / 2;
		this.outputX = x + xSize - 7 - outputCols * 18;
		this.outputY = y + (ySize - outputRows * 18) / 2;
	}

	@Override
	public void move(double x, double y) {
		super.move(x, y);
		this.inputX = (int) this.canvasX + 7;
		this.inputY = (int) this.canvasY + (ySize - inputRows * 18) / 2;
		this.outputX = (int) this.canvasX + xSize - 7 - outputCols * 18;
		this.outputY = (int) this.canvasY + (ySize - outputRows * 18) / 2;
	}

	@Override
	public void draw(GuiFlowChart gui) {
		gui.drawRectPanelOutdent((int) this.canvasX, (int) this.canvasY, this.xSize, this.ySize, 0xFFC6C6C6);
		for (int i = 0; i < inputRows; i++) {
			for (int j = 0; j < inputCols; j++) {
				int index = i * inputCols + j;
				if (index >= step.getInputs().size()) break;
				gui.drawItemSlot(inputX + j * 18, inputY + i * 18);
				Ingredients currentInput = step.getInputs().get(index);
				currentInput.renderValue(gui, inputX + j * 18 + 1, inputY + i * 18 + 1, step.getInputRate(index) / CalculatorState.getMultiplier());
			}
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		gui.mc.getTextureManager().bindTexture(ARROW_TEXTURE);
		GuiProductionCalc.drawModalRectWithCustomSizedTexture(this.inputX + inputCols * 18 + 4, (int) this.canvasY + (this.ySize - 15) / 2, 0, 0, 22, 15, 22, 15);

		step.getMachine().renderValue(gui, this.inputX + inputCols * 18 + 7, (int) this.canvasY + (this.ySize - 15) / 2 - 20, step.getMachineCount());

		for (int i = 0; i < outputRows; i++) {
			for (int j = 0; j < outputCols; j++) {
				int index = i * outputCols + j;
				if (index >= step.getOutputs().size()) break;
				gui.drawItemSlot(outputX + j * 18, outputY + i * 18);
				Ingredients currentOutput = step.getOutputs().get(index);
				currentOutput.renderValue(gui, outputX + j * 18 + 1, outputY + i * 18 + 1, step.getOutputRate(index) / CalculatorState.getMultiplier());
			}
		}
	}


	@Override
	public double getInputY(int index) {
		return canvasY + this.getHeight() / 2.0;
	}

	@Override
	public double getOutputY(int index) {
		return canvasY + this.getHeight() / 2.0;
	}
}
