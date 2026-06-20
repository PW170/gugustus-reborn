package com.gugustus.client;

import com.gugustus.Gugustus;
import com.gugustus.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    public void registerCommands() {
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return ".bind";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return ".bind <module> <key>";
            }

            @Override
            public List<String> getCommandAliases() {
                return new ArrayList<>();
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }

            @Override
            public boolean canCommandSenderUseCommand(ICommandSender sender) {
                return true;
            }

            @Override
            public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                if (args.length == 1) {
                    List<String> completions = new ArrayList<>();
                    for (Module m : Gugustus.moduleManager.getModules()) {
                        completions.add(m.getName());
                    }
                    return completions;
                }
                return null;
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                if (args.length < 2) {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cUsage: .bind <module> <key>"));
                    return;
                }
                String moduleName = args[0];
                StringBuilder keyNameBuilder = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    keyNameBuilder.append(args[i]).append(" ");
                }
                String keyName = keyNameBuilder.toString().trim().toUpperCase();
                Module module = Gugustus.moduleManager.getModule(moduleName);
                if (module == null) {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cModule '" + moduleName + "' not found."));
                    return;
                }
                int keyCode = Keyboard.getKeyIndex(keyName);
                if (keyCode == Keyboard.KEY_NONE && !keyName.equals("NONE")) {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cInvalid key: " + keyName));
                    return;
                }
                module.setKeybind(keyCode);
                sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §aBound " + module.getName() + " to §e" + Keyboard.getKeyName(keyCode)));
                Gugustus.configManager.saveCurrentConfig();
            }
        });

        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return ".toggle";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return ".toggle <module>";
            }

            @Override
            public List<String> getCommandAliases() {
                return new ArrayList<>();
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }

            @Override
            public boolean canCommandSenderUseCommand(ICommandSender sender) {
                return true;
            }

            @Override
            public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
                if (args.length == 1) {
                    List<String> completions = new ArrayList<>();
                    for (Module m : Gugustus.moduleManager.getModules()) {
                        completions.add(m.getName());
                    }
                    return completions;
                }
                return null;
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                if (args.length < 1) {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cUsage: .toggle <module>"));
                    return;
                }
                String moduleName = args[0];
                Module module = Gugustus.moduleManager.getModule(moduleName);
                if (module == null) {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cModule '" + moduleName + "' not found."));
                    return;
                }
                module.toggle();
                sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §a" + module.getName() + " is now §e" + (module.isEnabled() ? "§aON" : "§cOFF")));
                Gugustus.configManager.saveCurrentConfig();
            }
        });

        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return ".config";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return ".config <save/load/folder> [name]";
            }

            @Override
            public List<String> getCommandAliases() {
                return new ArrayList<>();
            }

            @Override
            public int getRequiredPermissionLevel() {
                return 0;
            }

            @Override
            public boolean canCommandSenderUseCommand(ICommandSender sender) {
                return true;
            }

            @Override
            public void processCommand(ICommandSender sender, String[] args) {
                if (args.length < 1) {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cUsage: .config <save/load/folder> [name]"));
                    return;
                }
                String subCommand = args[0].toLowerCase();
                if (subCommand.equals("save")) {
                    if (args.length < 2) {
                        sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cUsage: .config save <name>"));
                        return;
                    }
                    String name = args[1];
                    Gugustus.configManager.saveConfig(name);
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §aConfig saved as '" + name + "'."));
                } else if (subCommand.equals("load")) {
                    if (args.length < 2) {
                        sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cUsage: .config load <name>"));
                        return;
                    }
                    String name = args[1];
                    if (Gugustus.configManager.loadConfig(name)) {
                        sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §aConfig '" + name + "' loaded."));
                    } else {
                        sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cConfig '" + name + "' not found."));
                    }
                } else if (subCommand.equals("folder")) {
                    File configDir = Gugustus.configManager.getConfigDir();
                    if (configDir.exists()) {
                        try {
                            Runtime.getRuntime().exec("explorer.exe " + configDir.getAbsolutePath());
                        } catch (Exception e) {
                            sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cCould not open folder."));
                        }
                    }
                } else {
                    sender.addChatMessage(new ChatComponentText("§7[§6Gugustus§7] §cUsage: .config <save/load/folder> [name]"));
                }
            }
        });
    }
}
