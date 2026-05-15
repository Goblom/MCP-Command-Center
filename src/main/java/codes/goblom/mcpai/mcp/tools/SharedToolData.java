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
package codes.goblom.mcpai.mcp.tools;

import codes.goblom.mcpai.WrappedCommandSender;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.bukkit.Bukkit;

/**
 *
 * @author Bryan
 */
public class SharedToolData {
    
    public static final WrappedCommandSender COMMAND_SENDER = new WrappedCommandSender(Bukkit.getConsoleSender());
    
    public static final Handler LOG_HANDLER = new Handler() {
        
        StringBuilder logOutput = new StringBuilder();
        
        @Override
        public void publish(LogRecord record) {
            if (record == null) return;
            
            if (record.getMessage() != null) {
                COMMAND_SENDER.sendMessage(record.getMessage()); //This may cause double Strings
                this.logOutput.append(record.getMessage()).append("\n");
            }
        }

        @Override
        public void flush() { 
            this.logOutput = new StringBuilder();
        }

        @Override
        public void close() { }
    };
}
