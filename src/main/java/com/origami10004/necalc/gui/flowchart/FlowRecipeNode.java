package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.gui.RecipeViewHelper;

public class FlowRecipeNode extends FlowNode {
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

		this.inputX = 7;
		this.inputY = (ySize - inputRows * 18) / 2;
		this.outputX = xSize - 7 - outputCols * 18;
		this.outputY = (ySize - outputRows * 18) / 2;
	}
}
