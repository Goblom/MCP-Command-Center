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
package codes.goblom.mcc.mcp.tools.player;

import codes.goblom.mcc.mcp.providers.ToolProvider;
import codes.goblom.mcc.mcp.InputSchemaBuilder;
import codes.goblom.mcc.mcp.context.McpToolContext;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan
 */
public class GetLoggedInPlayers extends ToolProvider {

    private static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addOptionalProperty("uuid", InputSchemaBuilder.ParameterType.Boolean)
            .addPropertyDescription("uuid", "Wether to return the data as uuid or as player name")
            .addPropertyDefault("uuid", false)
            .toString();
    
    public GetLoggedInPlayers() {
        super(
                "get_logged_in_players",
                "Get a list of all logged in players by name or uuid",
                INPUT_SCHEMA
        );
    }
    
    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        boolean uuid = context.getArgument("uuid");
        
        return McpSchema.CallToolResult.builder().content(
                Bukkit.getOnlinePlayers().stream().map((Player p) -> (McpSchema.Content) new McpSchema.TextContent(uuid ? p.getUniqueId().toString() : p.getName())).toList()
        ).build();
    }
    
}
