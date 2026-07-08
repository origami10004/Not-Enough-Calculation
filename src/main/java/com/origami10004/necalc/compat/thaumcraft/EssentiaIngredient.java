package com.origami10004.necalc.compat.thaumcraft;

import thaumcraft.api.aspects.Aspect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiCommon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import org.lwjgl.opengl.GL11;
import java.util.Locale;

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

	private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("##.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
	private static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	public String formatValue(double value) {
		if (value <= 0) {
			return "";
		}
		if (value < 1000) {
			// Normal
			if (value >= 100) {
				return String.valueOf((int) value);
			} else if (value >= 10) {
				return ONE_DECIMAL.format(value);
			} else {
				return TWO_DECIMAL.format(value);
			}
		} else if (value < 1000000) {
			// Thousands
			if (value >= 100000) {
				return String.format("%.0fK", value / 1000);
			} else if (value >= 10000) {
				return String.format("%.1fK", value / 1000);
			} else {
				return String.format("%.2fK", value / 1000);
			}
		} else if (value < 1000000000) {
			// Millions
			if (value >= 100000000) {
				return String.format("%.0fM", value / 1000000);
			} else if (value >= 10000000) {
				return String.format("%.1fM", value / 1000000);
			} else {
				return String.format("%.2fM", value / 1000000);
			}
		} else if (value < 1000000000000L) {
			// Billions
			if (value >= 100000000000L) {
				return String.format("%.0fB", value / 1000000000);
			} else if (value >= 10000000000L) {
				return String.format("%.1fB", value / 1000000000);
			} else {
				return String.format("%.2fB", value / 1000000000);
			}
		} else {
			// Trillions
			if (value >= 100000000000000L) {
				return String.format("%.0fT", value / 1000000000000L);
			} else if (value >= 10000000000000L) {
				return String.format("%.1fT", value / 1000000000000L);
			} else {
				return String.format("%.2fT", value / 1000000000000L);
			}
		}
	}

	public void render(GuiCommon parent, int x, int y) {
		parent.mc.getTextureManager().bindTexture(essentia.getImage());
		int color = essentia.getColor();
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8)  & 0xFF) / 255f;
		float b = ((color)       & 0xFF) / 255f;

		GlStateManager.enableBlend();
		GlStateManager.color(r, g, b, 1f);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x,      y + 16, 0).tex(0, 1).endVertex();
		buf.pos(x + 16, y + 16, 0).tex(1, 1).endVertex();
		buf.pos(x + 16, y,      0).tex(1, 0).endVertex();
		buf.pos(x,      y,      0).tex(0, 0).endVertex();
		tess.draw();

		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
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