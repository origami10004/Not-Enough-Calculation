package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;

public class CalculationTarget {
	public static final CalculationTarget EMPTY = new CalculationTarget(ItemStack.EMPTY, 0.0);
	private ItemStack targetItem;
	private double targetRate;
	public CalculationTarget(ItemStack targetItem, double targetRate) {
		this.targetItem = targetItem;
		this.targetRate = targetRate;
	}
	public ItemStack getTargetItem() {
		return targetItem;
	}
	public double getTargetRate() {
		return targetRate;
	}
	public boolean setTargetItem(ItemStack targetItem, double rate) {
		ItemStack old = this.targetItem;
		this.targetItem = targetItem;
		if (!ItemStack.areItemStacksEqual(old, targetItem)){
			this.targetRate = rate;
			return true;
		}
		return false;
	}
	public boolean setTargetRate(double targetRate) {
		double old = this.targetRate;
		this.targetRate = targetRate;
		return old != targetRate;
	}
}
