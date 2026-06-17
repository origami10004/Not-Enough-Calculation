package com.origami10004.necalc.items;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

import com.origami10004.necalc.gui.GuiProductionCalc;
import com.origami10004.necalc.proxy.ClientProxy;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
			Minecraft.getMinecraft().displayGuiScreen(new GuiProductionCalc(playerIn.inventory, ClientProxy.calcState));
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
}