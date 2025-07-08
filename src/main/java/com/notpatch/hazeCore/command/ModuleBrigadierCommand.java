package com.notpatch.hazeCore.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.manager.ModuleManager;
import com.notpatch.hazeCore.model.HazeModule;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ModuleBrigadierCommand {

    public static LiteralCommandNode<CommandSourceStack> create() {
        final ModuleManager moduleManager = HazeCore.getInstance().getModuleManager();

        return Commands.literal("module")
                .requires(source -> source.getSender().isOp())
                .executes(ModuleBrigadierCommand::sendUsage)
                .then(Commands.literal("list")
                        .executes(context -> {
                            final CommandSender sender = context.getSource().getSender();
                            Collection<HazeModule> activeModules = moduleManager.getLoadedModules().values();

                            if (activeModules.isEmpty()) {
                                sender.sendMessage("§c §7No active module found!");
                                return Command.SINGLE_SUCCESS;
                            }

                            String moduleNames = activeModules.stream()
                                    .map(HazeModule::getName)
                                    .collect(Collectors.joining("§f, §a"));

                            sender.sendMessage("§a! §7Active modules: §a" + moduleNames);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("load")
                        .then(Commands.argument("moduleName", StringArgumentType.word())
                                .suggests(ModuleBrigadierCommand::suggestUnloadedModules)
                                .executes(context -> {
                                    final String moduleName = context.getArgument("moduleName", String.class);
                                    moduleManager.loadModule(moduleName);
                                    context.getSource().getSender().sendMessage("§a! §7Attempting to load module '" + moduleName + "'.");
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("unload")
                        .then(Commands.argument("moduleName", StringArgumentType.word())
                                .suggests(ModuleBrigadierCommand::suggestLoadedModules)
                                .executes(context -> {
                                    final String moduleName = context.getArgument("moduleName", String.class);
                                    moduleManager.unloadModule(moduleName);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("reload")
                        .then(Commands.argument("moduleName", StringArgumentType.word())
                                .suggests(ModuleBrigadierCommand::suggestLoadedModules)
                                .executes(context -> {
                                    final String moduleName = context.getArgument("moduleName", String.class);
                                    moduleManager.reloadModule(moduleName);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                ).build();
    }


    private static int executeModuleCommand(CommandContext<CommandSourceStack> context, ModuleManager moduleManager, String action) {
        final CommandSender sender = context.getSource().getSender();
        final String moduleName = context.getArgument("moduleName", String.class);

        if (moduleName == null) {
            sender.sendMessage("§c! " + "§7'" + moduleName + "' can't found.");
            return Command.SINGLE_SUCCESS;
        }

        switch (action) {
            case "load":
                moduleManager.loadModule(moduleName);
                sender.sendMessage("§a! §7'" + moduleManager.getLoadedModules().get(moduleName).getName() + "' loaded successfully.");
                break;
            case "unload":
                moduleManager.unloadModule(moduleName);
                sender.sendMessage("§a! §7'" + moduleName + "' unloaded successfully.");
                break;
            case "reload":
                moduleManager.reloadModule(moduleName);
                sender.sendMessage("§a! §7'" + moduleManager.getLoadedModules().get(moduleName).getName() + "' reloaded successfully.");
                break;
        }
        return Command.SINGLE_SUCCESS;

    }

    private static int sendUsage(CommandContext<CommandSourceStack> context) {
        context.getSource().getSender().sendMessage("§c! " + "§7/module [<list | load | unload | reload>] [module]");
        return Command.SINGLE_SUCCESS;
    }

    private static CompletableFuture<Suggestions> suggestLoadedModules(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String loadedModuleNames = HazeCore.getInstance().getModuleManager().getLoadedModules().keySet().stream().collect(Collectors.joining(", "));
        return builder.suggest(loadedModuleNames).buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestUnloadedModules(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {

        return builder.suggest(HazeCore.getInstance().getModuleManager().getUnloadedModuleNames().stream().collect(Collectors.joining(", "))).buildFuture();
    }
}