package com.origami10004.necalc.compat.thaumcraft;

import thaumcraft.api.aspects.Aspect;

import net.minecraft.client.Minecraft;

import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiCommon;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class EssentiaIngredient extends Ingredients {
	private final Aspect essentia;
	public EssentiaIngredient(Aspect aspect, double value) {
		super(value);
		this.essentia = aspect;
	}

	public String getDisplayName() {
		return essentia.getName();
	}

	public List<String> getTooltip(Minecraft mc) {
		List<String> tooltip = new ArrayList<>();
		return tooltip;
	}

	public String serialize() {
		return "essentia:" + essentia.getTag() + ":" + getValue();
	}

	public Ingredients copy() {
		return new EssentiaIngredient(essentia, getValue());
	}

	public String formatValue(double value) {
		// TODO: Format the value as needed
		return String.format("%.2f", value);
	}

	public void render(GuiCommon parent, int x, int y) {
		// Implement rendering logic for the essentia ingredient here
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof EssentiaIngredient)) return false;
		EssentiaIngredient other = (EssentiaIngredient) obj;
		return this.essentia == other.essentia;
	}

	@Override
	public int hashCode() {
		return Objects.hash(EssentiaIngredient.class, essentia);
	}

	public static Ingredients deserialize(String serialized) {
		try {
			String[] p = serialized.split(":", 3);
			if (p.length != 3) return Ingredients.EMPTY;
			Aspect aspect = Aspect.getAspect(p[1]);
			if (aspect == null) return Ingredients.EMPTY;
			return new EssentiaIngredient(aspect, Double.parseDouble(p[2]));
		} catch (Exception e) {
			return Ingredients.EMPTY;
		}
	}
}