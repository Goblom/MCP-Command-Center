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
package codes.goblom.mcc;

import codes.goblom.mcc.JellyConfiguration.ConfigSource;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Bryan
 */
@ConfigSource("config.yml")
public class CommandCenterConfig implements JellyConfiguration {
    
    // Can keep this static
    // Dont add @ConfigValue, dont need to save it
    public static Random RANDOM = new Random();
    
    @ConfigValue(
            value = "Debug",
            handler = ConfigValue.BooleanHandler.class,
            comments = {
                "Debug Mode:",
                "   Default: false",
                "   Enable this to print debug output to console."
            }
    )
    private boolean debugMode = false;
    
    @ConfigValue(
            value = "HTTP Port",
            handler = ConfigValue.IntHandler.class,
            comments = {
                "HTTP Port:",
                "   Default: 8080",
                "   The port the tools server is listening on"
            }
    )
    private int httpPort = 8080;
    
    @ConfigValue(
            value = "MCP Path",
            handler = ConfigValue.StringHandler.class
    )
    private String mcpPath = "/mcp";
    
    @ConfigValue(
            value = "Tokens",
            handler = ConfigValue.MapHandler.class
    )
    private Map<String, List<String>> tokenPermissions = new HashMap() {
        {
            put("super-secret-token", "tools.all");
            put("limited-permission", Lists.newArrayList("tools.get_logged_in_players", "tools.get_player_info", "tools.teleport_player", "tools.get_player_location"));
        }
    };
    
    @ConfigValue("MCP Console Name")
    private String mcpConsoleName = "[LLM]";
    
    @ConfigValue("Command Prefix") //Do we need to change this in the config?
    private String commandPrefix = "[MCC]";
    
    // Return:
    //     Should we allow this in config and potentially open up an
    //     attack vector for people who don't know or make it so someone
    //     has to compile their own version of MCC with this enabled manually.
    @ConfigValue(
            value = "Environment.Enable Lua",
            handler = ConfigValue.BooleanHandler.class
    )
    private boolean enableLua = false;
    
    @ConfigValue(
            value = "Disabled Tools",
            handler = ConfigValue.StringListHandler.class
    )
    private List<String> disabledTools = Lists.newArrayList();

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public int getHttpPort() {
        return this.httpPort;
    }

    public String getMcpPath() {
        return this.mcpPath;
    }

    public Map<String, List<String>> getTokenPermissions() {
        return this.tokenPermissions;
    }

    public String getMcpConsoleName() {
        return this.mcpConsoleName;
    }

    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    public boolean isLuaEnabled() {
        return this.enableLua;
    }

    public List<String> getDisabledTools() {
        return disabledTools;
    }
}
