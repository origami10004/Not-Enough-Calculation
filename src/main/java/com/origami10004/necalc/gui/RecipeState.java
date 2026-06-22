package com.origami10004.necalc.gui;

import java.util.ArrayList;

import com.origami10004.necalc.data.RecipeEntry;
import com.origami10004.necalc.proxy.ClientProxy;

import net.minecraft.item.ItemStack;

public class RecipeState {
	private ArrayList<RecipeEntry> recipes;
	private RecipeEntry stagedRecipe;
	private int stagedId;

	public RecipeState() {
		this.recipes = new ArrayList<>();
		this.stagedRecipe = RecipeEntry.EMPTY;
	}

	public void loadRecipes() {
		// TODO: Implement recipe loading logic here
	}

	public void reset() {
		this.stagedId = -1;
		this.stagedRecipe = RecipeEntry.EMPTY;
	}

	public void stageRecipe(int index) {
		if (index < 0 || index >= recipes.size()) {
			throw new IndexOutOfBoundsException("Invalid recipe index");
		}
		this.stagedRecipe = recipes.get(index).copy();
		this.stagedId = index;
	}

	public ItemStack getInput(int index) {
		if (index < 0 || index >= stagedRecipe.getInputs().size()) {
			return ItemStack.EMPTY;
		}
		return stagedRecipe.getInputs().get(index);
	}
	public void setInput(int index, ItemStack stack) {
		stagedRecipe.setInput(index, stack);
	}
	public void alterInput(int index, ItemStack stack, int inc, int mult) {
		stagedRecipe.alterInput(index, stack, inc, mult);
	}

	public ItemStack getMachine() {
		return stagedRecipe.getMachine();
	}
	public void setMachine(ItemStack stack) {
		stagedRecipe.setMachine(stack);
	}

	public ItemStack getOutput(int index) {
		if (index < 0 || index >= stagedRecipe.getOutputs().size()) {
			return ItemStack.EMPTY;
		}
		return stagedRecipe.getOutputs().get(index);
	}
	public void setOutput(int index, ItemStack stack) {
		stagedRecipe.setOutput(index, stack);
	}
	public void alterOutput(int index, ItemStack stack, int inc, int mult) {
		stagedRecipe.alterOutput(index, stack, inc, mult);
	}

	public int getTime() {
		return stagedRecipe.getTime();
	}

	public void confirmRecipe(int time) {
		if (stagedRecipe.isEmpty()){
			if (stagedId != -1) {
				recipes.remove(stagedId);
				ClientProxy.calcState.recalculateRecipes();
			}
			reset();
			return;
		}
		stagedRecipe.setTime(time);
		stagedRecipe.clean();
		if (stagedId == -1) {
			recipes.add(stagedRecipe.copy());
		} else {
			recipes.set(stagedId, stagedRecipe.copy());
		}
		ClientProxy.calcState.recalculateRecipes();
	}
	public void deleteRecipe() {
		if (stagedId != -1) {
			recipes.remove(stagedId);
			ClientProxy.calcState.recalculateRecipes();
			reset();
		}
	}
}
