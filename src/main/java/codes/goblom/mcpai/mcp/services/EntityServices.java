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

import codes.goblom.mcpai.Configuration;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import codes.goblom.mcpai.mcp.McpServiceProvider;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bryan
 */
public class EntityServices implements McpServiceProvider {
    
    private static final List<EntityType> DISALLOWED_ENTITY_TYPES = new ArrayList(){
        {
            add(EntityType.PLAYER);
            add(EntityType.UNKNOWN);
        }
    };
    
    @Tool(
            name = "spawn_entity_at_location",
            description = 
                    """
                    Spawn an entity at a given location with given entity type.
                    
                    Entity Rules:
                    - Entity type must match to an option in the enum class org.bukkit.entity.EntityType
                    - Location must include the world and if no x, y, z coordinates set them to 0
                    - If the user specifies randomized data add "randomizeData" to the properties
                    """,
            inputSchema = 
                    """
                    {
                        "type": "object",
                        "required": ["loc-world", "loc-x", "loc-y", "loc-z", "entityType"],
                        "properties": {
                            "loc-world": { "type": "string" },
                            "loc-x": { "type": "number" },
                            "loc-y": { "type": "number" },
                            "loc-z": { "type": "number" },
                            "entityType": { "type": "string" },
                            "randomizeData": { "type": "boolean" }
                        }
                    }
                    """,
            requiresSyncMethod = true
            
    )
    public McpSchema.CallToolResult spawnEntityAtLocation(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws InterruptedException, ExecutionException, TimeoutException {
        String worldName = (String) request.arguments().get("loc-world");
        double x = Double.parseDouble(request.arguments().get("loc-x").toString());
        double y = Double.parseDouble(request.arguments().get("loc-y").toString());
        double z = Double.parseDouble(request.arguments().get("loc-z").toString());
        EntityType entityType = EntityType.fromName((String) request.arguments().get("entityType"));
        boolean randomizeData = (boolean) request.arguments().getOrDefault("randomizeData", Configuration.RANDOM.nextBoolean());
        
        if (entityType == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: EntityType " + entityType + " is not a valid EntityType.")
                    .addTextContent("Does not match an entity in the EntityType enum.")
                    .build();
        }
        
        if (DISALLOWED_ENTITY_TYPES.contains(entityType)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Entity type " + entityType + " is not allowed.")
                    .build();
        }
        
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: World " + worldName + " does not exist.")
                    .build();
        }
        
        Location location = new Location(world, x, y, z);
        Entity entity = world.spawnEntity(location, entityType, randomizeData);

        return McpSchema.CallToolResult.builder()
                    .addTextContent("Success! Spawned entity at " + entity.getLocation().toString())
                    .build();
        
    }
    
    @Tool(
            name = "get_available_entity_types",
            description = "Get a list of all EntityTypes available on the server.",
            inputSchema = 
                    """
                    {
                      "type" : "object",
                      "id" : "urn:jsonschema:Operation",
                      "properties" : {
                        "operation" : {
                          "type" : "string"
                        }
                      }
                    }
                    """
    )
    public McpSchema.CallToolResult getAvailableEntityTypes(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
        
        for (EntityType type : EntityType.values()) {
            if (DISALLOWED_ENTITY_TYPES.contains(type)) continue; //Don't show the LLM types that arent allowed.
            
            builder.addTextContent(type.name().toLowerCase());
        }
        
        return builder.build();
    }
}
