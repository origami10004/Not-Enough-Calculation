package com.origami10004.necalc.data.ingredient;

import net.minecraft.client.Minecraft;

import com.origami10004.necalc.gui.GuiCommon;

import java.util.List;
import java.util.ArrayList;

public abstract class Ingredients {
	public static final Ingredients EMPTY = new Ingredients(0.0) {
		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public List<String> getTooltip(Minecraft mc) {
			return new ArrayList<>();
		}

		@Override
		public void render(GuiCommon parent, int x, int y) {
			// Do nothing
		}

		@Override
		public void renderValue(GuiCommon parent, int x, int y, double customValue) {
			// Do nothing
		}

		@Override
		public String formatValue(double value) {
			return "";
		}

		@Override
		public String serialize() {
			return "";
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Ingredients && ((Ingredients) obj).isEmpty();
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Ingredients copy() {
			return this;
		}

		@Override
		public void setValue(double value) {
			// Do nothing
		}
	};


	private double value;

	protected Ingredients(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = Math.max(0, value);
	}

	public boolean isEmpty() {
		return false;
	}

	public String formatValue() {
		return formatValue(getValue());
	}

	public void renderValue(GuiCommon parent, int x, int y) {
		renderValue(parent, x, y, getValue());
	}

	public abstract void render(GuiCommon parent, int x, int y);
	public abstract void renderValue(GuiCommon parent, int x, int y, double customValue);
	public abstract String getDisplayName();
	public abstract List<String> getTooltip(Minecraft mc);
	public abstract String formatValue(double value);
	public abstract String serialize();
	public abstract Ingredients copy();

	// Forced overrides
	@Override
	public abstract boolean equals(Object obj);
	@Override
	public abstract int hashCode();
}
