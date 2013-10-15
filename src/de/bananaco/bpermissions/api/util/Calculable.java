package de.bananaco.bpermissions.api.util;

import java.util.Set;
import java.util.TreeSet;

import de.bananaco.bpermissions.api.Permission;

@Deprecated
public abstract class Calculable extends de.bananaco.bpermissions.api.Calculable {

    public Calculable(String name, TreeSet<String> groups,
                      Set<Permission> permissions, String world) {
        super(name, groups, permissions, world);
    }
}
