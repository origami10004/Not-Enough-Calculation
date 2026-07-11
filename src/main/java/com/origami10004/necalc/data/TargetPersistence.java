package com.origami10004.necalc.data;

import net.minecraftforge.fml.common.Loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.origami10004.necalc.Necalc;
import com.origami10004.necalc.data.ingredient.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TargetPersistence {
	private static final File SAVE_FILE = new File(Loader.instance().getConfigDir(), "necalc/targets.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class Wrapper {
		public List<String> targets;
	}
	public static void saveTargetData(List<Ingredients> targetSlots) {
		SAVE_FILE.getParentFile().mkdirs();

		Wrapper wrapper = new Wrapper();
		wrapper.targets = new ArrayList<>();
		for (Ingredients target : targetSlots) {
			if (target.isEmpty()) continue;
			wrapper.targets.add(target.serialize());
		}
		try (FileWriter writer = new FileWriter(SAVE_FILE)) {
			GSON.toJson(wrapper, writer);
		} catch (Exception e) {
			Necalc.logger.error("Failed to save target data: {}", e.getMessage());
		}
	}

	public static List<Ingredients> loadTargetData() {
		List<Ingredients> targetSlots = new ArrayList<>();
		if (!SAVE_FILE.exists()) return targetSlots;

		try (FileReader reader = new FileReader(SAVE_FILE)) {
			Wrapper wrapper = GSON.fromJson(reader, Wrapper.class);
			if (wrapper == null || wrapper.targets == null) return targetSlots;
			for (String data : wrapper.targets) {
				if (data == null || data.isEmpty()) continue;
				Ingredients target = IngredientManager.deserialize(data);
				if (target.isEmpty()) continue;
				targetSlots.add(target);
			}
		} catch (Exception e) {
			Necalc.logger.error("Failed to load target data: {}", e.getMessage());
		}
		return targetSlots;
	}
}
