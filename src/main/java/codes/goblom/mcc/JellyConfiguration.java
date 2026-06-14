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
package codes.goblom.mcc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Thought i would name this library Jelly Configuration because of how moldable
 * it is
 * 
 *
 * @version 1.0
 * @author Bryan
 */
public interface JellyConfiguration {

    public default <T extends JellyConfiguration> T save(Plugin plugin) {
        String fileName = "config.yml";
        FileConfiguration loadedConfig = null;
        
        if (getClass().isAnnotationPresent(ConfigSource.class)) {
            ConfigSource config = getClass().getAnnotation(ConfigSource.class);
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
        Arrays.asList(getClass().getDeclaredFields()).stream()
            .filter((field) -> { return field.isAnnotationPresent(ConfigValue.class); })
            .forEach((field) -> {
                field.setAccessible(true);
                ConfigValue config = field.getAnnotation(ConfigValue.class);
                ConfigValue.ValueHandler handler;
                Object value;

                try {
                    value = field.get(JellyConfiguration.this);
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
        
        return (T) this;
    }
    
    public default <T extends JellyConfiguration> T load(Plugin plugin) {
        String fileName = "config.yml";
        FileConfiguration loadedConfig = null;
        
        if (getClass().isAnnotationPresent(ConfigSource.class)) {
            ConfigSource source = getClass().getAnnotation(ConfigSource.class);
            fileName = source.value();
            
            try {
                loadedConfig = source.type().newInstance();
            } catch (Exception e) {
                plugin.getLogger().warning("Error using " + source.type().getSimpleName() + " using YamlConfiguration instead.");
//                loadedConfig = new YamlConfiguration();
            }
        }
        
        if (loadedConfig == null) {
            // Use default if it failed to load above or type not provided
            loadedConfig = new YamlConfiguration();
        }
        
        File configFile = new File(plugin.getDataFolder(), fileName);
             
        if (!configFile.exists()) {
            save(plugin);
        } else {
            try {
                loadedConfig.load(configFile);
            } catch (Exception e) {
                e.printStackTrace();
                return (T) this; //Dont do anything if config failed to load, this is normally a yaml error
            }
                           
            final FileConfiguration fLoaded = loadedConfig;
            Arrays.asList(getClass().getDeclaredFields()).stream()
                    .filter((field) -> { return field.isAnnotationPresent(ConfigValue.class); })
                    .forEach((field) -> {
                        field.setAccessible(true);
                        ConfigValue config = field.getAnnotation(ConfigValue.class);
                        ConfigValue.ValueHandler handler;
                        
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
                                handler.set(fLoaded, config.value(), field.get(JellyConfiguration.this));
                                
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
                                field.set(JellyConfiguration.this, value);
                            } catch (Exception e) {
                                // This mostly likely will fail if types dont match up.
                                // This should not happen due to Handler#load
                                e.printStackTrace();
                            }
                        }
                    });
        }
        
        return (T) this;
    }
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ConfigSource {

        public String value() default "config.yml";

        // TODO:
        //   Support other types of configs that dont use Files
        //   But Bukkit doesn't support others by default.
        //   LOW_PRIORITY
        public Class<? extends FileConfiguration> type() default YamlConfiguration.class;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface ConfigValue {

        /**
         * Ex. "path.to.value"
         *
         * @return Path to config value
         */
        String value();

        /**
         * Custom handler for Setting and Loaded config Data to avoid errors
         *
         * @return The required Loaded, default ObjectHandler.class
         */
        Class<? extends ValueHandler> handler() default ObjectHandler.class;

        /**
         * Any comments in the Configuration value() path
         *
         * @return The comments
         */
        String[] comments() default {};

        public static interface ValueHandler<T> {

            public T load(Configuration config, String path);

            public default void set(Configuration config, String path, T value) {
                config.set(path, value);
            }
        }

        static class ObjectHandler implements ValueHandler<Object> {

            @Override
            public Object load(Configuration config, String path) {
                return config.get(path);
            }
        }

        static class StringHandler implements ValueHandler<String> {

            @Override
            public String load(Configuration config, String path) {
                return config.getString(path);
            }
        }

        static class BooleanHandler implements ValueHandler<Boolean> {

            @Override
            public Boolean load(Configuration config, String path) {
                return config.getBoolean(path);
            }
        }

        static class StringListHandler implements ValueHandler<List<String>> {

            @Override
            public List<String> load(Configuration config, String path) {
                return config.getStringList(path);
            }
        }

        static class IntHandler implements ValueHandler<Integer> {

            @Override
            public Integer load(Configuration config, String path) {
                return config.getInt(path);
            }
        }

        static class DoubleHandler implements ValueHandler<Double> {

            @Override
            public Double load(Configuration config, String path) {
                return config.getDouble(path);
            }
        }

        static class MapHandler implements ValueHandler<Map<String, Object>> {

            @Override
            public void set(Configuration config, String path, Map<String, Object> value) {
                ConfigurationSection section = config.createSection(path);

                value.entrySet().forEach((entry) -> {
                    if (entry.getValue() instanceof Collection) {
                        Collection coll = (Collection) entry.getValue();

                        if (coll.size() == 1) {
                            section.set(entry.getKey(), coll.stream().findFirst().get());
                            return;
                        }
                    }

                    section.set(entry.getKey(), entry.getValue());
                });
            }

            @Override
            public Map<String, Object> load(Configuration config, String path) {
                ConfigurationSection section = config.getConfigurationSection(path);
                Set<String> keys = section.getKeys(false);
                Map<String, Object> map = Maps.newHashMap();

                for (String key : keys) {
                    List<Object> tokens = Lists.newArrayList();

                    if (section.isList(key)) {
                        tokens.addAll(section.getList(key));
                    } else {
                        tokens.add(section.get(key));
                    }

                    map.put(key, tokens);
                }

                return map;
            }

        }
    }
}
