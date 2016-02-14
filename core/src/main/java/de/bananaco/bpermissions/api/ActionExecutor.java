package de.bananaco.bpermissions.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.bananaco.bpermissions.api.*;

public class ActionExecutor {

    private static WorldManager wm = WorldManager.getInstance();

    public static boolean execute(String name, CalculableType type, String action, String value, String world) {
        Set<World> worlds = new HashSet<World>();
        // add all if null
        if (world == null || (world.equals("global") && wm.isUseGlobalUsers())) {
            worlds.addAll(wm.getAllWorlds());
        } else {
            worlds.add(wm.getWorld(world));
        }

        for (World w : worlds) {
            Calculable c = w.get(name, type);
            //c.setDirty(true);

            if (action.equalsIgnoreCase(ActionType.ADD_GROUP.getName())) {
                c.addGroup(value);
            } else if (action.equalsIgnoreCase(ActionType.REMOVE_GROUP.getName())) {
                c.removeGroup(value);
            } else if (action.equalsIgnoreCase(ActionType.SET_GROUP.getName())) {
                for (String g : new ArrayList<String>(c.getGroupsAsString())) {
                    c.removeGroup(g);
                }
                c.addGroup(value);
            } else if (action.equalsIgnoreCase(ActionType.ADD_PERMISSION.getName())) {
                Permission perm = Permission.loadFromString(value);
                c.addPermission(perm.nameLowerCase(), perm.isTrue());
            } else if (action.equalsIgnoreCase(ActionType.REMOVE_PERMISSION.getName())) {
                c.removePermission(value);
            } else if (action.startsWith(ActionType.ADD_META.getName())) {
                String meta = action.split(":")[1];
                c.setValue(meta, value);
            } else if (action.startsWith(ActionType.REMOVE_META.getName())) {
                String meta = action.split(":")[1];
                c.removeValue(meta);
            } else {
                return false;
            }

            //w.setupAll();

        }

        return true;
    }
}