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

    public CommandResult execute(CommandSource src, CommandContext context, Commands cmd, String command) {
        Calculable calc = cmd.getCalculable();
        CalculableType type = command.equalsIgnoreCase("user") ? CalculableType.USER : CalculableType.GROUP;
        CalculableType opposite = !command.equalsIgnoreCase("user") ? CalculableType.USER : CalculableType.GROUP;

        if ((WorldManager.getInstance().isUseGlobalUsers()) && (command.equalsIgnoreCase("user")) && (cmd.getWorld() != WorldManager.getInstance().getDefaultWorld())) {
            sendMessage(src, "Changing to the global world");
            cmd.setWorld("global", src);
        }

        Optional<GameProfile> user = context.<GameProfile>getOne("player");
        Optional<String> group = context.<String>getOne("group");
        Optional<String[]> actions = context.<String[]>getOne("action");

        if (!user.isPresent() && !group.isPresent() && !actions.isPresent()) {
            if (calc == null) {
                sendMessage(src, "Nothing is selected!");
            } else {
                sendMessage(src, "Currently selected " + calc.getType().getName() + ": " + calc.getName());
            }
        } else if ((user.isPresent() || group.isPresent()) && !actions.isPresent()) {
            if (command.equalsIgnoreCase("user")) {
                GameProfile usr = user.get();

                cmd.setCalculable(type, usr, src);
            } else {
                cmd.setCalculable(type, group.get(), src);
            }
        } else if (actions.isPresent()) {
            String[] args = actions.get();

            if (args.length == 2 || (args.length > 2 && args[0].equalsIgnoreCase("list"))) {
                if (calc == null) {
                    sendMessage(src, "Nothing is selected!");
                } else if (calc.getType() != type) {
                    sendMessage(src, "Please select a " + type.getName() + ", you currently have a " + opposite.getName() + " selected.");
                } else {
                    String action = args[0];
                    String value = args[1];
                    if (action.equalsIgnoreCase("addgroup")) {
                        cmd.addGroup(value, src);
                    } else if (action.equalsIgnoreCase("rmgroup")) {
                        cmd.removeGroup(value, src);
                    } else if (action.equalsIgnoreCase("setgroup")) {
                        cmd.setGroup(value, src);
                    } else if (action.equalsIgnoreCase("list")) {
                        int page = 1;
                        if (args.length == 3 && !args[2].equalsIgnoreCase("all"))
                            page = Integer.parseInt(args[2]);
                        else
                            page = -1;

                        value = value.toLowerCase();
                        if (value.equalsIgnoreCase("groups") || value.equalsIgnoreCase("group") || value.equalsIgnoreCase("g")) {
                            cmd.listGroups(src);
                        } else if (value.startsWith("perm") || value.equalsIgnoreCase("p")) {
                            cmd.listPermissions(src, page);
                        }
                    } else if (action.equalsIgnoreCase("meta")) {
                        cmd.showValue(value, src);
                    } else if (action.equalsIgnoreCase("cmeta")) {
                        cmd.clearMeta(value, src);
                    } else if (action.equalsIgnoreCase("addperm")) {
                        cmd.addPermission(value, src);
                    } else if (action.equalsIgnoreCase("rmperm")) {
                        cmd.removePermission(value, src);
                    } else if (action.equals("has")) {
                        cmd.hasPermission(value, src);
                    } else {
                        sendMessage(src, "Please consult the command documentation!");
                    }
                }
            } else if (args.length >= 3 && args[0].equalsIgnoreCase("meta")) {
                if (calc == null) {
                    sendMessage(src, "Nothing is selected!");
                } else if (calc.getType() != type) {
                    sendMessage(src, "Please select a " + type.getName() + ", you currently have a " + opposite.getName() + " selected.");
                } else {
                    String meta = "";
                    for(int i = 2; i < args.length; i++){
                        String arg = args[i] + " ";
                        meta = meta + arg;
                    }
                    meta = meta.trim();
                    cmd.setValue(args[1], meta, src);
                }
            } else {
                sendMessage(src, "Too many arguments.");
            }
        }

        return CommandResult.success();
    }
}
