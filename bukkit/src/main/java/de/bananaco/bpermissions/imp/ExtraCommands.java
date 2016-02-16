package de.bananaco.bpermissions.imp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.Permission;
import de.bananaco.bpermissions.api.RecursiveGroupException;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExtraCommands {

    private static WorldManager wm = WorldManager.getInstance();

    public static boolean execute(String name, CalculableType type, String action, String value, String world) {
        Set<World> worlds = new HashSet<World>();
        // add all if null
        if (world == null || (world.equals("global") && wm.isUseGlobalUsers())) {
            worlds.addAll(wm.getAllWorlds());
        } else {
            worlds.add(wm.getWorld(world));
        }

        if (type == CalculableType.USER) {
            if (!name.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
                if (Bukkit.getPlayer(name) != null) {
                    name = Bukkit.getPlayer(name).getUniqueId().toString();
                } else {
                    name = Bukkit.getOfflinePlayer(name).getUniqueId().toString();
                }
            }
        }
        for (World w : worlds) {
            Calculable c = w.get(name, type);

            if (action.equalsIgnoreCase("addgroup")) {
                c.addGroup(value);
            } else if (action.equalsIgnoreCase("rmgroup")) {
                c.removeGroup(value);
            } else if (action.equalsIgnoreCase("setgroup")) {
                for (String g : new ArrayList<String>(c.getGroupsAsString())) {
                    c.removeGroup(g);
                }
                c.addGroup(value);
            } else if (action.equalsIgnoreCase("addperm")) {
                Permission perm = Permission.loadFromString(value);
                c.addPermission(perm.nameLowerCase(), perm.isTrue());
            } else if (action.equalsIgnoreCase("rmperm")) {
                c.removePermission(value);
            } else if (action.startsWith("addmeta")) {
                String meta = action.split(":")[1];
                c.setValue(meta, value);
            } else if (action.startsWith("rmmeta") || action.startsWith("cmeta")) {
                String meta = action.split(":")[1];
                c.removeValue(meta);
            } else {
                return false;
            }

            if (type == CalculableType.GROUP) {
                w.setupAll();
            } else {
                w.setupPlayer(name);
            }

        }

        return true;
    }
}