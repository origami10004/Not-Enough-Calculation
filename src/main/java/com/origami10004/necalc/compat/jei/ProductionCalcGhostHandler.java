package com.origami10004.necalc.compat.jei;

import mezz.jei.api.gui.IGhostIngredientHandler;

import com.origami10004.necalc.gui.GuiProductionCalc;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ingredient.IngredientManager;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class ProductionCalcGhostHandler implements IGhostIngredientHandler<GuiProductionCalc> {
	@Override
	public <I> List<Target<I>> getTargets(GuiProductionCalc gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();

		int rows = GuiProductionCalc.TARGET_ROWS;
		int cols = GuiProductionCalc.SLOTS_PER_ROW;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final int rowFinal = row;
				final int colFinal = col;
				targets.add(new Target<I>() {
					@Override
					public Rectangle getArea() {
						return gui.getTargetSlotArea(rowFinal, colFinal);
					}

					@Override
					public void accept(I ingredient2) {
						CalculatorState.setTargetSlot((rowFinal + gui.getTargetScrollRow()) * cols + colFinal, IngredientManager.of(ingredient2));
					}
				});
			}
		}

		return targets;
	}

	@Override
	public void onComplete() {

	}
}
