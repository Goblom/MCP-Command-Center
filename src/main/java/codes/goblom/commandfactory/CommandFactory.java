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

import codes.goblom.mcpai.Configuration;
import com.google.common.collect.Lists;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Bryan
 */
public abstract class CommandFactory {
    public class ExecutorMap {

        final Method method;
        final CommandInfo info;
        final CommandListener exec;
        
        public ExecutorMap(Method method, CommandInfo info, CommandListener exec) {
            this.method = method;
            this.info = info;
            this.exec = exec;
        }
    }
    
    private static CommandMap commandMap;
    
    protected static CommandMap getCommandMap() {
        if (commandMap != null) {
            return commandMap;
        }
        
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                  f.setAccessible(true);
                  
            commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        
        return getCommandMap();
    }
    
    private final List<ExecutorMap> execMap = Lists.newArrayList();
    private final JavaPlugin plugin;
    
    public CommandFactory(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public abstract void sendMessage(CommandSender sender, String message);
    
    public void executeNoArgsWithStub(CommandContext context) { }
    
    public final boolean invokeCommand(boolean isTab, CommandSender sender, String cmd, String args) {
        return invokeCommand(isTab, sender, cmd, args.split(" "));
    }
    
    public final boolean invokeCommand(boolean isTab, CommandSender sender, String cmd, String... args) {
        ExecutorMap map = getMap(cmd);
        
        if (map == null) {
            return false;
        }
        
        try {
            CommandContext context = new CommandContext(this, sender, args, false);
            
            map.method.invoke(map.exec, context);
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
    public final void register() {
        register(null);
    }
    
    public final void register(String commandStub) {
        if (commandStub != null && !commandStub.isEmpty()) {
            WrappedCommand cmd = new WrappedCommand(plugin, this, commandStub, true);
                           cmd.setUsage("/" + commandStub + " [args]");
                           cmd.setDescription("Main command that holds sub commands");
            getCommandMap().register(commandStub, cmd);
        } else {
            execMap.forEach((map) -> {
                WrappedCommand cmd = new WrappedCommand(plugin, this, map.info.name(), false);
                
                if (!map.info.description().isEmpty()) {
                    cmd.setDescription(map.info.description());
                }
                
                if (!map.info.usage().isEmpty()) {
                    cmd.setUsage(map.info.usage());
                }
                
                if (map.info.alias().length > 0) {
                    cmd.setAliases(Arrays.asList(map.info.alias()));
                }
                
                boolean registered = getCommandMap().register(map.info.name(), cmd);
                
//               Configuration.PLUGIN.debug(Level.INFO, "Registered " + map.info.name() + ": " + registered);
            });
        }
    }
    
    public final List<CommandInfo> getAvailableCommands() {
        return Lists.transform(execMap,  (em) -> em.info);
    }
    
    private ExecutorMap getMap(String label) {
        return execMap.stream().filter((map) -> map.info.name().equalsIgnoreCase(label) || Arrays.asList(map.info.alias()).contains(label)).findFirst().orElse(null);
    }
    
    public final void addExecutors(CommandListener listener) {
        Class clazz = listener.getClass();
        
        while (clazz != null) {
            Arrays.asList(clazz.getDeclaredMethods()).stream()
                    .filter((m) -> m.isAnnotationPresent(CommandInfo.class))
                    .forEach((m) -> {
                        CommandInfo info = m.getAnnotation(CommandInfo.class);
                        m.setAccessible(true);
                        
                        execMap.add(new ExecutorMap(m, info, listener));
                    });
            clazz = clazz.getSuperclass();
        }
    }
    
    private static final class WrappedCommand extends Command { 
        private final JavaPlugin plugin;
        private final CommandFactory factory;
        private final boolean shiftArgs;
        
        public WrappedCommand(JavaPlugin plugin, CommandFactory factory, String name, boolean shiftArgs) {
            super(name);
            this.plugin = plugin;
            this.factory = factory;
            this.shiftArgs = shiftArgs;
        }
        
        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
//            System.out.println("Alias: " + alias + " || Args: " + Arrays.asList(args));

            List<String> suggest = Lists.newArrayList();
            CommandContext context = new CommandContext(factory, sender, args, true);
            
            if (shiftArgs) {                
                context = new CommandContext(factory, sender, Arrays.copyOfRange(args, 1, args.length), true); //Dont have start arg... that is origination
                alias = args[0];
            }
            
            ExecutorMap map = factory.getMap(alias);
            final String fAlias = alias;
            
            if (map == null) {                
                factory.execMap.stream().filter((m) -> {
                    if (!m.info.permission().isEmpty()) {
                        return sender.hasPermission(m.info.permission());
                    }
                    
                    return true;
                }).filter((m) -> {
                    return m.info.name().startsWith(fAlias);
                }).forEachOrdered((m) -> {
                    suggest.add(m.info.name() + (!m.info.usage().isEmpty() ? " " + m.info.usage() : ""));
                });
            } else {
                try {
                    map.method.invoke(map.exec, context);
                    
                    if (context.getTabComplete() != null && !context.getTabComplete().isEmpty()) {
                        suggest.addAll(context.getTabComplete());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            return suggest;
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            CommandContext context = new CommandContext(factory, sender, args);
            
            if (shiftArgs) {
                if (args.length == 0) {
                    factory.executeNoArgsWithStub(context);

                    return true;
                }
                
                //Override with new values that are accurate now
                context = new CommandContext(factory, sender, Arrays.copyOfRange(args, 1, args.length)); //Dont have start arg... that is origination
                label = args[0];
            }
            
            final ExecutorMap executor = factory.getMap(label);
            
            if (executor == null) {
                // Is only for stub...
                factory.executeNoArgsWithStub(context);
                return true;
            }
            
            if (!executor.info.permission().isEmpty()) {
                String permission = executor.info.permission();

                if (!sender.hasPermission(permission)) {
                    context.message(executor.info.noPermissionMessage());
                    return true;
                }
            }

            final CommandContext fContext = context;

            Runnable r = () -> {
                try {
                    executor.method.invoke(executor.exec, fContext);
                } catch (Throwable e) { //Use generic Throwable because a command may throw a different error
                    e.printStackTrace();
                }
            };

            if (executor.info.async()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
            } else {
                r.run();
            }

            return true;
        }
    }
}
