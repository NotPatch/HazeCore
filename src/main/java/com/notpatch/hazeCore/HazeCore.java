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

        moduleManager = new ModuleManager(this);
        moduleManager.loadModules();
        moduleManager.enableModules();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        cooldownManager = new CooldownManager();

        configManager = new ConfigManager(this);

        if(!VaultHelper.setupEconomy()){
            NLogger.warn("Vault bulunamadı, ekonomi işlemleri yapılamaz");
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
