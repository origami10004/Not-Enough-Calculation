package com.origami10004.necalc.data;

import java.util.ArrayList;
import java.util.List;

import com.origami10004.necalc.data.ingredient.Ingredients;

public class RecipeState {
	private static ArrayList<RecipeEntry> recipes;
	private static RecipeEntry stagedRecipe;
	private static int stagedId;

	public static void init() {
		stagedRecipe = RecipeEntry.EMPTY.copy();
	}

	public static void loadRecipes() {
		recipes = RecipePersistence.loadRecipeData();
	}

	public static void reset() {
		RecipeState.stagedId = -1;
		RecipeState.stagedRecipe = RecipeEntry.EMPTY.copy();
	}

	public static void stageRecipe(int index) {
		if (index < 0 || index >= recipes.size()) {
			throw new IndexOutOfBoundsException("Invalid recipe index");
		}
		RecipeState.stagedRecipe = recipes.get(index).copy();
		RecipeState.stagedId = index;
	}

	public static Ingredients getInput(int index) {
		if (index < 0 || index >= stagedRecipe.getInputs().size()) {
			return Ingredients.EMPTY;
		}
		return stagedRecipe.getInputs().get(index);
	}
	public static void setInput(int index, Ingredients ingredient) {
		stagedRecipe.setInput(index, ingredient);
	}
	public static void alterInput(int index, Ingredients ingredient, int inc, double mult) {
		stagedRecipe.alterInput(index, ingredient, inc, mult);
	}

	public static Ingredients getMachine() {
		return stagedRecipe.getMachine();
	}
	public static void setMachine(Ingredients ingredient) {
		stagedRecipe.setMachine(ingredient);
	}

	public static Ingredients getOutput(int index) {
		if (index < 0 || index >= stagedRecipe.getOutputs().size()) {
			return Ingredients.EMPTY;
		}
		return stagedRecipe.getOutputs().get(index);
	}
	public static void setOutput(int index, Ingredients ingredient) {
		stagedRecipe.setOutput(index, ingredient);
	}
	public static void alterOutput(int index, Ingredients ingredient, int inc, double mult) {
		stagedRecipe.alterOutput(index, ingredient, inc, mult);
	}

	public static int getTime() {
		return stagedRecipe.getTime();
	}

	public static void confirmRecipe(int time) {
		if (!stagedRecipe.isValid()){
			if (stagedId != -1) {
				MachineState.removeMachine(recipes.get(stagedId).getMachine());
				recipes.remove(stagedId);
				RecipePersistence.saveRecipeData(recipes);
				CalculatorState.recalculateRecipes();
			}
			reset();
			return;
		}
		stagedRecipe.setTime(time);
		if (stagedId == -1) {
			MachineState.addMachine(stagedRecipe.getMachine());
			recipes.add(stagedRecipe.copy());
		} else {
			MachineState.removeMachine(recipes.get(stagedId).getMachine());
			MachineState.addMachine(stagedRecipe.getMachine());
			recipes.set(stagedId, stagedRecipe.copy());
		}
		reset();
		RecipePersistence.saveRecipeData(recipes);
		CalculatorState.recalculateRecipes();
	}
	public static void deleteRecipe() {
		if (stagedId != -1) {
			MachineState.removeMachine(recipes.get(stagedId).getMachine());
			recipes.remove(stagedId);
			RecipePersistence.saveRecipeData(recipes);
			CalculatorState.recalculateRecipes();
			reset();
		}
	}

	public static RecipeEntry getRecipe(int index) {
		if (index < 0 || index >= recipes.size()) {
			return RecipeEntry.EMPTY;
		}
		return recipes.get(index);
	}

	public static List<RecipeEntry> getRecipes() {
		return recipes;
	}

	public static int getInputRows() {
		return stagedRecipe.getInputs().size() / 8 + 1;
	}
	public static int getOutputRows() {
		return stagedRecipe.getOutputs().size() / 8 + 1;
	}
	public static int getRecipeRows() {
		return recipes.size() / 8 + 1;
	}

	public static void mergeInputIngredient(Ingredients ingredient) {
		stagedRecipe.mergeInputIngredient(ingredient);
	}
	public static void mergeOutputIngredient(Ingredients ingredient) {
		stagedRecipe.mergeOutputIngredient(ingredient);
	}
}
