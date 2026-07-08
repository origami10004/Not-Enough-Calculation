package com.origami10004.necalc.compat.mekanism;

import net.minecraft.client.Minecraft;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;

import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiCommon;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class GasIngredient extends Ingredients {
	private final Gas gas;

	public GasIngredient(Gas gas, double amount) {
		super(amount);
		this.gas = gas;
	}

	public GasIngredient(Gas gas) {
		this(gas, 0.0);
	}

	public GasIngredient(GasStack gasStack, double amount) {
		super(amount);
		this.gas = gasStack.getGas();
	}

	public GasIngredient(GasStack gasStack) {
		this(gasStack, gasStack.amount);
	}

	public String getDisplayName() {
		return gas.getLocalizedName();
	}

	public List<String> getTooltip(Minecraft mc) {
		List<String> tooltip = new ArrayList<>();
		return tooltip;
	}

	public String serialize() {
		return "gas:" + gas.getName() + ":" + getValue();
	}

	public Ingredients copy() {
		return new GasIngredient(gas, getValue());
	}

	public String formatValue(double value) {
		// TODO: Format the value as needed
		return String.format("%.2f", value);
	}

	public void render(GuiCommon parent, int x, int y) {
		// Implement rendering logic for the gas ingredient here
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof GasIngredient)) return false;
		GasIngredient other = (GasIngredient) obj;
		return this.gas == other.gas;
	}

	@Override
	public int hashCode() {
		return Objects.hash(GasIngredient.class, gas);
	}

	public static Ingredients deserialize(String serialized) {
		try {
			String[] p = serialized.split(":", 3);
			if (p.length < 3 || !p[0].equals("gas")) return Ingredients.EMPTY;
			Gas gas = GasRegistry.getGas(p[1]);
			if (gas == null) return Ingredients.EMPTY;
			return new GasIngredient(gas, Double.parseDouble(p[2]));
		} catch (Exception e) {
			return Ingredients.EMPTY;
		}
	}

	// helper functions
	private GasStack getStack() {
		return new GasStack(gas, Math.max(1, (int) getValue()));
	}

}
