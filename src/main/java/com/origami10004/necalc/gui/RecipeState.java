package com.origami10004.necalc.gui;

import java.util.ArrayList;

import com.origami10004.necalc.data.RecipeEntry;

import net.minecraft.item.ItemStack;

public class RecipeState {
	private ArrayList<RecipeEntry> recipes;
	private RecipeEntry stagedRecipe;

	public RecipeState() {
		this.recipes = new ArrayList<>();
		this.stagedRecipe = RecipeEntry.EMPTY;
	}

	public void loadRecipes() {
		// TODO: Implement recipe loading logic here
	}

	public void reset() {
		this.stagedRecipe = RecipeEntry.EMPTY;
	}

	public void stageRecipe(int index) {
		if (index < 0 || index >= recipes.size()) {
			throw new IndexOutOfBoundsException("Invalid recipe index");
		}
		this.stagedRecipe = recipes.get(index);
	}

	public ItemStack getInput(int index) {
		if (index < 0 || index >= stagedRecipe.getInputs().size()) {
			return ItemStack.EMPTY;
		}
		return stagedRecipe.getInputs().get(index);
	}

	public ItemStack getMachine() {
		return stagedRecipe.getMachine();
	}

	public ItemStack getOutput(int index) {
		if (index < 0 || index >= stagedRecipe.getOutputs().size()) {
			return ItemStack.EMPTY;
		}
		return stagedRecipe.getOutputs().get(index);
	}

	public int getTime() {
		return stagedRecipe.getTime();
	}
}
