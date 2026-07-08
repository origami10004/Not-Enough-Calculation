package com.origami10004.necalc.data.ingredient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Loader;

import com.origami10004.necalc.gui.GuiCommon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lwjgl.opengl.GL11;

public class FluidIngredient extends Ingredients {

	private final Fluid fluid;
	private final NBTTagCompound nbt;

	public FluidIngredient(Fluid fluid, NBTTagCompound nbt, double value) {
		super(value);
		this.fluid = fluid;
		this.nbt = nbt;
	}

	public FluidIngredient(Fluid fluid, double value) {
		this(fluid, null, value);
	}

	public FluidIngredient(Fluid fluid, NBTTagCompound nbt) {
		this(fluid, nbt, 0.0);
	}

	public FluidIngredient(Fluid fluid) {
		this(fluid, 0.0);
	}

	public FluidIngredient(FluidStack stack, double value) {
		super(value);
		this.fluid = stack.getFluid();
		this.nbt = stack.tag;
	}

	public FluidIngredient(FluidStack stack) {
		this(stack, stack.amount);
	}
	
	@Override
	public String getDisplayName() {
		return getStack().getLocalizedName();
	}

	@Override
	public List<String> getTooltip(Minecraft mc) {
		List<String> tooltip = new ArrayList<>();
		tooltip.add(getDisplayName());
		if (getValue() > 0) {
			tooltip.add(TextFormatting.GRAY + String.format("%d mB", (int) getValue()));
		}
		if (mc.gameSettings.advancedItemTooltips && FluidRegistry.getFluidName(fluid) != null) {
			tooltip.add(TextFormatting.DARK_GRAY + FluidRegistry.getFluidName(fluid));
		}
		
		String modId = FluidRegistry.getModId(getStack());
		if (modId != null) {
			ModContainer modContainer = Loader.instance().getIndexedModList().get(modId);
			if (modContainer != null) {
				tooltip.add(TextFormatting.BLUE.toString() + TextFormatting.ITALIC + modContainer.getName());
			}
		}
		return tooltip;
	}

	@Override
	public void render(GuiCommon parent, int x, int y) {
		TextureAtlasSprite fluidTexture = parent.mc.getTextureMapBlocks().getAtlasSprite(
				fluid.getStill() != null
						? fluid.getStill().toString()
						: TextureMap.LOCATION_MISSING_TEXTURE.toString());

		if (fluidTexture == null) return;

		parent.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int color = fluid.getColor();
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8)  & 0xFF) / 255f;
		float b = ((color)       & 0xFF) / 255f;
		float a = ((color >> 24) & 0xFF) / 255f;
		if (a == 0) a = 1f;

		GlStateManager.enableBlend();
		GlStateManager.color(r, g, b, a);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x,      y + 16, 0).tex(fluidTexture.getMinU(), fluidTexture.getMaxV()).endVertex();
		buf.pos(x + 16, y + 16, 0).tex(fluidTexture.getMaxU(), fluidTexture.getMaxV()).endVertex();
		buf.pos(x + 16, y,      0).tex(fluidTexture.getMaxU(), fluidTexture.getMinV()).endVertex();
		buf.pos(x,      y,      0).tex(fluidTexture.getMinU(), fluidTexture.getMinV()).endVertex();
		tess.draw();

		GlStateManager.color(1f, 1f, 1f, 1f);
		GlStateManager.disableBlend();
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FluidIngredient)) return false;
		FluidIngredient other = (FluidIngredient) obj;
		return this.fluid.equals(other.fluid);
	}

	@Override
	public String serialize() {
		return "fluid:" + fluid.getName() + "|" + (int) getValue();
	}

	public static Ingredients deserialize(String serialized) {
		// Should be of form "fluid:<fluidname>|<value>"
		// Could also be "fluid:<modid>:<fluidname>|<value>" for modded fluids
		try {
			if (!serialized.startsWith("fluid:")) return Ingredients.EMPTY;
			String rest = serialized.substring(6);
			int pipeIdx = rest.lastIndexOf('|');
			if (pipeIdx < 0) return Ingredients.EMPTY;
			String fluidName = rest.substring(0, pipeIdx);
			double amount    = Double.parseDouble(rest.substring(pipeIdx + 1));
			Fluid fluid = FluidRegistry.getFluid(fluidName);
			if (fluid == null) return Ingredients.EMPTY;
			return new FluidIngredient(fluid, amount);
		} catch (Exception e) {
			return Ingredients.EMPTY;
		}
	}

	@Override
	public Ingredients copy() {
		return new FluidIngredient(fluid, nbt != null ? nbt.copy() : null, getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hash(FluidIngredient.class, fluid);
	}

	//Helper functions
	private FluidStack getStack() {
		// Fluid stack does null checking internally already
		return new FluidStack(fluid, Math.max(1, (int) getValue()), nbt);
	}
}
