package com.gugustus;

import com.gugustus.client.ClientProxy;
import com.gugustus.gui.hud.ArrayListHUD;
import com.gugustus.gui.hud.TargetHUD;
import com.gugustus.gui.hud.Watermark;
import com.gugustus.module.ModuleManager;
import com.gugustus.config.ConfigManager;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Gugustus.MODID, name = Gugustus.NAME, version = Gugustus.VERSION)
public class Gugustus {

    public static final String MODID = "gugustus";
    public static final String NAME = "Gugustus Reborn";
    public static final String VERSION = "0.1.0";

    @Mod.Instance(MODID)
    public static Gugustus instance;

    @SidedProxy(clientSide = "com.gugustus.client.ClientProxy")
    public static ClientProxy proxy;

    public static ModuleManager moduleManager;
    public static ConfigManager configManager;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();
        configManager.loadDefaultConfig();

        MinecraftForge.EVENT_BUS.register(new ArrayListHUD());
        MinecraftForge.EVENT_BUS.register(new Watermark());
        MinecraftForge.EVENT_BUS.register(new TargetHUD());
        MinecraftForge.EVENT_BUS.register(instance);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof net.minecraft.client.gui.GuiMainMenu
                && !(event.gui instanceof com.gugustus.gui.GuiMainMenu)) {
            event.gui = new com.gugustus.gui.GuiMainMenu();
        }
    }
}
