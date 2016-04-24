package de.bananaco.bpermissions.imp;

//import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import de.bananaco.bpermissions.util.loadmanager.TaskThread;
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
            return doBukkitMultiPermissions(p, plugin, perm);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * The following methods for applying permissions are based off code by @Wolvereness
     */


    /**
     * This method uses two permissions for a player - one with positive nodes and one with negative modes
     *
     * @param p
     * @param plugin
     * @param permissions
     * @return
     */
    public static synchronized PermissionAttachment doBukkitMultiPermissions(final Permissible p, Plugin plugin, Map<String, Boolean> permissions) throws IllegalAccessException {
        final Player player;
        if (p instanceof bPermissible) {
            player = ((bPermissible) p).getPlayer();
        } else {
            player = (Player) p;
        }
        String uuid = player.getUniqueId().toString();


        Permission positive = plugin.getServer().getPluginManager().getPermission(uuid);
        Permission negative = plugin.getServer().getPluginManager().getPermission("^" + uuid);

        if (positive != null) {
            plugin.getServer().getPluginManager().removePermission(positive);
        }
        if (negative != null) {
            plugin.getServer().getPluginManager().removePermission(negative);
        }

        Map<String, Boolean> po = new HashMap<String, Boolean>();
        Map<String, Boolean> ne = new HashMap<String, Boolean>();

        for (String key : permissions.keySet()) {
            if (permissions.get(key)) {
                po.put(key, true);
            } else {
                ne.put(key, false);
            }
        }

        positive = new Permission(uuid, PermissionDefault.FALSE);
        negative = new Permission("^" + uuid, PermissionDefault.FALSE);

        // A touch of reflection
        Map<String, Boolean> positiveChildren = (Map<String, Boolean>) perms.get(positive);
        positiveChildren.clear();
        positiveChildren.putAll(po);

        // keeps the doBukkitPermissions times down
        Map<String, Boolean> negativeChildren = (Map<String, Boolean>) perms.get(negative);
        negativeChildren.clear();
        negativeChildren.putAll(ne);

        Permission positiveCheck = plugin.getServer().getPluginManager().getPermission(uuid);
        Permission negativeCheck = plugin.getServer().getPluginManager().getPermission("^" + uuid);

        // sometimes we have to double check this
        if (positiveCheck != null) {
            plugin.getServer().getPluginManager().removePermission(positiveCheck);
        }
        // i blame threads
        if (negativeCheck != null) {
            plugin.getServer().getPluginManager().removePermission(negativeCheck);
        }

        plugin.getServer().getPluginManager().addPermission(positive);
        plugin.getServer().getPluginManager().addPermission(negative);

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
            att.setPermission("^" + uuid, true);
        }

        player.recalculatePermissions();

        return att;
    }

    /**
     * This method uses one permission for a player - holding both negative and positive nodes
     *
     * Currently not used - will add a config option later
     *
     * @param p
     * @param plugin
     * @param permissions
     * @return
     */
    public static synchronized PermissionAttachment doBukkitPermissions(final Permissible p, Plugin plugin, Map<String, Boolean> permissions) throws IllegalAccessException {
        final Player player;
        if (p instanceof bPermissible) {
             player = ((bPermissible) p).getPlayer();
        } else {
            player = (Player) p;
        }
        String uuid = player.getUniqueId().toString();

        Permission permission = plugin.getServer().getPluginManager().getPermission(uuid);

        if (permission != null) {
            plugin.getServer().getPluginManager().removePermission(permission);
        }

        permission = new Permission(uuid, PermissionDefault.FALSE);

        // A touch of reflection
        Map<String, Boolean> permissionChildren = (Map<String, Boolean>) perms.get(permission);
        permissionChildren.clear();
        permissionChildren.putAll(permissions);

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

        player.recalculatePermissions();

        return att;
    }

    public static void runTest(Player player, Plugin plugin) {
        bPermissible permissible = null;

        if (player instanceof bPermissible) {
            permissible = (bPermissible) player;
            ((bPermissible) player).setWorld(player.getWorld().getName());
        } else {
            permissible = new bPermissible(player);
            org.bukkit.permissions.Permissible oldpermissible = Injector.inject(player, permissible);
            permissible.setOldPermissible(oldpermissible);
            permissible.setWorld(player.getWorld().getName());
        }

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
            att = BukkitCompat.doBukkitPermissions(permissible, plugin, permissions);
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
        att = BukkitCompat.setPermissions(permissible, plugin, permissions);
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
