package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiFlowChart;
import com.origami10004.necalc.gui.GuiProductionCalc;

import net.minecraft.client.resources.I18n;

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

	@Override
	public void drawHoveredToolTip(GuiFlowChart gui, int mouseX, int mouseY) {
		String text = String.format("%.4f", ingredient.getValue() / CalculatorState.getMultiplier()) + GuiProductionCalc.rateLabels[CalculatorState.getDisplayRate()];
		gui.drawItemExtraInfoTooltip(mouseX, mouseY, ingredient, I18n.format("necalc.gui.target_rate", text));
	}

	@Override
	public Ingredients hoveredStack(int mouseX, int mouseY) {
		return ingredient;
	}
}
