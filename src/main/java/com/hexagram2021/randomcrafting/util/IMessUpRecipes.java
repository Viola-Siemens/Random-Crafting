package com.hexagram2021.randomcrafting.util;

import net.minecraft.util.RandomSource;

public interface IMessUpRecipes {
	void revoke();
	void messup(RandomSource random);
}
