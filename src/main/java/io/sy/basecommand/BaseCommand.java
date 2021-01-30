package io.sy.basecommand;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class BaseCommand implements CommandExecutor, TabCompleter {

    @NotNull private final Map<String, BaseCommand> subCommands;
    @NotNull private final JavaPlugin plugin;
    @NotNull private final String command;
    private final boolean consoleUse;
    @Nullable private final String permission;
    @Nullable private final String noConsoleUsageMessage;
    @Nullable private final String commandUsageMessage;
    @Nullable private final String noPermissionMessage;
    @NotNull private final Set<String> aliases;

    public BaseCommand(CommandInfo info) {
        this.subCommands = new HashMap<>();
        this.plugin = info.getPlugin();
        this.command = info.getCommand();
        this.consoleUse = info.isConsoleUse();
        this.permission = info.getPermission();
        this.noConsoleUsageMessage = info.getNoConsoleUsageMessage();
        this.noPermissionMessage = info.getNoPermissionMessage();
        this.commandUsageMessage = info.getWrongUsageMessage() +  info.getCommandUsageMessage();
        this.aliases = info.getAliases().parallelStream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    //- Utilities
    @NotNull public String[] subArray(@NotNull String[] current) {
        String[] n = new String[current.length - 1];
        System.arraycopy(current, 1, n, 0, n.length);
        return n;
    }

    @NotNull private Optional<BaseCommand> findNext(@NotNull String command) {
        String x = command.toLowerCase();
        BaseCommand fromMap = subCommands.get(x);

        if(fromMap == null) return subCommands.values().stream().filter(cmd -> cmd.aliases.contains(x)).findFirst();
        else return Optional.of(fromMap);
    }

    public void subCommand(BaseCommand command) { subCommands.put(command.getCommand(), command); }

    //- Command Executor
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        run(commandSender, args);
        return true;
    }

    private void run(CommandSender sender, String[] args) {

        //- If console tries to use the command & is NOT allowed to -> send message if not null (should NOT be null as default message is set)
        if(!(sender instanceof Player)&& !consoleUse) {
            if(getNoConsoleUsageMessage() != null)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getNoConsoleUsageMessage()));
        }

        //- No sub commands or arguments present
        else if(subCommands.size() == 0 || args.length == 0) {
            run0(sender, args);
        }

        //-
        else {
            Optional<BaseCommand> optionalSubCommand = findNext(args[0]);

            //- If a sub command is found -> run it using the array without current sub command
            if(optionalSubCommand.isPresent()) optionalSubCommand.get().run(sender, subArray(args));

                //- If no sub command is present -> run command as is
            else run0(sender, args);
        }

    }

    public void run0(CommandSender sender, String[] args) {

        //- Checking if the sender has the permission -> messaging if they DO NOT
        if(permission != null && !sender.hasPermission(permission)) {
            if(getNoPermissionMessage() != null)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getNoPermissionMessage()));
        }

        else {

            //- Executing for both player and console
            executeBoth(sender, args);

            //- Executing if player only
            if(sender instanceof Player) executePlayer((Player) sender, args);

                //- Executing if console only
            else executeConsole(sender, args);

        }

    }

    //- These should be left empty -> they are what the plugin will fill in
    public void executeBoth(@NotNull CommandSender sender, @NotNull String[] args) {}
    public void executeConsole(@NotNull CommandSender sender, @NotNull String[] args) {}
    public void executePlayer(@NotNull Player player, @NotNull String[] args) {}

    //- Tab Completion
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return getTabComplete0(sender, args);
    }

    private List<String> getTabComplete0(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) return new ArrayList<>();
        else if (subCommands.size() == 0 || args.length <= 1) return getTabCompletion(sender, args);
        else return findNext(args[0]).map(cmd -> cmd.getTabComplete0(sender, subArray(args))).orElseGet(() -> getTabCompletion(sender, args));
    }

    public List<String> getTabCompletion(@NotNull CommandSender sender, String[] args) {
        //- Returning an empty list if no sub commands present
        if (args.length != 1) return new ArrayList<>();
        //- Returning a list of all sub commands that the player HAS permission to use or a sub command that DOES NOT have a permission set
        return getSubCommands().entrySet().stream()
                .filter(e -> (e.getValue().getPermission() == null || sender.hasPermission(e.getValue().getPermission())) && e.getKey().startsWith(args[0].toLowerCase()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

}
