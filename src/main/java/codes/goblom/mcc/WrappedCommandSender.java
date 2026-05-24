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

import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Bryan
 */
public class WrappedCommandSender implements CommandSender {
    
    private final CommandSender commandSender;
    private final CommandSender.Spigot spigotWrapped;
    
    private StringBuilder consoleOutput;
    
    public WrappedCommandSender(CommandSender sender) {
        this.commandSender = sender;
        this.consoleOutput = new StringBuilder();
        
        this.spigotWrapped = new CommandSender.Spigot() {
            @Override
            public void sendMessage(UUID sender, BaseComponent... components) {
                //Do we need to use the UUID somewhere?
                
                sendMessage(components);
            }

            @Override
            public void sendMessage(UUID sender, BaseComponent component) {
                // Do we need to uses the UUID somewhere?
                
                sendMessage(component);
            }

            @Override
            public void sendMessage(BaseComponent... components) {
                for (BaseComponent component : components) {
                    sendMessage(component);
                }
            }

            @Override
            public void sendMessage(BaseComponent component) {
                commandSender.spigot().sendMessage(component);
                
                String legacyText = TextComponent.toLegacyText(component);
                       legacyText = ChatColor.stripColor(legacyText);
                
                if (legacyText != null && !legacyText.isEmpty()) {
                    consoleOutput.append(legacyText).append("\n");
                } else {
                    
                    //TODO: Figure out if this is needed. Possibly remove the "{null}"
                    consoleOutput.append("{null}").append("\n");
                }
            }
            
        };
    }

    public String getConsoleOutput() {
        return consoleOutput.toString();
    }
    
    public void clearConsoleOutput() {
        consoleOutput = new StringBuilder();
    }
    
    public boolean hasOutput() {
        return !consoleOutput.isEmpty();
    }
    
    private String formattedConsoleMessage(String message) {
        return getName() + " " + message;
    }
    
    @Override
    public void sendMessage(String message) {
        commandSender.sendMessage(formattedConsoleMessage(message)); // We do thins so that the output is still shown to the CommandSender and then also forwards to the LLM
        
        String cleaned = ChatColor.stripColor(message);
        
//        if (!cleaned.isEmpty()) {
//            consoleOutput.append("\n");
//        }
        
        consoleOutput.append(cleaned).append("\n");
    }

    @Override
    public void sendMessage(String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(UUID sender, String message) {
        // Do we need to use UUID for something here?
        
        sendMessage(message);
    }

    @Override
    public void sendMessage(UUID sender, String... messages) {
        sendMessage(messages);
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public String getName() {
        return ((CommandCenterPlugin) JavaPlugin.getProvidingPlugin(WrappedCommandSender.class)).getMCCConfig().getConsolePrefix();
    }

    @Override
    public Spigot spigot() {
//        return consoleSender.spigot();
        return this.spigotWrapped;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return commandSender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return commandSender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return commandSender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return commandSender.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return commandSender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return commandSender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return commandSender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return commandSender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        commandSender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        commandSender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return commandSender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return commandSender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        commandSender.setOp(value);
    }
    
}
