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
package codes.goblom.mcpai.mcp.tools.entity;

import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Bryan
 */
public class GetEntityTypes extends ToolProvider {
    
    static final List<EntityType> DISALLOWED_ENTITY_TYPES = new ArrayList(){
        {
            add(EntityType.PLAYER);
            add(EntityType.UNKNOWN);
        }
    };
    
    
    static String INPUT_SCHEMA = InputSchemaBuilder.builder()
//            .id("urn:jsonschema:Operation")
            .toJson();
    
    public GetEntityTypes() {
        super(
                "get_entity_types",
                "Get a list of all EntityTypes available on the server.",
                INPUT_SCHEMA
        );
    }
    
    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
        
        for (EntityType type : EntityType.values()) {
            if (DISALLOWED_ENTITY_TYPES.contains(type)) continue;
            
            builder.addTextContent(type.name().toLowerCase());
        }
        
        return builder.build();
    }
}
