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
package codes.goblom.mcc;

import codes.goblom.factory.command.CommandContext;
import codes.goblom.factory.command.CommandFactory;
import codes.goblom.factory.config.ConfigurationFactory;
import codes.goblom.mcc.commands.TokenCommands;
import codes.goblom.mcc.commands.ToolCommands;
import codes.goblom.mcc.mcp.ServiceHandler;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import tools.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Bryan
 */
public class CommandCenterPlugin extends JavaPlugin {
    
    public JacksonMcpJsonMapper jsonMapper;
    HttpServletStreamableServerTransportProvider transportProvider;
    McpSyncServer syncServer;
    
    Server httpServer;
    ServletContextHandler contextHandler;
    
    protected List<SyncToolSpecification> tools;
    protected CommandCenterConfig ccConfig = new CommandCenterConfig();
    
    @Override
    public void onLoad() {
        CommandFactory mccCommands = new CommandFactory(this) {
            @Override
            public void sendMessage(CommandSender sender, String message) {
                sender.sendMessage(getMCCConfig().getConsolePrefix() + " " + message);
            }

            @Override
            public void executeNoArgsWithStub(CommandContext context) {
                if (context.isTabExecutor()) return;
                
                context.message("Available Commands: ");
                getAvailableCommands().forEach((info) -> {
                    context.message("|  - " + info.name() + (!info.usage().isEmpty() ? " " + info.usage() : ""));
                });
            }
        };
        
        mccCommands.addExecutors(new TokenCommands(this));
        mccCommands.addExecutors(new ToolCommands(this));
        mccCommands.register("mcc");
    }
    
    @Override
    public void onEnable() {
        jsonMapper = new JacksonMcpJsonMapper(
                JsonMapper.builder().build()
        );
        
        ConfigurationFactory.loadConfig(this, ccConfig);
        tools = ServiceHandler.getTools();
        
        transportProvider = HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(jsonMapper)
                .mcpEndpoint(getMCCConfig().getMcpPath())
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
                .tools(tools)
                .build();
        
        this.httpServer = new Server(getMCCConfig().getHttpPort());
        this.contextHandler = new ServletContextHandler();
        
        this.contextHandler.setContextPath("/");
        this.contextHandler.addServlet(new ServletHolder(transportProvider), getMCCConfig().getMcpPath());
        this.contextHandler.addFilter(new FilterHolder(new TokenAuthFilter(this)), getMCCConfig().getMcpPath(), EnumSet.of(DispatcherType.REQUEST));
        
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
    
    public CommandCenterConfig getMCCConfig() {
        return this.ccConfig;
    }
    
    public McpSyncServer getSyncServer() {
        return this.syncServer;
    }
    
    public List<McpServerFeatures.SyncToolSpecification> getTools() {
        return new ArrayList() {
            {
                addAll(tools);
            }
        };
    }
    
    public void debug(Level level, String message) {
        if (getMCCConfig().isDebugMode()) {
            getLogger().log(level, message);
        }
    }
    
    public void debug(Level level, String message, Object... vals) {
        if (getMCCConfig().isDebugMode()) {
            
            if (vals != null) {
                getLogger().log(level, message, vals);
            } else {
                getLogger().log(level, message);
            }
        }
    }
}
