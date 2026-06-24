package com.origami10004.necalc.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

public class RecipeEntry {
	private ArrayList<ItemStack> inputs;
	private ArrayList<ItemStack> outputs;
	private ItemStack machine;
	private int time;

	public static final RecipeEntry EMPTY = new RecipeEntry();

	public RecipeEntry() {
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.machine = ItemStack.EMPTY;
		this.time = 0;
	}
	public RecipeEntry(ArrayList<ItemStack> inputs, ItemStack machine, ArrayList<ItemStack> outputs, int time) {
		this.inputs = new ArrayList<>();
		for (ItemStack stack : inputs) {
			this.inputs.add(stack.copy());
		}
		this.machine = machine.copy();
		this.outputs = new ArrayList<>();
		for (ItemStack stack : outputs) {
			this.outputs.add(stack.copy());
		}
		this.time = time;
	}
	public RecipeEntry copy() {
		return new RecipeEntry(this.inputs, this.machine, this.outputs, this.time);
	}

	public ArrayList<ItemStack> getInputs() {
		return inputs;
	}

	public ItemStack getMachine() {
		return machine;
	}

	public ArrayList<ItemStack> getOutputs() {
		return outputs;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public boolean isEmpty() {
		return inputs.isEmpty() && outputs.isEmpty();
	}

	public boolean isValid() {
		return !outputs.isEmpty() && !machine.isEmpty();
	}

	public void setInput(int index, ItemStack stack) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid input index");
		} else if (index >= inputs.size()) {
			if (stack.isEmpty()) return;
			inputs.add(stack.copy());
		} else {
			if (stack.isEmpty()) {
				inputs.remove(index);
				return;
			}
			inputs.set(index, stack.copy());
		}
	}

	public void setMachine(ItemStack stack) {
		this.machine = stack.copy();
	}

	public void setOutput(int index, ItemStack stack) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid output index");
		} else if (index >= outputs.size()) {
			if (stack.isEmpty()) return;
			outputs.add(stack.copy());
		} else {
			if (stack.isEmpty()) {
				outputs.remove(index);
				return;
			}
			outputs.set(index, stack.copy());
		}
	}

	public void alterInput(int index, ItemStack stack, int inc, double mult) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid input index");
		}
		if (index >= inputs.size()) {
			ItemStack temp = stack.copy();
			temp.setCount(inc);
			inputs.add(temp);
		} else {
			int current = inputs.get(index).getCount();
			int newCount = (int) ((current + inc) * mult);
			if (mult == 0.5 && (current + inc) % 2 != 0) {
				return; // Prevent halving odd numbers
			}
			if (newCount > 0) {
				inputs.get(index).setCount(newCount);
			}
		}
	}

	public void alterOutput(int index, ItemStack stack, int inc, double mult) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid output index");
		}
		if (index >= outputs.size()) {
			ItemStack temp = stack.copy();
			temp.setCount(inc);
			outputs.add(temp);
		} else {
			int current = outputs.get(index).getCount();
			int newCount = (int) ((current + inc) * mult);
			if (newCount <= 0) {
				outputs.remove(index);
			} else {
				outputs.get(index).setCount(newCount);
			}
		}
	}

	public void clean() {
		Map <ItemKey, Integer> inputMap = new HashMap<>();
		ArrayList<ItemStack> newInputs = new ArrayList<>();
		for (ItemStack stack : inputs) {
			if (stack.isEmpty()) continue;
			ItemKey key = new ItemKey(stack);
			if (!inputMap.containsKey(key)) {
				inputMap.put(key, newInputs.size());
				newInputs.add(stack.copy());
			} else {
				int idx = inputMap.get(key);
				newInputs.get(idx).setCount(newInputs.get(idx).getCount() + stack.getCount());
			}
		}
		inputs.clear();
		inputs.addAll(newInputs);

		Map <ItemKey, Integer> outputMap = new HashMap<>();
		ArrayList<ItemStack> newOutputs = new ArrayList<>();
		for (ItemStack stack : outputs) {
			if (stack.isEmpty()) continue;
			ItemKey key = new ItemKey(stack);
			if (!outputMap.containsKey(key)) {
				outputMap.put(key, newOutputs.size());
				newOutputs.add(stack.copy());
			} else {
				int idx = outputMap.get(key);
				newOutputs.get(idx).setCount(newOutputs.get(idx).getCount() + stack.getCount());
			}
		}
		outputs.clear();
		outputs.addAll(newOutputs);
	}

}
