package com.origami10004.necalc.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import com.origami10004.necalc.client.KeyBindings;
import com.origami10004.necalc.client.KeyInputHandler;
import com.origami10004.necalc.config.ConfigHandler;
import com.origami10004.necalc.gui.CalculatorState;
import com.origami10004.necalc.gui.RecipeState;

public class ClientProxy extends CommonProxy {
    public static CalculatorState calcState;
    public static RecipeState recipeState;
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        KeyBindings.registerKeyBindings();
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        calcState = new CalculatorState();
        recipeState = new RecipeState();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

        calcState.loadTargets();
        recipeState.loadRecipes();
    }
}
