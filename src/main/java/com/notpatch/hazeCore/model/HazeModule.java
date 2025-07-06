package com.notpatch.hazeCore.model;

import com.notpatch.hazeCore.HazeCore;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class HazeModule {
    protected HazeCore plugin;

    protected YamlConfiguration config;

    public void init(HazeCore plugin, YamlConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public abstract void onEnable();
    public abstract void onDisable();
}
