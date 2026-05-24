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
package codes.goblom.mcc.mcp.tools.world;

import codes.goblom.mcc.mcp.InputSchemaBuilder;
import codes.goblom.mcc.mcp.context.McpToolContext;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 *
 * @author Bryan
 */
public class SetBlock extends ToolProvider {
    
    private static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("loc-world", InputSchemaBuilder.ParameterType.String)
            .addRequiredProperty("loc-x", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("loc-y", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("loc-z", InputSchemaBuilder.ParameterType.Number)
            .addRequiredProperty("blockType", InputSchemaBuilder.ParameterType.String)
            
            .addPropertyDescription("blockType", "execute tool 'get_block_types' to get a list of available block types")
            .addPropertyDefault("blockType", Material.AIR.name())
            
            .toJson();
    
    public SetBlock() {
        super(
                "set_block",
                """
                Change the type of a block at a specific location.
                
                To break a block set its blockType to AIR
                """,
                INPUT_SCHEMA
        );
    }

    @Override
    @RequireSyncMethod
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String worldName = context.getArgument("loc-world");
        int x = context.getArgument("loc-x");
        int y = context.getArgument("loc-y");
        int z = context.getArgument("loc-z");
        String blockType = context.getArgument("blockType");
        Material mat = Material.getMaterial(blockType);
        
        if (mat == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Material " + blockType + " does not exist.")
                    .build();
        }
        
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: World " + worldName + " does not exist.")
                    .build();
        }
        
        Location loc = new Location(world, x, y, z);
        
        world.getBlockAt(loc).setType(mat);
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Success! Block set to " + blockType)
                .build();
    }
    
    
}
