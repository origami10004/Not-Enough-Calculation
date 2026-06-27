package com.origami10004.necalc.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.origami10004.necalc.Necalc;

import java.util.HashSet;

import net.minecraft.item.ItemStack;

public class RecipeEntry {
	private ArrayList<ItemStack> inputs;
	private ArrayList<ItemStack> outputs;
	private HashSet<ItemKey> inputKeys;
	private HashSet<ItemKey> outputKeys;
	private ItemStack machine;
	private int time;

	public static final RecipeEntry EMPTY = new RecipeEntry();

	public RecipeEntry() {
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
		this.inputKeys = new HashSet<>();
		this.outputKeys = new HashSet<>();
		this.machine = ItemStack.EMPTY;
		this.time = 0;
	}
	public RecipeEntry(ArrayList<ItemStack> inputs, ItemStack machine, ArrayList<ItemStack> outputs, int time) {
		this.inputs = new ArrayList<>();
		this.inputKeys = new HashSet<>();
		for (ItemStack stack : inputs) {
			this.inputs.add(stack.copy());
			this.inputKeys.add(new ItemKey(stack));
		}
		this.machine = machine.copy();
		this.outputs = new ArrayList<>();
		this.outputKeys = new HashSet<>();
		for (ItemStack stack : outputs) {
			this.outputs.add(stack.copy());
			this.outputKeys.add(new ItemKey(stack));
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
		}
		if (inputKeys.contains(new ItemKey(stack))) {
			return;
		}
		if (index >= inputs.size()) {
			if (stack.isEmpty()) return;
			inputs.add(stack.copy());
			inputKeys.add(new ItemKey(stack));
		} else {
			if (stack.isEmpty()) {
				inputs.remove(index);
				inputKeys.remove(new ItemKey(stack));
				return;
			}
			inputKeys.remove(new ItemKey(inputs.get(index)));
			inputs.set(index, stack.copy());
			inputKeys.add(new ItemKey(stack));
		}
	}

	public void setMachine(ItemStack stack) {
		this.machine = stack.copy();
	}

	public void setOutput(int index, ItemStack stack) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("Invalid output index");
		}
		if (outputKeys.contains(new ItemKey(stack))) {
			return;
		}
		if (index >= outputs.size()) {
			if (stack.isEmpty()) return;
			outputs.add(stack.copy());
			outputKeys.add(new ItemKey(stack));
		} else {
			if (stack.isEmpty()) {
				outputs.remove(index);
				outputKeys.remove(new ItemKey(stack));
				return;
			}
			outputKeys.remove(new ItemKey(outputs.get(index)));
			outputs.set(index, stack.copy());
			outputKeys.add(new ItemKey(stack));
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
}
