/*
 * The MIT License
 *
 * Copyright 2026 Bryan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package codes.goblom.factory.config;

import codes.goblom.factory.config.ConfigValue.Handler;
import java.io.File;
import java.util.Arrays;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Bryan
 */
public class ConfigurationFactory {
    
    public static void saveConfig(Plugin plugin, Object configObj) {
        String fileName = "config.yml";
        FileConfiguration loadedConfig = null;
        
        if (configObj.getClass().isAnnotationPresent(ConfigFile.class)) {
            ConfigFile config = configObj.getClass().getAnnotation(ConfigFile.class);
            fileName = config.value();
            
            try {
                loadedConfig = config.type().newInstance();
            } catch (Exception e) {
                plugin.getLogger().warning("Error using " + config.type().getSimpleName() + " using YamlConfiguration instead.");
            }
        }
        
        if (loadedConfig == null) {
            loadedConfig = new YamlConfiguration();
        }
        
        loadedConfig.options().parseComments(true);
        File configFile = new File(plugin.getDataFolder(), fileName);
        
//        try {
//            if (!configFile.exists()) {
//                plugin.getDataFolder().mkdirs();
//                configFile.createNewFile();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        
        final FileConfiguration fLoaded = loadedConfig;
        Arrays.asList(configObj.getClass().getDeclaredFields()).stream()
            .filter((field) -> { return field.isAnnotationPresent(ConfigValue.class); })
            .forEach((field) -> {
                field.setAccessible(true);
                ConfigValue config = field.getAnnotation(ConfigValue.class);
                Handler handler;
                Object value;

                try {
                    value = field.get(configObj);
                    handler = config.handler().newInstance();

                    if (config.comments().length > 0) {
                        fLoaded.setComments(config.value(), Arrays.asList(config.comments()));
                    }
                    
                    handler.set(fLoaded, config.value(), value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        try {
            fLoaded.save(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void loadConfig(Plugin plugin, Object configObj) {
        String fileName = "config.yml";
        FileConfiguration loadedConfig = null;
        
        if (configObj.getClass().isAnnotationPresent(ConfigFile.class)) {
            ConfigFile config = configObj.getClass().getAnnotation(ConfigFile.class);
            
            fileName = config.value();
            
            try {
                loadedConfig = config.type().newInstance();
            } catch (Exception e) {
                plugin.getLogger().warning("Error using " + config.type().getSimpleName() + " using YamlConfiguration instead.");
//                loadedConfig = new YamlConfiguration();
            }
        }
        
        if (loadedConfig == null) {
            // Use default if it failed to load above or type not provided
            loadedConfig = new YamlConfiguration();
        }
        
        File configFile = new File(plugin.getDataFolder(), fileName);
             
        if (!configFile.exists()) {
            saveConfig(plugin, configObj);
        } else {
            try {
                loadedConfig.load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
                return; //Dont do anything if config failed to load, this is normally a yaml error
            }
                           
            final FileConfiguration fLoaded = loadedConfig;
            Arrays.asList(configObj.getClass().getDeclaredFields()).stream()
                    .filter((field) -> { return field.isAnnotationPresent(ConfigValue.class); })
                    .forEach((field) -> {
                        field.setAccessible(true);
                        ConfigValue config = field.getAnnotation(ConfigValue.class);
                        Handler handler;
                        
                        try {
                            handler = config.handler().newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return; //Dont do anything if failed to create new instance
                        }
                        
                        Object value = handler.load(fLoaded, config.value());
                        if (value == null) {
                            try {
                                // Try to set a default value in the Configuration
                                handler.set(fLoaded, config.value(), field.get(configObj));
                                
                                if (config.comments().length > 0) {
                                    // We dont save here, so no need to waste resources
//                                    fLoaded.setComments(config.value(), Arrays.asList(config.value()));
                                }
                            } catch (Exception e) {
                                //If field error, most likely wont happen.
                                e.printStackTrace();
                                return;
                            }
                        } else {
                            try {
                                // Updates the field
                                field.set(configObj, value);
                            } catch (Exception e) {
                                // This mostly likely will fail if types dont match up.
                                // This should not happen due to Handler#load
                                e.printStackTrace();
                            }
                        }
                    });
                    
        }
    }
}
