package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiFlowChart;

public class FlowItemNode extends FlowNode {
	private Ingredients ingredient;

	public FlowItemNode(Ingredients ingredient, int x, int y) {
		super(x, y);
		this.ingredient = ingredient;
		super.setSize(18, 18);
	}

	@Override
	public void draw(GuiFlowChart gui) {
		gui.drawItemSlot((int) this.canvasX, (int) this.canvasY);
		ingredient.renderValue(gui, (int) this.canvasX + 1, (int) this.canvasY + 1, ingredient.getValue() / CalculatorState.getMultiplier());
	}

	public void setValue(double value) {
		this.ingredient.setValue(value);
	}
}
