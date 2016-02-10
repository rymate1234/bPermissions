package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan on 25/05/2015.
 */
public class OldUserGroupCommand extends BaseCommand implements CommandExecutor {
    private final Permissions plugin;
    private final Map<String, Commands> commands;
    private final Map<String, String> mirrors = new HashMap<String, String>();
    private final Mirrors mrs = new Mirrors(mirrors);

    public OldUserGroupCommand(Permissions plugin, Map<String, Commands> commands) {
        this.plugin = plugin; // Store the plugin in situations where you need it.
        this.commands = commands;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean allowed = true;

        if (sender instanceof Player) {
            allowed = hasPermission((Player) sender, "bPermissions.admin")
                    || sender.isOp();
        }

        if (!allowed) {
            sendMessage(sender, "You're not allowed to do that!");
            return true;
        }

        /*
         * Create an entry in the commands selection if one does not exist
         */
        if (!commands.containsKey(getName(sender))) {
            commands.put(getName(sender), new Commands());
        }

        Commands cmd = commands.get(getName(sender));
        /*
         * Selecting and displaying the currently selected world
         */
        if (command.getName().equalsIgnoreCase("world")) {
            World world = cmd.getWorld();
            if (args.length == 0) {
                if (world == null) {
                    sendMessage(sender, "No world selected.");
                } else {
                    sendMessage(sender, "Currently selected world: " + world.getName());
                }
            } else if (args.length == 1) {
                cmd.setWorld(args[0], sender);
            } else if (args.length == 3 && args[0].equalsIgnoreCase("mirror")) {
                String worldFrom = args[1];
                String worldTo = args[2];
                mirrors.put(worldFrom, worldTo);
                mrs.save();
                sender.sendMessage(worldFrom + " mirrored to " + worldTo);
            } else {
                sendMessage(sender, "Too many arguments.");
            }
            return true;
        }

        /*
         * User/group
         *
         * Much is repeated here
         */
        if (command.getName().equalsIgnoreCase("user") || command.getName().equalsIgnoreCase("group")) {
            Calculable calc = cmd.getCalculable();
            CalculableType type = command.getName().equalsIgnoreCase("user") ? CalculableType.USER : CalculableType.GROUP;
            CalculableType opposite = !command.getName().equalsIgnoreCase("user") ? CalculableType.USER : CalculableType.GROUP;

            /*
             * Selecting, displaying, and executing commands on the Calculable
             */

            if ((WorldManager.getInstance().isUseGlobalUsers()) && (command.getName().equalsIgnoreCase("user")) && (cmd.getWorld() != WorldManager.getInstance().getDefaultWorld())) {
                sendMessage(sender, "Changing to the global world");
                cmd.setWorld("global", sender);
            }

            if (args.length == 0) {
                if (calc == null) {
                    sendMessage(sender, "Nothing is selected!");
                } else {
                    sendMessage(sender, "Currently selected " + calc.getType().getName() + ": " + calc.getName());
                }
            } else if (args.length == 1) {
                if (command.getName().equalsIgnoreCase("user")) {
                    String uuid;
                    if (!args[0].matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                        if (plugin.getServer().getPlayer(args[0]) != null) {
                            uuid = plugin.getServer().getPlayer(args[0]).getUniqueId().toString();
                        } else {
                            uuid = plugin.getServer().getOfflinePlayer(args[0]).getUniqueId().toString();
                        }
                    } else {
                        uuid = args[0];
                    }
                    cmd.setCalculable(type, uuid, sender);
                } else {
                    cmd.setCalculable(type, args[0], sender);
                }
            } else if (args.length == 2 || (args.length > 2 && args[0].equalsIgnoreCase("list"))) {
                if (calc == null) {
                    sendMessage(sender, "Nothing is selected!");
                } else if (calc.getType() != type) {
                    sendMessage(sender, "Please select a " + type.getName() + ", you currently have a " + opposite.getName() + " selected.");
                } else {
                    String action = args[0];
                    String value = args[1];
                    if (action.equalsIgnoreCase("addgroup")) {
                        cmd.addGroup(value, sender);
                    } else if (action.equalsIgnoreCase("rmgroup")) {
                        cmd.removeGroup(value, sender);
                    } else if (action.equalsIgnoreCase("setgroup")) {
                        cmd.setGroup(value, sender);
                    } else if (action.equalsIgnoreCase("list")) {
                        int page = 1;
                        if (args.length == 3)
                            page = Integer.parseInt(args[2]);

                        value = value.toLowerCase();
                        if (value.equalsIgnoreCase("groups") || value.equalsIgnoreCase("group") || value.equalsIgnoreCase("g")) {
                            cmd.listGroups(sender);
                        } else if (value.startsWith("perm") || value.equalsIgnoreCase("p")) {
                            cmd.listPermissions(sender, page);
                        }
                    } else if (action.equalsIgnoreCase("meta")) {
                        cmd.showValue(value, sender);
                    } else if (action.equalsIgnoreCase("cmeta")) {
                        cmd.clearMeta(value, sender);
                    } else if (action.equalsIgnoreCase("addperm")) {
                        cmd.addPermission(value, sender);
                    } else if (action.equalsIgnoreCase("rmperm")) {
                        cmd.removePermission(value, sender);
                    } else if (action.equals("has")) {
                        cmd.hasPermission(value, sender);
                    } else {
                        sendMessage(sender, "Please consult the command documentation!");
                    }
                    calc.setDirty(true);
                    ApiLayer.update();
                }
            } else if (args.length == 3 && args[0].equalsIgnoreCase("meta")) {
                if (calc == null) {
                    sendMessage(sender, "Nothing is selected!");
                } else if (calc.getType() != type) {
                    sendMessage(sender, "Please select a " + type.getName() + ", you currently have a " + opposite.getName() + " selected.");
                } else {
                    cmd.setValue(args[1], args[2], sender);
                }
            } else {
                sendMessage(sender, "Too many arguments.");
            }
            return true;
        }
        return true;
    }
}