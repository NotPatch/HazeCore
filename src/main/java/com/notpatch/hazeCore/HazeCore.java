package com.notpatch.hazeCore;

import com.notpatch.hazeCore.command.ModuleBrigadierCommand;
import com.notpatch.hazeCore.helper.LuckPermsHelper;
import com.notpatch.hazeCore.helper.VaultHelper;
import com.notpatch.hazeCore.manager.ConfigManager;
import com.notpatch.hazeCore.manager.CooldownManager;
import com.notpatch.hazeCore.manager.DatabaseManager;
import com.notpatch.hazeCore.util.NLogger;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import com.notpatch.hazeCore.manager.ModuleManager;

public final class HazeCore extends JavaPlugin {

    @Getter
    private static HazeCore instance;

    @Getter
    private ModuleManager moduleManager;

    @Getter
    private DatabaseManager databaseManager;

    @Getter
    private CooldownManager cooldownManager;

    @Getter
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveConfig();

        registerCommands();

        configManager = new ConfigManager(this);

        moduleManager = new ModuleManager(this);
        moduleManager.loadAllModules();
        moduleManager.enableAllModules();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        cooldownManager = new CooldownManager();

        if(!VaultHelper.setupEconomy()){
            NLogger.warn("Vault not found, economy features disabled!");
        }
        if(!LuckPermsHelper.setupLuckPerms()){
            NLogger.warn("LuckPerms not found, some features disabled!");
        }

    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAllModules();
        }
        if(cooldownManager != null){
            cooldownManager.clearAll();
        }

    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(ModuleBrigadierCommand.create(), "Manage HazeCore modules");
        });
    }

}
