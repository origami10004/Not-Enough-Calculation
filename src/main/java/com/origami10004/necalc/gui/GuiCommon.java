package com.origami10004.necalc.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.Container;

abstract class GuiCommon extends GuiContainer {
	protected static final int TAB_H			= 28;
	protected static final int TAB_W			= 28;
	protected static final int TAB_COUNT		= 4;
	protected static final int TAB_LEFT_PAD = 4;

	protected static final ResourceLocation TAB_ICONS = new ResourceLocation("necalc", "textures/gui/tab_icons.png");
	protected static final String[] TAB_LABELS = {"tab.main", "tab.flow", "tab.recipes", "tab.add"};
	
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
		int gy = y;
		int color;
		if (!active) {
			gy += 2;
			color = 0xFF8B8B8B;
		} else {
			color = 0xFFC6C6C6;
		}

		// Black border
		drawRect(x, gy + 2, x + TAB_W, y + TAB_H, 0xFF000000);
		drawRect(x + 1, gy + 1, x + TAB_W - 1, gy + 2, 0xFF000000);
		drawRect(x + 2, gy, x + TAB_W - 2, gy + 1, 0xFF000000);

		// Lighter top left
		drawRect(x + 1, gy + 2, x + 4, y + TAB_H, 0xFFFFFFFF);
		drawRect(x + 2, gy + 1, x + TAB_W - 3, gy + 4, 0xFFFFFFFF);

		// Darker right
		drawRect(x + TAB_W - 3, gy + 3, x + TAB_W - 1, y + TAB_H, 0xFF555555);

		// Fill
		drawRect(x + 3, gy + 4, x + TAB_W - 3, y + TAB_H, color);
		drawRect(x + 4, gy + 3, x + TAB_W - 3, y + TAB_H, color);

		if (active) {
			// Spillover
			drawRect(x + 1, gy + 2, x + 3, y + TAB_H + 3, 0xFFFFFFFF);

			drawRect(x + TAB_W - 3, gy + 3, x + TAB_W - 2, y + TAB_H + 3, 0xFF555555);
			drawRect(x + TAB_W - 2, gy + 3, x + TAB_W - 1, y + TAB_H + 2, 0xFF555555);

			drawRect(x + 3, gy + 4, x + TAB_W - 3, y + TAB_H + 3, color);
		}

		this.mc.getTextureManager().bindTexture(TAB_ICONS);
		drawTexturedModalRect(x + 6, y + 9, iconIndex * 16, 0, 16, 16);
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

	protected void drawItemSlot(int x, int y, int color) {
		drawRectPanelIndent(x, y, 18, 18, color);
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
				this.drawItemSlot(slotX, slotY, 0xFF8B8B8B);

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
			this.drawItemSlot(slotX, slotY, 0xFF8B8B8B);

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
}
