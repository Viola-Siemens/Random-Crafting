package com.hexagram2021.randomcrafting.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;

public interface IMessUpRecipes {
	void revoke(RegistryAccess registryAccess);
	void messup(RandomSource random, RegistryAccess registryAccess);
}
