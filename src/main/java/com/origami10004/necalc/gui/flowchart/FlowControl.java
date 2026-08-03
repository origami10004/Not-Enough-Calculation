package com.origami10004.necalc.gui.flowchart;

import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.ProductionStep;
import com.origami10004.necalc.gui.GuiFlowChart;
import com.origami10004.necalc.data.ingredient.Ingredients;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Comparator;

public class FlowControl {
	private static final int COL_PAD = 80;
	private static final int ROW_PAD = 30;
	private static int panX = -50;
	private static int panY = -50;
	private static double zoom = 1.0;
	private static List<FlowNode> nodes = new ArrayList<>();
	private static List<FlowEdge> edges = new ArrayList<>();
	private static boolean cached = false;

	public static int toScreenX(double canvasX) {
		return (int) ((canvasX - panX) * zoom);
	}
	public static int toScreenY(double canvasY) {
		return (int) ((canvasY - panY) * zoom);
	}
	public static double toCanvasX(int screenX) {
		return (screenX / zoom) + panX;
	}
	public static double toCanvasY(int screenY) {
		return (screenY / zoom) + panY;
	}

	public static void reset() {
		nodes.clear();
		edges.clear();
		cached = false;
	}

	public static void init(GuiFlowChart gui) {
		if (cached) return;

		List<ProductionStep> steps = CalculatorState.getAllRecipes();
		if (steps.isEmpty()) {
			cached = true;
			return;
		}
		Map<ProductionStep, FlowRecipeNode> stepToNode = new HashMap<>();
		List<FlowRecipeNode> recipeNodes = new ArrayList<>();
		List<FlowItemNode> itemNodes = new ArrayList<>();

		// Recipe nodes
		for (ProductionStep step : steps) {
			FlowRecipeNode node = new FlowRecipeNode(step, 0, 0);
			recipeNodes.add(node);
			stepToNode.put(step, node);
		}

		// Build consumer and producer maps
		Map<Ingredients, List<int[]>> consumerMap = new HashMap<>();
		Map<Ingredients, List<int[]>> producerMap = new HashMap<>();
		Map<Ingredients, List<Integer>> netConsumerMap = new HashMap<>();
		Map<Ingredients, List<Integer>> netProducerMap = new HashMap<>();
		Set<Ingredients> allItems = new HashSet<>();

		for (int i = 0; i < steps.size(); i++) {
			ProductionStep step = steps.get(i);
			List<Ingredients> inputs = step.getInputs();
			List<Ingredients> outputs = step.getOutputs();
			Map<Ingredients, Double> netOutputs = new HashMap<>();

			for (int j = 0; j < inputs.size(); j++) {
				consumerMap.computeIfAbsent(inputs.get(j), k -> new ArrayList<>()).add(new int[]{i, j});
				allItems.add(inputs.get(j));
				netOutputs.put(inputs.get(j), netOutputs.getOrDefault(inputs.get(j), 0.0) - step.getInputRate(j));
			}
			for (int j = 0; j < outputs.size(); j++) {
				producerMap.computeIfAbsent(outputs.get(j), k -> new ArrayList<>()).add(new int[]{i, j});
				allItems.add(outputs.get(j));
				netOutputs.put(outputs.get(j), netOutputs.getOrDefault(outputs.get(j), 0.0) + step.getOutputRate(j));
			}

			for (Map.Entry<Ingredients, Double> entry : netOutputs.entrySet()) {
				Ingredients item = entry.getKey();
				double netRate = entry.getValue();
				if (netRate < 0) {
					netConsumerMap.computeIfAbsent(item, k -> new ArrayList<>()).add(i);
				} else if (netRate > 0) {
					netProducerMap.computeIfAbsent(item, k -> new ArrayList<>()).add(i);
				}
			}
		}

		// Item nodes
		Map <Ingredients, FlowItemNode> itemToNode = new HashMap<>();
		for (Ingredients item : allItems) {
			Ingredients temp = item.copy();
			List<int[]> consumers = consumerMap.getOrDefault(item, new ArrayList<>());
			List<int[]> producers = producerMap.getOrDefault(item, new ArrayList<>());
			double totalConsumed = 0.0;
			for (int[] consumer : consumers) {
				ProductionStep step = steps.get(consumer[0]);
				totalConsumed += step.getInputRate(consumer[1]);
			}
			double totalProduced = 0.0;
			for (int[] producer : producers) {
				ProductionStep step = steps.get(producer[0]);
				totalProduced += step.getOutputRate(producer[1]);
			}
			temp.setValue(Math.max(totalConsumed, totalProduced));

			FlowItemNode itemNode = new FlowItemNode(temp, 0, 0);
			itemToNode.put(item, itemNode);
			itemNodes.add(itemNode);
		}

		// Create edges
		for (Ingredients item : allItems) {
			List<int[]> consumers = consumerMap.getOrDefault(item, new ArrayList<>());
			List<int[]> producers = producerMap.getOrDefault(item, new ArrayList<>());

			FlowItemNode itemNode = itemToNode.get(item);

			for (int[] consumer : consumers) {
				FlowRecipeNode consumerNode = recipeNodes.get(consumer[0]);
				edges.add(new FlowEdge(itemNode, 0, consumerNode, consumer[1]));
			}
			for (int[] producer : producers) {
				FlowRecipeNode producerNode = recipeNodes.get(producer[0]);
				edges.add(new FlowEdge(producerNode, producer[1], itemNode, 0));
			}
		}

		// Assign nodes to layers
		Map <FlowRecipeNode, Set<FlowRecipeNode>> children = new HashMap<>();
		Map <FlowRecipeNode, Set<FlowRecipeNode>> parents = new HashMap<>();
		for (FlowRecipeNode node : recipeNodes) {
			children.put(node, new HashSet<>());
			parents.put(node, new HashSet<>());
		}
		for (Ingredients item : allItems) {
			List<Integer> consumers = netConsumerMap.getOrDefault(item, new ArrayList<>());
			List<Integer> producers = netProducerMap.getOrDefault(item, new ArrayList<>());
			for (Integer consumer : consumers) {
				for (Integer producer : producers) {
					if (producer != consumer) {
						children.get(recipeNodes.get(consumer)).add(recipeNodes.get(producer));
						parents.get(recipeNodes.get(producer)).add(recipeNodes.get(consumer));
					}
				}
			}
		}
		Map<FlowRecipeNode, Integer> nodeToLayer = new HashMap<>();
		Map<Integer, List<FlowRecipeNode>> layerToNode = new HashMap<>();
		int maxLayer = 0;
		for (FlowRecipeNode node : recipeNodes) {
			int layer = assignLayer(node, children, nodeToLayer, layerToNode, new HashSet<>());
			maxLayer = Math.max(maxLayer, layer);
		}
		for (int i = maxLayer; i >= 0; i--) {
			// really shouldnt be possible but just in case
			if (!layerToNode.containsKey(i)) continue;
			for (int j = 0; j < layerToNode.get(i).size(); j++) {
				FlowRecipeNode node = layerToNode.get(i).get(j);
				
				int minParentLayer = Integer.MAX_VALUE;
				if (parents.get(node).isEmpty()) continue;
				for (FlowRecipeNode parent : parents.get(node)) {
					minParentLayer = Math.min(minParentLayer, nodeToLayer.get(parent));
				}
				if (nodeToLayer.get(node) < minParentLayer - 1) {
					nodeToLayer.put(node, minParentLayer - 1);
				}
			}
		}

		// Position nodes
		Map<Integer, List<FlowRecipeNode>> recipeColumns = new TreeMap<>();
		for (FlowRecipeNode node : recipeNodes) {
			recipeColumns.computeIfAbsent(nodeToLayer.getOrDefault(node, 0), k -> new ArrayList<>()).add(node);
		}
		int curX = 0;
		for (List<FlowRecipeNode> col : recipeColumns.values()) {
			double x = curX;
			double y = 0;
			int maxWidth = 0;
			for (FlowRecipeNode node : col) {
				node.move(x, y);
				y += node.getHeight() + ROW_PAD;
				maxWidth = Math.max(maxWidth, node.getWidth());
			}
			curX += maxWidth + (COL_PAD * 2) + 18;
		}

		for (Ingredients item : allItems) {
			FlowItemNode itemNode = itemToNode.get(item);
			List<int[]> consumers = consumerMap.getOrDefault(item, new ArrayList<>());
			List<int[]> producers = producerMap.getOrDefault(item, new ArrayList<>());
			double maxProdRight = 0;
			for (int[] producer : producers) {
				FlowRecipeNode prod = recipeNodes.get(producer[0]);
				maxProdRight = Math.max(maxProdRight, prod.canvasX + prod.getWidth());
			}

			double minConsLeft = Double.MAX_VALUE;
			for (int[] consumer : consumers) {
				FlowRecipeNode cons = recipeNodes.get(consumer[0]);
				minConsLeft = Math.min(minConsLeft, cons.canvasX);
			}
			if (minConsLeft == Double.MAX_VALUE) minConsLeft = maxProdRight + COL_PAD;
			if (maxProdRight == 0) maxProdRight = minConsLeft - COL_PAD;

			// check for loops
			boolean isLoop = false;
			for (int[] producer : producers) {
				FlowRecipeNode prod = recipeNodes.get(producer[0]);
				for (int[] consumer : consumers) {
					FlowRecipeNode cons = recipeNodes.get(consumer[0]);
					if (cons.canvasX <= prod.canvasX) {
						isLoop = true;
						break;
					}
				}
				if (isLoop) break;
			}

			double busX;
			if (isLoop) {
				double minConsX = Double.MAX_VALUE;
				for (int[] consumer : consumers){
					FlowRecipeNode cons = recipeNodes.get(consumer[0]);
					minConsX = Math.min(minConsX, cons.canvasX);
				}
				busX = minConsX - COL_PAD - 18;
			} else {
				busX = maxProdRight + (minConsLeft - maxProdRight - 18) / 2.0;
			}

			double sumY = 0;
			int count = 0;
			for (int i = 0; i < producers.size(); i++) {
				sumY += recipeNodes.get(producers.get(i)[0]).getOutputY(producers.get(i)[1]);
				count++;
			}
			for (int i = 0; i < consumers.size(); i++) {
				sumY += recipeNodes.get(consumers.get(i)[0]).getInputY(consumers.get(i)[1]);
				count++;
			}

			FlowItemNode bus = itemToNode.get(item);
			bus.canvasX = busX;
			bus.canvasY = (count > 0 ? sumY / count : 0) - 9;
		}
		Map<Integer, List<FlowItemNode>> xLanes = new HashMap<>();
		double X_SNAP_TOLERANCE = 18;

		for (Ingredients item : allItems) {
			FlowItemNode node = itemToNode.get(item);
			if (node == null) continue;

			int laneKey = (int) (Math.round(node.canvasX / X_SNAP_TOLERANCE) * X_SNAP_TOLERANCE);
			xLanes.computeIfAbsent(laneKey, k -> new ArrayList<>()).add(node);
		}

		for (List<FlowItemNode> laneNodes : xLanes.values()) {
			if (laneNodes.size() <= 1) continue;
			laneNodes.sort(Comparator.comparingDouble(n -> n.canvasY));

			for (int i = 0; i < laneNodes.size() - 1; i++) {
				FlowItemNode current = laneNodes.get(i);
				FlowItemNode next = laneNodes.get(i + 1);
				double requiredMinY = current.canvasY + 18;

				if (next.canvasY < requiredMinY) {
					next.canvasY = requiredMinY;
				}
			}
		}

		nodes.addAll(recipeNodes);
		nodes.addAll(itemNodes);


		// Recenter panning
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;
		for (FlowNode node : nodes) {
			minX = Math.min(minX, node.getCanvasX());
			minY = Math.min(minY, node.getCanvasY());
			maxX = Math.max(maxX, node.getCanvasX() + node.getWidth());
			maxY = Math.max(maxY, node.getCanvasY() + node.getHeight());
		}
		zoom = 1.0;
		panX = (int) ((minX + maxX) / 2.0 - gui.width / 2);
		panY = (int) ((minY + maxY) / 2.0 - gui.height / 2);

		cached = true;
	}

