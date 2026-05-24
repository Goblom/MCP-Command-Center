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
package codes.goblom.mcc.mcp;

import codes.goblom.mcc.mcp.tools.world.SetBlock;
import codes.goblom.mcc.mcp.tools.world.FillArea;
import codes.goblom.mcc.mcp.tools.world.GetBlockTypes;
import codes.goblom.mcc.mcp.tools.world.CreateWorld;
import codes.goblom.mcc.mcp.tools.server.ExecuteCommand;
import codes.goblom.mcc.mcp.tools.server.GetLog;
import codes.goblom.mcc.mcp.tools.server.ShutdownServer;
import codes.goblom.mcc.mcp.tools.server.GetServerVersion;
import codes.goblom.mcc.mcp.tools.plugin.ListPlugins;
import codes.goblom.mcc.mcp.tools.plugin.EnablePlugin;
import codes.goblom.mcc.mcp.tools.plugin.DisablePlugin;
import codes.goblom.mcc.mcp.tools.plugin.LoadPlugin;
import codes.goblom.mcc.mcp.tools.plugin.RestartPlugin;
import codes.goblom.mcc.mcp.tools.player.GetPlayerInfo;
import codes.goblom.mcc.mcp.tools.player.TeleportPlayer;
import codes.goblom.mcc.mcp.tools.player.GetPlayerLocation;
import codes.goblom.mcc.mcp.tools.player.GetLoggedInPlayers;
import codes.goblom.mcc.mcp.tools.files.ListDirectory;
import codes.goblom.mcc.mcp.tools.files.ReadFile;
import codes.goblom.mcc.mcp.tools.files.WriteFile;
import codes.goblom.mcc.mcp.tools.env.ExecuteLua;
import codes.goblom.mcc.mcp.tools.entity.GetEntityTypes;
import codes.goblom.mcc.mcp.tools.entity.SpawnEntityAtLocation;
import codes.goblom.mcc.CommandCenterPlugin;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Bryan
 */
public class ServiceHandler {
    
    // TODO: Maybe a better way to do this, but i like it for now
    protected static CommandCenterPlugin PLUGIN;
    static {
        try {
            PLUGIN = (CommandCenterPlugin) JavaPlugin.getProvidingPlugin(ServiceHandler.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static final Map<String, McpServerFeatures.SyncToolSpecification> REGISTERED_TOOLS = Maps.newHashMap();
    static final List<ToolProvider> TOOLS = new ArrayList() {
        {
            add(new GetPlayerInfo());
            add(new GetLoggedInPlayers());
            add(new GetPlayerLocation());
            add(new TeleportPlayer());
            
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
            add(new GetLog());
            
            add(new CreateWorld());
            add(new GetBlockTypes());
            add(new SetBlock());
            add(new FillArea());
            
            add(new ListDirectory());
            add(new ReadFile());
            add(new WriteFile());
            
            try {
                if (PLUGIN.getMCCConfig().isLuaEnabled()) {
                    add(new ExecuteLua());
                }
            } catch (Exception e) { 
                e.addSuppressed(e);
            }
        }
    };
    
    public static List<McpServerFeatures.SyncToolSpecification> getTools() {
        List<McpServerFeatures.SyncToolSpecification> wrappedTools = Lists.newArrayList();
        
        TOOLS.forEach((tool) -> {
            McpSchema.Tool.Builder builder = McpSchema.Tool.builder();
            
            String nameUsed;
            if (tool.getName() != null && !tool.getName().isEmpty()) {
                builder.name(tool.getName());
                nameUsed = tool.getName();
            } else {
                // Using class name with sometimes result in your LLM not recognizing it as a tool the first try
                builder.name(tool.getClass().getSimpleName());
                nameUsed = tool.getClass().getSimpleName();
            }
            
            if (tool.getInputSchema() != null && !tool.getInputSchema().isEmpty()) {
                builder.inputSchema(PLUGIN.jsonMapper, tool.getInputSchema());
            }
            
            if (tool.getOutputSchema() != null && !tool.getOutputSchema().isEmpty()) {
                builder.outputSchema(PLUGIN.jsonMapper, tool.getOutputSchema());
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
            
            if (!PLUGIN.getMCCConfig().getDisabledTools().contains(nameUsed)) {
                wrappedTools.add(wrappedTool);
            }
            
            REGISTERED_TOOLS.put(nameUsed, wrappedTool);
            
            PLUGIN.debug(Level.INFO, "Registered Tool {0}:{1}", tool.getClass().getSimpleName(), tool.getName());
        });
        
        return wrappedTools;
    }
    
    public static Set<String> getRegisteredTools() {
        return REGISTERED_TOOLS.keySet();
    }
    
    public static McpServerFeatures.SyncToolSpecification getRegisteredToolObject(String name) {
        return REGISTERED_TOOLS.get(name);
    }
}
