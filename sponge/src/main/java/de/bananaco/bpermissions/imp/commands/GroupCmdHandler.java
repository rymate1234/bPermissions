package de.bananaco.bpermissions.imp.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

import java.util.HashMap;

public class GroupCmdHandler extends UserGroupCmdHandler {

    public GroupCmdHandler(HashMap<String, Commands> commands) {
        super(commands);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args, Commands commands) {
        return execute(src, args, commands, "group");
    }
}
