package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import java.util.*;

public class ItemKey {
	public String registryName;
	public int meta;
	public NBTTagCompound nbt;
	public ItemKey(ItemStack stack) {
		this.registryName = stack.getItem().getRegistryName().toString();
		this.meta = stack.getMetadata();
		this.nbt = stack.getTagCompound();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		ItemKey other = (ItemKey) obj;
		return Objects.equals(registryName, other.registryName) &&
				meta == other.meta &&
				Objects.equals(nbt, other.nbt);
	}
	@Override
	public int hashCode() {
		return Objects.hash(registryName, meta, nbt);
	}
}
