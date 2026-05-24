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
package codes.goblom.mcc.mcp.tools.plugin;

import codes.goblom.mcc.mcp.providers.ToolProvider;
import codes.goblom.mcc.mcp.InputSchemaBuilder;
import codes.goblom.mcc.mcp.context.McpToolContext;
import codes.goblom.mcc.mcp.tools.SharedToolData;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Bryan
 */
public class LoadPlugin extends ToolProvider {
    
    private static String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("fileName", InputSchemaBuilder.ParameterType.String)
            .addPropertyDescription("fileName", "Name of plugin to load from a File in the /plugins/ directory.")
            .toJson();
    
    public LoadPlugin() {
        super(
                "load_plugin",
                "Loads a plugin",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String fileName = context.getArgument("fileName");
        
        if (fileName == null || fileName.isEmpty()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: fileName parameter is empty").build();
        }
        
        File file = new File(SharedToolData.SERVER_DIR.toFile(), "/plugins/" + fileName);
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
    
}
