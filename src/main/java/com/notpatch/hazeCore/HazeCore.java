package com.notpatch.hazeCore;

import com.notpatch.hazeCore.helper.VaultHelper;
import com.notpatch.hazeCore.manager.ConfigManager;
import com.notpatch.hazeCore.manager.CooldownManager;
import com.notpatch.hazeCore.manager.DatabaseManager;
import com.notpatch.hazeCore.util.NLogger;
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

        configManager = new ConfigManager(this);

        moduleManager = new ModuleManager(this);
        moduleManager.loadModules();
        moduleManager.enableModules();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        cooldownManager = new CooldownManager();



        if(!VaultHelper.setupEconomy()){
            NLogger.warn("Vault not found, economy features disabled!");
        }

    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableModules();
        }
        if(cooldownManager != null){
            cooldownManager.clearAll();
        }

    }
}
