package de.bananaco.bpermissions.imp.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;

/**
 * Utility class for commands
 */
public abstract class BaseCmdHandler implements CommandExecutor {

    public HashMap<String, Commands> commands;

    public BaseCmdHandler(HashMap<String, Commands> commands) {
        this.commands = commands;
    }

    public static Text format(String message) {
        TextColor vary = TextColors.GREEN;
        if (message.contains("!")) {
            vary = TextColors.RED;
        } else if (message.contains(":")) {
            vary = TextColors.AQUA;
        }

        return Text.builder("[bPermissions] ").color(TextColors.BLUE).append(
                Text.builder(message).color(vary).build()).build();
    }

    public static boolean hasPermission(CommandSource src, String node) {
        return src.hasPermission(node);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!commands.containsKey(getName(src))) {
            commands.put(getName(src), new Commands());
        }

        Commands cmd = commands.get(getName(src));

        return execute(src, args, cmd);
    }

    public abstract CommandResult execute(CommandSource src, CommandContext args, Commands commands);

    public void sendMessage(CommandSource sender, String message) {
        sender.sendMessage(format(message));
    }

    public static String getName(CommandSource src) {
        if (src instanceof Player) {
            return src.getName();
        }
        return "CONSOLE";
    }
}
