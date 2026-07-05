package com.origami10004.necalc.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Container;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.List;

abstract class GuiCommon extends GuiContainer {
	protected static final int TAB_H			= 28;
	protected static final int TAB_W			= 28;
	protected static final int TAB_COUNT		= 5;
	protected static final int TAB_LEFT_PAD = 4;

	protected static final ResourceLocation TAB_ICONS = new ResourceLocation("necalc", "textures/gui/tab_icons.png");
	protected static final ResourceLocation TAB_TEXTURE = new ResourceLocation("necalc", "textures/gui/tab.png");
	protected static final ResourceLocation SLOT_TEXTURE = new ResourceLocation("necalc", "textures/gui/slot.png");
	protected static final ResourceLocation SCROLL_TEXTURE = new ResourceLocation("necalc", "textures/gui/scroll.png");
	protected static final String[] TAB_LABELS = {"tab.main", "tab.flow", "tab.recipes", "tab.machine", "tab.add"};
	
	protected abstract int getActiveTab();
	private ItemStack inventoryHoverStack;

	public GuiCommon(Container container) {
		super(container);
	}

	protected void drawRectPanel(int x, int y, int w, int h, int fillColor) {
		assert w > 6 && h > 6 : "Panel too small for borders";
		// Black border
		drawRect(x + 2, y, x + w - 2, y + h, 0xFF000000);
		drawRect(x, y + 2, x + w, y + h - 2, 0xFF000000);
		drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF000000);

		// Lighter top left
		drawRect(x + 1, y + 2, x + 4, y + h - 3, 0xFFFFFFFF);
		drawRect(x + 2, y + 1, x + w - 3, y + 4, 0xFFFFFFFF);

		// Darker bottom right
		drawRect(x + w - 4, y + 3, x + w - 1, y + h - 2, 0xFF555555);
		drawRect(x + 3, y + h - 4, x + w - 2, y + h - 1, 0xFF555555);

