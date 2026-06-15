package com.origami10004.necalc.gui;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class CalculatorState {
	private int rateDisplay = 0;
	private final ItemStackHandler targetSlots = new ItemStackHandler(16);

	public int getRateDisplay() {
		return rateDisplay;
	}
	public boolean setRateDisplay(int newDisplay) {
		int old = rateDisplay;
		this.rateDisplay = newDisplay;
		return old != newDisplay;
	}

	public ItemStack getTargetSlot(int index) {
		return targetSlots.getStackInSlot(index);
	}
	public ItemStackHandler getTargetSlots() {
		return targetSlots;
	}
}
