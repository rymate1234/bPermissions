package de.bananaco.bpermissions.api;

import de.bananaco.bpermissions.util.Debugger;

import java.util.Set;

public abstract class CalculableWrapper extends MapCalculable {

    private WorldManager wm = WorldManager.getInstance();

    boolean loading = true;

    public CalculableWrapper(String name, Set<String> groups,
            Set<Permission> permissions, String world) {
        super(name, groups, permissions, world);

    }

    public boolean hasPermission(String node) {
        node = node.toLowerCase();
        boolean allowed = hasPermission(node, getMappedPermissions());
        return allowed;
    }

    /*
     * These methods are added to allow auto-saving of the World on any changes
     */

    /*
     * These methods are added to allow auto-saving of the World on any changes
     */
    @Override
    public void addGroup(String group) {
        super.addGroup(group);
        updateCalculable();
    }

    @Override
    public void removeGroup(String group) {
        super.removeGroup(group);
        updateCalculable();
    }

    @Override
    public void addPermission(String permission, boolean isTrue) {
        if (getPermissionsAsString().contains(permission)) {
            return;
        }
        super.addPermission(permission, isTrue);
        updateCalculable();
    }

    @Override
    public void removePermission(String permission) {
        if (!getPermissionsAsString().contains(permission)) {
            return;
        }
        super.removePermission(permission);
        updateCalculable();
    }

    @Override
    public void setValue(String key, String value) {
        super.setValue(key, value);
        updateCalculable();
    }

    @Override
    public void removeValue(String key) {
        super.removeValue(key);
        updateCalculable();
    }

    public void setLoaded() {
        this.loading = false;
    }


    private void updateCalculable() {
        if (loading) {
            return;
        }

        setDirty(true);
        setCalculablesWithGroupDirty();
        if (wm.getAutoSave()) {
            getWorldObject().save();
            if (getType().equals(CalculableType.USER)) {
                if (getWorldObject().isOnline((User) this)) getWorldObject().setupPlayer(getNameLowerCase());
            } else {
                getWorldObject().setupAll();
            }
        }
    }


    public void setCalculablesWithGroupDirty() {
        Set<Calculable> users = getWorldObject().getAll(CalculableType.USER);
        Set<Calculable> groups = getWorldObject().getAll(CalculableType.GROUP);
        if (users == null || users.size() == 0) {
            Debugger.log("Error setting users dirty");
        } else {
            for (Calculable user : users) {
                if (user.hasGroupRecursive(getName())) {
                    ((User) user).setDirty(true);
                }
            }
        }
        if (groups == null || groups.size() == 0) {
            Debugger.log("Error setting groups dirty");
        } else {
            for (Calculable group : groups) {
                if (group.hasGroupRecursive(getName())) {
                    ((Group) group).setDirty(true);
                }
            }
        }
    }
}
