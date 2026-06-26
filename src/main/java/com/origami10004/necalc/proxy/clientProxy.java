package com.origami10004.necalc.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import com.origami10004.necalc.client.KeyBindings;
import com.origami10004.necalc.client.KeyInputHandler;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.RecipeState;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        KeyBindings.registerKeyBindings();
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        CalculatorState.init();
        RecipeState.init();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        CalculatorState.loadTargets();
        RecipeState.loadRecipes();
    }
}
