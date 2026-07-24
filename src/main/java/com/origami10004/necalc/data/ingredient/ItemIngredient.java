package com.origami10004.necalc.data.ingredient;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.origami10004.necalc.gui.GuiCommon;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.List;
import java.util.Objects;

public class ItemIngredient extends Ingredients {

	private final Item item;
	private final int meta;
	private final NBTTagCompound nbt;

	// Constructors
	public ItemIngredient(ItemStack stack, double value) {
		super(value);
		this.item = stack.getItem();
		this.meta = stack.getMetadata();
		this.nbt = stack.getTagCompound();
	}

	public ItemIngredient(ItemStack stack) {
		this(stack, stack.getCount());
	}

	public ItemIngredient(Item item, int meta, NBTTagCompound nbt, double value) {
		super(value);
		this.item = item;
		this.meta = meta;
		this.nbt = nbt;
	}

	public ItemIngredient(Item item, int meta, NBTTagCompound nbt) {
		this(item, meta, nbt, 0.0);
	}

	public ItemIngredient(Item item, int meta, double value) {
		this(item, meta, null, value);
	}

	public ItemIngredient(Item item, int meta) {
		this(item, meta, null, 0.0);
	}

	public ItemIngredient(Item item, double value) {
		this(item, 0, null, value);
	}

	public ItemIngredient(Item item) {
		this(item, 0, null, 0.0);
	}

	@Override
	public String getDisplayName() {
		return getStack().getDisplayName();
	}

	@Override
	public List<String> getTooltip(Minecraft mc) {
		List<String> tooltip = getStack().getTooltip(
				mc.player,
				mc.gameSettings.advancedItemTooltips
						? ITooltipFlag.TooltipFlags.ADVANCED
						: ITooltipFlag.TooltipFlags.NORMAL);
		String modId = getStack().getItem().getCreatorModId(getStack());

		// if (modId != null && !modId.isEmpty()) {
		// 	ModContainer modContainer = Loader.instance().getIndexedModList().get(modId);
		// 	if (modContainer != null) {
		// 		tooltip.add(TextFormatting.BLUE.toString() + TextFormatting.ITALIC + modContainer.getName());
		// 	}
		// }
		
		return tooltip;
	}

	@Override
	public void render(GuiCommon parent, int x, int y) {
		RenderHelper.enableGUIStandardItemLighting();
		parent.getItemRender().renderItemAndEffectIntoGUI(getStack(), x, y);
		RenderHelper.disableStandardItemLighting();
	}

	private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("##.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
	private static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	@Override
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

	@Override
	public String serialize() {
		ResourceLocation itemLoc = item.getRegistryName();
		String itemStr = "item:" + (itemLoc != null ? itemLoc.toString() : "unknown") + ":" + meta + ":" + getValue();
		if (nbt != null) {
			itemStr += ":" + nbt.toString();
		}
		return itemStr;
	}

	public static Ingredients deserialize(String serialized) {
		// Should be of form "item:<modid>:<itemname>:<meta>:<value>[:<nbt>]"
		try {
			String[] p = serialized.split(":", 6);
			if (p.length < 5 || !p[0].equals("item")) return null;
			String regName = p[1] + ":" + p[2];
			int meta = Integer.parseInt(p[3]);
			double amount = Double.parseDouble(p[4]);
			Item item = Item.REGISTRY.getObject(new ResourceLocation(regName));
			if (item == null) return Ingredients.EMPTY;
			NBTTagCompound nbt = null;
			if (p.length > 5 && !p[5].isEmpty()) {
				nbt = net.minecraft.nbt.JsonToNBT.getTagFromJson(p[5]);
			}
			return new ItemIngredient(item, meta, nbt, amount);
		} catch (Exception e) {
			return Ingredients.EMPTY;
		}
	}

	@Override
	public Ingredients copy() {
		return new ItemIngredient(item, meta, nbt != null ? nbt.copy() : null, getValue());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ItemIngredient)) return false;
		ItemIngredient other = (ItemIngredient) obj;
		return this.item == other.item
				&& this.meta == other.meta
				&& ((this.nbt == null && other.nbt == null)
						|| (this.nbt != null && other.nbt != null && this.nbt.equals(other.nbt)));
	}

	@Override
	public int hashCode() {
		return Objects.hash(ItemIngredient.class, item, meta, nbt);
	}

	// Helper functions
	public ItemStack getStack() {
		ItemStack stack = new ItemStack(item, Math.max(1, (int) getValue()), meta);
		stack.setTagCompound(nbt);
		return stack;
	}
}
