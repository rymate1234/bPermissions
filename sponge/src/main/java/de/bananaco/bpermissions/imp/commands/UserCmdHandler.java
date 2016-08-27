package de.bananaco.bpermissions.imp.commands;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.HashMap;

/**
 * Created by Ryan on 26/08/2016.
 */
public class UserCmdHandler extends UserGroupCmdHandler {

    public UserCmdHandler(HashMap<String, Commands> commands) {
        super(commands);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args, Commands commands) {
        return execute(src, args, commands, "user");
    }
}
