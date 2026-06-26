package com.origami10004.necalc.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

import com.origami10004.necalc.data.RecipeEntry;
import com.origami10004.necalc.data.RecipeState;

public class GuiManageRecipes extends GuiCommon{
	// GUI textures
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/recipe_list.png");

	// constants for GUI layout
	private static final int GUI_WIDTH = 195;
	private static final int GUI_HEIGHT = 168;
	private static final int SLOT_SIZE = 18;
	private static final int ROWS = 8;
	private static final int COLS = 9;

	@Override
	protected int getActiveTab() {
		return 2;
	}

	// instance variables
	private InventoryPlayer playerInv;
	private int gx, gy;
	private int tableY;

	public GuiManageRecipes(InventoryPlayer playerInv) {
		super(new FakeContainer(playerInv, false, 0, 0));
		this.playerInv = playerInv;
	}

	@Override
	public void initGui() {
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT + TAB_H;
		super.initGui();
		this.gx = guiLeft;
		this.gy = guiTop;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		this.mc.getTextureManager().bindTexture(BG_TEXTURE);
		drawModalRectWithCustomSizedTexture(this.gx, this.gy + TAB_H, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		drawTabStrip(this.gx, this.gy);

		int curY = this.gy + TAB_H;

		this.fontRenderer.drawString(I18n.format("necalc.gui.recipe_manager"), this.gx + 8, curY + 6, 0xFF000000);
		curY += 17;

		this.tableY = curY;
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int index = row * COLS + col;
				int slotX = this.gx + 8 + col * SLOT_SIZE;
				int slotY = curY + row * SLOT_SIZE;
				drawItemSlot(slotX, slotY);

				RecipeEntry recipe = RecipeState.getRecipe(index);
				if (recipe != RecipeEntry.EMPTY) {
					ItemStack output = recipe.getOutputs().get(0);
					RenderHelper.enableGUIStandardItemLighting();
					this.itemRender.renderItemAndEffectIntoGUI(output, slotX + 1, slotY + 1);
					RenderHelper.disableStandardItemLighting();
				}
			}
		}
	}

	@Override
	public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		
		drawTabTooltips(mouseX - this.guiLeft, mouseY - this.guiTop, this.gx - this.guiLeft, this.gy - this.guiTop);

		int recipe = getRecipeAt(mouseX, mouseY);
		if (recipe != -1) {
			RecipeEntry entry = RecipeState.getRecipe(recipe);
			if (entry != RecipeEntry.EMPTY) {
				this.drawHoveringText(entry.getOutputs().get(0).getDisplayName(), mouseX - this.guiLeft, mouseY - this.guiTop);
			}
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (int i = 0; i < TAB_COUNT; i++) {
			int tx = gx + TAB_LEFT_PAD + i * (TAB_W + 2);
			if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= gy + 2 && mouseY < gy + 2 + TAB_H) {
				onTabClicked(i);
				return;
			}
		}

		int recipe = getRecipeAt(mouseX, mouseY);
		if (recipe != -1) {
			RecipeEntry entry = RecipeState.getRecipe(recipe);
			if (entry != RecipeEntry.EMPTY) {
				RecipeState.stageRecipe(recipe);
				mc.displayGuiScreen(new GuiRecipeEditor(this.playerInv, this, false));
			}
		}
	}


	// Helper functions
	private void onTabClicked(int tabIndex) {
		switch (tabIndex) {
			case 0:
				mc.displayGuiScreen(new GuiProductionCalc(this.playerInv));
				break;
			case 1:
				//mc.displayGuiScreen(new GuiFlowChart(this.playerInv));
				break;
			case 2:
				break;
			case 3:
				// mc.displayGuiScreen(new GuiAddRecipe(playerInv, container));
				break;
			case 4:
				mc.displayGuiScreen(new GuiRecipeEditor(this.playerInv, this, true));
				break;
		}
	}

	private int getRecipeAt(int mouseX, int mouseY) {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int index = row * COLS + col;
				int slotX = this.gx + 8 + col * SLOT_SIZE;
				int slotY = this.tableY + row * SLOT_SIZE;
				if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
					return index;
				}
			}
		}
		return -1;
	}
}
