package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * A class representing a single step in the production process
 */
public class ProductionStep {
	private List<ItemStack> inputs;
	private List<ItemStack> outputs;
	private ItemStack machine;
	private double machineCount;
	private double recipePerMinute;
	private boolean unknownRates;
	private boolean hidden = false;

	public ProductionStep(List<ItemStack> inputs, List<ItemStack> outputs, ItemStack machine, double machineCount, double recipePerMinute, boolean unknownRates) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.machine = machine;
		this.machineCount = machineCount;
		this.recipePerMinute = recipePerMinute;
		this.unknownRates = unknownRates;
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
	public ItemStack getPrimaryInput() {
		return inputs.isEmpty() ? null : inputs.get(0);
	}

	public double getPrimaryInputRate() {
		return recipePerMinute * inputs.get(0).getCount();
	}

	public List<ItemStack> getInputs() {
		return inputs;
	}

	// Machine management
	public ItemStack getMachine() {
		return machine;
	}

	public double getMachineCount() {
		return machineCount;
	}

	// Output management
	public List<ItemStack> getOutputs() {
		return outputs;
	}

	public ItemStack getPrimaryOutput() {
		return outputs.isEmpty() ? null : outputs.get(0);
	}

	public double getPrimaryOutputRate() {
		return recipePerMinute * outputs.get(0).getCount();
	}

	// Unknown rates
	public boolean isUnknown() {
		return unknownRates;
	}
}
