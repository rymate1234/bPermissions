package de.bananaco.bpermissions.imp.commands;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.WorldManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Ryan on 27/08/2016.
 */
public abstract class UserGroupCmdHandler extends BaseCmdHandler{
    public UserGroupCmdHandler(HashMap<String, Commands> commands) {
        super(commands);
    }

    public CommandResult execute(CommandSource src, CommandContext args, Commands commands, String cmd) {
        Calculable calc = commands.getCalculable();
        CalculableType type = cmd.equalsIgnoreCase("user") ? CalculableType.USER : CalculableType.GROUP;
        CalculableType opposite = !cmd.equalsIgnoreCase("user") ? CalculableType.USER : CalculableType.GROUP;

        if ((WorldManager.getInstance().isUseGlobalUsers()) && (cmd.equalsIgnoreCase("user")) && (commands.getWorld() != WorldManager.getInstance().getDefaultWorld())) {
            sendMessage(src, "Changing to the global world");
            commands.setWorld("global", src);
        }

        Optional<GameProfile> user = args.<GameProfile>getOne("player");

        Optional<String> group = args.<String>getOne("group");

        Optional<String> action = args.<String>getOne("action");


        if (!user.isPresent() && !group.isPresent() && !action.isPresent()) {
            if (calc == null) {
                sendMessage(src, "Nothing is selected!");
            } else {
                sendMessage(src, "Currently selected " + calc.getType().getName() + ": " + calc.getName());
            }
        } else if ((user.isPresent() || group.isPresent()) && !action.isPresent()) {
            if (cmd.equalsIgnoreCase("user")) {
                GameProfile usr = user.get();

                commands.setCalculable(type, usr, src);
            } else {
                commands.setCalculable(type, group.get(), src);
            }
        }

        return CommandResult.success();
    }
}
