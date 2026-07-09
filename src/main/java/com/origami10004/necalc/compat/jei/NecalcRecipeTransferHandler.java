package com.origami10004.necalc.compat.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.gui.GuiCommon;
import com.origami10004.necalc.gui.GuiRecipeEditor;
import com.origami10004.necalc.gui.NecalcContainer;
import com.origami10004.necalc.data.ingredient.IngredientManager;
import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.compat.mekanism.MekanismCompat;
import com.origami10004.necalc.compat.thaumcraft.ThaumcraftCompat;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

import java.util.List;

public class NecalcRecipeTransferHandler implements IRecipeTransferHandler<NecalcContainer> {
	@Override
	public Class<NecalcContainer> getContainerClass() {
		return NecalcContainer.class;
	}

	@Override
	public IRecipeTransferError transferRecipe(
				NecalcContainer container,
				IRecipeLayout recipeLayout,
				EntityPlayer player,
				boolean maxTransfer,
				boolean doTransfer) {
		if (!doTransfer) {
			return null;
		}

		GuiCommon gui = container.getGui();
		Necalc.logger.info("Transferring recipe from JEI to Necalc GUI: " + gui.getClass().getSimpleName());
		RecipeState.reset();

		Necalc.logger.info("Transferring items from JEI to Necalc");
		recipeLayout.getItemStacks().getGuiIngredients().values().forEach(slot -> {
			if (slot.isInput()) {
				RecipeState.mergeInputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
			} else {
				RecipeState.mergeOutputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
			}
		});

		Necalc.logger.info("Transferring fluids from JEI to Necalc");
		recipeLayout.getFluidStacks().getGuiIngredients().values().forEach(slot -> {
			if (slot.isInput()) {
				RecipeState.mergeInputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
			} else {
				RecipeState.mergeOutputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
			}
		});

		// if (MekanismCompat.isLoaded()) {
		// 	recipeLayout.getIngredientsGroup(mekanism.client.jei.MekanismJEI.TYPE_GAS).values().forEach(slot -> {
		// 		if (slot.isInput()) {
		// 			RecipeState.mergeInputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
		// 		} else {
		// 			RecipeState.mergeOutputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
		// 		}
		// 	});
		// }

		// This shit is broken, cant find thaumcraft's JEI class
		// if (ThaumcraftCompat.isLoaded()) {
		// 	Class<?> essentiaStackClass = ThaumcraftCompat.getEssentiaStackClass();
		// 	recipeLayout.getIngredientsGroup().values().forEach(slot -> {
		// 		if (slot.isInput()) {
		// 			RecipeState.mergeInputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
		// 		} else {
		// 			RecipeState.mergeOutputIngredient(IngredientManager.of(slot.getDisplayedIngredient()));
		// 		}
		// 	});
		// }

		if (NecalcJeiPlugin.recipeRegistry != null) {
			List<Object> catalysts = NecalcJeiPlugin.recipeRegistry.getRecipeCatalysts(recipeLayout.getRecipeCategory());
			if (!catalysts.isEmpty()) {
				RecipeState.setMachine(IngredientManager.of(catalysts.get(0)));
			}
		} else {
			Necalc.logger.warn("Error loading machine data from JEI, recipe registry is null");
		}

		if ((gui instanceof GuiRecipeEditor)) return null;


		Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(new GuiRecipeEditor(player.inventory, gui, true));
		return null;
	}
}
