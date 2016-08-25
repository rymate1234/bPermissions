package de.bananaco.bpermissions.imp.commands;

/**
 * Class for executing commands
 *
 * Created by Ryan on 25/08/2016.
 */
import java.util.*;

import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.imp.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.*;

public class Commands {
    private final WorldManager instance = WorldManager.getInstance();
    private World world = null;
    private CalculableType calc = null;
    private String name = null;

    protected Commands() {
        if (Sponge.getServer().getWorlds().size() == 1) {
            org.spongepowered.api.world.World w = getDefaultWorld();
            world = instance.getWorld(w.getName());
        }
    }

    protected Text format(String message) {
        return BaseCmdHandler.format(message);
    }

    public void setDefaultWorld(CommandSource sender) {
        if (instance.getUseGlobalFiles() || (calc == CalculableType.USER && instance.isUseGlobalUsers()))
            setWorld("global", sender);
        else
            setWorld(getDefaultWorld().getName(), sender);
    }

    public void setWorld(String w, CommandSource sender) {
        World world = instance.getWorld(w);
        // If the world does not exist
        if (world == null) {
            sender.sendMessage(format("Please select a loaded world!"));
            return;
        }
        // If a different world is selected
        if (!world.equals(this.world)) {
            calc = null;
        }

        this.world = world;
        sender.sendMessage(format("Set selected world to " + world.getName()));
    }

    public World getWorld() {
        return world;
    }

    public void setCalculable(CalculableType type, String c, CommandSource sender) {
        // If the world does not exist
        if (world == null) {
            sender.sendMessage(format("No world selected, selecting the default world"));
            sender.sendMessage(format("To select a world use: /world worldname"));

            if (instance.getUseGlobalFiles() || (type == CalculableType.USER && instance.isUseGlobalUsers()))
                setWorld("global", sender);
            else
                setWorld(getDefaultWorld().getName(), sender);
        }

        calc = type;
        name = c;
        sender.sendMessage(format(getCalculable().getName() + " selected."));
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

    /*
     * Main functions go here
     */
    public void addGroup(String group, CommandSource sender) {
        //getCalculable().addGroup(group);
        boolean executed = ActionExecutor.execute(name, calc, "addgroup", group, world.getName());
        if (executed) {
            sender.sendMessage(format("Added " + group + " to " + getCalculable().getName()));
        } else {
            sender.sendMessage(format("Failed to add " + group + " to " + getCalculable().getName()));
        }
    }

    public void removeGroup(String group, CommandSource sender) {
        //getCalculable().removeGroup(group);
        boolean executed = ActionExecutor.execute(name, calc, "rmgroup", group, world.getName());
        if (executed) {
            sender.sendMessage(format("Removed " + group + " from " + getCalculable().getName()));
        } else {
            sender.sendMessage(format("Failed to add " + group + " to " + getCalculable().getName()));
        }
    }

    public void setGroup(String group, CommandSource sender) {
        //getCalculable().getGroupsAsString().clear();
        //getCalculable().addGroup(group);
        boolean executed = ActionExecutor.execute(name, calc, "setgroup", group, world.getName());
        if (executed) {
            sender.sendMessage(format("Set " + getCalculable().getName() + "'s group to " + group));
        } else {
            sender.sendMessage(format("Failed to set " + getCalculable().getName() + "'s group to " + group));
        }
    }

    public void listGroups(CommandSource sender) {
        List<String> groups = getCalculable().serialiseGroups();
        String[] gr = groups.toArray(new String[groups.size()]);
        String mgr = Arrays.toString(gr);
        sender.sendMessage(format("The " + getCalculable().getType().getName() + " " + getCalculable().getName() + " has these groups:"));
        sender.sendMessage(Text.of(mgr));
    }

    public void addPermission(String permission, CommandSource sender) {
        //Permission perm = Permission.loadFromString(permission);
        //getCalculable().addPermission(perm.name(), perm.isTrue());
        boolean executed = ActionExecutor.execute(name, calc, "addperm", permission, world.getName());
        if (executed) {
            sender.sendMessage(format("Added " + permission + " to " + getCalculable().getName()));
        } else {
            sender.sendMessage(format("Failed to add " + permission + " to " + getCalculable().getName()));
        }
    }

    public void removePermission(String permission, CommandSource sender) {
        //getCalculable().removePermission(permission);
        boolean executed = ActionExecutor.execute(name, calc, "rmperm", permission, world.getName());
        if (executed) {
            sender.sendMessage(format("Removed " + permission + " from " + getCalculable().getName()));
        } else {
            sender.sendMessage(format("Failed to remove " + permission + " from " + getCalculable().getName()));
        }
    }

    public void listPermissions(CommandSource sender, int page) {
        List<Permission> permissionsSet = (getCalculable()).getEffectivePermissions();
        Permission[] permissions = permissionsSet.toArray(new Permission[permissionsSet.size()]);
        int length = (int) (Math.round((permissions.length + 5) / 10.0) * 10.0);
        int maxPages = length / 10;
        int end = page * 10;
        if (end > permissions.length)
            end = permissions.length - 1;
        int beginning = end - 10;

        if (permissionsSet.size() < 10 || page == -1) {
            maxPages = 1;
            beginning = 0;
            end = permissionsSet.size();
        }

        if (maxPages < page) {
            sender.sendMessage(format("Page " + page + " doesn't exist!"));
            return;
        }

        sender.sendMessage(format("Permissions for " + getCalculable().getType().getName() + " " + getCalculable().getName() + ": Page " + page + " of " + maxPages));
        for (int i = beginning; i < end; i++) {
            Permission permission = permissions[i];
            sender.sendMessage(format(" - " + permission.name() + ": " + permission.isTrue()));
        }
    }

    public void hasPermission(String node, CommandSource sender) {
        Calculable c = getCalculable();
        if (c instanceof User) {
            User user = (User) c;
            sender.sendMessage(format(user.getName() + " - " + node + ": " + user.hasPermission(node)));
            Optional<Player> player = Sponge.getServer().getPlayer(UUID.fromString(user.getName()));
            if (player.isPresent()) {
                sender.sendMessage(format("SUPERPERMS" + " - " + node + ": " + player.get().hasPermission(node)));
            }
        } else if (c instanceof Group) {
            Group group = (Group) c;
            sender.sendMessage(format(group.getName() + " - " + node + ": " + group.hasPermission(node)));
        }
    }

    public void setValue(String key, String value, CommandSource sender) {
        //getCalculable().setValue(key, value);
        boolean executed = ActionExecutor.execute(name, calc, "addmeta:" + key, value, world.getName());
        if (executed) {
            sender.sendMessage(format(key + " set to " + value + " for " + getCalculable().getName()));
        } else {
            sender.sendMessage(format("Failed to set key " + key + " to " + value + " for " + getCalculable().getName()));
        }
    }

    public void showValue(String key, CommandSource sender) {
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

    public void clearMeta(String value, CommandSource sender) {
        //getCalculable().removeValue(value);
        boolean executed = ActionExecutor.execute(name, calc, "rmmeta:" + value, "", world.getName());
        if (executed) {
            sender.sendMessage(format("Meta for " + calc.getName() + " " + getCalculable().getName() + " - cleared"));
        } else {
            sender.sendMessage(format("Failed to clear " + calc.getName() + " meta value for " + getCalculable().getName()));
        }
    }

    public org.spongepowered.api.world.World getDefaultWorld() {
        return (org.spongepowered.api.world.World) Sponge.getServer().getWorlds().toArray()[0];
    }
}