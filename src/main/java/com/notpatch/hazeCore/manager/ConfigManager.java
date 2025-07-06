package com.notpatch.hazeCore.manager;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.model.HazeModule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final HazeCore plugin;
    private final Map<String, FileConfiguration> moduleConfigs;

    public ConfigManager(HazeCore plugin) {
        this.plugin = plugin;
        this.moduleConfigs = new HashMap<>();
    }

    public FileConfiguration createConfig(HazeModule module, String fileName) {
        File moduleFolder = new File(plugin.getDataFolder() + "/modules/" + module.getName());
        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }

        File configFile = new File(moduleFolder, fileName);
        if (!configFile.exists()) {
            try {
                module.saveResource(fileName, false);
            } catch (Exception e) {
                try {
                    configFile.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        moduleConfigs.put(module.getName() + ":" + fileName, config);
        return config;
    }

    public FileConfiguration getConfig(HazeModule module, String fileName) {
        return moduleConfigs.get(module.getName() + ":" + fileName);
    }

    public void saveConfig(HazeModule module, String fileName) {
        String key = module.getName() + ":" + fileName;
        FileConfiguration config = moduleConfigs.get(key);
        if (config != null) {
            try {
                File file = new File(plugin.getDataFolder() + "/modules/" + module.getName(), fileName);
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
