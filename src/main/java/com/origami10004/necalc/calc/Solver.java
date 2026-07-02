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
import com.origami10004.necalc.data.CalculationTarget;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ItemKey;
import com.origami10004.necalc.data.MachineKey;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.MachineState;

import net.minecraft.item.ItemStack;

public class Solver {
	private static List<CalculationTarget> targets;
	private static List<RecipeEntry> recipes;

	private static HashMap<ItemKey, Integer> itemToId;
	private static HashMap<Integer, Integer> recipeToId;
	private static ArrayList<ItemKey> idToItem;
	private static ArrayList<Integer> idToRecipe;
	private static List<ItemKey> inputItems;

	private static HashMap<ItemKey, ArrayList<Integer>> itemToRecipe;

	public static List<ProductionStep> steps;
	public static LinkedHashMap<ItemKey, Double> inputRates;

	private static void preprocess() {
		itemToId = new HashMap<>();
		recipeToId = new HashMap<>();
		idToItem = new ArrayList<>();
		idToRecipe = new ArrayList<>();
		inputItems = new ArrayList<>();

		itemToRecipe = new HashMap<>();
 		for (int i = 0; i < recipes.size(); i++) {
			List<ItemStack> outputs = recipes.get(i).getOutputs();
			for (ItemStack output : outputs) {
				ItemKey key = new ItemKey(output);
				itemToRecipe.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
			}
		}
		Queue<ItemKey> bfsq = new LinkedList<>();

		for (CalculationTarget target : targets) {
			ItemKey key = new ItemKey(target.getTargetItem());
			bfsq.add(key);
		}
		while (!bfsq.isEmpty()) {
			ItemKey current = bfsq.poll();
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
						for (ItemStack input : recipe.getInputs()) {
							bfsq.add(new ItemKey(input));
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
			MachineKey machine = new MachineKey(recipe.getMachine());
			int speed = MachineState.getMachineSpeeds().getOrDefault(machine, 1);
			double craftsPerMinute = ((double)(1200 * speed)) / recipe.getTime();

			for (ItemStack output : recipe.getOutputs()) {
				ItemKey key = new ItemKey(output);
				Integer itemId = itemToId.get(key);
				if (itemId != null) matrix[itemId][i] += ((double)output.getCount()) * craftsPerMinute;
			}
			for (ItemStack input : recipe.getInputs()) {
				ItemKey key = new ItemKey(input);
				Integer itemId = itemToId.get(key);
				if (itemId != null) matrix[itemId][i] -= ((double)input.getCount()) * craftsPerMinute;
			}
		}

		// Rates input
		for (CalculationTarget target : targets) {
			ItemKey key = new ItemKey(target.getTargetItem());
			Integer itemId = itemToId.get(key);
			if (itemId != null) rates[itemId] = target.getTargetRate();
			else {
				// If this happens, none of recipes produce target item, treat it as input item
				inputRates.put(key, target.getTargetRate());
				inputItems.remove(key);
			}
		}
		Necalc.logger.info("Solving with " + n + " recipes and " + m + " items");
		Necalc.logger.info("Matrix is : " + Arrays.deepToString(matrix));
		Necalc.logger.info("Rates are : " + Arrays.toString(rates));
		Necalc.logger.info("Costs are : " + Arrays.toString(cost));
		for (int i = 0; i < idToItem.size(); i++) {
			Necalc.logger.info("item[" + i + "] = " + idToItem.get(i));
		}
		for (int i = 0; i < idToRecipe.size(); i++) {
			Necalc.logger.info("recipe[" + i + "] = " + idToRecipe.get(i) 
				+ " -> " + recipes.get(idToRecipe.get(i)).getOutputs().get(0).getDisplayName());
		}
		
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
			Necalc.logger.info("Optimal value: " + optimalValue);
			Necalc.logger.info("Solution point: " + Arrays.toString(solutionPoint));

			for (int i = 0; i < n; i++) {
				double machineCount = solutionPoint[i];
				if (machineCount < 1e-9) continue;

				RecipeEntry recipe = recipes.get(idToRecipe.get(i));
				MachineKey machineKey = new MachineKey(recipe.getMachine());
				int speed = MachineState.getMachineSpeeds().getOrDefault(machineKey, 1);
				double craftsPerMinute = ((double)(1200 * speed)) / recipe.getTime();
				double recipePerMinute = machineCount * craftsPerMinute;

				steps.add(new ProductionStep(recipe, machineCount, recipePerMinute));
			}

			// TODO: Add input item rates
		} catch (Exception e) {
			Necalc.logger.error("Error during optimization: " + e.getMessage());
		}
	}
}
