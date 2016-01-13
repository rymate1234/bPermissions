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

    public static void execute(String name, CalculableType type, String action, String value, String world) {
        Set<World> worlds = new HashSet<World>();
        // add all if null
        if (world == null) {
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
            }

            try {
                c.calculateGroups();
                c.calculateEffectiveMeta();
                c.calculateEffectivePermissions();

                wm.update();
            } catch (RecursiveGroupException ex) {
                Logger.getLogger(ExtraCommands.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}