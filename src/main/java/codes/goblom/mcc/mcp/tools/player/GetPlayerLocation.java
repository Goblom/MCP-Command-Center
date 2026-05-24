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
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan
 */
public class GetPlayerLocation extends ToolProvider {
    
    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("player", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("isUUID", InputSchemaBuilder.ParameterType.Boolean)
            .addPropertyDefault("isUUID", false)
            .addPropertyDescription("isUUID", "Wether 'player' is a uuid or a player name")
            .toJson();
    
    public GetPlayerLocation() {
        super(
                "get_player_location",
                """
                Find a players location either by name or uuid
                """,
                INPUT_SCHEMA);
    }

    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String playerStr = context.getArgument("player");
        boolean isUUID = context.getArgument("isUUID", false);
        
        Player player;
        
        if (isUUID) {
            player = Bukkit.getPlayer(UUID.fromString(playerStr));
        } else {
            player = Bukkit.getPlayer(playerStr);
        }
        
        if (player == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Player is not online")
                    .build();
        }
        
        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
                                         builder.isError(false);
                                         builder.addTextContent("World: " + player.getLocation().getWorld().getName());
                                         builder.addTextContent("x: " + player.getLocation().getX());
                                         builder.addTextContent("y: " + player.getLocation().getY());
                                         builder.addTextContent("z: " + player.getLocation().getZ());
                                         builder.addTextContent("yaw: " + player.getLocation().getYaw());
                                         builder.addTextContent("pitch: " + player.getLocation().getPitch());
                                         builder.addTextContent("direction:" + player.getLocation().getDirection());
                                         
        return builder.build();
    }
}
