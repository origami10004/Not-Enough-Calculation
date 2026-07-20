package com.origami10004.necalc.gui.flowchart;

import java.util.List;

import com.origami10004.necalc.gui.GuiCommon;

import java.util.ArrayList;

public class FlowControl {
	private static int panX = 80;
	private static int panY = 80;
	private static double zoom = 1.0;
	private static List<FlowNode> nodes = new ArrayList<>();
	private static List<FlowEdge> edges = new ArrayList<>();
	private static boolean cached = false;

	public static int toScreenX(double canvasX) {
		return (int) ((canvasX - panX) * zoom) + 8;
	}
	public static int toScreenY(double canvasY) {
		return (int) ((canvasY - panY) * zoom) + 16 + 28;
	}
	public static double toCanvasX(int screenX) {
		return (screenX - 8) / zoom + panX;
	}
	public static double toCanvasY(int screenY) {
		return (screenY - 16 - 28) / zoom + panY;
	}

	public static void reset() {
		nodes.clear();
		edges.clear();
		cached = false;
	}

	public static void init() {
		if (cached) return;

		// dummy data for testing
		FlowNode node1 = new FlowNode(100, 100);
		node1.setSize(100, 50);
		nodes.add(node1);
		FlowNode node2 = new FlowNode(250, 100);
		node2.setSize(100, 50);
		nodes.add(node2);
		FlowEdge edge = new FlowEdge(node1, 0, node2, 0);
		edges.add(edge);
		cached = true;
	}

	public static List<FlowNode> getNodes() {
		return nodes;
	}

	public static List<FlowEdge> getEdges() {
		return edges;
	}

	public static double getZoom() {
		return zoom;
	}

	public static void pan(int dx, int dy) {
		panX -= dx / zoom;
		panY -= dy / zoom;
	}
}
