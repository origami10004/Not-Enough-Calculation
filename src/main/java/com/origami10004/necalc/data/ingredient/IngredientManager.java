package com.origami10004.necalc.data.ingredient;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import com.origami10004.necalc.compat.mekanism.MekanismCompat;
import com.origami10004.necalc.compat.thaumcraft.ThaumcraftCompat;

public class IngredientManager {
	public static String serialize(Ingredients ingredient) {
		return ingredient.serialize();
	}

	public static Ingredients deserialize(String serialized) {
		if (serialized == null || serialized.isEmpty()) return Ingredients.EMPTY;

		int colon = serialized.indexOf(':');
		if (colon < 0) return Ingredients.EMPTY;
		String type = serialized.substring(0, colon);

		switch (type) {
			case "item":
				return ItemIngredient.deserialize(serialized);

			case "fluid":
				return FluidIngredient.deserialize(serialized);

			case "gas":
				if (MekanismCompat.isLoaded()) {
					return com.origami10004.necalc.compat.mekanism.GasIngredient.deserialize(serialized);
				}
				return Ingredients.EMPTY;

			case "essentia":
				if (ThaumcraftCompat.isLoaded()) {
					return com.origami10004.necalc.compat.thaumcraft.EssentiaIngredient.deserialize(serialized);
				}
				return Ingredients.EMPTY;

			default:
				return Ingredients.EMPTY;
		}
	}

	public static Ingredients of(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return Ingredients.EMPTY;
		return new ItemIngredient(stack);
	}

	public static Ingredients of(FluidStack stack) {
		if (stack == null) return Ingredients.EMPTY;
		return new FluidIngredient(stack);
	}

	public static Ingredients of(Object obj) {
		if (obj instanceof ItemStack) {
			return of((ItemStack) obj);
		} else if (obj instanceof FluidStack) {
			return of((FluidStack) obj);
		} else if (MekanismCompat.isGasStack(obj)) {
			return new com.origami10004.necalc.compat.mekanism.GasIngredient((mekanism.api.gas.GasStack) obj);
		} else if (ThaumcraftCompat.isEssentiaStack(obj)) {
			return new com.origami10004.necalc.compat.thaumcraft.EssentiaIngredient((thaumcraft.api.aspects.Aspect) obj);
		}
		return Ingredients.EMPTY;
	}
}
