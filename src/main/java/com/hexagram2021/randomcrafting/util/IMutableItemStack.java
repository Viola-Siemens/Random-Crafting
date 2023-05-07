package com.hexagram2021.randomcrafting.util;

import net.minecraft.world.item.Item;

public interface IMutableItemStack {
	void setItemAndCount(Item item, int count);
}
