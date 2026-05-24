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
package codes.goblom.commandfactory;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Bryan
 */
public class CommandContext {
    
    private final CommandSender sender;
    private final CommandFactory factory;
    private final String[] args;
    
    private List<String> tabComplete;
    final boolean tabExecutor;

    public CommandSender getSender() {
        return sender;
    }

    public CommandFactory getFactory() {
        return factory;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean isTabExecutor() {
        return tabExecutor;
    }
    
    CommandContext(CommandFactory factory, CommandSender sender, String[] args) {
        this.factory = factory;
        this.sender = sender;
        this.args = args;
        this.tabExecutor = false;
    }
    
    CommandContext(CommandFactory factory, CommandSender sender, String[] args, boolean isTab) {
        this.sender = sender;
        this.factory = factory;
        this.args = args;
        this.tabExecutor = isTab;
    }
    
    public List<String> getTabComplete() {
        if (tabComplete == null) return null;
        
        if (sender instanceof Player) {
            return tabComplete;
        }
        
        return Lists.transform(tabComplete, ChatColor::stripColor);
    }
    
    public boolean hasArg(int i) {
        try {
            return args[i] != null && !args[i].trim().isEmpty() && args[i].replace(" ", "").length() != 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getArg(int i) {
        try {
            return ChatColor.stripColor(args[i]);
        } catch (Exception e) {
            return null; //return null instead of exception
        }
    }
    
    public String combineRemaining(int start) {
        if (!hasArg(start)) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < args.length; i++) {
            sb.append(getArg(i)).append(" ");
        }

        return sb.toString().trim();
    }
    
    public String getArgs(int start, int end) {
        if (!hasArg(start) || !hasArg(end)) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();

        for (int i = start; i < args.length && i != end; i++) {
            sb.append(getArg(i)).append(" ");
        }

        return sb.toString().trim();
    }
    
    public int argsLength() {
        return args.length;
    }
    
    public void suggest(String suggestion) {
        if (tabComplete == null) {
            this.tabComplete = new ArrayList();
        }
        
        tabComplete.add(ChatColor.stripColor(suggestion));
    }
    
    public void message(String... messages) {
//        System.out.println("isTabExecutor = " + isTabExecutor());
        if (isTabExecutor()) {
            for (String m : messages) {
                suggest(m);
            }
            
            return;
        }
        
        for (String message : messages) {
            factory.sendMessage(sender, message);
        }
    }
}
