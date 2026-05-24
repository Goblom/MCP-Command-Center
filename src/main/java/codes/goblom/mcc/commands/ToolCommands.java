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
import codes.goblom.mcc.mcp.ServiceHandler;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import io.modelcontextprotocol.server.McpServerFeatures;

/**
 *
 * @author Bryan
 */
public class ToolCommands implements CommandListener {
    
    private final CommandCenterPlugin plugin;

    public ToolCommands(CommandCenterPlugin plugin) {
        this.plugin = plugin;
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
                
                plugin.getTools().forEach((st) -> {
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
        boolean toolExists = plugin.getSyncServer().listTools().stream().anyMatch((tool) -> {
            return tool.name().equalsIgnoreCase(toolName);
        });
        
        String boolStr = context.getArg(1);
        boolean persistant = false;
        try {
            persistant = Boolean.parseBoolean(boolStr);
        } catch (Exception e) { }
        
        if (toolExists) {
            plugin.getSyncServer().removeTool(toolName);
            context.message("Tool " + toolName + " removed");
            
            if (persistant) {
                plugin.getMCCConfig().getDisabledTools().add(toolName);
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
        boolean toolExists = ServiceHandler.getRegisteredTools().stream().anyMatch((tool) -> {
            return tool.equalsIgnoreCase(toolName);
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
                plugin.getSyncServer().addTool(tool);
                context.message("Enabled tool " + toolName);
                
                if (persistant) {
                    plugin.getMCCConfig().getDisabledTools().remove(toolName);
                }
            }
        } else {
            context.message("Tool already enabled!");
        }
    }
}
