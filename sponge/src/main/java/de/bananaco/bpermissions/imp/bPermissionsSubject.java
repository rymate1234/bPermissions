package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.MapCalculable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.service.permission.option.OptionSubjectData;
import org.spongepowered.api.util.Tristate;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Ryan on 18/03/2016.
 */
public class bPermissionsSubject implements OptionSubject {
    private final bPermissionsSubjectCollection collection;
    private final String identifier;
    private bPermissionsSubjectData subjectData;

    public bPermissionsSubject(String identifier, SubjectCollection subjectCollection) {
        this.identifier = identifier;
        this.collection = (bPermissionsSubjectCollection) subjectCollection;
        this.subjectData = new bPermissionsSubjectData(identifier, collection.getType());
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        final CommandSource source = Permissions.instance.game.getServer().getPlayer(UUID.fromString(identifier)).get();
        if (source != null) {
            return Optional.of(source);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return collection;
}

    @Override
    public OptionSubjectData getSubjectData() {
        return this.subjectData;
    }

    @Override
    public OptionSubjectData getTransientSubjectData() {
        return null;
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        Calculable c = ((bPermissionsSubjectData) getSubjectData()).getCalculableWithContext(contexts);
        if (c != null) {
            return c.hasPermission(permission);
        }
        return false;
    }

    @Override
    public boolean hasPermission(String permission) {
        Calculable c = ((bPermissionsSubjectData) getSubjectData()).getCalculable();
        if (c != null) {
            return c.hasPermission(permission);
        }
        return false;
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        Tristate ret = Tristate.UNDEFINED;

        Calculable c = ((bPermissionsSubjectData) getSubjectData()).getCalculableWithContext(contexts);

        if (c == null) {
            return Tristate.UNDEFINED;
        }

        if (c instanceof MapCalculable) {
            if (((MapCalculable) c).getMappedPermissions().get(permission) != null) {
                boolean b = c.hasPermission(permission);
                return Tristate.fromBoolean(b);
            }
        }

        return ret;
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        return false;
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        return null;
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        Calculable c = ((bPermissionsSubjectData) getSubjectData()).getCalculableWithContext(contexts);
        if (c != null && c.getEffectiveMeta().containsKey(key)) {
            return Optional.of(c.getEffectiveMeta().get(key));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getOption(String key) {
        Calculable c = ((bPermissionsSubjectData) getSubjectData()).getCalculable();
        if (c != null && c.getEffectiveMeta().containsKey(key)) {
            return Optional.of(c.getEffectiveMeta().get(key));
        }
        return Optional.empty();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public Set<Context> getActiveContexts() {
        return null;
    }
}
