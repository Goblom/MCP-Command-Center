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
package codes.goblom.mcpai.mcp.tools.server;

import codes.goblom.mcpai.Configuration;
import codes.goblom.mcpai.mcp.InputSchemaBuilder;
import codes.goblom.mcpai.mcp.context.McpToolContext;
import codes.goblom.mcpai.mcp.providers.ToolProvider;
import codes.goblom.mcpai.mcp.tools.SharedToolData;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 * This is sometimes not used over read_file. Figure out description better so LLM can
 *      correctly call this tool
 * 
 * @author Bryan
 */
public class GetLog extends ToolProvider {
    
    private static final String LATEST = "latest.log";
    
    private static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addOptionalProperty("timestamp", InputSchemaBuilder.ParameterType.String)
            .addPropertyDefault("timestamp", LATEST)
            .addPropertyDescription("timestamp", """
                                                 Follows '{year}-{month}-{day}' and if multiple logs with the same date are present dd '-{num}'
                                                 
                                                 get_log Rules:
                                                  - If multiple logs are found with the same {year}-{month}-{day} the tool will return with a list of logs found.
                                                  - Display all log files found if multiple are found.
                                                  - The tool will prompt the user to specifiy which file to use.
                                                 """)
            .toJson();
    
    
    public GetLog() {
        super(
                "get_log",
                "Read a log file from the server",
                INPUT_SCHEMA
        );
    }

    @Override
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String logFile = context.getArgument("timestamp", LATEST);
        
        if (logFile.equalsIgnoreCase(LATEST) || logFile.equalsIgnoreCase("latest")) {
            Path path = SharedToolData.SERVER_DIR.resolve("logs/" + LATEST);
            
            if (!Files.exists(path)) {
                return McpSchema.CallToolResult.builder().isError(true).addTextContent("No latest.log found").build();
            }
            
            McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder();
            
            Files.readAllLines(path, StandardCharsets.UTF_8).forEach((line) -> {
                builder.addTextContent(line);
            });
            
            return builder.build();
        }
        
        List<Path> logFiles = Files.list(SharedToolData.SERVER_DIR.resolve("logs/"))
            .filter((path) -> {
                Configuration.PLUGIN.debug(Level.INFO, "Found Path {0}", path);
                return !path.endsWith(".log.gz");
            }
        ).toList();
        
        Path filePath = null;
        
        if (logFiles.size() == 1) {
            filePath = logFiles.getFirst();
        } else {
            List<String> fileNames = logFiles.stream().map((path) -> path.getFileName().toString() + "\n").toList();
            
            McpSchema.CallToolResult.Builder builder = McpSchema.CallToolResult.builder()
                    .addTextContent("Multiple Logs Found - Show the user all of them.");
                    
            
            fileNames.forEach((name) -> builder.addTextContent(name));
            
            builder.addTextContent("--- END LOGS FOUND ---");
            builder.addTextContent("Which log file should we use? ");
                            
            return builder.build();
        }
        
        try (GZIPInputStream is = new GZIPInputStream(new FileInputStream(filePath.toFile()))) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            StringBuilder sb = new StringBuilder();
            
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            
            return McpSchema.CallToolResult.builder()
                    .addTextContent(sb.toString())
                    .build();
        } catch (Exception e) {
            return McpSchema.CallToolResult.builder()
                    .isError(true)
                    .addTextContent("Error: Unable to unzip " + filePath + " - " + e.getMessage())
                    .build();
        }
    }
}
