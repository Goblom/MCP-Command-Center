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
import codes.goblom.mcpai.mcp.McpServiceProvider.Prompt;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.Content;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author Bryan
 */
public class WrappedSyncPromptSpecification implements BiFunction<McpSyncServerExchange, McpSchema.GetPromptRequest, McpSchema.GetPromptResult>{

    private final McpPlugin plugin;
    private final McpServiceProvider serviceProvider;
    private final McpServiceProvider.Prompt prompt;
    private final Method method;

    public WrappedSyncPromptSpecification(McpPlugin plugin, McpServiceProvider serviceProvider, Prompt prompt, Method method) {
        this.plugin = plugin;
        this.serviceProvider = serviceProvider;
        this.prompt = prompt;
        this.method = method;
    }
    
    @Override
    public McpSchema.GetPromptResult apply(McpSyncServerExchange exchange, McpSchema.GetPromptRequest request) {
        plugin.debug(Level.INFO, "Calling {0} prompt with args[{1}]", prompt.name(), new Gson().toJson(request));
        
        String token = ((String[]) exchange.transportContext().get("token"))[0];
        List<String> permissions = Configuration.TOKEN_PERMISSIONS.get(token);
        
        if (permissions == null || permissions.isEmpty()) {
            return new McpSchema.GetPromptResult("invalid_permissions", PromptMessageBuilder.builder().add(McpSchema.Role.USER, "You have invalid permissions.").build());
        }
        
        //Append "prompt." to the permissions to differentiate between prompts and tools
        if (!permissions.contains("prompt.all") && !permissions.contains("prompt." + prompt.name())) {
//            plugin.debug(Level.INFO, "Permissions Found - {0}", Arrays.toString(permissions.toArray()));
            return new McpSchema.GetPromptResult("no_permissions", PromptMessageBuilder.builder().add(McpSchema.Role.USER, "No Permissions").build());
        }
        
        if (prompt.requiresSyncMethod()) {
            Future<McpSchema.GetPromptResult> future = Bukkit.getScheduler().callSyncMethod(plugin, () -> (McpSchema.GetPromptResult) ServiceHandler.invokeMethod(plugin, serviceProvider, method, exchange, request));
            
            try {
                if (prompt.syncMethodTimeout() <= 0) {
                    return future.get();
                }
                
                return future.get(prompt.syncMethodTimeout(), TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                return new McpSchema.GetPromptResult("sync_method_error", PromptMessageBuilder.builder().add(McpSchema.Role.USER, "Problem with future").build());
            }
        }
        
        return (McpSchema.GetPromptResult) ServiceHandler.invokeMethod(plugin, serviceProvider, method, exchange, request);
    }
    
    
    protected List<McpSchema.PromptMessage> promptBuilder(Map<McpSchema.Role, McpSchema.TextContent> map) {
        List<McpSchema.PromptMessage> list = Lists.newArrayList();
        
        map.forEach((role, message) -> {
            list.add(new McpSchema.PromptMessage(role, message));
        });
        
        return list;
    }
    
    //TODO: Add support for other messages like...
    //      ImageContent, AudioContent, EmbeddedResource, ResourceLink
    static class PromptMessageBuilder {
        
        
        static PromptMessageBuilder builder() {
            return new PromptMessageBuilder();
        }
        
        List<McpSchema.PromptMessage> messages = Lists.newArrayList();
        
        public PromptMessageBuilder add(McpSchema.Role role, String message) {
            return add(role, new McpSchema.TextContent(message));
        }
        
        public PromptMessageBuilder add(McpSchema.Role role, Content content) {
            messages.add(new McpSchema.PromptMessage(role, content));
            return this;
        }
        
        public List<McpSchema.PromptMessage> build() {
            return messages;
        }
    }
}
