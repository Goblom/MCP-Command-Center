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
package codes.goblom.mcc.commands;

import codes.goblom.factory.command.CommandContext;
import codes.goblom.factory.command.CommandInfo;
import codes.goblom.factory.command.CommandListener;
import codes.goblom.mcc.CommandCenterPlugin;
import codes.goblom.mcc.JellyConfiguration.ConfigValue;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bryan
 */
public class ConfigCommands implements CommandListener {
    
    private final CommandCenterPlugin plugin;

    public ConfigCommands(CommandCenterPlugin plugin) {
        this.plugin = plugin;
    }
    
    @CommandInfo(
            name = "saveConfig",
            permission = "mcc.saveconfig",
            description = "Saves the config after editing",
            alias = { "sc" }
    )
    public void saveConfig(CommandContext context) {
        if (context.isTabExecutor()) return;
        
        plugin.getMCCConfig().save(plugin);
    }
    
    @CommandInfo(
            name = "loadConfig",
            permission = "mcc.loadconfig",
            description = "Loads the edited config into memory",
            alias = { "lc", "reloadConfig", "rc" }
    )
    public void loadConfig(CommandContext context) {
        if (context.isTabExecutor()) return;
        
        plugin.getMCCConfig().load(plugin);
    }
    
    // TODO: Buggy, not completed
//    @CommandInfo(
//            name = "editConfig",
//            permission = "mcc.editconfig",
//            description = "Edit config object. Does not save"
//    )
    // /mcc editconfig httpPort [int]
    // /mcc editconfig mcpPath [string]
    // /mcc editconfig debugMode [true/false]
    // /mcc editconfig tokenPermissions [put/remove] [token] [perms...]
    public void editConfig(CommandContext context) {
//        for (int i = 0; i < context.argsLength(); i++) {
//            String arg = context.getArg(i);
//            context.suggest("[" + i + "]" + arg);
//        }
        
        Field[] foundFields = plugin.getMCCConfig().getClass().getDeclaredFields();
        List<Field> validFields = Arrays.asList(foundFields).stream() //Possibly resource intensive, maybe store this 
                .filter((f) -> { return f.isAnnotationPresent(ConfigValue.class); })
                .toList();
        
        
        String fieldName = context.getArg(0);
        
        if (context.argsLength() == 1 && context.isTabExecutor()) {
            validFields.stream()
                    .filter((f) -> { return fieldName == null || fieldName.isEmpty() || f.getName().startsWith(fieldName); } )
                    .forEach((f) -> context.suggest(f.getName()));
        }
        
        Field field = validFields.stream().filter((f) -> { return f.getName().equalsIgnoreCase(fieldName); }).findFirst().orElse(null);
        if (context.argsLength() == 2 || field == null) {
            if (context.isTabExecutor()) return;
            
            context.message("Error: Config Variable doesnt exist!");
            return;
        }
        
        field.setAccessible(true);
        Class type = field.getType();

        if (type == String.class) {
            context.suggest("[string]");
            //not tabExecutor do stuff
            if (!context.isTabExecutor()) {
                String value = context.getArg(1);

                if (value == null || value.isEmpty()) {
                    context.message("String value cannot be null or empty");
                    return;
                }

                try {
                    field.set(plugin.getMCCConfig(), value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (type == int.class || type == Integer.class) {
            context.suggest("[number]");
            String numStr = context.getArg(1);
            
            try {
                int number = Integer.parseInt(numStr);
                
                field.set(plugin.getMCCConfig(), number);
                context.message("Updated " + fieldName + " to " + number + ".");
            } catch (Exception e) {
                e.printStackTrace();
                context.message("Error: Must be a number without a decimal.");
            }
        } else if (type == boolean.class || type == Boolean.class) {
            Map<String, Boolean> bools = new HashMap() {
                {
                    put("true", true);
                    put("false", false);
                    put("yes", true);
                    put("no", false);
                    put("t", true);
                    put("f", false);
                    put("y", true);
                    put("n", false);
                }
            };
            
            for (String key : bools.keySet()) {
                context.suggest(key);
            }
            
            String boolStr = context.getArg(1);
            
            if (!bools.containsKey(boolStr)) {
                context.message("Not a valid boolean.");
                return;
            }
            
            boolean bool = bools.get(boolStr);
            
            try {
                field.set(plugin.getMCCConfig(), bool);
            } catch (Exception e) {
                e.printStackTrace();
                context.message("Error updating boolean for " + fieldName + ". Check logs.");
            }
        } else if (Map.class.isAssignableFrom(type) /* || field.get(plugin.getMCCConfig()) instanceof Map */) { // Should be if type is instanceof Map
            String action = context.getArg(1);

            // This doesnt suggest
            if (context.argsLength() == 2) {
                context.suggest("add");
                context.suggest("remove");
            }

            String token = context.getArg(2);

            if (action == null || action.isEmpty()) {
                return;
            }
            
            switch (action.toLowerCase()) {
                case "add":
                    if (context.argsLength() == 3) {
                        context.suggest("[secret token]");
                        return;
                    }
                    
                    String permissions = context.combineRemaining(3);
                    if (permissions == null || permissions.isEmpty()) {
                        return;
                    }
                    
                    List<String> nodes = Arrays.asList(permissions.split(" "));
                    
                    if (nodes.size() == 0) {
                        nodes.add("tools.all");
                    }
                    
                    if (context.isTabExecutor()) {
                        plugin.getTools().forEach((tool) -> {
                            ToolProvider provider = (ToolProvider) tool.callHandler();
                            String permission = "tools." + provider.getName();
                            String currentArg = context.getArg(context.argsLength() - 1);

                            if (nodes.contains(permission)) return;
                            
                            if (currentArg == null || currentArg.isEmpty() || permission.startsWith(currentArg)) {
                                context.suggest(permission);
                            }
                        });

                        return;
                    }

//                    plugin.getMCCConfig().getTokenPermissions().put(token, nodes);
                    try {
                        ((Map) field.get(plugin.getMCCConfig())).put(token, nodes);
                    } catch (Exception e) {
                        e.printStackTrace();
                        context.message("Error updating map for " + fieldName + ". Check Logs.");
                    }
                    break;
                case "remove":
                    if (context.isTabExecutor()) {
                        if (context.argsLength() >= 3) {
                            plugin.getMCCConfig().getTokenPermissions().keySet()
                                    .forEach((key) -> context.suggest(key));
                        }

                        return;
                    }

                    // TODO: Needs to use Field Accessor
//                    plugin.getMCCConfig().getTokenPermissions().remove(token);
                    try {
                        ((Map) field.get(plugin.getMCCConfig())).remove(token);
                        context.message("Removed Token: " + token);
                    } catch (Exception e) {
                        e.printStackTrace();
                        context.message("Error updating map for " + fieldName + ". Check Logs.");
                    }
                    break;
                default:
                    if (context.isTabExecutor()) {
                        return;
                    }

                    context.message("Unknown action: " + action);
                    break;
            }
//        }  else if (Collection.class.isAssignableFrom(type) /* || field.get(plugin.getMCCConfig()) instanceof Collection */) { // Should be if type is instanceof Collection
//            String action = context.getArg(1);
//            
//            if (context.argsLength() == 2) {
//                context.suggest("add");
//                context.suggest("remove");
//            }
//            
//            switch (action.toLowerCase()) {
//                case "add":
//                case "remove":
//                    //TODO
//            }
        } else {
            context.message("Unknown field type " + type + " for " + fieldName);
        }
    }
}
