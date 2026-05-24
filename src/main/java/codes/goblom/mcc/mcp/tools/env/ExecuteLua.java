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
package codes.goblom.mcc.mcp.tools.env;

import codes.goblom.mcc.mcp.InputSchemaBuilder;
import codes.goblom.mcc.mcp.context.McpToolContext;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import codes.goblom.mcc.mcp.tools.SharedToolData;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

/**
 *
 * @author Bryan
 */
public class ExecuteLua extends ToolProvider {
    
    private static final String INPUT_SCHEMA = InputSchemaBuilder.builder()
            .addRequiredProperty("code", InputSchemaBuilder.ParameterType.String)
            .addPropertyDescription("code", "The Lua Code that we are to execute on the server.")
            .toJson();
    
    private static Globals _globals = JsePlatform.standardGlobals();
    
    public ExecuteLua() {
        super(
                "execute_lua",
                """
                Run lua code on the server.
                
                Note: This is experimental and many things can go wrong.
                      Please have the user review all code before executing this on the server.
                      This is also a known attack vector for bad actors. Give access cautiosly.
                
                Special Functions:
                 - import(String) ## Import a class. {bukkit}, {spigot}, {nms} can be used to shorten org.bukkit, org.spigotmc, net.minecraft
                 - getOnlinePlayers() ## Returns an array of Wrapped Player data. Needed since LuaJ has hard time with Collection
                """,
                INPUT_SCHEMA
        );
        
        _globals.set("import", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
//                getPlugin().debug(Level.INFO, "Calling Lua Function - import");
                try {
                    String path = arg.checkjstring();
                    
                    path = path.replace("{bukkit}", "org.bukkit");
                    path = path.replace("{spigotmc}", "org.spigotmc");
                    path = path.replace("{nms}", "net.minecraft");
                    
                    Class clazz = Class.forName(path);
                    
                    return CoerceJavaToLua.coerce(clazz);
                } catch (Exception e) {
                    e.printStackTrace();

                    throw new LuaError(e.getMessage());
                }
            }
        });
        
        _globals.set("print", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue lv) {
                SharedToolData.COMMAND_SENDER.sendMessage(lv.checkjstring());
                return null;
            }
            
        });
        
        _globals.set("getOnlinePlayers", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                int count = 1;
                LuaTable table = new LuaTable();
                
                List<Player> players = Bukkit.getOnlinePlayers().stream().collect(Collectors.toList());

                for (Player player : players) {
                    table.set(count++, CoerceJavaToLua.coerce(player));
                }
                
                return table;
            }
        });
        
        //TODO: Add more required classes if needed
        for (String className : new String[] { "org.bukkit.Bukkit" }) {
            try {
                _globals.set("Bukkit", CoerceJavaToLua.coerce(Class.forName("org.bukkit.Bukkit")));
            } catch (Exception e) {
                getPlugin().debug(Level.WARNING, "Unable to load {0}. It must be declared manually in lua code.", className);
            }
        }
    }

    @Override
    @RequireSyncMethod
    public McpSchema.CallToolResult execute(McpToolContext context) throws Exception {
        String luaCode = context.getArgument("code");
        
        LuaValue compiled = _globals.load(luaCode);
        
        compiled.call();
        
        return McpSchema.CallToolResult.builder()
                .addTextContent("Success! (At least no errors)").build();
    }
}
