package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiFlowChart;
import com.origami10004.necalc.gui.GuiProductionCalc;

import net.minecraft.client.resources.I18n;

import java.util.HashMap;
import java.util.Map;

public class FlowItemNode extends FlowNode {
	private static final double EPSILON = 1e-9;
	Ingredients ingredient;
	private double producedRate;
	private double consumedRate;

	public FlowItemNode(Ingredients ingredient, int x, int y, double producedRate, double consumedRate) {
		super(x, y);
		this.ingredient = ingredient;
		this.producedRate = producedRate;
		this.consumedRate = consumedRate;
		super.setSize(18, 18);
	}

	@Override
	public void draw(GuiFlowChart gui) {
		if (!isVisible()) return;
		gui.drawItemSlot((int) this.canvasX, (int) this.canvasY, getSlotVariant());
		ingredient.renderValue(gui, (int) this.canvasX + 1, (int) this.canvasY + 1, getValue() / CalculatorState.getMultiplier());
	}

	@Override
	public boolean isVisible() {
		return this.producedRate > EPSILON || this.consumedRate > EPSILON;
	}

	public double getValue() {
		return Math.max(this.consumedRate, this.producedRate);
	}

	public int getSlotVariant() {
		if (this.consumedRate > this.producedRate + EPSILON) return 1;
		if (this.producedRate > this.consumedRate + EPSILON) return 3;
		return 0;
	}

	public void increaseProducedRate(double amount) {
		this.producedRate += amount;
	}

	public void increaseConsumedRate(double amount) {
		this.consumedRate += amount;
	}

	@Override
	public void drawHoveredToolTip(GuiFlowChart gui, int mouseX, int mouseY) {
		String text = String.format("%.4f", getValue() / CalculatorState.getMultiplier()) + GuiProductionCalc.rateLabels[CalculatorState.getDisplayRate()];
		gui.drawItemExtraInfoTooltip(mouseX, mouseY, ingredient, I18n.format("necalc.gui.target_rate", text));
	}

	@Override
	public Ingredients hoveredStack(int mouseX, int mouseY) {
		ingredient.setValue(getValue());
		return ingredient;
	}

	@Override
	public int getColour() {
		return ingredient.getColour();
	}
}
