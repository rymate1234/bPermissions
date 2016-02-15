package de.bananaco.bpermissions.api;

import de.bananaco.bpermissions.util.Debugger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class wraps around the Calculable and effectively does the same job,
 * just with the added benefit of calculating a Map<String, Boolean> for the
 * Set<Permission>
 * for direct access and faster permission node checking.
 *
 * Currently both User and Group extends MapCalculable.
 */
// "temporary" extend for backwards compatability
public abstract class MapCalculable extends de.bananaco.bpermissions.api.util.Calculable {

    private boolean calculatingMapped;

    public MapCalculable(String name, Set<String> groups,
            Set<Permission> permissions, String world) {
        super(name, groups, permissions, world);
    }

    private Map<String, Boolean> permissions = new HashMap<String, Boolean>();

    /**
     * Return the calculated map The map will be blank unless
     * calculateMappedPermissions has been called which admittedly is very
     * likely to have happened.
     *
     * @return Map<String, Boolean>
     */
    public Map<String, Boolean> getMappedPermissions() {
        if (isDirty()) {
            try {
                calculateMappedPermissions();
            } catch (RecursiveGroupException e) {
                e.printStackTrace();
            }
        } else if (permissions.size() == 0) {
            try {
                // force-dirty
                setDirty(true);
                calculateMappedPermissions();
            } catch (RecursiveGroupException e) {
                e.printStackTrace();
            }
        }
        return permissions;
    }

    public void calculateMappedPermissions() throws RecursiveGroupException {
        if (calculatingMapped) {
            return;
        }

        if (!isDirty()) {
            return;
        }

        calculatingMapped = true;

        long time = System.currentTimeMillis();
        HashMap<String, Boolean> tempPerms = new HashMap<String, Boolean>();
        for (Permission perm : getEffectivePermissions()) {
            tempPerms.put(perm.nameLowerCase(), perm.isTrue());
        }
        this.calculateEffectiveMeta();

        permissions.clear();
        permissions = tempPerms;

        setDirty(false);
        long finish = System.currentTimeMillis()-time;

        Debugger.log("Calculated mapped permissions for " + getType().getName() + " " + getName() + ". Took " + finish + "ms.");
        calculatingMapped = false;
    }

    @Override
    public void clear() {
        this.permissions.clear();
        super.clear();
        setDirty(true);
    }
}
