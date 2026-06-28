package com.origami10004.necalc.data;

import net.minecraft.item.ItemStack;
import java.util.*;

public class MachineKey {
	public String registryName;
	public int meta;
	public MachineKey(ItemStack stack) {
		this.registryName = stack.getItem().getRegistryName().toString();
		this.meta = stack.getMetadata();
	}
	public MachineKey(String registryName, int meta) {
		this.registryName = registryName;
		this.meta = meta;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		MachineKey other = (MachineKey) obj;
		return Objects.equals(registryName, other.registryName) &&
				meta == other.meta;
	}
	@Override
	public int hashCode() {
		return Objects.hash(registryName, meta);
	}
}
