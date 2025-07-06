package com.notpatch.hazeCore.manager;

import com.notpatch.hazeCore.HazeCore;
import com.notpatch.hazeCore.model.HazeModule;
import com.notpatch.hazeCore.util.NLogger;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;
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

    public void loadModules() {
        File[] moduleFiles = moduleFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (moduleFiles == null) {
            NLogger.warn("Modül klasörü bulunamadı veya boş!");
            return;
        }

        for (File moduleFile : moduleFiles) {
            try {
                loadModule(moduleFile);
            } catch (Exception e) {
                NLogger.error(moduleFile.getName() + " modül yüklenirken hata oluştu:" + e.getMessage());
            }
        }
    }

    private void loadModule(File file) throws Exception {
        JarFile jarFile = new JarFile(file);
        URL[] urls = {new URL("jar:file:" + file.getPath() + "!/")};
        URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());

        var moduleEntry = jarFile.getEntry("module.yml");
        if (moduleEntry == null) {
            throw new Exception("module.yml bulunamadı!");
        }

        YamlConfiguration moduleConfig = YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(jarFile.getInputStream(moduleEntry))
        );

        String mainClass = moduleConfig.getString("main");
        String moduleName = moduleConfig.getString("name");
        String version = moduleConfig.getString("version");

        if (mainClass == null || moduleName == null) {
            throw new Exception("Geçersiz module.yml yapılandırması!");
        }

        Class<?> clazz = classLoader.loadClass(mainClass);
        if (!HazeModule.class.isAssignableFrom(clazz)) {
            throw new Exception("Ana sınıf HazeModule'den türetilmemiş!");
        }

        HazeModule module = (HazeModule) clazz.getDeclaredConstructor().newInstance();
        module.init(plugin, moduleConfig);
        
        loadedModules.put(moduleName, module);
        NLogger.info(moduleName + " v" + version + " modülü başarıyla yüklendi!");
    }

    public void enableModules() {
        for(HazeModule module : loadedModules.values()){
            module.onEnable();
            NLogger.info("Modül: " + module.getName() + " aktif edildi!");
        }
        loadedModules.values().forEach(HazeModule::onEnable);
        NLogger.info("Toplam " + loadedModules.size() + " adet modül aktif edildi.");
    }

    public void disableModules() {
        loadedModules.values().forEach(HazeModule::onDisable);
    }
}