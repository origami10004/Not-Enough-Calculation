package com.origami10004.necalc.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

import com.origami10004.necalc.client.KeyBindings;
import com.origami10004.necalc.client.KeyInputHandler;
import com.origami10004.necalc.data.CalculatorState;
import com.origami10004.necalc.data.RecipeState;
import com.origami10004.necalc.gui.GuiCommon;
import com.origami10004.necalc.data.MachineState;
import com.origami10004.necalc.compat.mekanism.MekanismCompat;
import com.origami10004.necalc.compat.thaumcraft.ThaumcraftCompat;

public class ClientProxy extends CommonProxy {

	public static GuiCommon lastOpenedGui = null;

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
		MachineState.init();
		MekanismCompat.init();
		ThaumcraftCompat.init();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);

		CalculatorState.loadTargets();
		RecipeState.loadRecipes();
		MachineState.loadMachines();
	}
}
