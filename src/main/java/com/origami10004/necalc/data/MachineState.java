package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.HashMap;

public class MachineState {
	private static LinkedHashMap<ItemKey, Integer> machineSpeed;
	private static HashMap<ItemKey, Integer> machineCount;
	public static void init() {
		machineSpeed = new LinkedHashMap<>();
		machineCount = new HashMap<>();
	}

	public static LinkedHashMap<ItemKey, Integer> getMachineSpeeds() {
		return machineSpeed;
	}

	public static void addMachine(ItemStack machine) {
		ItemKey key = new ItemKey(machine);
		if (!machineSpeed.containsKey(key)) {
			machineSpeed.put(key, 1);
			machineCount.put(key, 1);
		} else {
			machineCount.put(key, machineCount.get(key) + 1);
		}
	}

	public static void removeMachine(ItemStack machine) {
		ItemKey key = new ItemKey(machine);
		if (machineSpeed.containsKey(key)) {
			if (machineCount.get(key) > 1) {
				machineCount.put(key, machineCount.get(key) - 1);
			} else if (machineSpeed.get(key) == 1){
				machineSpeed.remove(key);
				machineCount.remove(key);
			} else {
				machineCount.put(key, machineCount.get(key) - 1);
			}
		}
	}

	public static void setMachineSpeed(ItemKey key, int speed) {
		machineSpeed.put(key, speed);
	}

	public static void loadMachines() {
		// TODO: Load machine data
	}
}
