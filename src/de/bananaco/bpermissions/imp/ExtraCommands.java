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
            } else if (action.equalsIgnoreCase("setprefix")) {
                c.setValue("prefix", value);
            } else if (action.equalsIgnoreCase("setsuffix")) {
                c.setValue("suffix", value);
            } else if (action.equalsIgnoreCase("setpriority")) {
                c.setValue("priority", value);
            } else if (action.equalsIgnoreCase("rmprefix")) {
                c.removeValue("prefix");
            }    else if (action.equalsIgnoreCase("rmsuffix")) {
                c.removeValue("suffix");
            }
            try {
                c.calculateEffectiveMeta();
                c.calculateEffectivePermissions();
            } catch (RecursiveGroupException ex) {
                Logger.getLogger(ExtraCommands.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}