package com.origami10004.necalc.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class FakeContainer extends Container{
	public FakeContainer(InventoryPlayer playerInv, boolean includeInventory, int invX, int invY) {
		if (!includeInventory) return;
		// Register 9 dummy slots that should not be shown in the GUI (InventoryPlayer indices 0..8)
		for (int col = 0; col < 9; col++) {
			addSlotToContainer(new Slot(playerInv, col, -999, -999));
		}

		// Register the 27 main inventory slots (InventoryPlayer indices 9..35)
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlotToContainer(new Slot(playerInv,
						col + row * 9 + 9,
						invX + col * 18,
						invY + row * 18));
			}
		}

		// Register the 9 hotbar slots (InventoryPlayer indices 0..8)
		for (int col = 0; col < 9; col++) {
			addSlotToContainer(new Slot(playerInv, col, invX + col * 18, invY + 58));
		}
	}

	@Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);
		if (slot == null || !slot.getHasStack()) return result;

		ItemStack stack = slot.getStack();
		result = stack.copy();

		if (index >= 9 && index < 36) {
			// Main inv -> hotbar
			if (!mergeItemStack(stack, 36, 45, false))
				return ItemStack.EMPTY;
		} else {
			// Hotbar -> main inv
			if (!mergeItemStack(stack, 9, 36, false))
				return ItemStack.EMPTY;
		}

		if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
		else slot.onSlotChanged();

		return result;
	}
}