		// Fill
		drawRect(x + 3, y + 4, x + w - 4, y + h - 3, fillColor);
		drawRect(x + 4, y + 3, x + w - 3, y + h - 4, fillColor);
	}

	protected void drawRectPanel(int x, int y, int w, int h) {
		drawRectPanel(x, y, w, h, 0xFFC6C6C6);
	}

	protected void drawTabStrip(int gx, int gy) {
		for (int i = 0; i < TAB_COUNT; i++) {
			int tabX = gx + TAB_LEFT_PAD + i * (TAB_W + 1);
			drawTab(tabX, gy, i == getActiveTab(), i);
		}

	}

	private void drawTab(int x, int y, boolean active, int iconIndex) {
		this.mc.getTextureManager().bindTexture(TAB_TEXTURE);
		if (active) {
			drawModalRectWithCustomSizedTexture(x, y, 0, 0, 28, 31, 56, 31);
		} else {
			drawModalRectWithCustomSizedTexture(x, y, 28, 0, 28, 31, 56, 31);
		}

		this.mc.getTextureManager().bindTexture(TAB_ICONS);
		drawModalRectWithCustomSizedTexture(x + 6, y + 9, iconIndex * 16, 0, 16, 16, 80, 16);
	}

	protected void drawRectPanelIndent(int x, int y, int w, int h, int fillColor) {
		drawRect(x, y, x + w, y + h, 0xFF8B8B8B);
		drawRect(x, y, x + w - 1, y + h - 1, 0xFF373737);
		drawRect(x + 1, y + 1, x + w, y + h, 0xFFFFFFFF);
		drawRect(x + 1, y + 1, x + w - 1, y + h - 1, fillColor);
	}

	// I'm ngl idk what the opposite of indent is really
	protected void drawRectPanelOutdent(int x, int y, int w, int h, int fillColor) {
		drawRect(x, y, x + w - 1, y + h - 1, 0xFFFFFFFF);
		drawRect(x + 1, y + 1, x + w, y + h, 0xFF373737);
		drawRect(x + 1, y + 1, x + w - 1, y + h - 1, fillColor);
	}

	protected void drawButton(int x, int y, int w, int h, String label, int mouseX, int mouseY, boolean pressed, boolean active) {
		drawRect(x, y, x + w, y + h, 0xFF565656);

		if (!active) {
			drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF999999);
			this.fontRenderer.drawString(label, x + (w - this.fontRenderer.getStringWidth(label)) / 2, y + (h - 7) / 2, 0xFF000000);
			return;
		}
		boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
		int topleft = pressed ? 0xFF808080 : (hovered ? 0xFFCED7E9 : 0xFFE5E5E5);
		int botright = pressed ? 0xFFE5E5E5 : (hovered ? 0xFF8EA3CC : 0xFF808080);
		int color = pressed || !hovered ? 0xFFC6C6C6 : 0xFFA8B8D8;
		drawRect(x + 1, y + 1, x + w - 1, y + h - 1, color);
		drawRect(x + 1, y + 1, x + w - 2, y + h - 2, topleft);
		drawRect(x + 2, y + 2, x + w - 1, y + h - 1, botright);
		drawRect(x + 2, y + 2, x + w - 2, y + h - 2, color);
		this.fontRenderer.drawString(label, x + (w - this.fontRenderer.getStringWidth(label)) / 2, y + (h - 7) / 2, 0xFF000000);
	}

	protected void drawTabTooltips(int mouseX, int mouseY, int gx, int gy) {
		for (int i = 0; i < TAB_COUNT; i++) {
			int tabX = gx + TAB_LEFT_PAD + i * (TAB_W + 1);
			if (mouseX >= tabX && mouseX < tabX + TAB_W && mouseY >= gy && mouseY < gy + TAB_H) {
				this.drawHoveringText(I18n.format("necalc.gui." + TAB_LABELS[i]), mouseX, mouseY);
				break;
			}
		}
	}

	protected void drawItemSlot(int x, int y) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(SLOT_TEXTURE);
		drawModalRectWithCustomSizedTexture(x, y, 0, 0, 18, 18, 18, 18);
	}

	/**
	 * Important, arrow drawn uses y as the center of the arrow
	 */
	protected void drawArrow(int x, int midY, int color) {
		drawRect(x, midY, x + 5, midY + 1, color);
		drawRect(x + 2, midY - 1, x + 4, midY + 2, color);
		drawRect(x + 2, midY - 2, x + 3, midY + 3, color);
	}

	protected void drawPlayerInventory(int x, int y, int mouseX, int mouseY, InventoryPlayer inv) {
		inventoryHoverStack = ItemStack.EMPTY;
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int slotX = x + col * 18;
				int slotY = y + row * 18;
				this.drawItemSlot(slotX, slotY);

				ItemStack stack = inv.getStackInSlot(col + row * 9 + 9);
				if (!stack.isEmpty()) {
					if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
						if (!stack.isEmpty()) {
							inventoryHoverStack = stack;
						}
					}
				}
			}
		}
		for (int col = 0; col < 9; col++) {
			int slotX = x + col * 18;
			int slotY = y + 58;
			this.drawItemSlot(slotX, slotY);

			ItemStack stack = inv.getStackInSlot(col);
			if (!stack.isEmpty()) {
				if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
					if (!stack.isEmpty()) {
						inventoryHoverStack = stack;
					}
				}
			}
		}
	}
	
	protected void drawPlayerInventoryTooltips(int mouseX, int mouseY) {
		if (!inventoryHoverStack.isEmpty()) {
			this.renderToolTip(inventoryHoverStack, mouseX, mouseY);
		}
	}

	protected void drawItemExtraInfoTooltip(int mouseX, int mouseY, ItemStack stack, String extraInfo) {
		ITooltipFlag flag = this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;

		List<String> tooltip = stack.getTooltip(this.mc.player, flag);
		tooltip.add(1, extraInfo);
		this.drawHoveringText(tooltip, mouseX, mouseY);
	}

	private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("##.#", DecimalFormatSymbols.getInstance(Locale.ROOT));
	private static final DecimalFormat TWO_DECIMAL = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
	protected String formatValue(double value) {
		if (value == 0) {
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

	protected void drawSlotWithCustomCount(ItemStack stack, int x, int y, double count) {
		if (stack.isEmpty()) return;

		RenderHelper.enableGUIStandardItemLighting();
		this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		RenderHelper.disableStandardItemLighting();
		String text = formatValue(count);
		if (text == "") return;

		GlStateManager.disableDepth();
		GlStateManager.disableBlend();
		GlStateManager.pushMatrix();

		float scale;
		if (text.length() > 3) {
			scale = 0.5f;
		} else {
			scale = 0.75f;
		}
		GlStateManager.scale(scale, scale, 1.0f);

		int width = this.fontRenderer.getStringWidth(text);

		float textX = ((x + 16) / scale) - width;
		float textY = ((y + 16) / scale) - 8;

		this.fontRenderer.drawStringWithShadow(text, textX, textY, 0xFFFFFF);

		GlStateManager.popMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableDepth();
	}

	protected void drawScrollbar(int x, int y, int w, int h, float scrollPercent, boolean active) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(SCROLL_TEXTURE);
		double scrollY = y + (h - 9) * scrollPercent;
		if (active) {
			drawModalRectWithCustomSizedTexture(x, (int) scrollY, 0, 0, 12, 9, 24, 9);
		} else {
			drawModalRectWithCustomSizedTexture(x, (int) scrollY, 12, 0, 12, 9, 24, 9);
		}
	}

	protected float updateScroll(int mouseY, int y, int h) {
		int mouseYpos = mouseY - 4;
		int correctH = h - 9;
		return Math.max(0, Math.min(1, (mouseYpos - y) / (float) correctH));
	}
}
