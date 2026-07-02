package com.origami10004.necalc.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

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

	public static ItemStack getInput(int index) {
		if (index < 0 || index >= stagedRecipe.getInputs().size()) {
			return ItemStack.EMPTY;
		}
		return stagedRecipe.getInputs().get(index);
	}
	public static void setInput(int index, ItemStack stack) {
		stagedRecipe.setInput(index, stack);
	}
	public static void alterInput(int index, ItemStack stack, int inc, double mult) {
		stagedRecipe.alterInput(index, stack, inc, mult);
	}

	public static ItemStack getMachine() {
		return stagedRecipe.getMachine();
	}
	public static void setMachine(ItemStack stack) {
		stagedRecipe.setMachine(stack);
	}

	public static ItemStack getOutput(int index) {
		if (index < 0 || index >= stagedRecipe.getOutputs().size()) {
			return ItemStack.EMPTY;
		}
		return stagedRecipe.getOutputs().get(index);
	}
	public static void setOutput(int index, ItemStack stack) {
		stagedRecipe.setOutput(index, stack);
	}
	public static void alterOutput(int index, ItemStack stack, int inc, double mult) {
		stagedRecipe.alterOutput(index, stack, inc, mult);
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
}
