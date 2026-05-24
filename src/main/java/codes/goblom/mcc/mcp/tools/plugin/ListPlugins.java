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
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Bryan
 */
public class ListPlugins extends ToolProvider {
    
    private static String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addOptionalProperty("version", InputSchemaBuilder.ParameterType.Boolean)
            .addOptionalProperty("file", InputSchemaBuilder.ParameterType.Boolean)
            .addOptionalProperty("status", InputSchemaBuilder.ParameterType.Boolean)
            
            .addPropertyDescription("version", "Version of the plugin")
            .addPropertyDescription("file", "Location of the file in /plugins/")
            .addPropertyDescription("status", "Plugin enabled or disabled")
            
            .addPropertyDefault("version", false)
            .addPropertyDefault("file", false)
            .addPropertyDefault("status", false)
            
            .toJson();
    
    public ListPlugins() {
        super(
                "list_plugins",
                "Lists available plugins on the server and if asked can include: version, file and status",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        
        boolean version = context.getArgument("version");
        boolean file = context.getArgument("file");
        boolean status = context.getArgument("status");
        
//        List<String> dataSet = Lists.newArrayList();
//        
//        for (Plugin plugin : plugins) {
//            StringBuilder sb = new StringBuilder(plugin.getName());
//            
//            if (version) {
//                sb.append(" | ");
//                sb.append(plugin.getDescription().getVersion());
//            }
//            
//            if (file) {
//                sb.append(" | ");
//                sb.append(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
//            }
//            
//            if (status) {
//                sb.append(" | ");
//                sb.append(plugin.isEnabled());
//            }
//            
//            dataSet.add(sb.toString());
//        }
//        
//        return McpSchema.CallToolResult.builder().textContent(dataSet).build();

        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
        
        for (Plugin plugin : plugins) {
            builder.addTextContent("Plugin: " + plugin.getName());
            
            if (version) builder.addTextContent("Plugin Version: " + plugin.getDescription().getVersion());
            if (file) builder.addTextContent("Plugin File: " + plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
            if (status) builder.addTextContent(plugin.isEnabled() ? "enabled" : "disabled");
        }
        
        return builder.build();
    }
}
