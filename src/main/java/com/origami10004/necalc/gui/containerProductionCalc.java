package com.origami10004.necalc.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class containerProductionCalc {
	private int targetNumRows = 1;
	private int selectedRate = 0;

	containerProductionCalc(InventoryPlayer playerInv) {

	}

	public int getTargetNumRows() {
		return targetNumRows;
	}

	public int getSelectedRate() {
		return selectedRate;
	}

	public boolean setSelectedRate(int newRate) {
		int old = selectedRate;
		this.selectedRate = newRate;
		return old != newRate;
	}

	public ItemStack getTargetSlot(int index) {
		return ItemStack.EMPTY;
	}

	public double getTargetSlotRate(int index) {
		return 0;
	}
}
