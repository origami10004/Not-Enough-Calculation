package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.item.Item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.origami10004.necalc.Necalc;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TargetPersistence {
	private static final File SAVE_FILE = new File(Loader.instance().getConfigDir(), "necalc/targets.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static class TargetData {
		public String item;
		public int meta;
		public JsonElement nbt;
		public double rate;
		public TargetData(ItemKey item, double rate) {
			this.item = item.registryName;
			this.meta = item.meta;
			if (item.nbt != null) {
				this.nbt = new JsonParser().parse(item.nbt.toString());
			} else {
				this.nbt = null;
			}
			this.rate = rate;
		}
	}
	private static class Wrapper {
		public List<TargetData> targets;
	}
	public static void saveTargetData(List<CalculationTarget> targetSlots) {
		SAVE_FILE.getParentFile().mkdirs();

		Wrapper wrapper = new Wrapper();
		wrapper.targets = new ArrayList<>();
		for (CalculationTarget target : targetSlots) {
			if (target.getTargetItem().isEmpty()) continue;
			ItemKey key = new ItemKey(target.getTargetItem());
			wrapper.targets.add(new TargetData(key, target.getTargetRate()));
		}
		try (FileWriter writer = new FileWriter(SAVE_FILE)) {
			GSON.toJson(wrapper, writer);
		} catch (IOException e) {
			Necalc.logger.error("Failed to save target data: {}", e.getMessage());
		}
	}

	public static List<CalculationTarget> loadTargetData() {
		List<CalculationTarget> targetSlots = new ArrayList<>();
		if (!SAVE_FILE.exists()) return targetSlots;

		try (FileReader reader = new FileReader(SAVE_FILE)) {
			Wrapper wrapper = GSON.fromJson(reader, Wrapper.class);
			if (wrapper == null || wrapper.targets == null) return targetSlots;
			int index = 0;
			for (TargetData data : wrapper.targets) {
				if (data.item == null || data.item.isEmpty()) continue;
				ResourceLocation itemLoc = new ResourceLocation(data.item);
				Item item = Item.REGISTRY.getObject(itemLoc);
				if (item == null) {
					Necalc.logger.warn("Unknown item in target data: {}", data.item);
					continue;
				}
				ItemStack stack = new ItemStack(item, 1, data.meta);
				if (data.nbt != null && data.nbt.isJsonObject()) {
					try {
						NBTTagCompound nbt = JsonToNBT.getTagFromJson(data.nbt.toString());
						stack.setTagCompound(nbt);
					} catch (Exception e) {
						Necalc.logger.warn("Failed to parse NBT for item {}: {}", data.item, e.getMessage());
					}
				}
				targetSlots.add(new CalculationTarget(stack, data.rate));
				index++;
			}
		} catch (IOException e) {
			Necalc.logger.error("Failed to load target data: {}", e.getMessage());
		}
		return targetSlots;
	}
}
