package com.origami10004.necalc.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.hipparchus.optim.PointValuePair;
import org.hipparchus.optim.linear.*;
import org.hipparchus.optim.nonlinear.scalar.GoalType;

import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.data.RecipeEntry;
import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.MachineState;
import com.origami10004.necalc.data.ingredient.*;

public class Solver {
	private static List<Ingredients> targets;
	private static List<RecipeEntry> recipes;

	private static HashMap<Ingredients, Integer> itemToId;
	private static HashMap<Integer, Integer> recipeToId;
	private static ArrayList<Ingredients> idToItem;
	private static ArrayList<Integer> idToRecipe;
	private static List<Ingredients> inputItems;

	private static HashMap<Ingredients, ArrayList<Integer>> itemToRecipe;

	public static List<ProductionStep> steps;
	public static LinkedHashMap<Ingredients, Double> inputRates;

	private static void preprocess() {
		itemToId = new HashMap<>();
		recipeToId = new HashMap<>();
		idToItem = new ArrayList<>();
		idToRecipe = new ArrayList<>();
		inputItems = new ArrayList<>();

		itemToRecipe = new HashMap<>();
 		for (int i = 0; i < recipes.size(); i++) {
			List<Ingredients> outputs = recipes.get(i).getOutputs();
			for (Ingredients output : outputs) {
				itemToRecipe.computeIfAbsent(output.copy(), k -> new ArrayList<>()).add(i);
			}
		}
		Queue<Ingredients> bfsq = new LinkedList<>();

		for (Ingredients target : targets) {
			bfsq.add(target);
		}
		while (!bfsq.isEmpty()) {
			Ingredients current = bfsq.poll();
			if (itemToId.containsKey(current)) {
				continue;
			}
			if (itemToRecipe.containsKey(current)) {
				itemToId.put(current, idToItem.size());
				idToItem.add(current);
				for (int recipeIndex : itemToRecipe.get(current)) {
					if (!recipeToId.containsKey(recipeIndex)) {
						recipeToId.put(recipeIndex, idToRecipe.size());
						idToRecipe.add(recipeIndex);
						RecipeEntry recipe = recipes.get(recipeIndex);
						for (Ingredients input : recipe.getInputs()) {
							bfsq.add(input.copy());
						}
					}
				}
			} else {
				inputItems.add(current);
			}
		}
		
	}
	public static void solve() {
		steps = new ArrayList<>();
		targets = CalculatorState.getTargets();
		recipes = RecipeState.getRecipes();
		inputRates = new LinkedHashMap<>();
		if (targets.isEmpty() || recipes.isEmpty()) {
			return;
		}
		preprocess();

		int n = idToRecipe.size();
		int m = idToItem.size();

		double[][] matrix = new double[m][n];
		double[] rates = new double[m];
		double[] cost = new double[n];

		Arrays.fill(cost, 1.0);
		
		// Recipes table
		for (int i = 0; i < n; i++) {
			RecipeEntry recipe = recipes.get(idToRecipe.get(i));
			Ingredients machine = recipe.getMachine();
			int speed = MachineState.getMachineSpeeds().getOrDefault(machine, 1);
			double craftsPerMinute = ((double)(1200 * speed)) / recipe.getTime();

			for (Ingredients output : recipe.getOutputs()) {
				Integer itemId = itemToId.get(output);
				if (itemId != null) matrix[itemId][i] += ((double)output.getValue()) * craftsPerMinute;
			}
			for (Ingredients input : recipe.getInputs()) {
				Integer itemId = itemToId.get(input);
				if (itemId != null) matrix[itemId][i] -= ((double)input.getValue()) * craftsPerMinute;
			}
		}

		// Rates input
		for (Ingredients target : targets) {
			Integer itemId = itemToId.get(target);
			if (itemId != null) rates[itemId] = target.getValue();
			else {
				// If this happens, none of recipes produce target item, treat it as input item
				inputRates.put(target, target.getValue());
				inputItems.remove(target);
			}
		}

		// for (int i = 0; i < n; i++) {
		// 	RecipeEntry recipe = recipes.get(idToRecipe.get(i));
		// 	Necalc.logger.info("Recipe " + i + " inputs:");
		// 	for (Ingredients input : recipe.getInputs()) {
		// 		Integer id = itemToId.get(input);
		// 		Necalc.logger.info("  " + input + "(" + input.serialize() + ") -> itemId=" + id);
		// 	}
		// }
		
		LinearObjectiveFunction objectiveFunction = new LinearObjectiveFunction(cost, 0.0);
		List<LinearConstraint> constraints = new ArrayList<>();
		for (int i = 0; i < m; i++) {
			constraints.add(new LinearConstraint(matrix[i], Relationship.GEQ, rates[i]));
		}
		SimplexSolver solver = new SimplexSolver();
		try {
			PointValuePair solution = solver.optimize(
					objectiveFunction,
					new LinearConstraintSet(constraints),
					GoalType.MINIMIZE,
					new NonNegativeConstraint(true));
			double[] solutionPoint = solution.getPoint();
			double optimalValue = solution.getValue();

			// for (int i = 0; i < m; i++) {
			// 	double netFlow = 0;
			// 	for (int j = 0; j < n; j++) {
			// 		netFlow += matrix[i][j] * solutionPoint[j];
			// 	}
			// 	Necalc.logger.info("Item " + idToItem.get(i) + "(" + idToItem.get(i).serialize() + 
			// 			") net flow: " + netFlow + " required: " + rates[i]);
			// }

			for (int i = 0; i < n; i++) {
				double machineCount = solutionPoint[i];
				if (machineCount < 1e-9) continue;

				RecipeEntry recipe = recipes.get(idToRecipe.get(i));
				int speed = MachineState.getMachineSpeeds().getOrDefault(recipe.getMachine(), 1);
				double craftsPerMinute = ((double)(1200 * speed)) / recipe.getTime();
				double recipePerMinute = machineCount * craftsPerMinute;

				steps.add(new ProductionStep(recipe, machineCount, recipePerMinute));

				for (Ingredients input : recipe.getInputs()) {
					if (inputItems.contains(input)) {
						double inputRate = ((double)input.getValue()) * recipePerMinute;
						inputRates.put(input, inputRates.getOrDefault(input, 0.0) + inputRate);
					}
				}
			}

		} catch (Exception e) {
			Necalc.logger.error("Error during optimization: " + e.getMessage());
		}
	}
}
