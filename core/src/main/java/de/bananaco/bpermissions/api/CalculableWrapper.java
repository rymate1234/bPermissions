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
        return hasPermission(node, getMappedPermissions());
    }

    private CalculableChange createChange() {
        CalculableChange change = new CalculableChange();
        change.setWorld(this.getWorld());
        change.setCalculable(this);

        return change;
    }

    /*
     * These methods are added to allow auto-saving of the World on any changes
     */
    @Override
    public void addGroup(String group) {
        super.addGroup(group);

        CalculableChange change = createChange();
        change.setType(ChangeType.ADD_GROUP);
        change.setGroup(group);
        updateCalculable(change);
    }

    @Override
    public void removeGroup(String group) {
        super.removeGroup(group);

        CalculableChange change = createChange();
        change.setType(ChangeType.REMOVE_GROUP);
        change.setGroup(group);
        updateCalculable(change);
    }

    public void replaceGroup(String oldGroup, String newGroup) {
        super.removeGroup(oldGroup);
        super.addGroup(newGroup);

        CalculableChange change = createChange();
        change.setType(ChangeType.REPLACE_GROUP);
        change.setGroup(newGroup);
        change.setReplacedGroup(oldGroup);
        updateCalculable(change);
    }

    @Override
    public void setGroup(String group) {
        super.setGroup(group);

        CalculableChange change = createChange();
        change.setType(ChangeType.SET_GROUP);
        change.setGroup(group);
        updateCalculable(change);
    }

    @Override
    public void addPermission(String permission, boolean isTrue) {
        super.addPermission(permission, isTrue);

        CalculableChange change = createChange();
        change.setType(ChangeType.ADD_PERMISSION);
        change.setPermission((isTrue ? "" : "^") + permission);
        updateCalculable(change);
    }

    @Override
    public void removePermission(String permission) {
        super.removePermission(permission);

        CalculableChange change = createChange();
        change.setType(ChangeType.REMOVE_PERMISSION);
        change.setPermission(permission);
        updateCalculable(change);
    }

    @Override
    public void setValue(String key, String value) {
        super.setValue(key, value);

        CalculableChange change = createChange();
        change.setType(ChangeType.SET_VALUE);
        change.setMetaKey(key);
        change.setMetaValue(value);
        updateCalculable(change);
    }

    @Override
    public void removeValue(String key) {
        CalculableChange change = createChange();
        change.setType(ChangeType.REMOVE_VALUE);
        change.setMetaKey(key);
        change.setMetaValue(this.getValue(key));

        super.removeValue(key);

        updateCalculable(change);
    }

    public void setLoaded() {
        this.loading = false;
    }

    private void updateCalculable(CalculableChange change) {
        if (loading) {
            return;
        }

        getWorldObject().runChangeListeners(change);

        setDirty(true);

        try {
            setCalculablesWithGroupDirty();
        } catch (RecursiveGroupException e) {
            e.printStackTrace();
        }

        if (wm.getAutoSave()) {
            getWorldObject().save();
            if (getType().equals(CalculableType.USER)) {
                try {
                    calculateGroups();
                    calculateEffectiveMeta();
                } catch (RecursiveGroupException e) {
                    e.printStackTrace();
                }

                if (getWorldObject().isOnline((User) this)) {
                    getWorldObject().setupPlayer(getNameLowerCase());
                }
            } else {
                getWorldObject().setupAll();
            }
        }
    }


    public void setCalculablesWithGroupDirty() throws RecursiveGroupException {
        if (getType() == CalculableType.USER) {
            // changing a user does not affect other calculables!
            return;
        }

        Set<Calculable> users = getWorldObject().getAll(CalculableType.USER);
        Set<Calculable> groups = getWorldObject().getAll(CalculableType.GROUP);
        if (users == null || users.size() == 0) {
            Debugger.log("Error setting users dirty");
        } else {
            for (Calculable user : users) {
                if (user.hasGroupRecursive(getName())) {
                    user.setDirty(true);
                    user.calculateEffectiveMeta();
                }
            }
        }
        if (groups == null || groups.size() == 0) {
            Debugger.log("Error setting groups dirty");
        } else {
            for (Calculable group : groups) {
                if (group.hasGroupRecursive(getName())) {
                    group.setDirty(true);
                }
            }
        }
    }
}
