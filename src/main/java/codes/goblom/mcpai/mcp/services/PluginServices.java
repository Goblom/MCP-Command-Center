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
package codes.goblom.mcpai.mcp.services;

import codes.goblom.mcpai.Configuration;
import codes.goblom.mcpai.McpPlugin;
import com.google.common.collect.Lists;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.File;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import codes.goblom.mcpai.mcp.McpServiceProvider;

/**
 *
 * @author Bryan
 */
public class PluginServices implements McpServiceProvider {
    
    private final McpPlugin plugin;
    
    public PluginServices(McpPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Tool(
            name = "list_plugins",
            description =
                    """
                    List plugins available on the server
                    
                    List Plugin Rules:
                    - 'version' property true/false to display the version number
                    - 'file' property true/famse to display file name
                    - 'status' property true/false to display enabled/disabled status
                    """,
            inputSchema =
                """
                {
                    "type": "object",
                    "properties": {
                        "version": { "type": "boolean" },
                        "file": { "type": "boolean" },
                        "status": { "type": "boolean" }
                    }
                }
                """
    )
    public McpSchema.CallToolResult listPlugins(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        
        boolean version = (boolean) request.arguments().getOrDefault("version", false);
        boolean file = (boolean) request.arguments().getOrDefault("file", false);
        boolean status = (boolean) request.arguments().getOrDefault("status", false);
        
        List<String> dataSet = Lists.newArrayList();
        
        for (Plugin plugin : plugins) {
            StringBuilder sb = new StringBuilder(plugin.getName());
            
            if (version) {
                sb.append(" | ");
                sb.append(plugin.getDescription().getVersion());
            }
            
            if (file) {
                sb.append(" | ");
                sb.append(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
            }
            
            if (status) {
                sb.append(" | ");
                sb.append(plugin.isEnabled());
            }
            
            dataSet.add(sb.toString());
        }
        
        return McpSchema.CallToolResult.builder().textContent(dataSet).build();
    }
    
    @Tool(
            name = "enable_plugin",
            description = "Enable a named plugin",
            inputSchema =
                """
                {
                    "type": "object",
                    "required": ["pluginName"],
                    "properties": {
                        "pluginName": {
                            "type": "string"
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult enablePlugin(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        String pluginName = (String) request.arguments().get("pluginName");
        
        if (pluginName == null || pluginName.isEmpty()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: pluginName parameter is empty").build();
        }
        
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        
        if (plugin == null) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Plugin " + pluginName + " does not exists or is not found by the server.").build();
        }
        
        Bukkit.getPluginManager().enablePlugin(plugin);
        
        boolean enabled = plugin.isEnabled();
        
        return McpSchema.CallToolResult.builder().isError(enabled).addTextContent(enabled ? "Success! Plugin " + pluginName + " is reporting as enabled." : "Error: " + pluginName + " was not enabled.").build();
    }
    
    @Tool(
            name = "disable_plugin",
            description = "Disable a named plugin",
            inputSchema =
                """
                {
                    "type": "object",
                    "required": ["pluginName"],
                    "properties": {
                        "pluginName": {
                            "type": "string"
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult disablePlugin(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        String pluginName = (String) request.arguments().get("pluginName");
        
        if (pluginName == null || pluginName.isEmpty()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: pluginName parameter is empty").build();
        }
        
        if (pluginName.equals(plugin.getDescription().getName())) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Cannot disable the MCP Plugin").build();
        }
        
        Plugin foundPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        
        if (foundPlugin == null) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Plugin " + pluginName + " does not exists or is not found by the server.").build();
        }
        
        Bukkit.getPluginManager().disablePlugin(foundPlugin);
        
        boolean disabled = !foundPlugin.isEnabled();
        
        return McpSchema.CallToolResult.builder().isError(disabled).addTextContent(disabled ? "Success! Plugin " + pluginName + " is reporting as disabled." : "Error: " + pluginName + " was not enabled.").build();
    }
    
    @Tool(
            name = "restart_plugin",
            description = "Restarts a named plugin",
            inputSchema =
                """
                {
                    "type": "object",
                    "required": ["pluginName"],
                    "properties": {
                        "pluginName": {
                            "type": "string"
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult restartPlugin(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        String pluginName = (String) request.arguments().get("pluginName");
        
        if (pluginName == null || pluginName.isEmpty()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: pluginName parameter is empty").build();
        }
        
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        
        if (plugin == null) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Plugin " + pluginName + " does not exists or is not found by the server.").build();
        }
        
        Bukkit.getPluginManager().disablePlugin(plugin);
        Bukkit.getPluginManager().enablePlugin(plugin);
        
        boolean enabled = plugin.isEnabled();
        
        return McpSchema.CallToolResult.builder().isError(enabled).addTextContent(enabled ? "Success! Plugin " + pluginName + " is reporting as enabled." : "Error: " + pluginName + " was not enabled.").build();
    }
    
    @Tool(
            name = "load_plugin",
            description = "Load a plugin from a file in the /plugins/ directory",
            inputSchema =
                """
                {
                    "type": "object",
                    "required": ["fileName"],
                    "properties": {
                        "fileName": {
                            "type": "string"
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult loadPlugin(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws InvalidPluginException, InvalidDescriptionException {
        String fileName = (String) request.arguments().get("fileName");
        
        if (fileName == null || fileName.isEmpty()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: fileName parameter is empty").build();
        }
        
        File file = new File(Configuration.SERVER_DIR.toFile(), "/plugins/" + fileName);
        if (!file.exists()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: " + fileName + " does not exist.").build();
        }
        
        Plugin plugin = Bukkit.getPluginManager().loadPlugin(file);
        
        if (plugin == null) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Plugin " + fileName + " does not exists or is not found by the server.").build();
        }
        
        boolean enabled = plugin.isEnabled();
        return McpSchema.CallToolResult.builder().isError(enabled).addTextContent(enabled ? "Plugin " + plugin.getName() + " successfully loaded" : "Error: " + fileName + " was not enabled.").build();
    }
    
    //lookup commands a plugin has
    //public void lookupCommands() {}
    
    //Maybe not do this one
    //public void lookupMemoryUsage() {}
    
    //Outside API Requests
    //public void installPlugin() {}
    //public void uninstallPlugin() {}
    //public void updatePlugin() {}
    
    
}
