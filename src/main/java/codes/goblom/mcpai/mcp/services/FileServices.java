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
import codes.goblom.mcpai.McpPlugin;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import codes.goblom.mcpai.mcp.McpServiceProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Bryan
 */
public class FileServices implements McpServiceProvider {

    final McpPlugin plugin;

    public FileServices(McpPlugin plugin) {
        this.plugin = plugin;

        plugin.debug(Level.INFO, "Found Server Directory at {0}", Configuration.SERVER_DIR);
    }

    @Tool(
            name = "list_directory",
            description = 
                    """
                    Lists files and directories relative to the Minecraft server root.

                    Path rules:
                    - "root" refers to the server root directory
                    - All returned paths are relative to the server root
                    - Use relative paths like:
                      - plugins
                      - worlds/world
                      - logs/latest.log
                    - Do NOT use absolute filesystem paths
                    """,
            inputSchema =
                    """
                    {
                        "type": "object",
                        "required": [ "path" ],
                        "properties": {
                            "path": {
                                "type": "string",
                                "description": "Relative path inside the server directory."
                            }
                        }
                    }
                    """,
            outputSchema = 
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
                    """
    )
    public McpSchema.CallToolResult lsDirectory(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws IOException {

        String searchPathStr = (String) request.arguments().getOrDefault("path", ".");
        Path searchPath
                = searchPathStr.equalsIgnoreCase("root")
                || searchPathStr.equalsIgnoreCase("server")
                || searchPathStr.equalsIgnoreCase("/")
                || searchPathStr.equalsIgnoreCase("C:\\")
                ? Configuration.SERVER_DIR
                : Configuration.SERVER_DIR.resolve(searchPathStr).normalize();

        plugin.debug(Level.INFO, "Looking at path {0}", searchPath);

        if (!searchPath.startsWith(Configuration.SERVER_DIR)) {
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

    @Tool(
            name = "read_file",
            description = "Read a file from path",
            inputSchema = 
                    """
                    {
                        "type": "object",
                          "required": ["path"],
                          "properties": {
                            "path": {
                              "type": "string",
                              "description": "Relative path inside the server directory."
                            }
                          }
                    }
                    """
    )
    public McpSchema.CallToolResult readFile(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws IOException {
        String pathStr = (String) request.arguments().get("path");
        Path path = Configuration.SERVER_DIR.resolve(pathStr).normalize();
        
        if (!path.startsWith(Configuration.SERVER_DIR)) {
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
    
    @Tool(
            name = "write_file",
            description = "write a file to the server",
            inputSchema = 
                    """
                    {
                        "type": "object",
                        "required": [ "path", "fileContent" ],
                        "properties": {
                            "path": {
                                "type": "string",
                                "description": "Path to file including file type"
                            },
                            "fileContent": {
                                "type": "string"
                            },
                            "overrideFile": {
                                "type": "boolean",
                                "description": "Should we override the file if one already exists? Defaults to true"
                            }
                        }
                    }
                    """
    )
    public McpSchema.CallToolResult writeFile(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) throws IOException {
        String pathStr = (String) request.arguments().get("path");
        String fileContent = (String) request.arguments().get("fileContent");
        boolean overrideFile = (boolean) request.arguments().getOrDefault("overrideFile", true);
        
        Path path = Configuration.SERVER_DIR.resolve(pathStr).normalize();
        
        if (!path.startsWith(Configuration.SERVER_DIR)) {
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
