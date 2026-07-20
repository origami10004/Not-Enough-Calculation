package com.origami10004.necalc.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.lwjgl.input.Mouse;

import com.origami10004.necalc.data.MachineState;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.ingredient.Ingredients;
import com.origami10004.necalc.proxy.ClientProxy;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;

public class GuiManageMachines extends GuiCommon {
	// GUI textures
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/machine_list.png");

	// constants for GUI layout
	private static final int GUI_WIDTH = 195;
	private static final int GUI_HEIGHT = 168;
	private static final int SLOT_SIZE = 18;
	public static final int ROWS = 8;
	public static final int COLS = 9;

	@Override
	protected int getActiveTab() {
		return 3;
	}

	// instance variables
	private InventoryPlayer playerInv;
	private int gx, gy;
	private int tableY;
	private SpeedEditHelper editor;
	protected int scrollRow = 0;
	private float scrollPercent = 0.0f;
	private boolean draggingScroll = false;

	public GuiManageMachines(InventoryPlayer playerInv) {
		super(new NecalcContainer(playerInv, false, 0, 0));
		this.playerInv = playerInv;
		this.editor = new SpeedEditHelper(this);
		ClientProxy.lastOpenedGui = this;
	}

	@Override
	public void initGui() {
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT + TAB_H;
		super.initGui();
		this.gx = guiLeft;
		this.gy = guiTop;
		this.tableY = this.gy + TAB_H + 17;
		this.editor.reInit(gx, tableY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawDefaultBackground();
		this.mc.getTextureManager().bindTexture(BG_TEXTURE);
		drawModalRectWithCustomSizedTexture(this.gx, this.gy + TAB_H, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		drawTabStrip(this.gx, this.gy);

		int curY = this.gy + TAB_H;

		this.fontRenderer.drawString(I18n.format("necalc.gui.machine_manager"), this.gx + 8, curY + 6, 0xFF000000);
		curY += 17;

		this.tableY = curY;
		List<Map.Entry<Ingredients, Integer>> machineKeys = new ArrayList<>(MachineState.getMachineSpeeds().entrySet());
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int index = (row + this.scrollRow) * COLS + col;
				int slotX = this.gx + 8 + col * SLOT_SIZE;
				int slotY = curY + row * SLOT_SIZE;
				drawItemSlot(slotX, slotY);

				if (index < machineKeys.size()) {
					Map.Entry<Ingredients, Integer> entry = machineKeys.get(index);
					entry.getKey().render(this, slotX + 1, slotY + 1);
				}
			}
		}
		drawMachineScrollBar();
		editor.drawOverlay(mouseX, mouseY);
	}

	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		List<Map.Entry<Ingredients, Integer>> machineKeys = new ArrayList<>(MachineState.getMachineSpeeds().entrySet());
		
		if (editor.hovered(mouseX, mouseY)) {
			return;
		}

		drawTabTooltips(mouseX, mouseY, this.gx, this.gy);

		int index = getMachineAt(mouseX, mouseY);
		if (index != -1) {
			if (index < machineKeys.size()) {
				Map.Entry<Ingredients, Integer> entry = machineKeys.get(index);
				if (entry.getKey() != null && !entry.getKey().isEmpty()) {
					drawItemExtraInfoTooltip(mouseX,
							mouseY, entry.getKey(),
							I18n.format("necalc.gui.machine_speed", entry.getValue()));
				}
			}
		}

		if (mouseX >= this.gx + 174 && mouseX < this.gx + 188 && mouseY >= this.gy + TAB_H + 3 && mouseY < this.gy + TAB_H + 16) {
			this.drawHoveringText(
					fontRenderer.listFormattedStringToWidth(I18n.format("necalc.gui.machine_help"), 100),
					mouseX, mouseY);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		editor.updateCursorCounter();
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (editor.mouseClicked(mouseX, mouseY, mouseButton)) return;

		for (int i = 0; i < TAB_COUNT; i++) {
			int tx = gx + TAB_LEFT_PAD + i * (TAB_W + 2);
			if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= gy + 2 && mouseY < gy + 2 + TAB_H) {
				onTabClicked(i);
				return;
			}
		}

		int machine = getMachineAt(mouseX, mouseY);
		if (machine != -1 && mouseButton == 0) {
			this.editor.openSlot(machine, this.gx + 8 + (machine % COLS) * SLOT_SIZE, this.tableY + (machine / COLS - this.scrollRow) * SLOT_SIZE);
		}

		if (mouseX >= this.gx + 174 && mouseX < this.gx + 188 && mouseY >= this.gy + TAB_H + 17 && mouseY < this.gy + TAB_H + 161) {
			if (mouseButton == 0) {
				this.draggingScroll = true;
				this.scrollPercent = updateScroll(mouseY, this.gy + TAB_H + 17, 144);
				int scrollRows = Math.max(0, MachineState.getMachineRows() - ROWS);
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
			int scrollRows = Math.max(0, MachineState.getMachineRows() - ROWS);
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
			int scrollRows = Math.max(0, MachineState.getMachineRows() - ROWS);
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

	private void drawMachineScrollBar() {
		int sbX = this.gx + 175;
		int sbY = this.gy + TAB_H + 18;
		int width = 12;
		int height = 142;
		if (MachineState.getMachineRows() <= ROWS) {
			this.scrollPercent = 0.0f;
			this.scrollRow = 0;
		}
		this.drawScrollbar(sbX, sbY, width, height, this.scrollPercent, MachineState.getMachineRows() > ROWS);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (editor.keyTyped(typedChar, keyCode)) return;
		super.keyTyped(typedChar, keyCode);
	}

	//Helper functions
	private void onTabClicked(int tabIndex) {
		switch (tabIndex) {
			case 0:
				mc.displayGuiScreen(new GuiProductionCalc(this.playerInv));
				break;
			case 1:
				mc.displayGuiScreen(new GuiFlowChart(this.playerInv));
				break;
			case 2:
				mc.displayGuiScreen(new GuiManageRecipes(this.playerInv));
				break;
			case 3:
				break;
			case 4:
				RecipeState.reset();
				mc.displayGuiScreen(new GuiRecipeEditor(this.playerInv, this, true));
				break;
		}
	}

	private int getMachineAt(int mouseX, int mouseY) {
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
		int machine = getMachineAt(mouseX, mouseY);
		if (machine != -1) {
			List<Map.Entry<Ingredients, Integer>> machineKeys = new ArrayList<>(MachineState.getMachineSpeeds().entrySet());
			if (machine < machineKeys.size()) {
				return machineKeys.get(machine).getKey();
			}
		}
		return Ingredients.EMPTY;
	}
}
