package com.origami10004.necalc.compat.jei;

import com.origami10004.necalc.gui.GuiProductionCalc;
import com.origami10004.necalc.gui.GuiRecipeEditor;

import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;

@JEIPlugin
public class NecalcJeiPlugin implements IModPlugin {

	@Override
	public void register(IModRegistry registry) {
		registry.addGhostIngredientHandler(GuiProductionCalc.class, new ProductionCalcGhostHandler());
		registry.addGhostIngredientHandler(GuiRecipeEditor.class, new RecipeEditorGhostHandler());

		IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
		transferRegistry.addUniversalRecipeTransferHandler(new NecalcRecipeTransferHandler());

	}
}