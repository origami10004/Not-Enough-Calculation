package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.gui.GuiFlowChart;

public class FlowNode {
	protected double canvasX;
	protected double canvasY;
	private int width = 0;
	private int height = 0;
	private double dragOffsetX = 0;
	private double dragOffsetY = 0;
	private boolean isDragging = false;

	public FlowNode(double canvasX, double canvasY) {
		this.canvasX = canvasX;
		this.canvasY = canvasY;
	}

	protected void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void move(double x, double y) {
		this.canvasX = (x - dragOffsetX);
		this.canvasY = (y - dragOffsetY);
	}

	public double getCanvasX() {
		return canvasX;
	}

	public double getCanvasY() {
		return canvasY;
	}

	public double getInputX() {
		return canvasX;
	}

	public double getInputY(int index) {
		return canvasY + height / 2.0;
	}

	public double getOutputX() {
		return canvasX + width;
	}

	public double getOutputY(int index) {
		return canvasY + height / 2.0;
	}

	public boolean containsPoint(double x, double y) {
		return x >= canvasX && x <= canvasX + width && y >= canvasY && y <= canvasY + height;
	}

	public void draw(GuiFlowChart gui) {
		// Draw the node as a rectangle
		gui.drawRect((int) canvasX, (int) canvasY, (int) (canvasX + width), (int) (canvasY + height), 0xFFAAAAAA);
	}

	public void startDragging(double mouseX, double mouseY) {
		isDragging = true;
		dragOffsetX = mouseX - canvasX;
		dragOffsetY = mouseY - canvasY;
	}

	public void stopDragging() {
		isDragging = false;
	}

	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
}
