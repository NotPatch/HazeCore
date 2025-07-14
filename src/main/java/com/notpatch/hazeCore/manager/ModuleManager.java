package com.notpatch.hazeCore.manager;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.model.HazeModule;
import com.notpatch.hazeCore.util.NLogger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

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

    public void loadAllModules() {
        File[] moduleFiles = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (moduleFiles == null) {
            return;
        }
        for (File moduleFile : moduleFiles) {
            try {
                loadAndEnableModuleFromFile(moduleFile);
            } catch (Exception e) {
                NLogger.error("Module '" + moduleFile.getName() + "' could not be loaded during startup! " + e.getMessage());
            }
        }
    }

    public void enableAllModules() {
        List<String> moduleNames = new ArrayList<>(loadedModules.keySet());
        if (!moduleNames.isEmpty()) {
            NLogger.info("Loaded modules: " + String.join(", ", moduleNames));
        }
        NLogger.info("Total " + loadedModules.size() + " modules loaded.");
    }

    public void disableAllModules() {
        new ArrayList<>(loadedModules.keySet()).forEach(this::unloadModule);
    }

    private void loadAndEnableModuleFromFile(File file) throws Exception {
        try (JarFile jarFile = new JarFile(file)) {
            var moduleEntry = jarFile.getEntry("module.yml");
            if (moduleEntry == null) {
                throw new Exception("module.yml not found in " + file.getName());
            }

            YamlConfiguration moduleConfig;
            try (InputStreamReader reader = new InputStreamReader(jarFile.getInputStream(moduleEntry))) {
                moduleConfig = YamlConfiguration.loadConfiguration(reader);
            }

            String mainClass = moduleConfig.getString("main");
            String moduleName = moduleConfig.getString("name");
            String version = moduleConfig.getString("version");

            if (mainClass == null || moduleName == null) {
                throw new Exception("Invalid module.yml! 'main' and 'name' are required.");
            }

            if (loadedModules.containsKey(moduleName)) {
                return;
            }

            URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());

            Class<?> clazz = classLoader.loadClass(mainClass);
            if (!HazeModule.class.isAssignableFrom(clazz)) {
                throw new Exception("Main class does not extend HazeModule!");
            }

            HazeModule module = (HazeModule) clazz.getDeclaredConstructor().newInstance();
            module.init(plugin, moduleConfig);

            loadedModules.put(moduleName, module);
            module.onEnable();

            NLogger.info("Module '" + moduleName + ":" + version + "' loaded and enabled successfully!");
        }
    }

    public void loadModule(String moduleName) {
        if (loadedModules.containsKey(moduleName)) {
            NLogger.info("Module '" + moduleName + "' is already loaded!");
            return;
        }

        File moduleFile = findFileForModuleName(moduleName);
        if (moduleFile == null) {
            NLogger.error("A .jar file for module '" + moduleName + "' could not be found.");
            return;
        }

        try {
            loadAndEnableModuleFromFile(moduleFile);
        } catch (Exception e) {
            NLogger.error("Module '" + moduleFile.getName() + "' could not be loaded! " + e.getMessage());
        }
    }

    public void unloadModule(String moduleName) {
        HazeModule module = loadedModules.get(moduleName);
        if (module != null) {
            module.unregisterListeners();
            module.onDisable();

            loadedModules.remove(moduleName);
            NLogger.info("Module '" + moduleName + "' unloaded successfully.");
        } else {
            NLogger.warn("Module '" + moduleName + "' is not loaded!");
        }
    }

    public void reloadModule(String moduleName) {
        unloadModule(moduleName);
        loadModule(moduleName);
    }

    public HazeModule getLoadedModule(String name) {
        return loadedModules.get(name);
    }

    public List<String> getUnloadedModuleNames() {
        List<String> unloadedNames = new ArrayList<>();
        File[] moduleFiles = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (moduleFiles == null) {
            return unloadedNames;
        }

        for (File moduleFile : moduleFiles) {
            try (JarFile jarFile = new JarFile(moduleFile)) {
                var moduleEntry = jarFile.getEntry("module.yml");
                if (moduleEntry == null) {
                    continue;
                }
                try (InputStream input = jarFile.getInputStream(moduleEntry)) {
                    YamlConfiguration moduleConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(input));
                    String moduleName = moduleConfig.getString("name");
                    if (moduleName != null && !loadedModules.containsKey(moduleName)) {
                        unloadedNames.add(moduleName);
                    }
                }
            } catch (Exception e) {
                NLogger.warn("Could not read module.yml from " + moduleFile.getName());
            }
        }
        return unloadedNames;
    }

    private File findFileForModuleName(String moduleName) {
        File[] moduleFiles = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (moduleFiles == null) {
            return null;
        }

        for (File moduleFile : moduleFiles) {
            try (JarFile jarFile = new JarFile(moduleFile)) {
                var moduleEntry = jarFile.getEntry("module.yml");
                if (moduleEntry == null) {
                    continue;
                }
                try (InputStream input = jarFile.getInputStream(moduleEntry)) {
                    YamlConfiguration moduleConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(input));
                    String nameInYml = moduleConfig.getString("name");
                    if (moduleName.equals(nameInYml)) {
                        return moduleFile;
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }
}
