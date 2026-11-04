package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.ingredient.Ingredients;

public class FlowOutputNode extends FlowItemNode {
    public FlowOutputNode(Ingredients ingredient, int x, int y, double producedRate, double consumedRate) {
        super(ingredient, x, y, producedRate, consumedRate);
    }

    @Override
    public int getSlotVariant() {
		return 2;
	}

    @Override
    public boolean isVisible() {
        return true;
    }
    
}
