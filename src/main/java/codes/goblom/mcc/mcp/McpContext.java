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
package codes.goblom.mcc.mcp;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 *
 * @author Bryan
 */
public class McpContext<R extends McpSchema.Request> {
    
    private final McpSyncServerExchange exchange;
    private final R request;
    
    protected McpContext(McpSyncServerExchange exchange, R request) {
        this.exchange = exchange;
        this.request = request;
    }
    
    public McpSyncServerExchange exchange() {
        return this.exchange;
    }
    
    public R request() {
        return this.request;
    }
    
    public void notification(ProgressNotification notif) {
        exchange().progressNotification(notif);
    }
    
    public void notification(Object progressToken, double progress, double total, String message) {
        ProgressNotification notif = new ProgressNotification(progressToken, progress, total, message);
        
        notification(notif);
    }
}
