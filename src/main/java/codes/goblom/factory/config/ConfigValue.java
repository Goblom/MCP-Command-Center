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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Bryan
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValue {
    
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
    Class<? extends Handler> handler() default ObjectHandler.class;
    
    /**
     * Any comments in the Configuration value() path
     * 
     * @return The comments
     */
    String[] comments() default { };
    
    static interface Handler<T> {
        
        public T load(Configuration config, String path);
        
        public default void set(Configuration config, String path, T value) {
            config.set(path, value);
        }
    }
    
    static class ObjectHandler implements Handler<Object> {

        @Override
        public Object load(Configuration config, String path) {
            return config.get(path);
        }
    }
    
    static class StringHandler implements Handler<String> {
        
        @Override
        public String load(Configuration config, String path) {
           return config.getString(path);
        }
    }
    
    static class BooleanHandler implements Handler<Boolean> {

        @Override
        public Boolean load(Configuration config, String path) {
            return config.getBoolean(path);
        }        
    }
    
    static class StringListHandler implements Handler<List<String>> {

        @Override
        public List<String> load(Configuration config, String path) {
            return config.getStringList(path);
        }
    }
    
    static class IntHandler implements Handler<Integer> {

        @Override
        public Integer load(Configuration config, String path) {
            return config.getInt(path);
        }        
    }
    
    static class DoubleHandler implements Handler<Double> {

        @Override
        public Double load(Configuration config, String path) {
            return config.getDouble(path);
        }
    }
    
    static class MapHandler implements Handler<Map<String, Object>> {

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
                
                map.put(key, map);
            }
            
            return map;
        }
        
    }
}
