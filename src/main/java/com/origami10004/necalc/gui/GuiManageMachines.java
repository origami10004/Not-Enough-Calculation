package com.origami10004.necalc.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.MachineKey;
import com.origami10004.necalc.data.MachineState;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;

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

	public GuiManageMachines(InventoryPlayer playerInv) {
		super(new FakeContainer(playerInv, false, 0, 0));
		this.playerInv = playerInv;
		this.editor = new SpeedEditHelper(this);
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
		List<Map.Entry<MachineKey, Integer>> machineKeys = new ArrayList<>(MachineState.getMachineSpeeds().entrySet());
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				int index = row * COLS + col;
				int slotX = this.gx + 8 + col * SLOT_SIZE;
				int slotY = curY + row * SLOT_SIZE;
				drawItemSlot(slotX, slotY);

				if (index < machineKeys.size()) {
					Map.Entry<MachineKey, Integer> entry = machineKeys.get(index);
					ResourceLocation machineLoc = new ResourceLocation(entry.getKey().registryName);
					Item machine = Item.REGISTRY.getObject(machineLoc);
					if (machine == null) {
						Necalc.logger.warn("Unknown machine in machine data: {}", entry.getKey().registryName);
						continue;
					}
					ItemStack stack = new ItemStack(machine, 1, entry.getKey().meta);

					RenderHelper.enableGUIStandardItemLighting();
					this.itemRender.renderItemAndEffectIntoGUI(stack, slotX + 1, slotY + 1);
					RenderHelper.disableStandardItemLighting();
				}
			}
		}

		editor.drawOverlay(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		List<Map.Entry<MachineKey, Integer>> machineKeys = new ArrayList<>(MachineState.getMachineSpeeds().entrySet());
		ITooltipFlag flag = this.mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;

		if (editor.hovered(mouseX, mouseY)) {
			return;
		}

		drawTabTooltips(mouseX - this.guiLeft, mouseY - this.guiTop, this.gx - this.guiLeft, this.gy - this.guiTop);

		int index = getMachineAt(mouseX, mouseY);
		if (index != -1) {
			if (index < machineKeys.size()) {
				Map.Entry<MachineKey, Integer> entry = machineKeys.get(index);
				ResourceLocation machineLoc = new ResourceLocation(entry.getKey().registryName);
				Item machine = Item.REGISTRY.getObject(machineLoc);
				if (machine != null) {
					ItemStack stack = new ItemStack(machine, 1, entry.getKey().meta);
					
					List<String> tooltip = stack.getTooltip(this.mc.player, flag);
					tooltip.add(1, I18n.format("necalc.gui.machine_speed", entry.getValue()));
					this.drawHoveringText(tooltip, mouseX - this.guiLeft, mouseY - this.guiTop);
				}
			}
		}

		if (mouseX >= this.gx + 174 && mouseX < this.gx + 188 && mouseY >= this.gy + TAB_H + 3 && mouseY < this.gy + TAB_H + 16) {
			this.drawHoveringText(
					fontRenderer.listFormattedStringToWidth(I18n.format("necalc.gui.machine_help"), 100),
					mouseX - this.guiLeft, mouseY - this.guiTop);
		}
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
			this.editor.openSlot(machine, this.gx + 8 + (machine % COLS) * SLOT_SIZE, this.tableY + (machine / COLS) * SLOT_SIZE);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
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
				//mc.displayGuiScreen(new GuiFlowChart(this.playerInv));
				break;
			case 2:
				mc.displayGuiScreen(new GuiManageRecipes(this.playerInv));
				break;
			case 3:
				break;
			case 4:
				mc.displayGuiScreen(new GuiRecipeEditor(this.playerInv, this, true));
				break;
		}
	}

	private int getMachineAt(int mouseX, int mouseY) {
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
