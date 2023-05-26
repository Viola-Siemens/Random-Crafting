package com.hexagram2021.randomcrafting;

import com.hexagram2021.randomcrafting.command.RCCommands;
import com.hexagram2021.randomcrafting.config.RCCommonConfig;
import com.hexagram2021.randomcrafting.config.RCServerConfig;
import com.hexagram2021.randomcrafting.util.RCLogger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;

@Mod(RandomCrafting.MODID)
public class RandomCrafting {
    public static final String MODID = "randomcrafting";

    public RandomCrafting() {
        RCLogger.logger = LogManager.getLogger(MODID);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, RCCommonConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, RCServerConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(RCCommands::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onServerStarted(ServerStartedEvent event) {
        if(!RCServerConfig.DISABLE.get()) {
            RCCommands.messup(event.getServer());
        }
    }
}
