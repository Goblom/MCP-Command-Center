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
package codes.goblom.mcpai;

import codes.goblom.commandfactory.CommandContext;
import codes.goblom.commandfactory.CommandInfo;
import codes.goblom.commandfactory.CommandListener;
import codes.goblom.mcpai.mcp.ServiceHandler;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import com.google.common.collect.Lists;
import io.modelcontextprotocol.server.McpServerFeatures;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Bryan
 */
public class CommandCenterCommands implements CommandListener {
    
    // Inspiration from, split the sections
    // https://stackoverflow.com/a/20536597
    protected static String getSaltString(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        int sections = length < 12 ? 2 : length < 24 ? 3 : 4;
        int charsOnly = length - (sections - 1);

        int base = charsOnly / sections;
        int extra = charsOnly % sections;

        StringBuilder salt = new StringBuilder();

        for (int s = 0; s < sections; s++) {
            int size = base + (s < extra ? 1 : 0);

            for (int i = 0; i < size; i++) {
                salt.append(chars.charAt(Configuration.RANDOM.nextInt(chars.length())
                ));
            }

            if (s < sections - 1) {
                salt.append("-");
            }
        }

        return salt.toString();
    }
    
    @CommandInfo(
            name = "generateToken",
            permission = "mcc.generatetoken",
            description = "Generate a secret token",
            usage = "[length] {permissions}",
            alias = { "gen", "token", "gentoken", "gt" }
    )
    public void generateToken(CommandContext context) {
        String lengthStr = context.getArg(0);
        
        if (context.isTabExecutor()) {
            if (context.argsLength() == 1) {
                context.suggest("[length]");
            } else if (context.argsLength() > 1) {
                Configuration.PLUGIN.tools.forEach((tool) -> {
                    ToolProvider provider = (ToolProvider) tool.callHandler();
                    String permission = "tools." + provider.getName();
                    String currentArg = context.getArg(context.argsLength() - 1);
                    
                    if (currentArg == null || currentArg.isEmpty() || permission.startsWith(currentArg)) {
                        context.suggest(permission);
                    }
                });
            }
            
            return;
        }
        
        boolean pull = false;
        
        if (lengthStr == null) {
//            context.message("No length given, using default of 15");
            lengthStr = "15";
            pull = true;
        }
        
        int length;
        try {
            length = Integer.parseInt(lengthStr);
        } catch (Exception e) { 
            context.message("Error: Length must be a number!");
            return;
        }
        
        String genToken = getSaltString(length);
        
        List<String> permissions = Lists.newArrayList();
        if (context.argsLength() > 1) {
            // Pull from zero if expected length is not a number.
            // Start at 1 if 0 is a number. Start at 0 if 0 not a number.
            int startIndex = pull ? 0 : 1; 
            permissions = Arrays.asList(context.combineRemaining(startIndex).split(" "));
        } else {
            permissions.add("tools.all");
        }
        
        context.message("Generated Token: " + genToken);
        context.message("Added token to config with permissions: " + permissions);
        
        Configuration.TOKEN_PERMISSIONS.put(genToken, permissions);
    }
    
    @CommandInfo(
            name = "displayTokens",
            description = "Display all tokens that have been created",
            permission = "mcc.displaytokens"
    )
    public void displayTokens(CommandContext context) {
        if (context.isTabExecutor()) return;
        
        context.message("Created Tokens:");
        context.message("===============================");
        
        Configuration.TOKEN_PERMISSIONS.entrySet().forEach((entry) -> {
            String token = entry.getKey();
            List<String> permissions = entry.getValue();
            
            context.message("| " + token);
            permissions.forEach((str) -> {
                context.message("|  - " + str);
            });
        });
    }
    
    @CommandInfo(
            name = "disableTool",
            description = "Disables a tool",
            usage = "[name] {persistant}",
            permission = "mcc.disabletool"
    )
    public void disableTool(CommandContext context) {
        if (context.isTabExecutor()) {
            if (context.argsLength() == 1) {
                final String currentArg = context.getArg(0);
                
                Configuration.PLUGIN.tools.forEach((st) -> {
                    ToolProvider tool = (ToolProvider) st.callHandler();
                    String name = tool.getName();
                    
                    if (currentArg == null || currentArg.isEmpty() || name.startsWith(currentArg)) {
                        context.suggest(name);
                    }
                });
            } else if (context.argsLength() == 2) {
                context.suggest("true");
                context.suggest("false");
            }
            
            return;
        }
        
        if (context.argsLength() == 0) {
            context.message("Error: Not tool name given!");
            return;
        }
        
        final String toolName = context.getArg(0);
        boolean toolExists = Configuration.PLUGIN.syncServer.listTools().stream().anyMatch((tool) -> {
            return tool.name().equalsIgnoreCase(toolName);
        });
        
        String boolStr = context.getArg(1);
        boolean persistant = false;
        try {
            persistant = Boolean.parseBoolean(boolStr);
        } catch (Exception e) { }
        
        if (toolExists) {
            Configuration.PLUGIN.syncServer.removeTool(toolName);
            context.message("Tool " + toolName + " removed");
            
            if (persistant) {
                Configuration.DISABLED_TOOLS.add(toolName);
            }
        } else {
            context.message("Error: Tool " + toolName + " ddoes not exist!");
        }
    }
    
    @CommandInfo(
            name = "enableTool",
            description = "Enables a disabled tool",
            usage = "[name] {persistant}",
            permission = "mcc.enabletool"
    )
    public void enableTool(CommandContext context) {
        if (context.isTabExecutor()) {
            if (context.argsLength() == 1) {
                final String currentArg = context.getArg(0);
                
                ServiceHandler.getRegisteredTools().forEach((st) -> {
                    ToolProvider tool = (ToolProvider) ServiceHandler.getRegisteredToolObject(st).callHandler();
                    String name = tool.getName();
                    
                    if (currentArg == null || currentArg.isEmpty() || name.startsWith(currentArg)) {
                        context.suggest(name);
                    }
                });
            } else if (context.argsLength() == 2) {
                context.suggest("true");
                context.suggest("false");
            }
            
            return;
        }
         if (context.argsLength() == 0) {
            context.message("Error: Not tool name given!");
            return;
        }
        
        final String toolName = context.getArg(0);
        boolean toolExists = Configuration.PLUGIN.syncServer.listTools().stream().anyMatch((tool) -> {
            return tool.name().equalsIgnoreCase(toolName);
        });
        
        String boolStr = context.getArg(1);
        boolean persistant = false;
        
        try {
            persistant = Boolean.parseBoolean(boolStr);
        } catch (Exception e) { }
        
        if (!toolExists) {
            McpServerFeatures.SyncToolSpecification tool = ServiceHandler.getRegisteredToolObject(toolName);
            
            if (tool == null) {
                context.message("Error: We dont know a " + toolName + " tool.");
            } else {
                Configuration.PLUGIN.syncServer.addTool(tool);
                context.message("Enabled tool " + toolName);
                
                if (persistant) {
                    Configuration.DISABLED_TOOLS.remove(toolName);
                }
            }
        } else {
            context.message("Tool already enabled!");
        }
    }
}
