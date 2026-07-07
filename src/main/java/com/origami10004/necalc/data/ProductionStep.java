package com.origami10004.necalc.data;

import com.origami10004.necalc.data.ingredient.Ingredients;

import java.util.List;

/**
 * A class representing a single step in the production process
 */
public class ProductionStep {
	private List<Ingredients> inputs;
	private List<Ingredients> outputs;
	private Ingredients machine;
	private double machineCount;
	private double recipePerMinute;
	private boolean hidden = false;

	public ProductionStep(List<Ingredients> inputs, List<Ingredients> outputs, Ingredients machine, double machineCount, double recipePerMinute) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.machine = machine;
		this.machineCount = machineCount;
		this.recipePerMinute = recipePerMinute;
	}

	public ProductionStep(RecipeEntry recipe, double machineCount, double recipePerMinute) {
		this.inputs = recipe.getInputs();
		this.outputs = recipe.getOutputs();
		this.machine = recipe.getMachine();
		this.machineCount = machineCount;
		this.recipePerMinute = recipePerMinute;

	}

	// View management
	public boolean isHidden() {
		return hidden;
	}

	public void hide() {
		this.hidden = true;
	}

	public void show() {
		this.hidden = false;
	}

	// Input management
	public Ingredients getPrimaryInput() {
		return inputs.isEmpty() ? null : inputs.get(0);
	}

	public double getPrimaryInputRate() {
		return recipePerMinute * inputs.get(0).getValue();
	}

	public List<Ingredients> getInputs() {
		return inputs;
	}

	// Machine management
	public Ingredients getMachine() {
		return machine;
	}

	public double getMachineCount() {
		return machineCount;
	}

	// Output management
	public List<Ingredients> getOutputs() {
		return outputs;
	}

	public Ingredients getPrimaryOutput() {
		return outputs.isEmpty() ? null : outputs.get(0);
	}

	public double getPrimaryOutputRate() {
		return recipePerMinute * outputs.get(0).getValue();
	}
}
