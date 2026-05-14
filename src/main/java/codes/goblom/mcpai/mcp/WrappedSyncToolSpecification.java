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
package codes.goblom.mcpai.mcp;

import codes.goblom.mcpai.Configuration;
import codes.goblom.mcpai.McpPlugin;
import com.google.gson.Gson;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author Bryan
 */
public class WrappedSyncToolSpecification implements BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> {

    private final McpPlugin plugin;
    private final McpServiceProvider serviceProvider;
    private final McpServiceProvider.Tool tool;
    private final Method method;

    public WrappedSyncToolSpecification(McpPlugin plugin, McpServiceProvider toolHandler, McpServiceProvider.Tool tool, Method method) {
        this.plugin = plugin;
        this.serviceProvider = toolHandler;
        this.tool = tool;
        this.method = method;
    }

    @Override
    public CallToolResult apply(McpSyncServerExchange exchange, CallToolRequest arguments) {
        plugin.debug(Level.INFO, "Calling " + tool.name() + " with args [" + new Gson().toJson(arguments) + "]");
        
        String token = ((String[]) exchange.transportContext().get("token"))[0];
        List<String> permissions = Configuration.TOKEN_PERMISSIONS.get(token);
        
//        plugin.debug(Level.INFO, "Trying token permission for {0}", token);
        
        if (permissions == null || permissions.isEmpty()) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Invalid Token Permissions").build();
        }
        
        //Append "tools." to the permissions to differentiate between prompts and tools
        if (!permissions.contains("tools.all") && !permissions.contains("tools." + tool.name())) {
//            plugin.debug(Level.INFO, "Permissions Found - {0}", Arrays.toString(permissions.toArray()));
            
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: No Permissions").build();
        }
        
        if (tool.requiresSyncMethod()) {
            Future<McpSchema.CallToolResult> future = Bukkit.getScheduler().callSyncMethod(plugin, () -> (McpSchema.CallToolResult) ServiceHandler.invokeMethod(plugin, serviceProvider, method, exchange, arguments));
            
            try {
                if (tool.syncMethodTimeout() <= 0) {
                    return future.get();
                }

                return future.get(tool.syncMethodTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                return McpSchema.CallToolResult.builder()
                        .isError(true)
                        .addTextContent("Error: Problem with Future - " + e.getMessage())
                        .build();
                
            }
        }
        
        return (McpSchema.CallToolResult) ServiceHandler.invokeMethod(plugin, serviceProvider, method, exchange, arguments);
    }
}
