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
package codes.goblom.mcpai;

import codes.goblom.mcpai.mcp.ServiceHandler;
import codes.goblom.mcpai.mcp.services.EntityServices;
import codes.goblom.mcpai.mcp.services.FileServices;
import codes.goblom.mcpai.mcp.services.PlayerServices;
import codes.goblom.mcpai.mcp.services.PluginServices;
import codes.goblom.mcpai.mcp.services.ServerServices;
import codes.goblom.mcpai.mcp.services.WorldServices;
import com.google.common.collect.Lists;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import tools.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Bryan
 */
public class McpPlugin extends JavaPlugin {
    
    public JacksonMcpJsonMapper jsonMapper;
    HttpServletStreamableServerTransportProvider transportProvider;
    McpSyncServer syncServer;
    
    Server httpServer;
    ServletContextHandler contextHandler;
    
    ServiceHandler toolHandler;
    
    @Override
    public void onEnable() {        
        jsonMapper = new JacksonMcpJsonMapper(
                JsonMapper.builder().build()
        );
        
        List<McpServerFeatures.SyncToolSpecification> foundTools = Lists.newArrayList();
                                                      foundTools.addAll(ServiceHandler.findTools(this, new PlayerServices()));
                                                      foundTools.addAll(ServiceHandler.findTools(this, new EntityServices()));
                                                      foundTools.addAll(ServiceHandler.findTools(this, new PluginServices(this)));
                                                      foundTools.addAll(ServiceHandler.findTools(this, new FileServices(this)));
                                                      foundTools.addAll(ServiceHandler.findTools(this, new ServerServices(this)));
                                                      foundTools.addAll(ServiceHandler.findTools(this, new WorldServices()));
        List<McpServerFeatures.SyncPromptSpecification> foundPrompts = Lists.newArrayList();
        
        transportProvider = HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint(Configuration.MCP_PATH)
                .contextExtractor(new McpTransportContextExtractor<HttpServletRequest>() {
                    @Override
                    public McpTransportContext extract(HttpServletRequest request) {
                        Map<String, Object> paramaterMap = new HashMap();
                        request.getParameterMap().forEach((name, args) -> {
//                            debug(Level.INFO, "Found Parameter {0} with value of {1}", name, Arrays.toString(args));
                            
                            paramaterMap.put(name, args);
                        });
                        
                        return McpTransportContext.create(paramaterMap);
                    }
                })
//                .securityValidator(new ServerTransportSecurityValidator() {
//                    @Override
//                    public void validateHeaders(Map<String, List<String>> headers) throws ServerTransportSecurityException {
//                        String token = headers.get("token").get(0);
//                        
//                        // Token Compute
//                        // This uses headers instead of http get request. 
//                        // Prefer to use the HttpFilter instead.
//                    }
//                })
                .build();
        
        
        syncServer = McpServer.sync(transportProvider)
                .jsonMapper(jsonMapper)
                .jsonSchemaValidator(new DefaultJsonSchemaValidator(jsonMapper.getJsonMapper()))
                .serverInfo(this.getDescription().getName(), this.getDescription().getVersion())
                .capabilities(ServerCapabilities.builder()
                        .tools(true)
                        .prompts(true)
//                        .logging()
                        .build())
                .tools(foundTools)
                .prompts(foundPrompts)
                .build();
        
        this.httpServer = new Server(Configuration.HTTP_PORT);
        this.contextHandler = new ServletContextHandler();
        
        this.contextHandler.setContextPath("/");
        this.contextHandler.addServlet(new ServletHolder(transportProvider), Configuration.MCP_PATH);
        this.contextHandler.addFilter(new FilterHolder(new TokenAuthFilter(this)), Configuration.MCP_PATH, EnumSet.of(DispatcherType.REQUEST));
        
        this.httpServer.setHandler(contextHandler);
        
        try {
            this.httpServer.start();
        } catch (Exception e) {
            debug(Level.SEVERE, "Severe error when trying to start HTTP server. Plugin shutting down.");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        try {
            this.syncServer.closeGracefully();
            this.httpServer.stop();
        } catch (Exception e) {
            debug(Level.WARNING, "Plugin was unable to gracefully shutdown MCP or HTTP server. Please restart server.");
            e.printStackTrace();
        }
    }
    
    public void debug(Level level, String message) {
        if (Configuration.DEBUG) {
            getLogger().log(level, message);
        }
    }
    
    public void debug(Level level, String message, Object... vals) {
        if (Configuration.DEBUG) {
            
            if (vals != null) {
                getLogger().log(level, message, vals);
            } else {
                getLogger().log(level, message);
            }
        }
    }
}
