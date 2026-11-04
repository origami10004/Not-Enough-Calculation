package com.origami10004.necalc.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.input.Mouse;

import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.gui.flowchart.FlowControl;
import com.origami10004.necalc.gui.flowchart.FlowNode;
import com.origami10004.necalc.gui.flowchart.FlowRecipeNode;
import com.origami10004.necalc.proxy.ClientProxy;
import com.origami10004.necalc.data.ingredient.Ingredients;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;

public class GuiFlowChart extends GuiCommon {
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/flowchart.png");
	private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("necalc", "textures/gui/button_icons.png");
	private int drawMouseX, drawMouseY;

	private static final int GUI_WIDTH = 552;
	private static final int GUI_HEIGHT = 300;

	@Override
	protected int getActiveTab() {
		return 1;
	}

	private FlowNode draggingNode = null;
	private boolean isPanning = false;
	private int panStartX = 0;
	private int panStartY = 0;


	public GuiFlowChart(InventoryPlayer playerInv) {
		super(new NecalcContainer(playerInv, false, 0, 0));
		this.playerInv = playerInv;
		ClientProxy.lastOpenedGui = this;
	}

	@Override
	public void initGui() {
		this.xSize = GUI_WIDTH;
		this.ySize = GUI_HEIGHT + TAB_H;
		super.initGui();

		FlowControl.init(this);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawMouseX = mouseX;
		drawMouseY = mouseY;
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BG_TEXTURE);
		drawModalRectWithCustomSizedTexture(guiLeft, guiTop + TAB_H, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		drawTabStrip(guiLeft, guiTop);
		this.fontRenderer.drawString(I18n.format("necalc.gui.flowchart" ), this.guiLeft + 8, this.guiTop + TAB_H + 6, 0xFF000000);
		drawEyeButton(guiLeft + GUI_WIDTH - 22, guiTop + TAB_H + 3, mouseX, mouseY,
				CalculatorState.hasHidden(GuiProductionCalc.TableType.PROD));

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		int scissorX = this.guiLeft + 8;
		int scissorY = this.height - (this.guiTop + TAB_H + 17 + 274); // flip Y
		int scissorW = 534;
		int scissorH = 274;

		// Scale by Minecraft's GUI scale factor
		int scale = new ScaledResolution(mc).getScaleFactor();
		GL11.glScissor(
			scissorX * scale,
			scissorY * scale,
			scissorW * scale,
			scissorH * scale
		);

		for (int i = 0; i < FlowControl.getEdges().size(); i++) {
			FlowControl.getEdges().get(i).draw();
		}

		GlStateManager.pushMatrix();
		GlStateManager.scale((float) FlowControl.getZoom(), (float) FlowControl.getZoom(), 1.0f);
		GlStateManager.translate(-FlowControl.getPanX(), -FlowControl.getPanY(), 0);
		
		for (int i = 0; i < FlowControl.getNodes().size(); i++) {
			FlowControl.getNodes().get(i).draw(this);
		}

		GlStateManager.popMatrix();
		GL11.glDisable(GL11.GL_SCISSOR_TEST);

	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
		// tab clicked
		for (int i = 0; i < TAB_COUNT; i++) {
			int tx = guiLeft + TAB_LEFT_PAD + i * (TAB_W + 2);
			if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= guiTop + 2 && mouseY < guiTop + 2 + TAB_H) {
				onTabClicked(i);
				return;
			}
		}

		if (isOnShowAll(mouseX, mouseY)) {
			if (mouseButton == 0) {
				CalculatorState.showAllRecipes();
			}
			return;
		}

