package com.origami10004.necalc.data;

import java.util.LinkedHashMap;
import java.util.HashMap;

import com.origami10004.necalc.data.ingredient.Ingredients;

public class MachineState {
	// Integer represents the count of recipes using this machine
	private static LinkedHashMap<Ingredients, Integer> machineSpeed;
	private static HashMap<Ingredients, Integer> machineCount;
	public static void init() {
		machineSpeed = new LinkedHashMap<>();
		machineCount = new HashMap<>();
	}

	public static LinkedHashMap<Ingredients, Integer> getMachineSpeeds() {
		return machineSpeed;
	}

	public static void addMachine(Ingredients machine) {
		if (!machineSpeed.containsKey(machine)) {
			machineSpeed.put(machine, 1);
			machineCount.put(machine, 1);
		} else {
			machineCount.put(machine, machineCount.get(machine) + 1);
		}
	}

	public static void removeMachine(Ingredients machine) {
		if (machineSpeed.containsKey(machine)) {
			if (machineCount.get(machine) > 1) {
				machineCount.put(machine, machineCount.get(machine) - 1);
			} else if (machineSpeed.get(machine) == 1){
				machineSpeed.remove(machine);
				machineCount.remove(machine);
			} else {
				machineCount.put(machine, machineCount.get(machine) - 1);
			}
		}
	}

	public static void setMachineSpeed(Ingredients machine, int speed) {
		if (machineSpeed.get(machine) != speed) {
			machineSpeed.put(machine, speed);
			CalculatorState.recalculateRecipes();
			MachinePersistence.saveMachineData(machineSpeed);
		}
	}

	public static void initMachineSpeed(Ingredients machine, int speed) {
		machineSpeed.put(machine, speed);
	}

	public static void loadMachines() {
		MachinePersistence.loadMachineData();
	}

	public static int getMachineRows() {
		return machineSpeed.size() / 8 + 1;
	}
}
