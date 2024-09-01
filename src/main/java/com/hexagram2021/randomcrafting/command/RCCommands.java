package com.hexagram2021.randomcrafting.command;

import com.hexagram2021.randomcrafting.config.RCCommonConfig;
import com.hexagram2021.randomcrafting.config.RCServerConfig;
import com.hexagram2021.randomcrafting.util.IMessUpRecipes;
import com.hexagram2021.randomcrafting.util.RCLogger;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public class RCCommands {
	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("rc").then(
				Commands.literal("shuffle").requires(stack -> canUseCommand(stack))
						.executes(context -> shuffle(context.getSource().getServer()))).then(
				Commands.literal("reshuffle").requires(stack -> canUseCommand(stack))
						.executes(context -> reshuffle(context.getSource().getServer(), context.getSource().getPlayerOrException()))
						.then(
								Commands.argument("salt", LongArgumentType.longArg())
										.executes(context -> reshuffle(context.getSource().getServer(), LongArgumentType.getLong(context, "salt")))
						)
		).then(
				Commands.literal("revoke").requires(stack -> canUseCommand(stack))
						.executes(context -> revoke(context.getSource().getServer()))
		);
	}

	private static boolean canUseCommand(CommandSourceStack stack) {
		return RCServerConfig.ALLOW_RESHUFFLE_WITHOUT_CHEATS.get() ||
				stack.hasPermission(RCServerConfig.PERMISSION_LEVEL_RESHUFFLE.get());
	}

	private static int reshuffle(MinecraftServer server, ServerPlayer entity) {
		return reshuffle(server, entity.getRandom().nextLong());
	}

	private static int reshuffle(MinecraftServer server, long seed) {
		RCServerConfig.SALT.set(seed);
		if(RCServerConfig.DISABLE.get()) {
			RCServerConfig.DISABLE.set(false);
		}
		messup(server);
		server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomcrafting.reshuffle.success"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int revoke(MinecraftServer server) {
		RCServerConfig.DISABLE.set(true);
		((IMessUpRecipes) server.getRecipeManager()).revoke(server.registryAccess());
		sendRecipeUpdatePacket(server);
		server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomcrafting.revoke.success"), false);
		return Command.SINGLE_SUCCESS;
	}

	private static int shuffle(MinecraftServer server) {
		long salt = RCServerConfig.SALT.get();
		if(RCServerConfig.DISABLE.get()) {
			RCServerConfig.DISABLE.set(false);
		}
		autoShuffling(server, salt);
		server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomcrafting.reshuffle.success"), false);
		return Command.SINGLE_SUCCESS;
	}

	public static void messup(MinecraftServer server) {
		if (!RCServerConfig.DISABLE.get()) {
			long seed = server.getWorldData().worldGenOptions().seed() ^ RCServerConfig.SALT.get();
			RandomSource random = RandomSource.create(seed);
			try {
				((IMessUpRecipes) server.getRecipeManager()).messup(random, server.registryAccess());
				sendRecipeUpdatePacket(server);
				RCLogger.info("Recipes shuffled successfully with seed: " + seed);
			} catch (Exception e) {
				RCLogger.error("Failed to shuffle recipes: " + e.getMessage());
				e.printStackTrace();
				server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomcrafting.reshuffle.error"), false);
			}
		} else {
			RCLogger.warn("Recipe shuffling is disabled. Skipping messup operation.");
		}
	}

	public static void autoShuffling(MinecraftServer server, long salt) {
		if (!RCServerConfig.DISABLE.get()) {
			long seed = server.getWorldData().worldGenOptions().seed() ^ salt;
			RandomSource random = RandomSource.create(seed);
			try {
				((IMessUpRecipes) server.getRecipeManager()).messup(random, server.registryAccess());
				sendRecipeUpdatePacket(server);
				RCLogger.info("Recipes shuffled successfully with last used salt: " + salt);
			} catch (Exception e) {
				RCLogger.error("Failed to shuffle recipes: " + e.getMessage());
				e.printStackTrace();
				server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomcrafting.reshuffle.error"), false);
			}
		} else {
			RCLogger.warn("Recipe shuffling is disabled. Skipping messup operation.");
		}
	}

	private static void sendRecipeUpdatePacket(MinecraftServer server) {
		ClientboundUpdateRecipesPacket clientboundupdaterecipespacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes());

		for(ServerPlayer serverplayer : server.getPlayerList().getPlayers()) {
			serverplayer.connection.send(clientboundupdaterecipespacket);
			serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
		}
	}
}
