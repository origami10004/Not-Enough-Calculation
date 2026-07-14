package com.origami10004.necalc.data;

import com.origami10004.necalc.calc.Solver;
import com.origami10004.necalc.data.ingredient.*;

import java.util.*;
import java.util.stream.Collectors;

public class CalculatorState {
	private static int displayRate = 0;
	private static final List<Ingredients> targets = new ArrayList<>();
	private static int[] rateMultiplier = new int[] {1, 60, 1200};
	private static List<ProductionStep> recipeSteps;
	private static LinkedHashMap<Ingredients, Solver.Input> recipeInputs;
	private static HashSet<Ingredients> targetItems = new HashSet<>();

	private static boolean cached = false;

	public static void init() {
		recipeSteps = new ArrayList<>();
		recipeInputs = new LinkedHashMap<>();
		cached = false;
	}

	public static void loadTargets() {
		List<Ingredients> loadedTargets = TargetPersistence.loadTargetData();
		targets.clear();
		targets.addAll(loadedTargets);
		targetItems.clear();
		targets.forEach(target -> targetItems.add(target));
	}

	public static int getTargetNumRows() {
		return targets.size() / 8 + 1;
	}

	public static int getDisplayRate() {
		return displayRate;
	}

	public static boolean setDisplayRate(int newRate) {
		int old = displayRate;
		CalculatorState.displayRate = newRate;
		return old != newRate;
	}

	public static List<Ingredients> getTargets() {
		return targets;
	}

	public static Ingredients getTargetSlot(int index) {
		if (index < 0 || index >= targets.size()) {
			return Ingredients.EMPTY;
		}
		return targets.get(index);
	}

	public static boolean setTargetSlot(int index, Ingredients ingredient) {
		if (index < 0) {
			return false;
		} else{
			if (ingredient.isEmpty()) {
				if (index >= targets.size()) return false;
				Ingredients old = targets.get(index);
				targetItems.remove(old);
				targets.remove(index);
				TargetPersistence.saveTargetData(CalculatorState.targets);
				recalculateRecipes();
				return true;
			}
			if (targetItems.contains(ingredient)) return false;
			Ingredients cur = ingredient.copy();
			cur.setValue(getMultiplier());
			if (index >= targets.size()) {
				targets.add(cur);
				targetItems.add(ingredient);
			} else {
				Ingredients old = targets.get(index);
				targetItems.remove(old);
				targets.set(index, cur);
				targetItems.add(ingredient);
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
		return targets.get(index).getValue() / getMultiplier();
	}
	public static boolean setTargetSlotRate(int index, double rate) {
		// This function should never be called with an index that doesn't have a target item
		if (index < 0 || index >= targets.size()) {
			return false;
		}
		Ingredients cur = targets.get(index);
		if (cur.getValue() != rate * getMultiplier()) {
			cur.setValue(rate * getMultiplier());
			TargetPersistence.saveTargetData(CalculatorState.targets);
			recalculateRecipes();
			return true;
		}
		return false;
	}

	public static boolean hasHidden(boolean showProd) {
		getResult();
		if (showProd) {
			return recipeSteps.stream()
					.filter(r -> r.isHidden())
					.count() > 0;
		} else {
			return recipeInputs.entrySet().stream()
					.filter(entry -> entry.getValue().hidden)
					.count() > 0;
		}
	}

	public static int getHiddenCount(boolean showProd) {
		getResult();
		if (showProd) {
			return (int) recipeSteps.stream()
					.filter(r -> r.isHidden())
					.count();
		} else {
			return (int) recipeInputs.entrySet().stream()
					.filter(entry -> entry.getValue().hidden)
					.count();
		}
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

	private static boolean getResult() {
		if (cached) return false;
		recipeSteps.clear();
		recipeInputs.clear();
		Solver.Result result = Solver.solve();
		recipeSteps.addAll(result.steps);
		recipeInputs.putAll(result.inputRates);

		cached = true;
		return true;
	}

	public static Map<Ingredients, Solver.Input> getVisibleInputs() {
		getResult();
		return recipeInputs.entrySet().stream()
				.filter(entry -> !entry.getValue().hidden)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
	}

	public static void hideInput(Ingredients input) {
		if (recipeInputs.containsKey(input)) {
			recipeInputs.get(input).hidden = true;
		}
	}

	public static void showAllInputs() {
		recipeInputs.values().forEach(input -> input.hidden = false);
	}
}
