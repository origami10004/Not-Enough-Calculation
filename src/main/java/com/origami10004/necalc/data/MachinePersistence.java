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
import com.origami10004.necalc.data.ingredient.IngredientManager;
import com.origami10004.necalc.data.ingredient.Ingredients;

public class MachinePersistence {
	private static final File SAVE_FILE = new File(Loader.instance().getConfigDir(), "necalc/machines.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class Wrapper {
		public List<String> machines;
	}
	public static void saveMachineData(LinkedHashMap<Ingredients, Integer> machineSpeed) {
		SAVE_FILE.getParentFile().mkdirs();
		Wrapper wrapper = new Wrapper();
		wrapper.machines = new ArrayList<>();
		for (Ingredients key : machineSpeed.keySet()) {
			int speed = machineSpeed.get(key);
			if (speed > 1) {
				Ingredients temp = key.copy();
				temp.setValue(speed);
				wrapper.machines.add(temp.serialize());
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
				for (String serializedData : wrapper.machines) {
					Ingredients data = IngredientManager.deserialize(serializedData);
					if (data.isEmpty()) continue;
					MachineState.initMachineSpeed(data, (int) data.getValue());
				}
			}
		} catch (IOException e) {
			Necalc.logger.error("Failed to load machine data", e);
		}
	}
}
