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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 *
 * @author Bryan
 */
public class ListDirectory extends ToolProvider {
    
    static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addOptionalProperty("path", InputSchemaBuilder.ParameterType.String)
            .toJson();
    
    //TODO: Convert to a Schema Builder
    static final String OUTPUT_SCHEMA = 
            """
            {
            "type": "object",
                "description": "List of files and directories in the requested path. Can also provide file size and last modified metadata.",
                "items": {
                  "type": "array",
                  "properties": {
                    "name": {
                      "type": "string",
                      "description": "File or directory name",
                      "default": "."
                    },
                    "isDirectory": {
                      "type": "boolean"
                    },
                    "size": {
                      "type": ["number", "null"],
                      "description": "File size in bytes (null for directories)"
                    },
                    "lastModified": {
                      "type": "string",
                      "description": "Last modified timestamp"
                    }
                  },
                  "required": ["name", "isDirectory", "lastModified"]
                }
            }
            """;
    
    public ListDirectory() {
        super(
                "list_directory",
                """
                List all files and directories relative to the minecraft server root.
                
                Path Rules:
                - All returned paths are relative to the server root
                - Use relative paths like:
                  - plugins/
                  - worlds/world/
                  - logs/
                - Do NOT use absolute filesystem paths
                """,
                INPUT_SCHEMA
        );
    }

    @Override
    public String getOutputSchema() {
        return OUTPUT_SCHEMA;
    }

    
    @Override
    public McpSchema.CallToolResult execute(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws Exception {
        String searchPathStr = (String) request.arguments().getOrDefault("path", ".");
        Path searchPath
                = searchPathStr.equalsIgnoreCase("root")
                || searchPathStr.equalsIgnoreCase("server")
                || searchPathStr.equalsIgnoreCase("/")
                || searchPathStr.equalsIgnoreCase("C:\\")
                ? SharedToolData.SERVER_DIR
                : SharedToolData.SERVER_DIR.resolve(searchPathStr).normalize();

        Configuration.PLUGIN.debug(Level.INFO, "Looking at path {0}", searchPath);

        if (!searchPath.startsWith(SharedToolData.SERVER_DIR)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Access is denied. Outside of server folder.")
                    .build();
        }

        if (!Files.exists(searchPath)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Directory not found: " + searchPathStr)
                    .build();
        }

        if (!Files.isDirectory(searchPath)) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Not a directory: " + searchPathStr)
                    .build();
        }

        List<Map<String, Object>> fileList = new ArrayList<>();

        try (Stream<Path> stream = Files.list(searchPath)) {
            stream.sorted().forEach(path -> {
                try {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", path.getFileName().toString());
                    entry.put("isDirectory", Files.isDirectory(path));
                    entry.put("size", Files.isDirectory(path) ? null : Files.size(path));
                    entry.put("lastModified", Files.getLastModifiedTime(path).toString());
                    fileList.add(entry);
                } catch (IOException ignored) { }
            });
        }

        // Human-readable summary for LLMs
        StringBuilder text = new StringBuilder(searchPathStr + " Folder:\n\n");
        for (Map<String, Object> file : fileList) {
            if ((boolean) file.get("isDirectory")) {
                text.append("/").append(file.get("name")).append("/\n");
            } else {
                text.append(file.get("name")).append("\n");
            }
        }

        return McpSchema.CallToolResult.builder()
                .addTextContent(text.toString())
                .structuredContent(new HashMap() {
                        {
                            put("items", fileList);
                        }
                })
                .build();
    }
}
