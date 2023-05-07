package com.hexagram2021.randomcrafting.command;

import com.hexagram2021.randomcrafting.config.RCServerConfig;
import com.hexagram2021.randomcrafting.util.IMessUpRecipes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Random;

public class RCCommands {
	public static void registerCommands(RegisterCommandsEvent event) {
		final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(RCCommands.register());
	}

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("rc").then(
				Commands.literal("reshuffle").requires(stack -> stack.hasPermission(RCServerConfig.PERMISSION_LEVEL_RESHUFFLE.get()))
						.executes(context -> reshuffle(context.getSource().getServer(), context.getSource().getPlayerOrException()))
		).then(
				Commands.literal("revoke").requires(stack -> stack.hasPermission(RCServerConfig.PERMISSION_LEVEL_REVOKE.get()))
						.executes(context -> revoke(context.getSource().getServer()))
		);
	}

	private static int reshuffle(MinecraftServer server, ServerPlayer entity) {
		RCServerConfig.SALT.set(entity.getRandom().nextLong());
		messup(server);
		return Command.SINGLE_SUCCESS;
	}

	private static int revoke(MinecraftServer server) {
		((IMessUpRecipes) server.getRecipeManager()).revoke();
		return Command.SINGLE_SUCCESS;
	}

	public static void messup(MinecraftServer server) {
		long seed = server.getWorldData().worldGenSettings().seed() ^ RCServerConfig.SALT.get();
		Random random = new Random(seed);
		((IMessUpRecipes) server.getRecipeManager()).messup(random);
	}
}
