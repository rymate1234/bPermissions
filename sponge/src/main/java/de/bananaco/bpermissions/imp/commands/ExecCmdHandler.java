package de.bananaco.bpermissions.imp.commands;

import de.bananaco.bpermissions.api.ActionExecutor;
import de.bananaco.bpermissions.api.CalculableType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Ryan on 28/08/2016.
 */
public class ExecCmdHandler extends BaseCmdHandler {
    public ExecCmdHandler(HashMap<String, Commands> commands) {
        super(commands);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext context, Commands commands) {
        Optional<String[]> argsOptional = context.<String[]>getOne("args");

        if (!argsOptional.isPresent()) {
            sendMessage(src, "No arguments to /exec command!");

        } else {
            String[] args = argsOptional.get();

            String name = "null";
            CalculableType type = CalculableType.USER;
            String action = "null";
            String value = "null";
            String world = null;
            for (String c : args) {
                if (c.startsWith("u:") || c.startsWith("g:")) {
                    if (c.startsWith("u:")) {
                        type = CalculableType.USER;
                    } else {
                        type = CalculableType.GROUP;
                    }
                    name = c.split(":")[1];
                } else if (c.startsWith("a:")) {
                    String[] actionArray = c.split(":");
                    if (actionArray.length == 3) {
                        action = actionArray[1] + ":" + actionArray[2];
                    } else {
                        action = actionArray[1];
                    }
                } else if (c.startsWith("v:")) {
                    value = c.split(":")[1];
                } else if (c.startsWith("w:")) {
                    world = c.split(":")[1];
                }
            }

            Text message = Text.builder("Executing action: ").color(TextColors.GOLD)
                    .append(Text.builder(action + " " + value).color(TextColors.GREEN).build(),
                            Text.builder(" in " ).color(TextColors.GOLD).build(),
                            Text.builder((world == null ? "all worlds" : "world: " + world)).color(TextColors.GREEN).build()).build();

            src.sendMessage(message);

            boolean executed = ActionExecutor.execute(name, type, action, value, world);
            if (executed) {
                Text successMessage = Text.builder("Action applied to ").color(TextColors.GOLD)
                        .append(Text.builder(type.getName() + " " + name).color(TextColors.GREEN).build()).build();

                src.sendMessage(successMessage);
            } else {
                sendMessage(src, "Invalid exec command!");
            }
        }

        return CommandResult.success();
    }
}
