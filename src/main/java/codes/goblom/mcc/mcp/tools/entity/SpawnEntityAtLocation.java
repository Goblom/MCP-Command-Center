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
package codes.goblom.mcc.mcp.tools.entity;

import codes.goblom.mcc.CommandCenterConfig;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import codes.goblom.mcc.mcp.InputSchemaBuilder;
import codes.goblom.mcc.mcp.context.McpToolContext;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Bryan
 */
public class SpawnEntityAtLocation extends ToolProvider {
    
    private static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("loc-world", InputSchemaBuilder.ParameterType.String)
            .addRequiredProperty("loc-x", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("loc-y", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("loc-z", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("entityType", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("randomizeData", InputSchemaBuilder.ParameterType.Boolean)
            .toJson();
    
    public SpawnEntityAtLocation() {
        super(
                "spawn_entity_at_location",
                """
                Spawn an entity at a given location with given entity type.
                                    
                Entity Rules:
                - Entity type must match to an option in the enum class org.bukkit.entity.EntityType
                - Location must include the world and if no x, y, z coordinates set them to 0
                - If the user specifies randomized data add "randomizeData" to the properties
                """,
                INPUT_SCHEMA);
    }
    
    @Override
    @RequireSyncMethod
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String worldName = context.getArgument("loc-world");
        double x = Double.parseDouble(context.getArgument("loc-x").toString());
        double y = Double.parseDouble(context.getArgument("loc-y").toString());
        double z = Double.parseDouble(context.getArgument("loc-z").toString());
        EntityType entityType = EntityType.fromName(context.getArgument("entityType"));
        boolean randomizeData = context.getArgument("randomizeData", CommandCenterConfig.RANDOM.nextBoolean());
        
        if (entityType == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: EntityType " + entityType + " is not a valid EntityType.")
                    .addTextContent("Does not match an entity in the EntityType enum.")
                    .build();
        }
        
        if (GetEntityTypes.DISALLOWED_ENTITY_TYPES.contains(entityType)) {
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
                    .build();    }
    
}
