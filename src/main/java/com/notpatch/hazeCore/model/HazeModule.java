package com.notpatch.hazeCore.model;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.configuration.DatabaseConfiguration;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class HazeModule {
    protected HazeCore main;
    protected YamlConfiguration moduleInfo;

    @Getter
    protected DatabaseConfiguration databaseConfig;

    @Getter
    private final String name;

    public HazeModule() {
        this.name = getClass().getSimpleName();
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
