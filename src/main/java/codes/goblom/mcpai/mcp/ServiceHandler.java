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

import codes.goblom.mcpai.McpPlugin;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Bryan
 */
public class ServiceHandler {
    
    private ServiceHandler() { }
    
    protected static Object invokeMethod(McpPlugin plugin, McpServiceProvider serviceProvider, Method method, McpSyncServerExchange exchange, Object arguments) {
        Object result;
        try {
            result =  method.invoke(serviceProvider, exchange, arguments);
        } catch (Exception e) {
            result = McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addContent(new McpSchema.TextContent("Error: " + e.getMessage()))
                    .build();
            e.printStackTrace();
        }
        
        plugin.debug(Level.INFO, "\n\n" + new Gson().toJson(result) + "\n\n");
        
        return result;
    }
        
    public static List<McpServerFeatures.SyncToolSpecification> findTools(McpPlugin plugin, McpServiceProvider toolObject) {
        Class clazz = toolObject.getClass();
        List<McpServerFeatures.SyncToolSpecification> tools = Lists.newArrayList();
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(McpServiceProvider.Tool.class)) {
                McpServiceProvider.Tool tool = method.getAnnotation(McpServiceProvider.Tool.class);
                plugin.debug(Level.WARNING, "Found Tool - {0} at {1}:{2}", tool.name(), clazz.getSimpleName(), method.getName());
                
                McpSchema.Tool.Builder toolBuilder = new McpSchema.Tool.Builder();
                                       toolBuilder.description(tool.description());
                                       
                if (tool.name() != null && !tool.name().isEmpty()) {
                    toolBuilder.name(tool.name());
                } else {
                    toolBuilder.name(method.getName()); // Using method name will sometimes result in your LLM not recognizing it as a tool the first try
                }
                
                if (tool.inputSchema() != null && !tool.inputSchema().isEmpty()) {
                    toolBuilder.inputSchema(plugin.jsonMapper, tool.inputSchema());
                }
                
                if (tool.outputSchema() != null && !tool.outputSchema().isEmpty()) {
                    toolBuilder.outputSchema(plugin.jsonMapper, tool.outputSchema());
                }
                
                if (tool.meta().length > 0) {
                    Map<String, Object> metaMap = Maps.newHashMap();
                    
                    for (McpServiceProvider.Attribute metaValue : tool.meta()) {
                        metaMap.put(metaValue.key(), metaValue.value());
                    }
                    
                    toolBuilder.meta(metaMap);
                }
                
                if (tool.toolAnnotations().length == 6) {
                    String title = null;
                    boolean readOnlyHint = false;
                    boolean destructiveHint = false;
                    boolean idempotentHint = false;
                    boolean openWorldHint = false;
                    boolean returnDirect = false;
                    
                    boolean error = false;
                    
                    for (McpServiceProvider.Attribute attribute : tool.toolAnnotations()) {
                        switch (attribute.key()) {
                            case "title":
                                title = attribute.value();
                                continue;
                            case "readOnlyHint":
                                readOnlyHint = Boolean.getBoolean(attribute.value());
                                continue;
                            case "destructiveHint":
                                 destructiveHint = Boolean.getBoolean(attribute.value());
                                continue;
                            case "idempotentHint":
                                 idempotentHint = Boolean.getBoolean(attribute.value());
                                continue;
                            case "openWorldHint":
                                 openWorldHint = Boolean.getBoolean(attribute.value());
                                continue;
                            case "returnDirect":
                                 returnDirect = Boolean.getBoolean(attribute.value());
                                continue;
                            default:
                                error = true;
                                plugin.debug(Level.WARNING, "Improper ToolAnnotation found on {0} with key of {1}", tool.name(), attribute.key());
                        }
                    }
                    
                    if (!error) {
                        toolBuilder.annotations(new McpSchema.ToolAnnotations(title, readOnlyHint, destructiveHint, idempotentHint, openWorldHint, returnDirect));
                    }
                } else if (tool.toolAnnotations().length != 0) {
                    plugin.debug(Level.WARNING, "Tool {0} is not properly setup for ToolAnnotations.", tool.name());
                }
                
                McpServerFeatures.SyncToolSpecification syncToolWrapper = new McpServerFeatures.SyncToolSpecification(
                        toolBuilder.build(),
                        new WrappedSyncToolSpecification(plugin, toolObject, tool, method)
                );
                
                tools.add(syncToolWrapper);
            }
        }
        
        return tools;
    }
}
