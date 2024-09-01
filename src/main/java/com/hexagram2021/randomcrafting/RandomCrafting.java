package com.hexagram2021.randomcrafting;

import com.hexagram2021.randomcrafting.command.RCCommands;
import com.hexagram2021.randomcrafting.config.RCCommonConfig;
import com.hexagram2021.randomcrafting.util.RCLogger;
import com.hexagram2021.randomcrafting.config.RCServerConfig;
import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraft.server.MinecraftServer;

public class RandomCrafting implements ModInitializer {
    public static final String MODID = "randomcrafting";

    public RandomCrafting() {
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, RCServerConfig.SPEC);
        ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, RCCommonConfig.SPEC);
    }

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(RCCommands.register())
        );
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
    }

    private void onServerStarting(MinecraftServer server) {
        if (!RCServerConfig.DISABLE.get() && RCServerConfig.AUTO_APPLY_ON_LOAD.get()) {
            long salt = RCServerConfig.SALT.get();
            RCLogger.info("Applying recipe shuffling with salt: " + salt);
            RCCommands.autoShuffling(server, salt);
        }
    }
}
