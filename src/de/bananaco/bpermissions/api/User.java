package de.bananaco.bpermissions.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * User extends MapCalculable to make permission checks in the HasPermission
 * class considerably faster. A slight increase in memory usage for a dramatic
 * increase in speed is definately a worthwhile trade-off.
 */
public class User extends CalculableWrapper {

    private World w;

    public User(String name, World w) {
        this(name, null, null, w.getName(), w);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public User(String name, TreeSet<String> groups, Set<Permission> permissions,
            String world, World w) {
        super(name, groups == null ? new TreeSet() : groups, permissions, world);
        this.w = w;
    }

    @Override
    public CalculableType getType() {
        return CalculableType.USER;
    }

    @Override
    protected World getWorldObject() {
        return w;
    }

    // override metadata here to use inherited priority too!
    @Override
    public int getPriority() {
        if (getEffectiveMeta().containsKey("priority")) {
            return Integer.parseInt(getEffectiveMeta().get("priority"));
        }
        return 0;
    }
}
