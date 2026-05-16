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

import codes.goblom.mcpai.mcp.tools.SharedToolData;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import io.modelcontextprotocol.spec.McpSchema;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;

/**
 *
 * @author Bryan
 */
public class ExecuteCommand extends ToolProvider {
    
    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("command", InputSchemaBuilder.ParameterType.String)
            .addPropertyDescription("command", "The command to run.")
            .toJson();
    
    public ExecuteCommand() {
        super(
                "execute_command",
                "Executes a minecraft command as Bukkit.getConsoleSender() and returns the output of the command if there is one.",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
    String command = context.getArgument("command");
        
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
        
        Bukkit.getLogger().addHandler(SharedToolData.LOG_HANDLER);
        Bukkit.dispatchCommand(SharedToolData.COMMAND_SENDER, command);
        
        if (!SharedToolData.COMMAND_SENDER.hasOutput()) {
            Field f = SharedToolData.LOG_HANDLER.getClass().getDeclaredField("logOutput");
                  f.setAccessible(true);
            StringBuilder logOutputBuilder = (StringBuilder) f.get(SharedToolData.LOG_HANDLER);

            if (logOutputBuilder.isEmpty()) {
                builder.addTextContent("Command produced no readable output. Even after checking logs/latest.log");
            } else {
                builder.addTextContent("Command had no output. Pulled recent output from logs/latest.log");
                builder.addTextContent(logOutputBuilder.toString());
            }
        } else {
            builder.addTextContent(SharedToolData.COMMAND_SENDER.getConsoleOutput());
        }
        
        SharedToolData.COMMAND_SENDER.clearConsoleOutput();
        SharedToolData.LOG_HANDLER.flush();
        Bukkit.getLogger().removeHandler(SharedToolData.LOG_HANDLER);
        
        return builder.build();}
}
