package com.origami10004.necalc.gui.flowchart;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FlowEdge {
	private final FlowNode source;
	private final int sourceIndex;
	private final FlowNode destination;
	private final int destinationIndex;

	public FlowEdge(FlowNode source, int sourceIndex, FlowNode destination, int destinationIndex) {
		this.source = source;
		this.sourceIndex = sourceIndex;
		this.destination = destination;
		this.destinationIndex = destinationIndex;
	}

	public void draw() {
		int x0 = FlowControl.toScreenX(startX());
		int y0 = FlowControl.toScreenY(startY());
		int x1 = FlowControl.toScreenX(endX());
		int y1 = FlowControl.toScreenY(endY());

		int color = 0xFFFFFFFF;
		// straight line, change to horizontal and vertical instead of diagonal
		drawline(x0, y0, x1, y1, color, 1.0f);
	}

	// helper
	private double startX() {
		return source.getOutputX();
	}
	private double startY() {
		return source.getOutputY(sourceIndex);
	}
	private double endX() {
		return destination.getInputX();
	}
	private double endY() {
		return destination.getInputY(destinationIndex);
	}
	private void drawline(int x0, int y0, int x1, int y1, int color, float width) {
		// Implementation for drawing a line on the screen
		float r = ((color >> 16) & 0xFF) / 255.0f;
		float g = ((color >> 8) & 0xFF) / 255.0f;
		float b = (color & 0xFF) / 255.0f;
		float a = ((color >> 24) & 0xFF) / 255.0f;
		if (a == 0) a = 1.0f;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.glLineWidth(width);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();
		buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		buf.pos(x0, y0, 0).color(r, g, b, a).endVertex();
		buf.pos(x1, y1, 0).color(r, g, b, a).endVertex();
		tess.draw();

		GlStateManager.glLineWidth(1.0f);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

}
