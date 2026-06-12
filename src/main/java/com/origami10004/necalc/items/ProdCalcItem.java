package com.origami10004.necalc.items;

import com.origami10004.necalc.gui.GuiProductionCalc;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class ProdCalcItem extends Item {

	public ProdCalcItem() {
		setMaxStackSize(1);
		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if (worldIn.isRemote) {
			// open GUI
			Minecraft.getMinecraft().displayGuiScreen(new GuiProductionCalc(playerIn.inventory));
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
}