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
	private static final List<CalculationTarget> targets = new ArrayList<>();
	private static int[] rateMultiplier = new int[] {1, 60, 1200};
	private static List<ProductionStep> recipeSteps;
	private static HashSet<ItemKey> targetItems = new HashSet<>();

	private static boolean cached = false;

	public static void init() {
		recipeSteps = new ArrayList<>();
		cached = false;
	}

	public static void loadTargets() {
		List<CalculationTarget> loadedTargets = TargetPersistence.loadTargetData();
		targets.clear();
		targets.addAll(loadedTargets);
		targetItems.clear();
		targets.forEach(target -> targetItems.add(new ItemKey(target.getTargetItem())));
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
		return targets;
	}

	public static ItemStack getTargetSlot(int index) {
		if (index < 0 || index >= targets.size()) {
			return ItemStack.EMPTY;
		}
		return targets.get(index).getTargetItem();
	}

	public static boolean setTargetSlot(int index, ItemStack stack) {
		if (index < 0) {
			return false;
		} else{
			ItemKey cur = new ItemKey(stack);
			if (stack.isEmpty()) {
				if (index >= targets.size()) return false;
				CalculationTarget old = targets.get(index);
				targetItems.remove(new ItemKey(old.getTargetItem()));
				targets.remove(index);
				TargetPersistence.saveTargetData(CalculatorState.targets);
				recalculateRecipes();
				return true;
			}
			if (targetItems.contains(cur)) return false;
			if (index >= targets.size()) {
				targets.add(new CalculationTarget(stack, 1.0 / getMultiplier()));
				targetItems.add(cur);
			} else {
				CalculationTarget old = targets.get(index);
				targetItems.remove(new ItemKey(old.getTargetItem()));
				targets.set(index, new CalculationTarget(stack, 1.0 / getMultiplier()));
				targetItems.add(cur);
			}
			TargetPersistence.saveTargetData(CalculatorState.targets);
			recalculateRecipes();
			return true;
		}
	}

	public static double getTargetSlotRate(int index) {
		if (index < 0 || index >= targets.size()) {
			return 0.0;
		}
		return targets.get(index).getTargetRate() * rateMultiplier[displayRate];
	}
	public static boolean setTargetSlotRate(int index, double rate) {
		// This function should never be called with an index that doesn't have a target item
		if (index < 0 || index >= targets.size()) {
			return false;
		}
		CalculationTarget cur = targets.get(index);
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
		Necalc.logger.info("Recalculating");
	}

	private static void getResult() {
		if (cached) return;
		Necalc.logger.info("Actually calculating lmao");
		recipeSteps.clear();
		recipeSteps.addAll(Solver.solve());
		cached = true;
	}
}
