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
import codes.goblom.mcpai.mcp.providers.PromptProvider;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.tools.entity.*;
import codes.goblom.mcpai.mcp.tools.files.*;
import codes.goblom.mcpai.mcp.tools.player.*;
import codes.goblom.mcpai.mcp.tools.plugin.*;
import codes.goblom.mcpai.mcp.tools.server.*;
import codes.goblom.mcpai.mcp.tools.world.*;
import com.google.common.collect.Lists;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author Bryan
 */
public class ServiceHandler {
    
    static final List<ToolProvider> TOOLS = new ArrayList() {
        {
            add(new GetPlayerInfo());
            add(new GetLoggedInPlayers());
            add(new GetPlayerLocation());
            
            add(new GetEntityTypes());
            add(new SpawnEntityAtLocation());
            
            add(new EnablePlugin());
            add(new DisablePlugin());
            add(new ListPlugins());
            add(new LoadPlugin());
            add(new RestartPlugin());
            
            add(new ShutdownServer());
            add(new ExecuteCommand());
            add(new GetServerVersion());
//            add(new GetLog());
            
            add(new CreateWorld());
            add(new GetBlockTypes());
            add(new SetBlock());
            add(new FillArea());
            
            add(new ListDirectory());
            add(new ReadFile());
            add(new WriteFile());
        }
    };
    
    static final List<PromptProvider> PROMPTS = new ArrayList() {
        {
        
        }
    };
    
    public static List<McpServerFeatures.SyncToolSpecification> getTools() {
        List<McpServerFeatures.SyncToolSpecification> wrappedTools = Lists.newArrayList();
        
        TOOLS.forEach((tool) -> {
            McpSchema.Tool.Builder builder = McpSchema.Tool.builder();
            
            if (tool.getName() != null && !tool.getName().isEmpty()) {
                builder.name(tool.getName());
            } else {
                // Using class name with sometimes result in your LLM not recognizing it as a tool the first try
                builder.name(tool.getClass().getSimpleName());
            }
            
            if (tool.getInputSchema() != null && !tool.getInputSchema().isEmpty()) {
                builder.inputSchema(Configuration.PLUGIN.jsonMapper, tool.getInputSchema());
            }
            
            if (tool.getOutputSchema() != null && !tool.getOutputSchema().isEmpty()) {
                builder.outputSchema(Configuration.PLUGIN.jsonMapper, tool.getOutputSchema());
            }
            
            if (tool.getMeta() != null && !tool.getMeta().isEmpty()) {
                builder.meta(tool.getMeta());
            }
            
            if (tool.getClass().isAnnotationPresent(ToolProvider.ToolAnnotation.class)) {
                ToolProvider.ToolAnnotation toolAnnotation = tool.getClass().getAnnotation(ToolProvider.ToolAnnotation.class);
                
                builder.annotations(new McpSchema.ToolAnnotations(
                        toolAnnotation.title(), 
                        toolAnnotation.readOnlyHint(), 
                        toolAnnotation.destructiveHint(), 
                        toolAnnotation.idempotentHint(), 
                        toolAnnotation.openWorldHint(), 
                        toolAnnotation.returnDirect()));
            }
            
            McpServerFeatures.SyncToolSpecification wrappedTool = new McpServerFeatures.SyncToolSpecification(
                    builder.build(),
                    tool
            );
            
            wrappedTools.add(wrappedTool);
            
            Configuration.PLUGIN.debug(Level.INFO, "Registered Tool {0}:{1}", tool.getClass().getSimpleName(), tool.getName());
        });
        
        return wrappedTools;
    }
    
    /**
     * @deprecated Not Implemented
     */
    @Deprecated
    public static List<McpServerFeatures.SyncPromptSpecification> getPrompts() {
        return null;
    }
}
