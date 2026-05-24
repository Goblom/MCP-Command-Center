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
package codes.goblom.mcc.mcp.providers;

import codes.goblom.mcc.mcp.ServiceHandler;
import codes.goblom.mcc.mcp.ServiceProvider;
import codes.goblom.mcc.mcp.context.McpToolContext;
import com.google.gson.Gson;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author Bryan
 */
public abstract class ToolProvider implements ServiceProvider<McpSchema.CallToolResult, McpSchema.CallToolRequest> {
    
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ToolAnnotation {
        String title();
        boolean readOnlyHint();
        boolean destructiveHint();
        boolean idempotentHint();
        boolean openWorldHint();
        boolean returnDirect();
    }
    
    private String name;
    private final String description;
    private String inputSchema;
    
    public ToolProvider(String description) {
        this.description = description;
    }
    
    public ToolProvider(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    public ToolProvider(String name, String description, String inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public String getInputSchema() {
        return this.inputSchema;
    }
    
    public String getOutputSchema() {
        return null;
    }
    
    public Map<String, Object> getMeta() {
        return null;
    }

    @Override
    public final boolean hasPermission(String token) {
        List<String> permissions = getPlugin().getMCCConfig().getTokenPermissions().get(token);
        
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        
        if (permissions.contains("tools.all") || permissions.contains("tools.*")) return true;
        return !permissions.contains("-tools." + getName()) && permissions.contains("tools." + getName());
    }
    
    @Override
    public final McpSchema.CallToolResult apply(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        getPlugin().debug(Level.INFO, "Calling {0} with args[{1}]", getName(), new Gson().toJson(request.arguments()));
        
        String token = ((String[]) exchange.transportContext().get("token"))[0];
//        List<String> permissions = Configuration.TOKEN_PERMISSIONS.get(token);
//        
//        if (permissions == null || permissions.isEmpty()) {
//            return McpSchema.CallToolResult.builder()
//                    .isError(true)
//                    .addTextContent("Error: Invalid Permissions")
//                    .build();
//        }
        
//        if (!permissions.contains("tools.all") && !permissions.contains("tools." + getName())) {
        if (!hasPermission(token)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: No Permissinos.")
                    .build();
        }
        
        try {
            McpToolContext toolContext = new McpToolContext(exchange, request);
            Method execute = getClass().getDeclaredMethod("execute", McpToolContext.class);
            
            if (execute != null && execute.isAnnotationPresent(RequireSyncMethod.class)) {
                RequireSyncMethod syncMethod = execute.getAnnotation(RequireSyncMethod.class);

                if (syncMethod != null) {
                    long timeout = syncMethod.timeout();
                    Future<McpSchema.CallToolResult> future = Bukkit.getScheduler().callSyncMethod(getPlugin(), () -> execute(toolContext));
                    
                    if (timeout <= 0) {
                        return future.get();
                    }
                    
                    return future.get(timeout, TimeUnit.MILLISECONDS);
                }
            }
            
            return execute(toolContext);
        } catch (Exception e) {
            e.printStackTrace();
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Problem executing " + getName() + " - " + e.getMessage())
                    .build();
        }
    }
    
    
    public abstract McpSchema.CallToolResult execute(McpToolContext context) throws Exception;
}
