package com.origami10004.necalc;

import com.origami10004.necalc.items.prodCalcItem;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class ItemRegistry {
    public static Item PRODUCTION_CALCULATOR;

    public static void init() {
        PRODUCTION_CALCULATOR = new prodCalcItem();
    }

    @SubscribeEvent
    public void registerItems(net.minecraftforge.event.RegistryEvent.Register<Item> event) {
        event.getRegistry().register(PRODUCTION_CALCULATOR);
    }

    @SubscribeEvent
    public void registerRecipes(net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent event) {
        // Register crafting recipes here if needed
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(net.minecraftforge.client.event.ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(PRODUCTION_CALCULATOR, 0, new ModelResourceLocation(PRODUCTION_CALCULATOR.getRegistryName(), "inventory"));
    }
}
