package com.origami10004.necalc;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import com.origami10004.necalc.proxy.commonProxy;

@Mod(
    modid = necalc.MODID,
    name = necalc.NAME,
    version = necalc.VERSION,
    //dependencies = "required-after:Forge@[14.23.5.2859,)",
    useMetadata = true)
public class necalc {
    public static final String MODID = "necalc";
    public static final String NAME = "Not Enough Calculation";
    public static final String VERSION = "1.0";
    
    @SidedProxy(clientSide = "com.origami10004.necalc.proxy.clientProxy", serverSide = "com.origami10004.necalc.proxy.serverProxy")
    public static commonProxy proxy;

    @Mod.Instance
    public static necalc instance;
    
    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }
}
