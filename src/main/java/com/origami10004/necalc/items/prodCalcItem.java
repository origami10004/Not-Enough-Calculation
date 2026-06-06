package com.origami10004.necalc.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;

import com.origami10004.necalc.necalc;

@Mod.EventBusSubscriber
public class prodCalcItem extends Item {

	public prodCalcItem() {
		setCreativeTab(CreativeTabs.TOOLS);
		setMaxStackSize(1);
        setHasSubtypes(true);
        setMaxDamage(0);
        setRegistryName(necalc.MODID, "prod_calc");
		setUnlocalizedName(necalc.MODID + ".prod_calc");
    }

	 @Override
	 public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
	 	if (worldIn.isRemote) {
             // open GUI
	 		necalc.logger.info("Opening calculator GUI for player: " + playerIn.getName());
	 	}

	 	return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	 }
}
