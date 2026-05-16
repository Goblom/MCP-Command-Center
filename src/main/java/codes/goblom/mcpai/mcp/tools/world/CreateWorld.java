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
package codes.goblom.mcpai.mcp.tools.world;

import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.tools.SharedToolData;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

/**
 *
 * @author Bryan
 */
public class CreateWorld extends ToolProvider {
        
    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("name", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("seed", InputSchemaBuilder.ParameterType.Number)
            .addOptionalProperty("worldEnvironment", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("worldType", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("generateStructures", InputSchemaBuilder.ParameterType.Boolean)
            .addOptionalProperty("generatorSettings", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("hardcore", InputSchemaBuilder.ParameterType.Boolean)
            
            // chunkGenerator
            // biomeProvider
            .toJson();
                
    public CreateWorld() {
        super(
                "create_world",
                "Create a world with the given values",
                INPUT_SCHEMA
        );
    }

    @Override
    @RequireSyncMethod( timeout = 20 * 5 )
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String name = context.getArgument("name");
        long seed = context.getArgument("seed", Long.MIN_VALUE); //MIN_VALUE to tell if no seed was present
        String worldEnvironmentStr = context.getArgument("worldEnvironment");
        // Chunk Generator
        // Biome Provider
        String worldTypeStr = context.getArgument("worldType");
        boolean generateStructures = context.getArgument("generateStructures", true); //Follows the WorldCreator constructor
        String generatorSettings = context.getArgument("generatorSettings");
        boolean hardcore = context.getArgument("hardcore", false); //Follows WorldCreator constructor
        
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
            creator.generator(generatorSettings, SharedToolData.COMMAND_SENDER);
        }
        
        creator.generateStructures(generateStructures);
        creator.hardcore(hardcore);
        
        World createdWorld = creator.createWorld();
        String output = SharedToolData.COMMAND_SENDER.getConsoleOutput();
        SharedToolData.COMMAND_SENDER.clearConsoleOutput();
        
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Successfully created world " + name)
                .addTextContent("Console Output: " + output)
                .build();
    }
}
