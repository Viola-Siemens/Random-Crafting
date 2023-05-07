package com.hexagram2021.randomcrafting;

import com.hexagram2021.randomcrafting.command.RCCommands;
import com.hexagram2021.randomcrafting.config.RCServerConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("randomcrafting")
public class RandomCrafting {
    public RandomCrafting() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RCServerConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(RCCommands::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onServerStarted(ServerStartedEvent event) {
        RCCommands.messup(event.getServer());
    }
}
