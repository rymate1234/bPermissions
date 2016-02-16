package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.World;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Implements group tab completing in bPermissions
 *
 * Created by rymate1234 on 10/02/2016.
 */

public class GroupsTabCompleter extends BaseCommand implements TabCompleter {
    private final Map<String, Commands> commands;

    public GroupsTabCompleter(Map<String, Commands> commands) {
        this.commands = commands;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        /*
         * Create an entry in the commands selection if one does not exist
         */
        if (!commands.containsKey(getName(sender))) {
            commands.put(getName(sender), new Commands());
        }

        Commands cmd = commands.get(getName(sender));

        if (cmd.getWorld() == null) {
            cmd.setDefaultWorld(sender);
        }

        World world = cmd.getWorld();
        Set<Calculable> groupsMap = world.getAll(CalculableType.GROUP);

        List<String> completion = new ArrayList<String>();

        if (checkCommand(command, args)) {
            for (Calculable group : groupsMap) {
                String toCheck;
                if (command.getName().equalsIgnoreCase("group")) {
                    toCheck = args[0].toLowerCase();
                    if (args.length == 2) {
                        toCheck = args[1].toLowerCase();
                    }
                } else if (command.getName().equalsIgnoreCase("setgroup") || command.getName().equalsIgnoreCase("user")) {
                    toCheck = args[1].toLowerCase();
                } else {
                    break;
                }
                String groupName = group.getNameLowerCase();
                if (groupName.startsWith(toCheck)) {
                    completion.add(group.getName());
                }
            }
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                String toCheck;
                if (command.getName().equalsIgnoreCase("setgroup") || command.getName().equalsIgnoreCase("user")) {
                    toCheck = args[0].toLowerCase();
                } else {
                    break;
                }

                String playerName = p.getName().toLowerCase();
                if (playerName.startsWith(toCheck)) {
                    completion.add(p.getName());
                }
            }
        }


        return completion;
    }

    private boolean checkCommand(Command command, String[] args) {
        return (command.getName().equalsIgnoreCase("group") && args.length >= 1)
                || (command.getName().equalsIgnoreCase("setgroup") && args.length == 2)
                || (command.getName().equalsIgnoreCase("user") && args.length == 2 && args[0].toLowerCase().contains("group"));
    }
}