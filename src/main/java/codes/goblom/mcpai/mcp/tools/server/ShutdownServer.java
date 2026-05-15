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
package codes.goblom.mcpai.mcp.tools.server;

import codes.goblom.mcpai.Configuration;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;

/**
 *
 * @author Bryan
 */
public class ShutdownServer extends ToolProvider {

    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addOptionalProperty("delay", InputSchemaBuilder.ParameterType.Number)
            .addOptionalProperty("confirmed", InputSchemaBuilder.ParameterType.Boolean)
            
            .addPropertyDefault("delay", 300)
            .addPropertyDefault("confirmed", false)
            
            .addPropertyDescription("delay", "Delay that corresponds to the ticks waited on the server. 20 ticks is one second.")
            .addPropertyDescription("confirmed", "Wether the user has confirmed the shutdown. Always ask the user, never assume.")
            .toJson();
    
    public ShutdownServer() {
        super(
                "shutdown_server",
                "Shutdown the minecraft server. Will ask for confirmation.",
                INPUT_SCHEMA
        );
    }
    
    @Override
    public McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws Exception {
        boolean confirmed = (boolean) request.arguments().get("confirmed");
        int delay = (int) request.arguments().get("delay");
        
        if (!confirmed) {
            return McpSchema.CallToolResult.builder()
                .addTextContent("Are you sure you want to shutdown the server?")
                .addTextContent("Please respond with true/false or yes/no")
                .build();
        }
        
        Bukkit.getScheduler().runTaskLater(Configuration.PLUGIN, () -> {
            Bukkit.shutdown();
        }, delay);
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Shutdown task successfully added to scheduler. Your client will lose connection soon.")
                .build();
    }
}
