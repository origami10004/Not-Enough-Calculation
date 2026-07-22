package com.origami10004.necalc.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.gui.flowchart.FlowControl;
import com.origami10004.necalc.gui.flowchart.FlowNode;
import com.origami10004.necalc.proxy.ClientProxy;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiFlowChart extends GuiCommon {
	private static final ResourceLocation BG_TEXTURE = new ResourceLocation("necalc", "textures/gui/flowchart.png");

	private static final int GUI_WIDTH = 552;
	private static final int GUI_HEIGHT = 300;

	@Override
	protected int getActiveTab() {
		return 1;
	}

	private InventoryPlayer playerInv;
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
		this.drawDefaultBackground();
		mc.getTextureManager().bindTexture(BG_TEXTURE);
		drawModalRectWithCustomSizedTexture(guiLeft, guiTop + TAB_H, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);
		drawTabStrip(guiLeft, guiTop);

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
		GlStateManager.translate(-FlowControl.getPanX(), -FlowControl.getPanY(), 0);
		GlStateManager.scale((float) FlowControl.getZoom(), (float) FlowControl.getZoom(), 1.0f);
		
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

		if (mouseX >= guiLeft + 8 && mouseX < guiLeft + 542 && mouseY >= guiTop + 17 + 28 && mouseY < guiTop + 291 + 28) {
			double cx = FlowControl.toCanvasX(mouseX);
			double cy = FlowControl.toCanvasY(mouseY);

			// dragging node
			for (int i = 0; i < FlowControl.getNodes().size(); i++) {
				FlowNode node = FlowControl.getNodes().get(i);
				if (node.containsPoint(cx, cy)) {
					Necalc.logger.info("Clicked on node at canvas coordinates: (" + cx + ", " + cy + ")");
					node.startDragging(cx, cy);
					draggingNode = node;
					return;
				}
			}
			
			// Panning
			Necalc.logger.info("Panning started at screen coordinates: (" + mouseX + ", " + mouseY + ")");
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
}
