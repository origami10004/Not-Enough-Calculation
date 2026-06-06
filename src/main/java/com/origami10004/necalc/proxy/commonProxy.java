package com.origami10004.necalc.proxy;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import com.origami10004.necalc.ItemRegistry;

public class commonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        // Register items
        ItemRegistry.init();
        MinecraftForge.EVENT_BUS.register(new ItemRegistry());
    }

    public void init(FMLInitializationEvent event) {
    }

    public void postInit(FMLPostInitializationEvent event) {
        
    }

}
