package com.origami10004.necalc.compat.jei;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

public class NecalcRecipeTransferHandler implements IRecipeTransferHandler<Container> {
	@Override
	public Class<Container> getContainerClass() {
		return Container.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(Container container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
		return null;
	}
}
