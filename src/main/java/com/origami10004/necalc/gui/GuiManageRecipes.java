package com.origami10004.necalc.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import org.lwjgl.input.Mouse;

import com.origami10004.necalc.data.RecipeEntry;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.proxy.ClientProxy;

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
	private int scrollRow = 0;
	private float scrollPercent = 0.0f;
	private boolean draggingScroll = false;

	public GuiManageRecipes(InventoryPlayer playerInv) {
		super(new NecalcContainer(playerInv, false, 0, 0));
		this.playerInv = playerInv;
		ClientProxy.lastOpenedGui = this;
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
				int index = (row + this.scrollRow) * COLS + col;
				int slotX = this.gx + 8 + col * SLOT_SIZE;
				int slotY = curY + row * SLOT_SIZE;
				drawItemSlot(slotX, slotY);

				RecipeEntry recipe = RecipeState.getRecipe(index);
				if (recipe != RecipeEntry.EMPTY) {
					Ingredients output = recipe.getOutputs().get(0);
					output.render(this, slotX + 1, slotY + 1);
				}
			}
		}
		drawRecipeScrollBar();
	}

	@Override
	public void renderHoveredToolTip(int mouseX, int mouseY) {
		
		drawTabTooltips(mouseX, mouseY, this.gx, this.gy);

		int recipe = getRecipeAt(mouseX, mouseY);
		if (recipe != -1) {
			RecipeEntry entry = RecipeState.getRecipe(recipe);
			if (entry != RecipeEntry.EMPTY) {
				drawHoveringText(entry.getOutputs().get(0).getTooltip(this.mc), mouseX, mouseY);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
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

		if (mouseX >= this.gx + 174 && mouseX < this.gx + 188 && mouseY >= this.gy + TAB_H + 17 && mouseY < this.gy + TAB_H + 161) {
			if (mouseButton == 0) {
				this.draggingScroll = true;
				this.scrollPercent = updateScroll(mouseY, this.gy + TAB_H + 17, 144);
				int scrollRows = Math.max(0, RecipeState.getRecipeRows() - ROWS);
				this.scrollRow = (int) (this.scrollPercent * scrollRows);
			}
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (this.draggingScroll) {
			this.scrollPercent = updateScroll(mouseY, this.gy + TAB_H + 17, 144);
			int scrollRows = Math.max(0, RecipeState.getRecipeRows() - ROWS);
			this.scrollRow = (int) (this.scrollPercent * scrollRows);
			return;
		}
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		this.draggingScroll = false;
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int scrollRows = Math.max(0, RecipeState.getRecipeRows() - ROWS);
			if (scrollRows <= 0) return;
			if (scroll > 0) {
				this.scrollRow = Math.max(0, this.scrollRow - 1);
			} else {
				this.scrollRow = Math.min(scrollRows, this.scrollRow + 1);
			}
			this.scrollPercent = (float) this.scrollRow / scrollRows;
			return;
		}
		super.handleMouseInput();
	}

	private void drawRecipeScrollBar() {
		int totalRows = RecipeState.getRecipeRows();

		int sbX = this.gx + 175;
		int sbY = this.gy + TAB_H + 18;
		int width = 12;
		int height = 142;
		if (totalRows <= ROWS) {
			this.scrollPercent = 0.0f;
			this.scrollRow = 0;
		}

		drawScrollbar(sbX, sbY, width, height, this.scrollPercent, totalRows > ROWS);
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
				mc.displayGuiScreen(new GuiManageMachines(this.playerInv));
				break;
			case 4:
				RecipeState.reset();
				mc.displayGuiScreen(new GuiRecipeEditor(this.playerInv, this, true));
				break;
		}
	}

	private int getRecipeAt(int mouseX, int mouseY) {
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int index = (row + this.scrollRow) * COLS + col;
				int slotX = this.gx + 8 + col * SLOT_SIZE;
				int slotY = this.tableY + row * SLOT_SIZE;
				if (mouseX >= slotX && mouseX < slotX + SLOT_SIZE && mouseY >= slotY && mouseY < slotY + SLOT_SIZE) {
					return index;
				}
			}
		}
		return -1;
	}

	@Override
	public Ingredients getHoveredStack(int mouseX, int mouseY) {
		int recipe = getRecipeAt(mouseX, mouseY);
		if (recipe != -1) {
			RecipeEntry entry = RecipeState.getRecipe(recipe);
			if (entry != RecipeEntry.EMPTY) {
				return entry.getOutputs().get(0);
			}
		}
		return Ingredients.EMPTY;
	}
}
