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
package codes.goblom.mcc.commands;

import codes.goblom.factory.command.CommandContext;
import codes.goblom.factory.command.CommandInfo;
import codes.goblom.factory.command.CommandListener;
import codes.goblom.mcc.CommandCenterConfig;
import codes.goblom.mcc.CommandCenterPlugin;
import codes.goblom.mcc.mcp.providers.ToolProvider;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Bryan
 */
public class TokenCommands implements CommandListener {
    
    // Inspiration from, split the sections
    // https://stackoverflow.com/a/20536597
    protected static String getSaltString(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        int sections = length < 12 ? 2 : length < 24 ? 3 : 4;
        int charsOnly = length - (sections - 1);

        int base = charsOnly / sections;
        int extra = charsOnly % sections;

        StringBuilder salt = new StringBuilder();

        for (int s = 0; s < sections; s++) {
            int size = base + (s < extra ? 1 : 0);

            for (int i = 0; i < size; i++) {
                salt.append(chars.charAt(CommandCenterConfig.RANDOM.nextInt(chars.length())
                ));
            }

            if (s < sections - 1) {
                salt.append("-");
            }
        }

        return salt.toString();
    }
    
    private final CommandCenterPlugin plugin;

    public TokenCommands(CommandCenterPlugin plugin) {
        this.plugin = plugin;
    }
    
    @CommandInfo(
            name = "generateToken",
            permission = "mcc.generatetoken",
            description = "Generate a secret token",
            usage = "[length] {permissions}",
            alias = { "gen", "token", "gentoken", "gt" }
    )
    public void generateToken(CommandContext context) {
        String lengthStr = context.getArg(0);
        
        if (context.isTabExecutor()) {
            if (context.argsLength() == 1) {
                context.suggest("[length]");
            } else if (context.argsLength() > 1) {
                plugin.getTools().forEach((tool) -> {
                    ToolProvider provider = (ToolProvider) tool.callHandler();
                    String permission = "tools." + provider.getName();
                    String currentArg = context.getArg(context.argsLength() - 1);
                    
                    if (currentArg == null || currentArg.isEmpty() || permission.startsWith(currentArg)) {
                        context.suggest(permission);
                    }
                });
            }
            
            return;
        }
        
        boolean pull = false;
        
        if (lengthStr == null) {
//            context.message("No length given, using default of 15");
            lengthStr = "15";
            pull = true;
        }
        
        int length;
        try {
            length = Integer.parseInt(lengthStr);
        } catch (Exception e) { 
            context.message("Error: Length must be a number!");
            return;
        }
        
        String genToken = getSaltString(length);
        
        List<String> permissions = Lists.newArrayList();
        if (context.argsLength() > 1) {
            // Pull from zero if expected length is not a number.
            // Start at 1 if 0 is a number. Start at 0 if 0 not a number.
            int startIndex = pull ? 0 : 1; 
            permissions = Arrays.asList(context.combineRemaining(startIndex).split(" "));
        } else {
            permissions.add("tools.all");
        }
        
        context.message("Generated Token: " + genToken);
        context.message("Added token to config with permissions: " + permissions);
        
       plugin.getMCCConfig().getTokenPermissions().put(genToken, permissions);
    }
    
    @CommandInfo(
            name = "displayTokens",
            description = "Display all tokens that have been created",
            permission = "mcc.displaytokens"
    )
    public void displayTokens(CommandContext context) {
        if (context.isTabExecutor()) return;
        
        context.message("Created Tokens:");
        context.message("===============================");
        
        plugin.getMCCConfig().getTokenPermissions().entrySet().forEach((entry) -> {
            String token = entry.getKey();
            List<String> permissions = entry.getValue();
            
            context.message("| " + token);
            permissions.forEach((str) -> {
                context.message("|  - " + str);
            });
        });
    }
}
