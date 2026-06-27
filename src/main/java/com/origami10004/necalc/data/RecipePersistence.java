package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraft.item.Item;

import com.origami10004.necalc.Necalc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class RecipePersistence {
	private static final File SAVE_FILE = new File(Loader.instance().getConfigDir(), "necalc/recipes.json");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static class MachineData {
		public String item;
		public int meta;
		public JsonElement nbt;
		public MachineData(ItemStack stack) {
			this.item = stack.getItem().getRegistryName().toString();
			this.meta = stack.getMetadata();
			if (stack.getTagCompound() != null) {
				this.nbt = new JsonParser().parse(stack.getTagCompound().toString());
			} else {
				this.nbt = null;
			}
		}
	}
	private static class ItemData extends MachineData{
		public int count;
		public ItemData(ItemStack stack) {
			super(stack);
			this.count = stack.getCount();
		}
	}
	private static class Recipe {
		public MachineData machine;
		public ArrayList<ItemData> inputs;
		public ArrayList<ItemData> outputs;
		public int time;
	}
	private static class Wrapper {
		public ArrayList<Recipe> recipes;
	}

	public static void saveRecipeData(ArrayList<RecipeEntry> recipes) {
		SAVE_FILE.getParentFile().mkdirs();
		Wrapper wrapper = new Wrapper();
		wrapper.recipes = new ArrayList<>();
		for (RecipeEntry entry : recipes) {
			Recipe recipe = new Recipe();
			recipe.machine = new MachineData(entry.getMachine());
			recipe.inputs = new ArrayList<>();
			recipe.outputs = new ArrayList<>();
			recipe.time = entry.getTime();
			for (ItemStack input : entry.getInputs()) {
				recipe.inputs.add(new ItemData(input));
			}
			for (ItemStack output : entry.getOutputs()) {
				recipe.outputs.add(new ItemData(output));
			}
			wrapper.recipes.add(recipe);
		}
		try (FileWriter writer = new FileWriter(SAVE_FILE)) {
			GSON.toJson(wrapper, writer);
		} catch (IOException e) {
			Necalc.logger.error("Failed to save recipe data", e);
		}
	}

	public static ArrayList<RecipeEntry> loadRecipeData() {
		ArrayList<RecipeEntry> recipes = new ArrayList<>();
		if (!SAVE_FILE.exists()) return recipes;

		try (FileReader reader = new FileReader(SAVE_FILE)) {
			Wrapper wrapper = GSON.fromJson(reader, Wrapper.class);
			if (wrapper == null || wrapper.recipes == null) return recipes;
			for (Recipe recipe : wrapper.recipes) {
				ArrayList<ItemStack> inputs = new ArrayList<>();
				for (ItemData inputData : recipe.inputs) {
					ResourceLocation itemLoc = new ResourceLocation(inputData.item);
					Item item = Item.REGISTRY.getObject(itemLoc);
					if (item == null) {
						Necalc.logger.warn("Unknown item in recipe data: {}", inputData.item);
						continue;
					}
					ItemStack stack = new ItemStack(item, inputData.count, inputData.meta);
					if (inputData.nbt != null && inputData.nbt.isJsonObject()) {
						try {
							NBTTagCompound nbt = JsonToNBT.getTagFromJson(inputData.nbt.toString());
							stack.setTagCompound(nbt);
						} catch (Exception e) {
							Necalc.logger.warn("Failed to parse NBT for item {}: {}", inputData.item, e.getMessage());
						}
					}
					inputs.add(stack);
				}
				ResourceLocation machineLoc = new ResourceLocation(recipe.machine.item);
				Item machineItem = Item.REGISTRY.getObject(machineLoc);
				if (machineItem == null) {
					Necalc.logger.warn("Unknown machine in recipe data: {}", recipe.machine.item);
					continue;
				}
				ItemStack machineStack = new ItemStack(machineItem, 1, recipe.machine.meta);
				if (recipe.machine.nbt != null && recipe.machine.nbt.isJsonObject()) {
					try {
						NBTTagCompound nbt = JsonToNBT.getTagFromJson(recipe.machine.nbt.toString());
						machineStack.setTagCompound(nbt);
					} catch (Exception e) {
						Necalc.logger.warn("Failed to parse NBT for machine {}: {}", recipe.machine.item, e.getMessage());
					}
				}
				ArrayList<ItemStack> outputs = new ArrayList<>();
				for (ItemData outputData : recipe.outputs) {
					ResourceLocation itemLoc = new ResourceLocation(outputData.item);
					Item item = Item.REGISTRY.getObject(itemLoc);
					if (item == null) {
						Necalc.logger.warn("Unknown item in recipe data: {}", outputData.item);
						continue;
					}
					ItemStack stack = new ItemStack(item, outputData.count, outputData.meta);
					if (outputData.nbt != null && outputData.nbt.isJsonObject()) {
						try {
							NBTTagCompound nbt = JsonToNBT.getTagFromJson(outputData.nbt.toString());
							stack.setTagCompound(nbt);
						} catch (Exception e) {
							Necalc.logger.warn("Failed to parse NBT for item {}: {}", outputData.item, e.getMessage());
						}
					}
					outputs.add(stack);
				}
				recipes.add(new RecipeEntry(inputs, machineStack, outputs, recipe.time));
			}
		} catch (IOException e) {
			Necalc.logger.error("Failed to load recipe data", e);
		}

		return recipes;
	}
}
