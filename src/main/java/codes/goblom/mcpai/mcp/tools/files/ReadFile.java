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
package codes.goblom.mcpai.mcp.tools.files;

import codes.goblom.mcpai.Configuration;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.tools.SharedToolData;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Bryan
 */
public class ReadFile extends ToolProvider {
    
    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("path", InputSchemaBuilder.ParameterType.String)
            .toJson();
    
    public ReadFile() {
        super(
                "read_file",
                "Read a file from path relative to the servers root directory.",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws Exception {
        String pathStr = (String) request.arguments().get("path");
        Path path = SharedToolData.SERVER_DIR.resolve(pathStr).normalize();
        
        if (!path.startsWith(SharedToolData.SERVER_DIR)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: File outside server root.")
                    .build();
        }
        
        if (!Files.exists(path) && !Files.isDirectory(path)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: File does not exist or is directory.")
                    .build();
        }
        
        String fileLines = Files.readString(path, StandardCharsets.UTF_8);
        
        return McpSchema.CallToolResult.builder()
                .addTextContent(fileLines)
                .build();
    }
}
