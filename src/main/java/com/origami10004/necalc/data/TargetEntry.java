package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;

/**
 * A class representing a target the player wants to calculate for
 */
public class TargetEntry {
	private ItemStack item;
	private double ratePerMinute;

	public TargetEntry(ItemStack item, double ratePerMinute) {
		this.item = item;
		this.ratePerMinute = ratePerMinute	;
	}


}
