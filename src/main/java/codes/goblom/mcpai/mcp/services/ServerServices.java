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
package codes.goblom.mcpai.mcp.services;

import codes.goblom.mcpai.McpPlugin;
import codes.goblom.mcpai.WrappedCommandSender;
import codes.goblom.mcpai.mcp.McpServiceProvider.Tool;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Field;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.bukkit.Bukkit;
import codes.goblom.mcpai.mcp.McpServiceProvider;

/**
 *
 * @author Bryan
 */
public class ServerServices implements McpServiceProvider {
    
    protected static final WrappedCommandSender COMMAND_SENDER = new WrappedCommandSender(Bukkit.getConsoleSender());
    protected static final Handler LOG_HANDLER = new Handler() {
        
        StringBuilder logOutput = new StringBuilder();
        
        @Override
        public void publish(LogRecord record) {
            if (record == null) return;
            
            if (record.getMessage() != null) {
                COMMAND_SENDER.sendMessage(record.getMessage()); //This may cause double Strings
                this.logOutput.append(record.getMessage()).append("\n");
            }
        }

        @Override
        public void flush() { 
            this.logOutput = new StringBuilder();
        }

        @Override
        public void close() { }
    };
    
    private final McpPlugin plugin;
    
    public ServerServices(McpPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Tool(
            name = "execute_command",
            description = "Executes a minecraft command as Bukkit.getConsoleSender()",
            inputSchema =
                    """
                    {
                        "type": "object",
                        "required": ["command"],
                        "properties": {
                            "command": { "type": "string" }
                        }
                    }
                    """,
            requiresSyncMethod = true
    )
    public McpSchema.CallToolResult executeCommand(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        String command = (String) request.arguments().get("command");
        
        if (command == null || command.isEmpty()) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Invalid command")
                    .build();
        }
        
        //Strip command of the / if provided
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
        
        Bukkit.getLogger().addHandler(LOG_HANDLER);
        Bukkit.dispatchCommand(COMMAND_SENDER, command);
        
        if (!COMMAND_SENDER.hasOutput()) {
            Field f = LOG_HANDLER.getClass().getDeclaredField("logOutput");
                  f.setAccessible(true);
            StringBuilder logOutputBuilder = (StringBuilder) f.get(LOG_HANDLER);

            if (logOutputBuilder.isEmpty()) {
                builder.addTextContent("Command produced no readable output. Even after checking logs/latest.log");
            } else {
                builder.addTextContent("Command had no output. Pulled recent output from logs/latest.log");
                builder.addTextContent(logOutputBuilder.toString());
            }
        } else {
            builder.addTextContent(COMMAND_SENDER.getConsoleOutput());
        }
        
        COMMAND_SENDER.clearConsoleOutput();
        LOG_HANDLER.flush();
        Bukkit.getLogger().removeHandler(LOG_HANDLER);
        
        return builder.build();
    }
    
    @Tool(
            name = "shutdown_server",
            description = "Shutdown the minecraft server. Will ask for confirmation.",
            inputSchema = 
                """
                {
                    "type": "object",
                    "properties": {
                        "delay": { 
                            "type": "number" ,
                            "description": "Number delay that corresponds to the ticks waited on the server. 20 ticks is 1 second.",
                            "default": "300"
                        },
                        "confirmed": { 
                            "type": "boolean",
                            "description": "Wether the user has confirmed the shutdown. Always ask the user, never assume."
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult shutdownServer(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        boolean confirmed = (boolean) request.arguments().getOrDefault("confirmed", false);
        int delay = (int) request.arguments().getOrDefault("delay", 20 * 5); // 5 seconds
        
        if (!confirmed) {
            return McpSchema.CallToolResult.builder()
                    .addTextContent("Are you sure you want to shutdown the server?")
                    .addTextContent("Please respond with true/false or yes/no")
                    .build();
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.shutdown();
        }, delay);
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Shutdown task successfully added to scheduler. Your client will lose connection soon.")
                .build();
    }
}
