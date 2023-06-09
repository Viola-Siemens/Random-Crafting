package com.hexagram2021.randomcrafting.mixin;

import com.hexagram2021.randomcrafting.util.IMutableItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IMutableItemStack {
	@Mutable @Shadow @Final
	private Item item;

	@Override
	public void setItemAndCount(Item item, int count) {
		this.item = item;
		((ItemStack)(Object) this).setCount(count);
	}
}
