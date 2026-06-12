package com.origami10004.necalc.client;

import com.origami10004.necalc.*;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value = Side.CLIENT, modid = Necalc.MODID)
public final class ClientRegistry {

	@SubscribeEvent
	public static void registerModels(final ModelRegistryEvent event) {
		for (Item item : ItemRegistry.getAllSimpleItems()) registerBasicItemRenderer(item);
	}

	private static void registerBasicItemRenderer(final Item item) {
		registerItemRenderer(item, 0, "inventory");
	}

	@SuppressWarnings({"ConstantConditions", "SameParameterValue"})
	private static void registerItemRenderer(final Item item, final int meta, final String id) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id));
	}
}