		if (isInChart(mouseX, mouseY)) {
			double cx = FlowControl.toCanvasX(mouseX);
			double cy = FlowControl.toCanvasY(mouseY);

			// dragging node
			for (int i = FlowControl.getNodes().size() - 1; i >= 0; i--) {
				FlowNode node = FlowControl.getNodes().get(i);
				if (node.containsPoint(cx, cy)) {
					if (node instanceof FlowRecipeNode && ((FlowRecipeNode) node).isOnHideButton(cx, cy)) {
						if (mouseButton == 0) {
							((FlowRecipeNode) node).getStep().hide();
						}
						return;
					}
					node.startDragging(cx, cy);
					draggingNode = node;
					return;
				}
			}
			
			// Panning
			isPanning = true;
			panStartX = mouseX;
			panStartY = mouseY;
			return;
		}

		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		double cx = FlowControl.toCanvasX(mouseX);
		double cy = FlowControl.toCanvasY(mouseY);

		if (draggingNode != null) {
			draggingNode.move(cx, cy);
		} else if (isPanning) {
			int dx = mouseX - panStartX;
			int dy = mouseY - panStartY;
			FlowControl.pan(dx, dy);
			panStartX = mouseX;
			panStartY = mouseY;
		}
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (draggingNode != null) {
			draggingNode.stopDragging();
			draggingNode = null;
		}
		if (isPanning) {
			isPanning = false;
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		int scroll = Mouse.getEventDWheel();
		if (scroll != 0) {
			int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

			FlowControl.zoom(scroll, mouseX, mouseY);
		}
		super.handleMouseInput();
	}

	// Helper functions
	protected void onTabClicked(int index) {
		switch (index) {
			case 0:
				mc.displayGuiScreen(new GuiProductionCalc(this.playerInv));
				break;
			case 1:
				break;
			case 2:
				mc.displayGuiScreen(new GuiManageRecipes(this.playerInv));
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

	@Override
	public void renderHoveredToolTip(int mouseX, int mouseY) {
		if (isOnShowAll(mouseX, mouseY)) {
			int hidden = CalculatorState.getHiddenCount(GuiProductionCalc.TableType.PROD);
			drawHoveringText(hidden > 0 ? I18n.format("necalc.gui.show_recipe.some", hidden)
					: I18n.format("necalc.gui.show_recipe.none"), mouseX, mouseY);
			return;
		}
		if (!isInChart(mouseX, mouseY)) {
			super.renderHoveredToolTip(mouseX, mouseY);
			return;
		}
		for (int i = FlowControl.getNodes().size() - 1; i >= 0; i--) {
			if (FlowControl.getNodes().get(i).containsPoint(FlowControl.toCanvasX(mouseX), FlowControl.toCanvasY(mouseY))) {
				FlowControl.getNodes().get(i).drawHoveredToolTip(this, mouseX, mouseY);
				return;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public Ingredients getHoveredStack(int mouseX, int mouseY) {
		if (!isInChart(mouseX, mouseY)) return Ingredients.EMPTY;
		for (int i = FlowControl.getNodes().size() - 1; i >= 0; i--) {
			if (FlowControl.getNodes().get(i).containsPoint(FlowControl.toCanvasX(mouseX), FlowControl.toCanvasY(mouseY))) {
				return FlowControl.getNodes().get(i).hoveredStack(mouseX, mouseY);
			}
		}
		return Ingredients.EMPTY;
	}

	private boolean isInChart(int x, int y) {
		return x >= guiLeft + 8 && x < guiLeft + 542 && y >= guiTop + TAB_H + 17 && y < guiTop + TAB_H + 291;
	}

	private boolean isOnShowAll(int x, int y) {
		return x >= guiLeft + GUI_WIDTH - 22 && x < guiLeft + GUI_WIDTH - 10
				&& y >= guiTop + TAB_H + 3 && y < guiTop + TAB_H + 15;
	}

	public void drawRecipeHideButton(int x, int y) {
		drawEyeButton(x, y, (int) FlowControl.toCanvasX(drawMouseX), (int) FlowControl.toCanvasY(drawMouseY), true);
	}

	private void drawEyeButton(int x, int y, int mouseX, int mouseY, boolean active) {
		drawButton(x, y, 12, 12, "", mouseX, mouseY, false, active);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(BUTTON_TEXTURE);
		drawModalRectWithCustomSizedTexture(x + 1, y + 1, active ? 10 : 20, 0, 10, 10, 30, 10);
	}
}
