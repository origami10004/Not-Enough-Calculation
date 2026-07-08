package com.origami10004.necalc.data;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.ingredient.Ingredients;

import java.util.ArrayList;
import java.util.HashSet;

public class RecipeEntry {
	private ArrayList<Ingredients> inputs;
	private ArrayList<Ingredients> outputs;
	private HashSet<Ingredients> inputKeys;
	private HashSet<Ingredients> outputKeys;
	private Ingredients machine;
	private int time;

	public static final RecipeEntry EMPTY = new RecipeEntry();

	public RecipeEntry() {
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.inputKeys = new HashSet<>();
		this.outputKeys = new HashSet<>();
		this.machine = Ingredients.EMPTY;
		this.time = 1;
	}
	public RecipeEntry(ArrayList<Ingredients> inputs, Ingredients machine, ArrayList<Ingredients> outputs, int time) {
		this.inputs = new ArrayList<>();
		this.inputKeys = new HashSet<>();
		for (Ingredients ing : inputs) {
			ing = ing.copy();
			this.inputs.add(ing);
			this.inputKeys.add(ing);
		}
		this.machine = machine.copy();
		this.outputs = new ArrayList<>();
		this.outputKeys = new HashSet<>();
		for (Ingredients ing : outputs) {
			ing = ing.copy();
			this.outputs.add(ing);
			this.outputKeys.add(ing);
		}
		this.time = Math.max(time, 1);
	}
	public RecipeEntry copy() {
		return new RecipeEntry(this.inputs, this.machine, this.outputs, this.time);
	}

	public ArrayList<Ingredients> getInputs() {
		return inputs;
	}

	public Ingredients getMachine() {
		return machine;
	}

	public ArrayList<Ingredients> getOutputs() {
		return outputs;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = Math.max(time, 1);
	}
	
	public boolean isEmpty() {
		return inputs.isEmpty() && outputs.isEmpty();
	}

	public boolean isValid() {
		return !outputs.isEmpty() && !machine.isEmpty();
	}

	public void setInput(int index, Ingredients ingredient) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid input index");
		}
		if (inputKeys.contains(ingredient)) {
			return;
		}
		if (index >= inputs.size()) {
			if (ingredient.isEmpty()) return;
			inputs.add(ingredient.copy());
			inputKeys.add(ingredient);
		} else {
			if (ingredient.isEmpty()) {
				inputKeys.remove(inputs.get(index));
				inputs.remove(index);
				return;
			}
			inputKeys.remove(inputs.get(index));
			inputs.set(index, ingredient.copy());
			inputKeys.add(ingredient);
		}
	}

	public void setMachine(Ingredients machine) {
		this.machine = machine.copy();
		this.machine.setValue(1);
	}

	public void setOutput(int index, Ingredients ingredient) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid output index");
		}
		if (outputKeys.contains(ingredient)) {
			return;
		}
		if (index >= outputs.size()) {
			if (ingredient.isEmpty()) return;
			outputs.add(ingredient.copy());
			outputKeys.add(ingredient);
		} else {
			if (ingredient.isEmpty()) {
				outputKeys.remove(outputs.get(index));
				outputs.remove(index);
				return;
			}
			outputKeys.remove(outputs.get(index));
			outputs.set(index, ingredient.copy());
			outputKeys.add(ingredient);
		}
	}

	public void alterInput(int index, Ingredients ingredient, int inc, double mult) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid input index");
		}
		if (index >= inputs.size()) {
			// new input
			if (ingredient.isEmpty() || inputs.contains(ingredient)) return;
			ingredient.setValue(inc);
			inputs.add(ingredient);
		} else {
			int current = (int) inputs.get(index).getValue();
			int newCount = (int) ((current + inc) * mult);
			if (mult == 0.5 && (current + inc) % 2 != 0) {
				return; // Prevent halving odd numbers
			}
			if (newCount > 0) {
				inputs.get(index).setValue(newCount);
			}
		}
	}

	public void alterOutput(int index, Ingredients ingredient, int inc, double mult) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid output index");
		}
		if (index >= outputs.size()) {
			// new output
			if (ingredient.isEmpty() || outputs.contains(ingredient)) return;
			ingredient.setValue(inc);
			outputs.add(ingredient);
		} else {
			int current = (int) outputs.get(index).getValue();
			int newCount = (int) ((current + inc) * mult);
			if (newCount <= 0) {
				outputs.remove(index);
			} else {
				outputs.get(index).setValue(newCount);
			}
		}
	}
}
