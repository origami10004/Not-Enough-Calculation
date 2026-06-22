package com.origami10004.necalc.gui;

import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.CalculationTarget;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.TargetPersistence;

import java.util.*;
import java.util.stream.Collectors;

public class CalculatorState {
	private int targetNumRows = 1;
	private int selectedRate = 0;
	private final Map<Integer, CalculationTarget> targets = new HashMap<>();
	private int[] rateMultiplier = new int[] {1, 60, 1200};
	private List<ProductionStep> recipeSteps;

	public CalculatorState() {
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

	public void loadTargets() {
		Map<Integer, CalculationTarget> loadedTargets = TargetPersistence.loadTargetData();
		targets.clear();
		targets.putAll(loadedTargets);
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
		return targets.getOrDefault(index, CalculationTarget.EMPTY).getTargetItem();
	}

	public boolean setTargetSlot(int index, ItemStack stack) {
		CalculationTarget cur = targets.getOrDefault(index, null);
		if (cur == null) {
			targets.put(index, new CalculationTarget(stack, 1.0 / getMultiplier()));
			TargetPersistence.saveTargetData(this.targets);
			recalculateRecipes();
			return true;
		} else {
			ItemStack old = cur.getTargetItem();
			if (cur.setTargetItem(stack, 1.0 / getMultiplier())){
				TargetPersistence.saveTargetData(this.targets);
				recalculateRecipes();
				return true;
			}
			return false;
		}
	}

	public double getTargetSlotRate(int index) {
		return targets.getOrDefault(index, CalculationTarget.EMPTY).getTargetRate() * rateMultiplier[selectedRate];
	}
	public boolean setTargetSlotRate(int index, double rate) {
		// This function should never be called with an index that doesn't have a target item
		CalculationTarget cur = targets.getOrDefault(index, null);
		if (cur == null) return false;

		if (cur.setTargetRate(rate / rateMultiplier[selectedRate])) {
			TargetPersistence.saveTargetData(this.targets);
			recalculateRecipes();
			return true;
		}
		return false;
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
		// TODO: Placeholder for recipe recalculation logic based on target slots and rates
		Necalc.logger.info("Recalculating recipes with current target slots and rates...");
	}
}
