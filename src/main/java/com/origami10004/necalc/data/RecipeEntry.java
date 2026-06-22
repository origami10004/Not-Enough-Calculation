package com.origami10004.necalc.data;

import java.util.ArrayList;

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

}
