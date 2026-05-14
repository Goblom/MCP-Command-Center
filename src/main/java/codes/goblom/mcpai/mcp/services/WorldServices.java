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

import codes.goblom.mcpai.mcp.McpServiceProvider;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 *
 * @author Bryan
 */
public class WorldServices implements McpServiceProvider {
    
//    {
//        InputSchemaBuilder.newBuilder()
//                .addRequiredProperty("name", InputSchemaBuilder.ParameterType.String)
//                .addOptionalProperty("seed", InputSchemaBuilder.ParameterType.Number)
//                .addOptionalProperty("worldEnvironment", InputSchemaBuilder.ParameterType.String)
//                .addOptionalProperty("worldType", InputSchemaBuilder.ParameterType.String)
//                .addOptionalProperty("generateStructures", InputSchemaBuilder.ParameterType.Boolean)
//                .addOptionalProperty("generatorSettings", InputSchemaBuilder.ParameterType.String)
//                .addOptionalProperty("hardcore", InputSchemaBuilder.ParameterType.Boolean)
//                .additionalProperties(true)
//                .toJson();
//                
//    }
    
    @Tool(
            name = "create_world",
            description = "Create a world wiith the given values",
            inputSchema =  // Needs Descriptions and chunkGenerator, biomeProvider
                    """
                    {
                        "type": "object",
                        "properties": {
                            "name": { "type": "string" },
                            "seed": { "type": "string" },
                            "worldEnvironment": { "type": "string" },
                            "worldType": { "type": "string" },
                            "generateStructures": { "type": "boolean" },
                            "generatorSettings": { "type": "string" },
                            "hardcore": { "type": "boolean" }
                        },
                        "required": ["name"],
                        "additionalProperties": "true"
                    }
                    """,
            requiresSyncMethod = true,
            syncMethodTimeout = 20 * 5 //5 Seconds
    ) 
    public McpSchema.CallToolResult createWorld(McpSyncServerExchange exchange, CallToolRequest request) {
        String name = (String) request.arguments().get("name");
        long seed = (long) request.arguments().getOrDefault("seed", Long.MIN_VALUE); //MIN_VALUE to tell if no seed was present
        String worldEnvironmentStr = (String) request.arguments().get("worldEnvironment");
        // Chunk Generator
        // Biome Provider
        String worldTypeStr = (String) request.arguments().get("worldType");
        boolean generateStructures = (boolean) request.arguments().getOrDefault("generateStructures", true); //Follows the WorldCreator constructor
        String generatorSettings = (String) request.arguments().get("generatorSettings");
        boolean hardcore = (boolean) request.arguments().getOrDefault("hardcore", false); //Follows WorldCreator constructor
        
        if (Bukkit.getWorld(name) != null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: World already exists.")
                    .build();
        }
        
        WorldCreator creator = new WorldCreator(name);
        
        if (seed != Long.MIN_VALUE) {
            creator.seed(seed);
        }
        
        if (worldEnvironmentStr != null && !worldEnvironmentStr.isEmpty()) {
            creator.environment(World.Environment.valueOf(worldEnvironmentStr));
        }
        
        if (worldTypeStr != null && !worldTypeStr.isEmpty()) {
            creator.type(WorldType.valueOf(worldTypeStr));
        }
        
        if (generatorSettings != null && !generatorSettings.isEmpty()) {
            creator.generator(generatorSettings, ServerServices.COMMAND_SENDER);
        }
        
        creator.generateStructures(generateStructures);
        creator.hardcore(hardcore);
        
        World createdWorld = creator.createWorld();
        String output = ServerServices.COMMAND_SENDER.getConsoleOutput();
        ServerServices.COMMAND_SENDER.clearConsoleOutput();
        
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Successfully created world " + name)
                .addTextContent("Console Output: " + output)
                .build();
    }
}
