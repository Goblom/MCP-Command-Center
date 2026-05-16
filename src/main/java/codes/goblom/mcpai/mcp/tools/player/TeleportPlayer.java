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
package codes.goblom.mcpai.mcp.tools.player;

import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan
 */
public class TeleportPlayer extends ToolProvider {
    
    private static String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("player", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("uuid", InputSchemaBuilder.ParameterType.Boolean)
            
            .addRequiredProperty("loc-world", InputSchemaBuilder.ParameterType.String)
            .addRequiredProperty("loc-x", InputSchemaBuilder.ParameterType.String)
            .addRequiredProperty("loc-y", InputSchemaBuilder.ParameterType.String)
            .addRequiredProperty("loc-z", InputSchemaBuilder.ParameterType.String)
            
            .addPropertyDescription("uuid", "Wether player os a uuid or a name")
            
            .toJson();
    
    public TeleportPlayer() {
        super(
                "teleport_player",
                "Teleports a player to a location",
                INPUT_SCHEMA
        );
    }

    @Override
    @RequireSyncMethod
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String name = context.getArgument("player");
        boolean uuid = context.getArgument("uuid", false);
        String worldName = context.getArgument("loc-world");
        int x = context.getArgument("loc-x");
        int y = context.getArgument("loc-y");
        int z = context.getArgument("loc-z");
        
        Player player;
        
        if (uuid) {
            player = Bukkit.getPlayer(UUID.fromString(name));
        } else {
            player = Bukkit.getPlayer(name);
        }
        
        if (player == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Player does not exist.")
                    .build();
        }
        
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: World does not exist.")
                    .build();
        }
        
        Location location = new Location(world, x, y, z);
        player.teleport(location);
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Success!")
                .build();
    }
}
