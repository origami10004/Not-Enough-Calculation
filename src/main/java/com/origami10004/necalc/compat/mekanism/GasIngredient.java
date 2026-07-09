package com.origami10004.necalc.compat.mekanism;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;

import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.gui.GuiCommon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import org.lwjgl.opengl.GL11;

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

	private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("##.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
	@Override
	public String formatValue(double value) {
		if (value <= 0) {
			return "";
		}
		if (value < 1000) {
			// Normal
			if (value >= 10) {
				return String.format("%.0fmB", value);
			} else {
				return ONE_DECIMAL.format(value);
			}
		} else if (value < 1000000) {
			// Thousands
			if (value >= 100000) {
				return String.format("%.0fB", value / 1000);
			} else if (value >= 10000) {
				return String.format("%.1fB", value / 1000);
			} else {
				return String.format("%.2fB", value / 1000);
			}
		} else if (value < 1000000000) {
			// Millions
			if (value >= 100000000) {
				return String.format("%.0fkB", value / 1000000);
			} else if (value >= 10000000) {
				return String.format("%.1fkB", value / 1000000);
			} else {
				return String.format("%.2fkB", value / 1000000);
			}
		} else if (value < 1000000000000L) {
			// Billions
			if (value >= 100000000000L) {
				return String.format("%.0fMB", value / 1000000000);
			} else if (value >= 10000000000L) {
				return String.format("%.1fMB", value / 1000000000);
			} else {
				return String.format("%.2fMB", value / 1000000000);
			}
		} else {
			// Trillions
			if (value >= 100000000000000L) {
				return String.format("%.0fBB", value / 1000000000000L);
			} else if (value >= 10000000000000L) {
				return String.format("%.1fBB", value / 1000000000000L);
			} else {
				return String.format("%.2fBB", value / 1000000000000L);
			}
		}
	}

	public void render(GuiCommon parent, int x, int y) {
		parent.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = gas.getSprite();
		if (sprite == null) return;

		parent.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int color = gas.getTint();
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8)  & 0xFF) / 255f;
		float b = ((color)       & 0xFF) / 255f;

		GlStateManager.enableBlend();
		GlStateManager.color(r, g, b, 1f);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x,      y + 16, 0).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
		buf.pos(x + 16, y + 16, 0).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
		buf.pos(x + 16, y,      0).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
		buf.pos(x,      y,      0).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
		tess.draw();

		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
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
	public GasStack getStack() {
		return new GasStack(gas, Math.max(1, (int) getValue()));
	}

}
