package de.bananaco.bpermissions.imp;

//import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;

/**
 * This code is distributed for your use and modification. Do what you like with
 * it, but credit me for the original!
 *
 * Also I'd be interested to see what you do with it.
 *
 * @author codename_B
 */
public class BukkitCompat {
    private static Field perms;

    static {
        try {
            perms = Permission.class.getDeclaredField("children");
            perms.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use to efficiently set a Map<String, Boolean> onto a Player Assumes one
     * large PermissionAttachment
     *
     * @param p
     * @param plugin
     * @param perm
     * @return
     */
    public static PermissionAttachment setPermissions(Permissible p, Plugin plugin, Map<String, Boolean> perm) {
        try {
            return doBukkitPermissions(p, plugin, perm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Thanks
     *
     * @Wolvereness it looks like my reflection is no longer needed!
     * @param p
     * @param plugin
     * @param permissions
     * @return
     */
    public static PermissionAttachment doBukkitPermissions(Permissible p, Plugin plugin, Map<String, Boolean> permissions) throws IllegalAccessException {
        Player player = (Player) p;
        String uuid = player.getUniqueId().toString();

        Permission permission = plugin.getServer().getPluginManager().getPermission(uuid);

        if (permission != null) {
            plugin.getServer().getPluginManager().removePermission(permission);
        }

        Map<String, Boolean> children = new HashMap<String, Boolean>();

        ListIterator<String> iter =
                new ArrayList<>(permissions.keySet()).listIterator(permissions.size());

        while (iter.hasPrevious()) {
            String perm = iter.previous();
            children.put(perm, permissions.get(perm));
        }

        permission = new Permission(uuid, PermissionDefault.FALSE);

        // A touch of reflection
        Map<String, Boolean> permissionChildren = (Map<String, Boolean>) perms.get(permission);
        permissionChildren.clear();
        permissionChildren.putAll(children);

        Permission permissionCheck = plugin.getServer().getPluginManager().getPermission(uuid);

        // sometimes we have to double check this
        if (permissionCheck != null) {
            plugin.getServer().getPluginManager().removePermission(permissionCheck);
        }

        plugin.getServer().getPluginManager().addPermission(permission);

        PermissionAttachment att = null;
        for (PermissionAttachmentInfo pai : new HashSet<PermissionAttachmentInfo>(player.getEffectivePermissions())) {
            if (pai.getAttachment() != null && pai.getAttachment().getPlugin() != null) {
                if (pai.getAttachment().getPlugin() instanceof Permissions) {
                    att = pai.getAttachment();
                    break;
                }
            }
        }
        // only if null
        if (att == null) {
            att = player.addAttachment(plugin);
            att.setPermission(uuid, true);
        }
        // recalculate permissions
        player.recalculatePermissions();
        return att;
    }

    public static void runTest(Player player, Plugin plugin) {
        long start, finish, time;
        // 1000 example permissions
        Map<String, Boolean> permissions = new HashMap<String, Boolean>();
        Set<String> keys = new HashSet<String>(permissions.keySet());

        for (int i = 0; i < 10000; i++) {
            permissions.put("example." + String.valueOf(i), true);
        }

        // superpermissions
        start = System.currentTimeMillis();
        PermissionAttachment att = player.addAttachment(plugin);
        // and obviously we iteratively add here!
        for (String key : permissions.keySet()) {
            att.setPermission(key, permissions.get(key));
        }
        finish = System.currentTimeMillis();
        time = finish - start;
        System.out.println("SuperPermissions default took: " + time + "ms.");
        if (!player.hasPermission("example.1")) {
            System.err.println("permissions not registered!");
        }
        // cleanup
        for (String key : keys) {
            att.unsetPermission(key);
        }
        att.remove();
        if (player.hasPermission("example.1")) {
            System.err.println("permissions not unregistered!");
        }
        // supersuperpermissions
        start = System.currentTimeMillis();
        try {
            att = BukkitCompat.doBukkitPermissions(player, plugin, permissions);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        finish = System.currentTimeMillis();
        time = finish - start;
        if (!player.hasPermission("example.1")) {
            System.err.println("permissions not registered!");
        }
        // cleanup
        att.unsetPermission(player.getName());
        att.remove();
        System.out.println("SuperPermissions hack took: " + time + "ms.");
        if (player.hasPermission("example.1")) {
            System.err.println("permissions not unregistered!");
        }
        // ourpermissions
        start = System.currentTimeMillis();
        att = BukkitCompat.setPermissions(player, plugin, permissions);
        finish = System.currentTimeMillis();
        time = finish - start;
        if (!player.hasPermission("example.1")) {
            System.err.println("permissions not registered!");
        }
        System.out.println("bPermissions default took: " + time + "ms.");
        // cleanup
        for (String key : keys) {
            att.unsetPermission(key);
        }
        att.remove();
        if (player.hasPermission("example.1")) {
            System.err.println("permissions not unregistered!");
        }
    }
}
