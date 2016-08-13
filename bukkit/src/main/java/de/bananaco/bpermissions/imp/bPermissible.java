package de.bananaco.bpermissions.imp;


/**
 * Evil Custom Permissible that retains standard SuperPerms behavior
 * <p/>
 * Found at https://github.com/weaondara/BungeePermsBukkit/blob/master/src/main/java/net/alpenblock/bungeeperms/platform/bukkit/Permissible.java
 **/

import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.util.Debugger;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;

public class bPermissible extends PermissibleBase {
    private Player player;
    private static WorldManager wm = WorldManager.getInstance();

    private Map<String, PermissionAttachmentInfo> permissions;
    private org.bukkit.permissions.Permissible oldpermissible = new PermissibleBase(null);
    private String world;
    private boolean permsLoaded = false;

    Map<String, Boolean> bpermissions;

    public bPermissible(Player player) {
        super(player);
        this.player = player;
        permissions = new LinkedHashMap<String, PermissionAttachmentInfo>() {
            @Override
            public PermissionAttachmentInfo put(String k, PermissionAttachmentInfo v) {
                PermissionAttachmentInfo existing = this.get(k);
                if (existing != null) {
                    return existing;
                }
                return super.put(k, v);
            }
        };

        setField(PermissibleBase.class, this, permissions, "permissions");
    }

    public org.bukkit.permissions.Permissible getOldPermissible() {
        return oldpermissible;
    }

    public void setOldPermissible(org.bukkit.permissions.Permissible oldPermissible) {
        this.oldpermissible = oldPermissible;
    }

    public boolean hasSuperPerm(String perm) {
        if (oldpermissible == null) {
            return super.hasPermission(perm);
        }
        return oldpermissible.hasPermission(perm);
    }

    @Override
    public boolean hasPermission(String permission) {
        // check with superperms first (gotta get them default perms yo)
        boolean ret = hasSuperPerm(permission);

        // then check with bPerms if it isn't set in superperms and it returned false
        if (!ret && !isPermissionSet(permission) && !bpermissions.isEmpty()) {
            long time = System.currentTimeMillis();
            Debugger.log("Begun check for " + permission);

            if (bpermissions.containsKey(permission)) {
                ret = Calculable.hasPermission(permission, bpermissions);
            }

            long finish = System.currentTimeMillis() - time;

            Debugger.log("Check for " + permission + ". took " + finish + "ms.");
        }

        // finally return it
        return ret;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getName());
    }

    @Override
    public synchronized void recalculatePermissions() {
        if (oldpermissible == null) {
            super.recalculatePermissions();
            return;
        }
        oldpermissible.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        if (oldpermissible == null) {
            return super.getEffectivePermissions();
        }
        return new LinkedHashSet<>(permissions.values());
    }

    @Override
    public boolean isOp() {
        if (oldpermissible == null) {
            return super.isOp();
        }
        return oldpermissible.isOp();
    }

    @Override
    public void setOp(boolean value) {
        if (oldpermissible == null) {
            super.setOp(value);
            return;
        }
        oldpermissible.setOp(value);
    }

    @Override
    public boolean isPermissionSet(String name) {
        if (oldpermissible == null) {
            return super.isPermissionSet(name);
        }
        return oldpermissible.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        if (oldpermissible == null) {
            return super.isPermissionSet(perm);
        }
        return oldpermissible.isPermissionSet(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        if (oldpermissible == null) {
            return super.addAttachment(plugin);
        }
        return oldpermissible.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        if (oldpermissible == null) {
            return super.addAttachment(plugin, ticks);
        }
        return oldpermissible.addAttachment(plugin, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        if (oldpermissible == null) {
            return super.addAttachment(plugin, name, value);
        }
        return oldpermissible.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        if (oldpermissible == null) {
            return super.addAttachment(plugin, name, value, ticks);
        }
        return oldpermissible.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        if (oldpermissible == null) {
            super.removeAttachment(attachment);
            return;
        }
        oldpermissible.removeAttachment(attachment);
    }

    @Override
    public synchronized void clearPermissions() {
        if (oldpermissible == null) {
            super.clearPermissions();
            return;
        }
        if (oldpermissible instanceof PermissibleBase) {
            PermissibleBase base = (PermissibleBase) oldpermissible;
            base.clearPermissions();
        }
    }

    public void setField(Class clazz, Object instance, Object var, String varname) {
        try {
            Field f = clazz.getDeclaredField(varname);
            f.setAccessible(true);
            f.set(instance, var);
        } catch (Exception ex) {
        }
    }

    public void setWorld(String world) {
        this.world = world;
        updatePerms();
    }

    private synchronized void updatePerms() {
        CalculableType type = CalculableType.USER;
        String name = player.getUniqueId().toString();

        if (world == null) {
            bpermissions = new HashMap<String, Boolean>();
        }

        bpermissions = ApiLayer.getEffectivePermissions(world, type, name);

        // get child perms as well (for vanish, etc.)
        for (String perm : new HashSet<>(bpermissions.keySet())) {
            Permission permission = Bukkit.getPluginManager().getPermission(perm);
            if (permission == null) {
                continue;
            }

            Map<String, Boolean> children = permission.getChildren();
            if (children == null || children.keySet().isEmpty() || children.isEmpty()) {
                continue;
            }

            for (String child : children.keySet()) {
                bpermissions.put(child, bpermissions.get(perm));
            }
        }
    }


    public Player getPlayer() {
        return player;
    }
}