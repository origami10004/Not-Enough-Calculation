package com.origami10004.necalc.calculator;

import net.minecraft.item.ItemStack;

/**
 * A class representing a target the player wants to calculate for
 */
public class targetEntry {
    private ItemStack item;
    private double ratePerSecond;

    public targetEntry(ItemStack item, double ratePerSecond) {
        this.item = item;
        this.ratePerSecond = ratePerSecond;
    }


}
