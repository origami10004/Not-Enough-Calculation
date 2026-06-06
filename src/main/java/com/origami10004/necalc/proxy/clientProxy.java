package com.origami10004.necalc.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.origami10004.necalc.client.KeyBindings;
import com.origami10004.necalc.client.KeyInputHandler;

public class clientProxy extends commonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        KeyBindings.registerKeyBindings();

        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }
}
