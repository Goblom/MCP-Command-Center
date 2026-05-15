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
package codes.goblom.mcpai;

import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Bryan
 */
public class Configuration {
    
    public static int HTTP_PORT = 8080;
    public static String MCP_PATH = "/mcp";
    public static boolean DEBUG = true;
    public static Random RANDOM = new Random();

    public static Map<String, List<String>> TOKEN_PERMISSIONS = new HashMap<String, List<String>>() {
        {
            put("super-secret-token", Lists.newArrayList("tools.all", "prompts.all"));
            put("permissions-test", Lists.newArrayList("prompts.all", "tools.list_plugins", "tools.get_logged_in_players_name"));
        }
    };
    
    public static String MCP_CONSOLE_NAME = "[LLM]";
    
    public static McpPlugin PLUGIN;
}
