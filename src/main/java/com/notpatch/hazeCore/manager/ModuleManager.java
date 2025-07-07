package com.notpatch.hazeCore.manager;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.model.HazeModule;
import com.notpatch.hazeCore.util.NLogger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModuleManager {

    private final HazeCore plugin;
    private final File moduleFolder;
    @Getter
    private final Map<String, HazeModule> loadedModules;

    public ModuleManager(HazeCore plugin) {
        this.plugin = plugin;
        this.moduleFolder = new File(plugin.getDataFolder(), "modules");
        this.loadedModules = new HashMap<>();
        
        if (!moduleFolder.exists()) {
            moduleFolder.mkdirs();
        }
    }

    public void loadModules() {
        File[] moduleFiles = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (moduleFiles == null) {
            NLogger.warn("Module folder is empty or invalid!");
            return;
        }

        for (File moduleFile : moduleFiles) {
            try {
                loadModule(moduleFile);
            } catch (Exception e) {
                NLogger.error("Module '" + moduleFile.getName() + "' could not be loaded!" + e.getMessage());
            }
        }
    }

    private void loadModule(File file) throws Exception {
        JarFile jarFile = new JarFile(file);
        URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
        URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());

        var moduleEntry = jarFile.getEntry("module.yml");
        if (moduleEntry == null) {
            throw new Exception("module.yml don't found!");
        }

        YamlConfiguration moduleConfig = YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(jarFile.getInputStream(moduleEntry))
        );

        String mainClass = moduleConfig.getString("main");
        String moduleName = moduleConfig.getString("name");
        String version = moduleConfig.getString("version");

        if (mainClass == null || moduleName == null) {
            throw new Exception("Invalid module.yml file! main and name fields are required!");
        }

        Class<?> clazz = classLoader.loadClass(mainClass);
        if (!HazeModule.class.isAssignableFrom(clazz)) {
            throw new Exception("Main class is not a HazeModule class! Please check your module.yml file and try again!");
        }

        HazeModule module = (HazeModule) clazz.getDeclaredConstructor().newInstance();
        module.init(plugin, moduleConfig);
        
        loadedModules.put(moduleName, module);
        String moduleKey = moduleName + ":" + version;
        NLogger.info("Module '" + moduleKey + "' loaded successfully!");
    }

    public void enableModules() {

        for (HazeModule module : loadedModules.values()) {
            module.onEnable();
        }

        List<String> moduleNames = loadedModules.values()
                .stream()
                .map(HazeModule::getName)
                .collect(Collectors.toList());

        if (!moduleNames.isEmpty()) {
            String joinedNames = String.join(", ", moduleNames);
            NLogger.info(" Loaded modules: " + joinedNames);
        }

        loadedModules.values().forEach(HazeModule::onEnable);
        NLogger.info("Total " + loadedModules.size() + " module loaded successfully.");
    }

    public void disableModules() {
        loadedModules.values().forEach(HazeModule::onDisable);
    }
}