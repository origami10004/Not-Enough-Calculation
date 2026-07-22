package com.origami10004.necalc.compat.jei;

import com.origami10004.necalc.gui.GuiProductionCalc;
import com.origami10004.necalc.gui.GuiRecipeEditor;
import com.origami10004.necalc.gui.GuiManageRecipes;
import com.origami10004.necalc.gui.GuiManageMachines;
import com.origami10004.necalc.gui.GuiFlowChart;

import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.IRecipesGui;

@JEIPlugin
public class NecalcJeiPlugin implements IModPlugin {

	public static IRecipeRegistry recipeRegistry;
	public static IRecipesGui recipesGui;

	@Override
	public void register(IModRegistry registry) {
		registry.addGhostIngredientHandler(GuiProductionCalc.class, new ProductionCalcGhostHandler());
		registry.addGhostIngredientHandler(GuiRecipeEditor.class, new RecipeEditorGhostHandler());

		registry.addAdvancedGuiHandlers(new NecalcGuiHandler<>(GuiProductionCalc.class));
		registry.addAdvancedGuiHandlers(new NecalcGuiHandler<>(GuiManageMachines.class));
		registry.addAdvancedGuiHandlers(new NecalcGuiHandler<>(GuiManageRecipes.class));
		registry.addAdvancedGuiHandlers(new NecalcGuiHandler<>(GuiRecipeEditor.class));
		registry.addAdvancedGuiHandlers(new NecalcGuiHandler<>(GuiFlowChart.class));

		IRecipeTransferRegistry transferRegistry = registry.getRecipeTransferRegistry();
		transferRegistry.addUniversalRecipeTransferHandler(new NecalcRecipeTransferHandler());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		recipeRegistry = jeiRuntime.getRecipeRegistry();
		recipesGui = jeiRuntime.getRecipesGui();
	}
}