package com.hexagram2021.randomcrafting.command;

import com.hexagram2021.randomcrafting.config.RCServerConfig;
import com.hexagram2021.randomcrafting.util.IMessUpRecipes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraftforge.event.RegisterCommandsEvent;

public class RCCommands {
	public static void registerCommands(RegisterCommandsEvent event) {
		final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(RCCommands.register());
	}

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("rc").then(
				Commands.literal("reshuffle").requires(stack -> stack.hasPermission(RCServerConfig.PERMISSION_LEVEL_RESHUFFLE.get()))
						.executes(context -> reshuffle(context.getSource().getServer(), context.getSource().getPlayerOrException()))
						.then(
								Commands.argument("salt", LongArgumentType.longArg())
										.executes(context -> reshuffle(context.getSource().getServer(), LongArgumentType.getLong(context, "salt")))
						)
		).then(
				Commands.literal("revoke").requires(stack -> stack.hasPermission(RCServerConfig.PERMISSION_LEVEL_REVOKE.get()))
						.executes(context -> revoke(context.getSource().getServer()))
		);
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
		((IMessUpRecipes) server.getRecipeManager()).revoke();
		sendRecipeUpdatePacket(server);
		server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.randomcrafting.revoke.success"), false);
		return Command.SINGLE_SUCCESS;
	}

	public static void messup(MinecraftServer server) {
		long seed = server.getWorldData().worldGenOptions().seed() ^ RCServerConfig.SALT.get();
		RandomSource random = RandomSource.create(seed);
		((IMessUpRecipes) server.getRecipeManager()).messup(random);
		sendRecipeUpdatePacket(server);
	}

	private static void sendRecipeUpdatePacket(MinecraftServer server) {
		ClientboundUpdateRecipesPacket clientboundupdaterecipespacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes());

		for(ServerPlayer serverplayer : server.getPlayerList().getPlayers()) {
			serverplayer.connection.send(clientboundupdaterecipespacket);
			serverplayer.getRecipeBook().sendInitialRecipeBook(serverplayer);
		}
	}
}
