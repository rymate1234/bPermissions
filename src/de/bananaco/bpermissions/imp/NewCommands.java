package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewCommands {

    private final WorldManager instance = WorldManager.getInstance();
    private World world = null;
    private CalculableType calc = null;
    private String name = null;
    private boolean singleWorld = false;

    protected NewCommands() {
        if (Bukkit.getServer().getWorlds().size() == 1) {
            world = instance.getWorld(Bukkit.getServer().getWorlds().get(0).getName());
        }
    }

    protected String format(String message) {
        return Permissions.format(message);
    }

    public void setWorld(String w, CommandSender sender) {
        World world = instance.getWorld(w);
        // If the world does not exist
        if (world == null) {
            sender.sendMessage(format("Please select a loaded world!"));
            return;
        }

        this.world = world;
        this.singleWorld = true;
        sender.sendMessage(format("Set selected world to " + world.getName()));
    }

    public World getWorld() {
        return world;
    }

    public void setCalculable(CalculableType type, String c) {
        // wow such code
        calc = type;
        name = c;
    }

    public Calculable getCalculable() {
        if (name == null) {
            return null;
        }
        if (calc == null) {
            return null;
        }
        return world.get(name, calc);
    }

    public Calculable getCalculable(World world) {
        if (name == null) {
            return null;
        }
        if (calc == null) {
            return null;
        }
        return world.get(name, calc);
    }

    /*
     * Main functions go here
     */
    public void addGroup(String group, CommandSender sender) {
        getCalculable().addGroup(group);
        sender.sendMessage(format("Added " + group + " to " + getCalculable().getName()));
    }

    public void removeGroup(String group, CommandSender sender) {
        getCalculable().removeGroup(group);
        sender.sendMessage(format("Removed " + group + " from " + getCalculable().getName()));
    }

    public void setGroup(String group, CommandSender sender) {
        if (singleWorld) {
            getCalculable().getGroupsAsString().clear();
            getCalculable().addGroup(group);
        } else {
            Set<World> worlds = new HashSet<World>();

            worlds.addAll(instance.getAllWorlds());

            for (World w : worlds) {
                getCalculable(w).getGroupsAsString().clear();
                getCalculable(w).addGroup(group);
            }
        }
    }

    public void listGroups(CommandSender sender) {
        List<String> groups = getCalculable().serialiseGroups();
        String[] gr = groups.toArray(new String[groups.size()]);
        String mgr = Arrays.toString(gr);
        sender.sendMessage(format("The " + getCalculable().getType().getName() + " " + getCalculable().getName() + " has these groups:"));
        sender.sendMessage(mgr);
    }

    public void addPermission(String permission, CommandSender sender) {
        Permission perm = Permission.loadFromString(permission);
        getCalculable().addPermission(perm.name(), perm.isTrue());
        sender.sendMessage(format("Added " + perm.toString() + " to " + getCalculable().getName()));
    }

    public void removePermission(String permission, CommandSender sender) {
        getCalculable().removePermission(permission);
        sender.sendMessage(format("Removed " + permission + " from " + getCalculable().getName()));
    }

    public void listPermissions(CommandSender sender) {
        List<String> permissions = getCalculable().serialisePermissions();
        String[] pr = permissions.toArray(new String[permissions.size()]);
        String mpr = Arrays.toString(pr);
        sender.sendMessage(format("The " + getCalculable().getType().getName() + " " + getCalculable().getName() + " has these permissions:"));
        sender.sendMessage(mpr);
    }

    public void hasPermission(String node, CommandSender sender) {
        Calculable c = getCalculable();
        if (c instanceof User) {
            User user = (User) c;
            sender.sendMessage(format(user.getName() + " - " + node + ": " + user.hasPermission(node)));
            Player player = Bukkit.getPlayer(user.getName());
            if (player != null) {
                sender.sendMessage(format("SUPERPERMS" + " - " + node + ": " + player.hasPermission(node)));
            }
        } else if (c instanceof Group) {
            Group group = (Group) c;
            sender.sendMessage(format(group.getName() + " - " + node + ": " + group.hasPermission(node)));
        }
    }

    public void setValue(String key, String value, CommandSender sender) {
        if (singleWorld) {
            getCalculable().setValue(key, value);
        } else {
            Set<World> worlds = new HashSet<World>();

            worlds.addAll(instance.getAllWorlds());

            for (World w : worlds) {
                getCalculable(w).setValue(key, value);
            }
        }
    }

    public void showValue(String key, CommandSender sender) {
        String value = getCalculable().getValue(key);
        sender.sendMessage(format("Meta for " + calc.getName() + " " + getCalculable().getName() + " - " + key + ": " + value));
    }

    /**
     * Remind the user to save when changes are finished!
     */
    public void save() {
        // Now saves everything
        WorldManager.getInstance().saveAll();
    }

    public void clearMeta(String value, CommandSender sender) {
        getCalculable().removeValue(value);
        sender.sendMessage(format("Meta for " + calc.getName() + " " + getCalculable().getName() + " - cleared"));
    }
}
