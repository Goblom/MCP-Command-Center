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
package codes.goblom.mcpai.mcp.tools.plugin;

import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Bryan
 */
public class EnablePlugin extends ToolProvider {
    
    private static String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("pluginName", InputSchemaBuilder.ParameterType.String)
            .addPropertyDescription("pluginName", "Name of plugin to enable.")
            .toJson();
    
    public EnablePlugin() {
        super(
                "enable_plugin",
                "Enables a plugin",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String pluginName = context.getArgument("pluginName");
        
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
    
}
