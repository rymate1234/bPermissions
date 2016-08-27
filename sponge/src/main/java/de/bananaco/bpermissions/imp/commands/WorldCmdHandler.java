package de.bananaco.bpermissions.imp.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Ryan on 25/08/2016.
 */
public class WorldCmdHandler extends BaseCmdHandler {

    public WorldCmdHandler(HashMap<String, Commands> commands) {
        super(commands);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args, Commands cmd) {
        Optional<WorldProperties> optional = args.<WorldProperties>getOne("world");
        if (!optional.isPresent()) {
            if (cmd.getWorld() == null) {
                sendMessage(src, "No world selected.");
            } else {
                sendMessage(src, "Currently selected world: " + cmd.getWorld().getName());
            }
        } else {
            cmd.setWorld(optional.get().getWorldName(), src);
        }

        return CommandResult.success();
    }
}
