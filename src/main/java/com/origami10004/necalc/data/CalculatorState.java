package com.origami10004.necalc.data;

import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.calculator.Solver;

import java.util.*;
import java.util.stream.Collectors;

public class CalculatorState {
	private static int displayRate = 0;
	private static final Map<Integer, CalculationTarget> targets = new HashMap<>();
	private static int[] rateMultiplier = new int[] {1, 60, 1200};
	private static List<ProductionStep> recipeSteps;

	private static boolean cached = false;

	public static void init() {
		recipeSteps = new ArrayList<>();
		cached = false;
	}

	public static void loadTargets() {
		Map<Integer, CalculationTarget> loadedTargets = TargetPersistence.loadTargetData();
		targets.clear();
		targets.putAll(loadedTargets);
	}

	public static int getTargetNumRows() {
		return targets.size() / 8;
	}

	public static int getDisplayRate() {
		return displayRate;
	}

	public static boolean setDisplayRate(int newRate) {
		int old = displayRate;
		CalculatorState.displayRate = newRate;
		return old != newRate;
	}

	public static List<CalculationTarget> getTargets() {
		return targets.values().stream()
				.collect(Collectors.toList());
	}

	public static ItemStack getTargetSlot(int index) {
		return targets.getOrDefault(index, CalculationTarget.EMPTY).getTargetItem();
	}

	public static boolean setTargetSlot(int index, ItemStack stack) {
		CalculationTarget cur = targets.getOrDefault(index, null);
		if (cur == null) {
			targets.put(index, new CalculationTarget(stack, 1.0 / getMultiplier()));
			TargetPersistence.saveTargetData(CalculatorState.targets);
			recalculateRecipes();
			return true;
		} else {
			if (cur.setTargetItem(stack, 1.0 / getMultiplier())){
				TargetPersistence.saveTargetData(CalculatorState.targets);
				recalculateRecipes();
				return true;
			}
			return false;
		}
	}

	public static double getTargetSlotRate(int index) {
		return targets.getOrDefault(index, CalculationTarget.EMPTY).getTargetRate() * rateMultiplier[displayRate];
	}
	public static boolean setTargetSlotRate(int index, double rate) {
		// This function should never be called with an index that doesn't have a target item
		CalculationTarget cur = targets.getOrDefault(index, null);
		if (cur == null) return false;

		if (cur.setTargetRate(rate / rateMultiplier[displayRate])) {
			TargetPersistence.saveTargetData(CalculatorState.targets);
			recalculateRecipes();
			return true;
		}
		return false;
	}

	public static boolean hasHidden() {
		getResult();
		return recipeSteps.stream()
				.filter(r -> r.isHidden())
				.count() > 0;
	}

	public static int getHiddenCount() {
		getResult();
		return (int) recipeSteps.stream()
				.filter(r -> r.isHidden())
				.count();
	}

	public static void showAllRecipes() {
		getResult();
		recipeSteps.forEach(r -> r.show());
	}

	public static List<ProductionStep> getVisibleRecipes() {
		getResult();
		return recipeSteps.stream()
				.filter(r -> !r.isHidden())
				.collect(Collectors.toList());
	}

	public static void hideRecipe(int index) {
		getResult();
		if (index < 0 || index >= recipeSteps.size()) return;
		recipeSteps.stream()
				.filter(r -> !r.isHidden())
				.collect(Collectors.toList())
				.get(index).hide();
	}

	public static int getMultiplier() {
		return rateMultiplier[displayRate];
	}

	public static void recalculateRecipes() {
		cached = false;
	}

	private static void getResult() {
		if (cached) return;
		recipeSteps.clear();
		recipeSteps.addAll(Solver.solve());
		cached = true;
	}
}
