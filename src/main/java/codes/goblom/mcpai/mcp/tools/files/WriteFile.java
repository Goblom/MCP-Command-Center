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
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Bryan
 */
public class WriteFile extends ToolProvider {
    
    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("path", InputSchemaBuilder.ParameterType.String, "Path to file including file type.")
            .addRequiredProperty("fileContent", InputSchemaBuilder.ParameterType.String)
            .addOptionalProperty("overrideFile", InputSchemaBuilder.ParameterType.Boolean, "Should we override the file if one already exists?")
            
            .addPropertyDefault("overrideFile", false)
            
            .toJson();
    
    public WriteFile() {
        super(
                "write_file",
                "Write a file to the server.",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws Exception {
        String pathStr = (String) request.arguments().get("path");
        String fileContent = (String) request.arguments().get("fileContent");
        boolean overrideFile = (boolean) request.arguments().getOrDefault("overrideFile", true);
        
        Path path = SharedToolData.SERVER_DIR.resolve(pathStr).normalize();
        
        if (!path.startsWith(SharedToolData.SERVER_DIR)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: File outside server root.")
                    .build();
        }
        
        Files.createDirectories(path);
        
        if (Files.exists(path) && !overrideFile) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: File already exists and overrideFile is set to false.")
                    .build();
        }
        
        Files.writeString(path, fileContent, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Success! Wrote ")
                .build();
    }
}
