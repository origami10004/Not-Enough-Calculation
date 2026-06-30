package com.origami10004.necalc.calculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;

import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.RecipeEntry;
import com.origami10004.necalc.data.CalculationTarget;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ItemKey;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.MachineState;

import net.minecraft.item.ItemStack;

public class Solver {
	public static List<ProductionStep> solve() {
		// Pre processing: BFS to find relevant recipes and items and input items
		List<ProductionStep> steps = new ArrayList<>();
		List<CalculationTarget> targets = CalculatorState.getTargets();
		List<RecipeEntry> recipes = RecipeState.getRecipes();

		Set<ItemKey> relevantItems = new HashSet<>();
		Set<Integer> relevantRecipes = new HashSet<>();
		List<ItemKey> inputItems = new ArrayList<>();

		Queue<ItemKey> bfsq = new LinkedList<>();
		HashMap<ItemKey, ArrayList<Integer>> itemToRecipe = new HashMap<>();
		for (int i = 0; i < recipes.size(); i++) {
			List<ItemStack> outputs = recipes.get(i).getOutputs();
			for (ItemStack output : outputs) {
				ItemKey key = new ItemKey(output);
				itemToRecipe.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
			}
		}
		for (CalculationTarget target : targets) {
			ItemKey key = new ItemKey(target.getTargetItem());
			bfsq.add(key);
		}
		while (!bfsq.isEmpty()) {
			ItemKey current = bfsq.poll();
			if (relevantItems.contains(current)) {
				continue;
			}
			relevantItems.add(current);
			if (itemToRecipe.containsKey(current)) {
				for (int recipeIndex : itemToRecipe.get(current)) {
					if (!relevantRecipes.contains(recipeIndex)) {
						relevantRecipes.add(recipeIndex);
						RecipeEntry recipe = recipes.get(recipeIndex);
						for (ItemStack input : recipe.getInputs()) {
							bfsq.add(new ItemKey(input));
						}
					}
				}
			} else {
				inputItems.add(current);
			}
		}

		int n = relevantRecipes.size();
		


		




		return steps;
	}
}
