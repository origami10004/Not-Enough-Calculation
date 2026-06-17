package com.origami10004.necalc.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class FakeContainer extends Container{
	public FakeContainer(InventoryPlayer playerInv) {
		// Register the 9 hotbar slots (InventoryPlayer indices 0..8)
		for (int col = 0; col < 9; col++) {
			addSlotToContainer(new Slot(playerInv, col, 9 + col * 18, 306));
		}

		// Register the 27 main inventory slots (InventoryPlayer indices 9..35)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlotToContainer(new Slot(playerInv,
						col + row * 9 + 9,
						9 + col * 18,
						248 + row * 18));
			}
		}
	}

	@Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
