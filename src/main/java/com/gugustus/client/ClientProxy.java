package com.gugustus.client;

import com.gugustus.Gugustus;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class ClientProxy {

    private KeyInputHandler keyInputHandler;
    private CommandHandler commandHandler;

    public void preInit(FMLPreInitializationEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        keyInputHandler = new KeyInputHandler();
        MinecraftForge.EVENT_BUS.register(keyInputHandler);
        commandHandler = new CommandHandler();
        commandHandler.registerCommands();
    }
}
