package com.origami10004.necalc.compat.jei;

import mezz.jei.api.gui.IGhostIngredientHandler;

import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.ingredient.IngredientManager;
import com.origami10004.necalc.gui.GuiRecipeEditor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


public class RecipeEditorGhostHandler implements IGhostIngredientHandler<GuiRecipeEditor> {
	@Override
	public <I> List<Target<I>> getTargets(GuiRecipeEditor gui, I ingredient, boolean doStart) {
		List<Target<I>> targets = new ArrayList<>();

		// inputs
		int rows = GuiRecipeEditor.IO_ROWS;
		int cols = GuiRecipeEditor.SLOTS_PER_ROW;
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final int rowFinal = row;
				final int colFinal = col;
				targets.add(new Target<I>() {
					@Override
					public Rectangle getArea() {
						return gui.getInputSlotArea(rowFinal, colFinal);
					}

					@Override
					public void accept(I ingredient2) {
						RecipeState.setInput((gui.getInputScrollRow() + rowFinal) * cols + colFinal, IngredientManager.of(ingredient2));
					}
				});
			}
		}
		// machine
		targets.add(new Target<I>() {
			@Override
			public Rectangle getArea() {
				return gui.getMachineSlotArea();
			}

			@Override
			public void accept(I ingredient2) {
				RecipeState.setMachine(IngredientManager.of(ingredient2));
			}
		});
		// outputs
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final int rowFinal = row;
				final int colFinal = col;
				targets.add(new Target<I>() {
					@Override
					public Rectangle getArea() {
						return gui.getOutputSlotArea(rowFinal, colFinal);
					}

					@Override
					public void accept(I ingredient2) {
						RecipeState.setOutput((gui.getOutputScrollRow() + rowFinal) * cols + colFinal, IngredientManager.of(ingredient2));
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
