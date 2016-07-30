package de.bananaco.permissions.fornoobs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import de.bananaco.bpermissions.api.Group;
import de.bananaco.bpermissions.api.User;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;

public class ForNoobs {

    private final WorldManager wm = WorldManager.getInstance();
    private final JavaPlugin plugin;

    public ForNoobs(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addAll() {
        System.out.println("Adding to example files...");
        try {
            addDefaults(wm.getWorld(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Files created!");
    }

    private void addDefaults(World world) throws Exception {
        boolean autosave = wm.getAutoSave();
        wm.setAutoSave(false);
        ArrayList<String> regPerms = getPermissions();
        // Do the groups first
        Group admin = world.getGroup("admin");
        Group mod = world.getGroup("moderator");
        Group def = world.getGroup(world.getDefaultGroup());
        // Let's sort the permissions into shizzledizzle

        for (String perm : regPerms) {
            if (perm.contains("user") || perm.contains("build")) {
                def.addPermission(perm, true);
            } else if (perm.contains(".ban") || perm.contains(".kick") || perm.contains(".mod") || perm.contains(".fly")) {
                mod.addPermission(perm, true);
            } else {
                admin.addPermission(perm, true);
            }
        }

        // admin
        admin.addGroup(mod.getName());
        admin.addPermission("group." + mod, false);
        admin.addPermission("group." + admin, true);
        admin.setValue("prefix", "&5admin");

        // moderator
        mod.addGroup(def.getName());
        mod.addPermission("group." + def, false);
        mod.addPermission("group." + mod, true);
        mod.setValue("prefix", "&7moderator");

        // default
        def.addPermission("group." + def, true);
        def.setValue("prefix", "&9user");

        // Now do some example users
        String user1 = "codename_B";
        String user2 = "rymate1234";
        String user3 = "Notch";
        String user4 = "pyraetos";

        // And set their groups
        // user 1
        User user = world.getUser(user1);
        user.getGroupsAsString().clear();
        user.addGroup(admin.getName());
        user.setValue("prefix", "&8old developer");

        // user 2
        user = world.getUser(user2);
        user.getGroupsAsString().clear();
        user.addGroup(admin.getName());
        user.setValue("prefix", "&8developer");

        // user 3
        user = world.getUser(user3);
        user.getGroupsAsString().clear();
        user.addGroup(mod.getName());
        user.setValue("prefix", "&8mojang");

        // user 4
        user = world.getUser(user4);
        user.setValue("prefix", "&3helper");

        // Finally, save the changes
        world.save();
        wm.setAutoSave(autosave);
    }

    private ArrayList<String> getPermissions() {
        ArrayList<String> regPerms = new ArrayList<String>();
        for (Permission p : plugin.getServer().getPluginManager().getPermissions()) {
            if (!p.getName().equals("*") && !p.getName().equals("*.*")) {
                regPerms.add(p.getName());
            }
        }
        Collections.sort(regPerms, new Comparator<String>() {
            public int compare(String a, String b) {
                return a.compareTo(b);
            }
        ;
        });

		return regPerms;
    }
}
