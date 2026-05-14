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

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import codes.goblom.mcpai.mcp.McpServiceProvider;

/**
 *
 * @author Bryan
 */
public class PlayerServices implements McpServiceProvider {

    @Tool(
            name = "get_logged_in_players_name",
            description = "Get a list of all players currently online by t heir name"
    )
    public McpSchema.CallToolResult getLoggedInPlayersName(McpSyncServerExchange exchange, CallToolRequest arguments) {
        return new CallToolResult.Builder().content(
                Bukkit.getOnlinePlayers().stream().map((Player p) -> (McpSchema.Content) new McpSchema.TextContent(p.getName())).toList()
        ).isError(false).build();
    }

    @Tool(
            name = "get_logged_in_players_uuid",
            description = "Get a list of all players currently online by their uuid"
    )
    public McpSchema.CallToolResult getLoggedInPlayersUUID(McpSyncServerExchange exchange, CallToolRequest arguments) {
        return new CallToolResult.Builder().content(
                Bukkit.getOnlinePlayers().stream().map((Player p) -> (McpSchema.Content) new McpSchema.TextContent(p.getUniqueId().toString())).toList()
        ).isError(false).build();
    }

    @Tool(
            name = "get_player_location_by_name",
            description = "find a players location",
            inputSchema =
                """
                {
                    "type": "object",
                    "required": ["playerName"],
                    "properties": {
                        "playerName": {
                            "type": "string"
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult getPlayerLocationByName(McpSyncServerExchange exchange, CallToolRequest request) {
        String playerName = (String) request.arguments().get("playerName");
        Player player = Bukkit.getPlayer(playerName);
        
        if (player == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Player is not online.")
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
    
    @Tool(
            name = "get_player_info",
            description = "Get all information for a specific  player",
            inputSchema =
                """
                {
                    "type": "object",
                    "required": ["playerName"],
                    "properties": {
                        "playerName": {
                            "type": "string"
                        }
                    }
                }
                """
    )
    public McpSchema.CallToolResult getPlayerInfo(McpSyncServerExchange exchange, CallToolRequest request) {
        String playerName = (String) request.arguments().get("playerName");
        Player player = Bukkit.getPlayer(playerName);
        
        if (player == null) {
            return McpSchema.CallToolResult.builder().isError(true).addTextContent("Error: Player is not online.").build();
        }
        
        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
        
        builder.addTextContent("Name: " + player.getName());
        builder.addTextContent("Display Name: " + player.getDisplayName());
        builder.addTextContent("UUID: " + player.getUniqueId());
        builder.addTextContent("Health: " + player.getHealth());
        builder.addTextContent("Food Level: " + player.getFoodLevel());
        builder.addTextContent("Level: " + player.getLevel() + "(XP: " + player.getTotalExperience() + ")");
        builder.addTextContent("GameMode: " + player.getGameMode());
        builder.addTextContent("Flying: " + player.isFlying() + "(Can Fly: " + player.getAllowFlight() + ")");
        builder.addTextContent("Locations: " + player.getLocation());
        builder.addTextContent("Direction: " + player.getLocation().getDirection());
        builder.addTextContent("IP Address: " + player.getAddress());
        builder.addTextContent("OP: " + player.isOp());
//        builder.addTextContent("Online: " + player.isOnline());
        builder.addTextContent("Ping: " + player.getPing());
        builder.addTextContent("Walk Speed: " + player.getWalkSpeed());
        builder.addTextContent("Fly Speed: " + player.getFlySpeed());
        
        // These return Object value instead of readable values
        builder.addTextContent("Item in Hand: " + player.getInventory().getItemInMainHand());
        builder.addTextContent("Item in OffHand: " + player.getInventory().getItemInOffHand());
        builder.addTextContent("Armor: " + player.getInventory().getArmorContents());
        builder.addTextContent("Inventory: " + player.getInventory());
        
        
        return builder.build();
    }
}
