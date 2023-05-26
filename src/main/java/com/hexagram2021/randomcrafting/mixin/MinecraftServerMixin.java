package com.hexagram2021.randomcrafting.mixin;

import com.hexagram2021.randomcrafting.config.RCServerConfig;
import com.hexagram2021.randomcrafting.util.IMessUpRecipes;
import com.hexagram2021.randomcrafting.util.RCLogger;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	private int tickCount;

	@Shadow
	private ProfilerFiller profiler;

	@Shadow @Final
	private RandomSource random;

	@Shadow
	public abstract RecipeManager getRecipeManager();

	@Shadow
	public abstract PlayerList getPlayerList();

	private int lastAutoRefreshRecipeTick = 0;

	@Inject(method = "tickServer", at = @At(value = "TAIL"))
	public void tryReshuffling(BooleanSupplier hasTime, CallbackInfo ci) {
		long second = RCServerConfig.AUTO_REFRESH_SECOND.get();
		if(second > 0 && this.tickCount - this.lastAutoRefreshRecipeTick >= second * 20) {
			this.lastAutoRefreshRecipeTick = this.tickCount;
			RCLogger.debug("Auto refresh recipes!");
			this.profiler.push("randomcrafting:refresh_recipes");
			IMessUpRecipes recipeManager = (IMessUpRecipes) this.getRecipeManager();
			recipeManager.messup(this.random);
			if(RCServerConfig.AUTO_REFRESH_CALLBACK.get()) {
				this.getPlayerList().broadcastSystemMessage(
						Component.translatable("commands.randomcrafting.reshuffle.success"), ChatType.SYSTEM
				);
			}
			this.profiler.pop();
		}
	}
}
