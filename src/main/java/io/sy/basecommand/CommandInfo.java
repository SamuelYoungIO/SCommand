package io.sy.basecommand;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

@Builder @Getter
public class CommandInfo {

    @NotNull private final JavaPlugin plugin;
    @NotNull private final String command;
    @Builder.Default private final String wrongUsageMessage = "&c[!] Wrong use of command.";
    @Builder.Default private final String commandUsageMessage = "/help";
    private final boolean consoleUse;
    @Nullable @Builder.Default private final String permission = null;
    @Builder.Default private final String noConsoleUsageMessage = "&c[!] This is a player only command.";
    @Builder.Default private final String noPermissionMessage = "&c[!] You do not have permissions to execute this command.";
    @NotNull final private List<String> aliases;

}
