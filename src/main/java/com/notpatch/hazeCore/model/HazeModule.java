package com.notpatch.hazeCore.model;

import com.notpatch.hazeCore.HazeCore;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public abstract class HazeModule {
    protected HazeCore main;
    protected YamlConfiguration moduleInfo;
    protected FileConfiguration config;

    @Getter
    private final String name;

    public HazeModule() {
        this.name = getClass().getSimpleName();
    }

    public void init(HazeCore main, YamlConfiguration moduleInfo) {
        this.main = main;
        this.moduleInfo = moduleInfo;
        this.config = main.getConfigManager().createConfig(this, "config.yml");
    }

    protected void saveConfig() {
        main.getConfigManager().saveConfig(this, "config.yml");
    }

    protected void reloadConfig() {
        this.config = main.getConfigManager().createConfig(this, "config.yml");
    }

    protected FileConfiguration getConfig() {
        return this.config;
    }

    protected FileConfiguration createCustomConfig(String fileName) {
        return main.getConfigManager().createConfig(this, fileName);
    }

    public void saveResource(String resourcePath, boolean replace) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("Resource " + resourcePath + " not found!");
            }

            File outFile = new File(main.getDataFolder() + "/modules/" + getName(), resourcePath);
            if (!outFile.exists() || replace) {
                // Dosyayı oluştur
                outFile.getParentFile().mkdirs();
                Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public abstract void onEnable();
    public abstract void onDisable();
}