	private static int assignLayer(FlowRecipeNode node,
			Map<FlowRecipeNode, Set<FlowRecipeNode>> children,
			Map<FlowRecipeNode, Integer> nodeToLayer,
			Map<Integer, List<FlowRecipeNode>> layerToNode,
			Set<FlowRecipeNode> visiting) {
		if (nodeToLayer.containsKey(node)) return nodeToLayer.get(node);
		if (visiting.contains(node)) return 0; // Cycle detected
		visiting.add(node);
		int maxDepth = 0;
		for (FlowRecipeNode child : children.get(node)) {
			maxDepth = Math.max(maxDepth, assignLayer(child, children, nodeToLayer, layerToNode, visiting));
		}
		visiting.remove(node);
		int layer = maxDepth + 1;
		nodeToLayer.put(node, layer);
		layerToNode.computeIfAbsent(layer, k -> new ArrayList<>()).add(node);
		return layer;
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

	public static int getPanX() {
		return panX;
	}
	public static int getPanY() {
		return panY;
	}

	public static void zoom(int scroll, int mouseX, int mouseY) {
		double oldZoom = zoom;
		if (scroll > 0) {
			zoom *= 1.1;
		} else if (scroll < 0) {
			zoom /= 1.1;
		}
		zoom = Math.max(0.1, Math.min(zoom, 1.0));

		panX += (mouseX / oldZoom) - (mouseX / zoom);
		panY += (mouseY / oldZoom) - (mouseY / zoom);
	}
}
