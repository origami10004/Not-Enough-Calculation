package com.origami10004.necalc.gui;

import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.ProductionStep;

import java.util.*;
import java.util.stream.Collectors;

public class CalculatorState {
	private int targetNumRows = 1;
	private int selectedRate = 0;
	private final Map<Integer, ItemStack> targetSlots = new HashMap<>();
	private final Map<Integer, Double> targetRates = new HashMap<>();
	private int[] rateMultiplier = new int[] {1, 60, 1200};
	private List<ProductionStep> recipeSteps;

	public CalculatorState() {
		targetSlots.put(0, new ItemStack(Blocks.DIAMOND_BLOCK));
		targetRates.put(0, 0.3);
		recipeSteps = new ArrayList<>();
		recipeSteps.add(new ProductionStep(
			Arrays.asList(new ItemStack(Items.DIAMOND, 9)),
			Arrays.asList(new ItemStack(Blocks.DIAMOND_BLOCK, 1)),
			new ItemStack(Blocks.CRAFTING_TABLE),
			1.0,
			0.3,
			false
		));
	}

	public int getTargetNumRows() {
		return targetNumRows;
	}

	public int getSelectedRate() {
		return selectedRate;
	}

	public boolean setSelectedRate(int newRate) {
		int old = selectedRate;
		this.selectedRate = newRate;
		return old != newRate;
	}

	public ItemStack getTargetSlot(int index) {
		return targetSlots.getOrDefault(index, ItemStack.EMPTY);
	}

	public boolean setTargetSlot(int index, ItemStack stack) {
		ItemStack old = targetSlots.getOrDefault(index, ItemStack.EMPTY);
		targetSlots.put(index, stack);
		if (!ItemStack.areItemStacksEqual(old, stack)) recalculateRecipes();
		return !ItemStack.areItemStacksEqual(old, stack);
	}

	public double getTargetSlotRate(int index) {
		return targetRates.getOrDefault(index, 0.0) * rateMultiplier[selectedRate];
	}
	public boolean setTargetSlotRate(int index, double rate) {
		double old = targetRates.getOrDefault(index, 0.0);
		targetRates.put(index, rate);
		if (old != rate) recalculateRecipes();
		return old != rate;
	}

	public boolean hasHidden() {
		return recipeSteps.stream()
				.filter(r -> r.isHidden())
				.count() > 0;
	}

	public int getHiddenCount() {
		return (int) recipeSteps.stream()
				.filter(r -> r.isHidden())
				.count();
	}

	public void showAllRecipes() {
		recipeSteps.forEach(r -> r.show());
	}

	public List<ProductionStep> getVisibleRecipes() {
		return recipeSteps.stream()
				.filter(r -> !r.isHidden())
				.collect(Collectors.toList());
	}

	public void hideRecipe(int index) {
		if (index < 0 || index >= recipeSteps.size()) return;
		recipeSteps.stream()
				.filter(r -> !r.isHidden())
				.collect(Collectors.toList())
				.get(index).hide();
	}

	public int getMultiplier() {
		return rateMultiplier[selectedRate];
	}

	private void recalculateRecipes() {
		// Placeholder for recipe recalculation logic based on target slots and rates
		Necalc.logger.info("Recalculating recipes with current target slots and rates...");
	}
}
