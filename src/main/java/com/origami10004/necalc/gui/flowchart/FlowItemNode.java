package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.ingredient.Ingredients;

public class FlowItemNode extends FlowNode {
	private Ingredients ingredient;

	public FlowItemNode(Ingredients ingredient, int x, int y) {
		super(x, y);
		this.ingredient = ingredient;
		super.setSize(18, 18);
	}
}
