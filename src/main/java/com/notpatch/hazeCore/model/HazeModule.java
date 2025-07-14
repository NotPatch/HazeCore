package com.notpatch.hazeCore.model;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.configuration.DatabaseConfiguration;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class HazeModule {

    @Getter
    public HazeCore main;
    protected YamlConfiguration moduleInfo;

    @Getter
    protected DatabaseConfiguration databaseConfig;

    @Getter
    private final String name;

    private final List<Listener> registeredListeners = new ArrayList<>();

    public HazeModule() {
        this.name = getClass().getSimpleName();
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, main);
        registeredListeners.add(listener);
    }

    public void unregisterListeners() {
        for(Listener listener : registeredListeners) {
            HandlerList.unregisterAll(listener);
        }
        registeredListeners.clear();
    }

    public void init(HazeCore main, YamlConfiguration moduleInfo) {
        this.main = main;
        this.moduleInfo = moduleInfo;
        File databaseConfigFile = new File(folderPath(), "database.yml");

        this.databaseConfig = ConfigManager.create(DatabaseConfiguration.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer());
            it.withBindFile(databaseConfigFile);
            it.withRemoveOrphans(true);
            it.saveDefaults();
            it.load(true);
        });
    }

    public String folderPath(){
        return main.getDataFolder().getPath() + File.separator + "modules" + File.separator + name;
    }

    public abstract void onEnable();
    public abstract void onDisable();

}
