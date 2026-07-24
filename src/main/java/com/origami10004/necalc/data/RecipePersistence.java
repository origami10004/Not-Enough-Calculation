package com.origami10004.necalc.data;

import net.minecraftforge.fml.common.Loader;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.data.ingredient.IngredientManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class RecipePersistence {
	private static final File SAVE_FILE = new File(Loader.instance().getConfigDir(), "necalc/recipes.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class Recipe {
		public String machine;
		public ArrayList<String> inputs;
		public ArrayList<String> outputs;
		public int time;
	}
	private static class Wrapper {
		public ArrayList<Recipe> recipes;
	}

	public static void saveRecipeData(ArrayList<RecipeEntry> recipes) {
		SAVE_FILE.getParentFile().mkdirs();
		Wrapper wrapper = new Wrapper();
		wrapper.recipes = new ArrayList<>();
		for (RecipeEntry entry : recipes) {
			Recipe recipe = new Recipe();
			Ingredients machine = entry.getMachine().copy();
			machine.setValue(1);
			recipe.machine = machine.serialize();
			recipe.inputs = new ArrayList<>();
			recipe.outputs = new ArrayList<>();
			recipe.time = entry.getTime();
			for (Ingredients input : entry.getInputs()) {
				Ingredients tempInput = input.copy();
				tempInput.setValue((int) tempInput.getValue());
				recipe.inputs.add(tempInput.serialize());
			}
			for (Ingredients output : entry.getOutputs()) {
				Ingredients tempOutput = output.copy();
				tempOutput.setValue((int) tempOutput.getValue());
				recipe.outputs.add(tempOutput.serialize());
			}
			wrapper.recipes.add(recipe);
		}
		try (FileWriter writer = new FileWriter(SAVE_FILE)) {
			GSON.toJson(wrapper, writer);
		} catch (Exception e) {
			Necalc.logger.error("Failed to save recipe data", e);
		}
	}

	public static ArrayList<RecipeEntry> loadRecipeData() {
		ArrayList<RecipeEntry> recipes = new ArrayList<>();
		if (!SAVE_FILE.exists()) return recipes;

		try (FileReader reader = new FileReader(SAVE_FILE)) {
			Wrapper wrapper = GSON.fromJson(reader, Wrapper.class);
			if (wrapper == null || wrapper.recipes == null) return recipes;
			for (Recipe recipe : wrapper.recipes) {
				ArrayList<Ingredients> inputs = new ArrayList<>();
				for (String inputData : recipe.inputs) {
					Ingredients input = IngredientManager.deserialize(inputData);
					if (input.isEmpty()) continue;
					inputs.add(input);
				}
				Ingredients machine = IngredientManager.deserialize(recipe.machine);
				if (machine.isEmpty()) continue; // must have machine
				machine.setValue(1);
				MachineState.addMachine(machine);

				ArrayList<Ingredients> outputs = new ArrayList<>();
				for (String outputData : recipe.outputs) {
					Ingredients output = IngredientManager.deserialize(outputData);
					if (output.isEmpty()) continue;
					outputs.add(output);
				}
				recipes.add(new RecipeEntry(inputs, machine, outputs, recipe.time));
			}
		} catch (Exception e) {
			Necalc.logger.error("Failed to load recipe data", e);
		}

		return recipes;
	}
}
