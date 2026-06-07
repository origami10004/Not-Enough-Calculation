package com.origami10004.necalc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.origami10004.necalc.items.ProdCalcItem;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import org.jetbrains.annotations.NotNull;

@ObjectHolder(necalc.MODID)
@EventBusSubscriber(modid = necalc.MODID)
public final class ItemRegistry {

	// This is a reference to your prod calc item instance
	public static final Item PROD_CALC = getNull();

	private static ImmutableList<Item> allSimpleItems;

	public static ImmutableList<Item> getAllSimpleItems() {
		return allSimpleItems;
	}

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		final Builder<Item> simpleItems = ImmutableList.builder();
		simpleItems.add(register(registry, "prod_calc", new ProdCalcItem(), CreativeTabs.TOOLS));
		allSimpleItems = simpleItems.build();
	}

	private static <T extends Item> T register(final IForgeRegistry<Item> r, final String name, final T item, final CreativeTabs ct) {
		item.setRegistryName(necalc.MODID, name);
		item.setTranslationKey(necalc.MODID + "." + name.replace('/', '.'));
		item.setCreativeTab(ct);
		r.register(item);
		return item;
	}

	/**
	 * Helper to spoof nullability warnings for 1.12s poorly thought out `ObjectHolder` solution for
	 * having references to registered game objects
	 */
	@NotNull
	@SuppressWarnings("ConstantConditions")
	private static <T> T getNull() {
		return null;
	}
}