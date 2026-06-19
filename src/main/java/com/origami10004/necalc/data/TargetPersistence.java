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
	}
	private static class ItemKey {
		public String registryName;
		public int meta;
		public NBTTagCompound nbt;
		public ItemKey(ItemStack stack) {
			this.registryName = stack.getItem().getRegistryName().toString();
			this.meta = stack.getMetadata();
			this.nbt = stack.getTagCompound();
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			ItemKey other = (ItemKey) obj;
			return Objects.equals(registryName, other.registryName) &&
					meta == other.meta &&
					Objects.equals(nbt, other.nbt);
		}
		@Override
		public int hashCode() {
			return Objects.hash(registryName, meta, nbt);
		}
	}
	private static class Wrapper {
		public List<TargetData> targets;
	}
	public static void saveTargetData(Map<Integer, CalculationTarget> targetSlots) {
		if (SAVE_FILE.getParentFile() != null) {
			SAVE_FILE.getParentFile().mkdirs();
		}
		Map <ItemKey, Double> compressedData = new HashMap<>();
		for (Integer index : targetSlots.keySet()) {
			ItemStack stack = targetSlots.get(index).getTargetItem();
			if (stack == null || stack.isEmpty()) continue;

			double rate = targetSlots.get(index).getTargetRate();
			ItemKey key = new ItemKey(stack);

			compressedData.put(key, compressedData.getOrDefault(key, 0.0) + rate);
		}

		Wrapper wrapper = new Wrapper();
		wrapper.targets = new ArrayList<>();
		for (Map.Entry<ItemKey, Double> entry : compressedData.entrySet()) {
			TargetData data = new TargetData();
			data.item = entry.getKey().registryName;
			data.meta = entry.getKey().meta;
			if (entry.getKey().nbt != null) {
				data.nbt = new JsonParser().parse(entry.getKey().nbt.toString());
			} else {
				data.nbt = null;
			}
			data.rate = entry.getValue();
			wrapper.targets.add(data);
		}
		try (FileWriter writer = new FileWriter(SAVE_FILE)) {
			GSON.toJson(wrapper, writer);
		} catch (IOException e) {
			Necalc.logger.error("Failed to save target data: {}", e.getMessage());
		}
	}

	public static Map<Integer, CalculationTarget> loadTargetData() {
		Map<Integer, CalculationTarget> targetSlots = new HashMap<>();
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
				targetSlots.put(index, new CalculationTarget(stack, data.rate));
				index++;
			}
		} catch (IOException e) {
			Necalc.logger.error("Failed to load target data: {}", e.getMessage());
		}
		return targetSlots;
	}
}
