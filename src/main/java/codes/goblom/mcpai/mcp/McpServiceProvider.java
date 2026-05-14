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
package codes.goblom.mcpai.mcp;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author Bryan
 */
public interface McpServiceProvider {
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Attribute {
        String key();
        
        /**
         * Limited to only supporting String.
         */
        String value();
    }
    
    /**
     * Used by the LLM to prompt the user questions.
     * 
     * DO NOT RUN COMMANDS WITH PROMPTS
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Prompt {
        public String name() default "";
        
        public String description();
        
        public String inputSchema() default
                """
                "type": "object",
                "properties": { }
                """;
        //Wrapper function helpers to call sync methods without having to use the Bukkit Scheduler in the method you are writing the tool.
        public boolean requiresSyncMethod() default false;
        public long syncMethodTimeout() default -1;
    }
    
    /**
     * Used by the LLM run commands on the server
     */
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Tool {
    
        public String name() default "";

        public String description();

        //TODO: Implements a Builder class to write more clean json schema
        public String inputSchema() default //Defaults to a schema that takes no arguments from the LLM
                """
                    {
                      "type": "object",
                      "properties": { },
                      "additionalProperties": false
                    }
                """;

        public String outputSchema() default "";
        
        /**
         *  Required Attributes:
         *      title - String
         *      readOnlyHint - boolean
         *      destructiveHint - boolean
         *      idempotentHint - boolean
         *      openWorldHint - boolean
         *      returnDirect - boolean
         */
        public Attribute[] toolAnnotations() default { };
        
        public Attribute[] meta() default { };
        
        //Wrapper function helpers to call sync methods without having to use the Bukkit Scheduler in the method you are writing the tool.
        public boolean requiresSyncMethod() default false;
        public long syncMethodTimeout() default -1;
    }
}
