package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Ryan on 18/03/2016.
 */
public class bPermissionsSubjectData implements OptionSubjectData {

    private final String identifier;
    private final CalculableType type;
    private Calculable calculable;
    WorldManager wm = WorldManager.getInstance();

    public bPermissionsSubjectData(String identifier, CalculableType type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public Map<Set<Context>, Map<String, String>> getAllOptions() {
        return null;
    }

    @Override
    public Map<String, String> getOptions(Set<Context> contexts) {
        return null;
    }

    @Override
    public boolean setOption(Set<Context> contexts, String key, String value) {
        return false;
    }

    @Override
    public boolean clearOptions(Set<Context> contexts) {
        return false;
    }

    @Override
    public boolean clearOptions() {
        return false;
    }

    @Override
    public Map<Set<Context>, Map<String, Boolean>> getAllPermissions() {
        return null;
    }

    @Override
    public Map<String, Boolean> getPermissions(Set<Context> contexts) {
        return null;
    }

    @Override
    public boolean setPermission(Set<Context> contexts, String permission, Tristate value) {
        return false;
    }

    @Override
    public boolean clearPermissions() {
        return false;
    }

    @Override
    public boolean clearPermissions(Set<Context> contexts) {
        return false;
    }

    @Override
    public Map<Set<Context>, List<Subject>> getAllParents() {
        return null;
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return null;
    }

    @Override
    public boolean addParent(Set<Context> contexts, Subject parent) {
        return false;
    }

    @Override
    public boolean removeParent(Set<Context> contexts, Subject parent) {
        return false;
    }

    @Override
    public boolean clearParents() {
        return false;
    }

    @Override
    public boolean clearParents(Set<Context> contexts) {
        return false;
    }

    public void setWorld(String world) {
        World w = wm.getWorld(world);
        calculable = w.get(identifier, type);
    }

    public Calculable getCalculable() {
        return calculable;
    }

    public Calculable getCalculableWithContext(Set<Context> contexts) {
        Calculable c = getCalculable();

        if (contexts == null) return c;

        for (Context context : contexts) {
            if (context.getKey().equalsIgnoreCase(Context.WORLD_KEY)) {
                World w = wm.getWorld(context.getValue());
                if (w != null) {
                    c = w.get(identifier, type);
                }
                break;
            }
        }

        return c;
    }

    public Calculable getCalculableWithWorldName(String name) {
        Calculable c = getCalculable();

        World w = wm.getWorld(name);
        if (w != null) {
            c = w.get(identifier, type);
        }

        return c;
    }
}
