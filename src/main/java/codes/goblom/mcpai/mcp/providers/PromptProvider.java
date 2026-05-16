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
package codes.goblom.mcpai.mcp.providers;

import codes.goblom.mcpai.mcp.ServiceProvider;
import com.google.common.collect.Lists;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;

/**
 * A prompt is something the LLM uses to communicate with the user. 
 * Do not run commands through here.
 * 
 * @author Bryan
 */
public abstract class PromptProvider implements ServiceProvider<McpSchema.GetPromptResult, McpSchema.GetPromptRequest> {
    
    @Override
    public final boolean hasPermission(String token) {
        return true;
    }
    
    //TODO: Add support for other messages like...
    //      ImageContent, AudioContent, EmbeddedResource, ResourceLink
    static class PromptMessageBuilder {
        
        static PromptMessageBuilder builder() {
            return new PromptMessageBuilder();
        }
        
        static PromptMessageBuilder builder(McpSchema.Role defaultRole, String... messages) {
            PromptMessageBuilder builder = builder();
            
            for (String message : messages) {
                builder.textContent(defaultRole, message);
            }
            
            return builder;
        }
        
        List<McpSchema.PromptMessage> messages = Lists.newArrayList();
        
        public PromptMessageBuilder textContent(McpSchema.Role role, String message) {
            return content(role, new McpSchema.TextContent(message));
        }
        
        public PromptMessageBuilder content(McpSchema.Role role, McpSchema.Content content) {
            messages.add(new McpSchema.PromptMessage(role, content));
            return this;
        }
        
        public List<McpSchema.PromptMessage> build() {
            return messages;
        }
    }
}
