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

import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author Bryan
 */
public class FillArea extends ToolProvider {
    
    private static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("loc-world", InputSchemaBuilder.ParameterType.String)
            .addRequiredProperty("start-x", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("start-y", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("start-z", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("end-x", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("end-y", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("end-z", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("blockType", InputSchemaBuilder.ParameterType.String)
            
            .addPropertyDescription("blockType", "execute tool 'get_block_types' to get a list of available block types")
            .addPropertyDefault("blockType", Material.AIR.name())
            
            .toJson();
    
    public FillArea() {
        super(
                "fill_area",
                "Fills an area with a specified blockType",
                INPUT_SCHEMA
        );
    }

    @Override
    @RequireSyncMethod
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String worldName = context.getArgument("loc-world");
        int startX = context.getArgument("start-x");
        int startY = context.getArgument("start-y");
        int startZ = context.getArgument("start-z");
        int endX = context.getArgument("end-x");
        int endY = context.getArgument("end-y");
        int endZ = context.getArgument("end-z");
        String blockType = context.getArgument("blockType");
        
        Material mat = Material.getMaterial(blockType);
        World world = Bukkit.getWorld(worldName);
        
        if (mat == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Material " + blockType + " does not exist.")
                    .build();
        }
        
        if (world == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: World " + worldName + " does not exist.")
                    .build();
        }
        
        int minX = Math.min(startX, endX);
        int minY = Math.min(startY, endY);
        int minZ = Math.min(startZ, endZ);
        int maxX = Math.max(startX, endX);
        int maxY = Math.max(startY, endY);
        int maxZ = Math.max(startZ, endZ);
        
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxX; z++) {
                    world.getBlockAt(x, y, z).setType(mat);
                }
            }
        }
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Success! All blocks changed to " + blockType)
                .build();
    }
    
    
}
