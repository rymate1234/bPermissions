package de.bananaco.bpermissions.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The Group object extends Calculable which allows recursive inheritance of
 * unlimited depth. This is the new bPermissions, and it is awesome.
 */
public class Group extends CalculableWrapper {

    private World w;

    public Group(String name, World w) {
        this(name, null, null, w.getName(), w);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Group(String name, TreeSet<String> groups, Set<Permission> permissions,
            String world, World w) {
        super(name, groups == null ? new TreeSet() : groups,
                permissions, world);
        this.w = w;
    }

    @Override
    public CalculableType getType() {
        return CalculableType.GROUP;
    }

    @Override
    protected World getWorldObject() {
        return w;
    }
}
