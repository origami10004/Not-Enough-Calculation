package com.origami10004.necalc.data;

import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.origami10004.necalc.Necalc;

public class MachinePersistence {
	private static final File SAVE_FILE = new File(Loader.instance().getConfigDir(), "necalc/machines.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class MachineData {
		public String item;
		public int meta;
		public int speed;

		public MachineData(MachineKey item, int speed) {
			this.item = item.registryName;
			this.meta = item.meta;
			this.speed = speed;
		}
	}
	private static class Wrapper {
		public List<MachineData> machines;
	}
	public static void saveMachineData(LinkedHashMap<MachineKey, Integer> machineSpeed) {
		SAVE_FILE.getParentFile().mkdirs();
		Wrapper wrapper = new Wrapper();
		wrapper.machines = new ArrayList<>();
		for (MachineKey key : machineSpeed.keySet()) {
			int speed = machineSpeed.get(key);
			if (speed > 1) {
				wrapper.machines.add(new MachineData(key, speed));
			}
		}
		try (FileWriter writer = new FileWriter(SAVE_FILE)) {
			GSON.toJson(wrapper, writer);
		} catch (IOException e) {
			Necalc.logger.error("Failed to save machine data", e);
		}
	}

	public static void loadMachineData() {
		if (!SAVE_FILE.exists()) {
			return;
		}
		try (FileReader reader = new FileReader(SAVE_FILE)) {
			Wrapper wrapper = GSON.fromJson(reader, Wrapper.class);
			if (wrapper != null && wrapper.machines != null) {
				for (MachineData data : wrapper.machines) {
					MachineKey key = new MachineKey(data.item, data.meta);
					MachineState.initMachineSpeed(key, data.speed);
				}
			}
		} catch (IOException e) {
			Necalc.logger.error("Failed to load machine data", e);
		}
	}
}
