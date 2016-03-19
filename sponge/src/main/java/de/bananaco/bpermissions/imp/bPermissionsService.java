package de.bananaco.bpermissions.imp;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.util.Debugger;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ryan on 18/03/2016.
 */
public class bPermissionsService implements PermissionService {
    WorldManager wm = WorldManager.getInstance();

    private final LoadingCache<String, bPermissionsSubjectCollection> subjectCollections = CacheBuilder.newBuilder().build(new CacheLoader<String, bPermissionsSubjectCollection>() {
        @Override
        public bPermissionsSubjectCollection load(String type) throws Exception {
            return new bPermissionsSubjectCollection(type, bPermissionsService.this);
        }
    });
    private SubjectData defaultData;

    @Override
    public SubjectCollection getUserSubjects() {
        try {
            return subjectCollections.get(SUBJECTS_USER);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SubjectCollection getGroupSubjects() {
        try {
            return subjectCollections.get(SUBJECTS_GROUP);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public SubjectData getDefaultData() {
        Debugger.log("Getting default subject data...");

        if (defaultData == null) {
            String g = wm.getDefaultWorld().getDefaultGroup();

            bPermissionsSubject subject = new bPermissionsSubject(g, getGroupSubjects());
            ((bPermissionsSubjectData) subject.getSubjectData()).getCalculableWithWorldName(wm.getDefaultWorld().getName());
            ((bPermissionsSubjectCollection) getGroupSubjects()).add(subject);

            defaultData = subject.getSubjectData();
        }
        return defaultData;
    }

    @Override
    public SubjectCollection getSubjects(String identifier) {
        Debugger.log("Getting subjects: " + identifier);

        if (identifier.equalsIgnoreCase(SUBJECTS_USER)) return getUserSubjects();
        if (identifier.equalsIgnoreCase(SUBJECTS_GROUP)) return getGroupSubjects();

        if (identifier.equalsIgnoreCase(SUBJECTS_SYSTEM)) try {
            return subjectCollections.get(SUBJECTS_SYSTEM);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, SubjectCollection> getKnownSubjects() {
        Debugger.log("Getting known subjects...");
        return null;
    }

    @Override
    public Optional<PermissionDescription.Builder> newDescriptionBuilder(Object plugin) {
        Optional<PluginContainer> container = Permissions.instance.game.getPluginManager().fromInstance(plugin);
        if (!container.isPresent()) {
            throw new IllegalArgumentException("Error when obtaining a plugin instance.");
        }
        return Optional.of(new PermissionDescriptionImpl.Builder(container.get(), this));
    }

    public void registerDescription(PermissionDescriptionImpl ret) {

    }

    @Override
    public Optional<PermissionDescription> getDescription(String permission) {
        return null;
    }

    @Override
    public Collection<PermissionDescription> getDescriptions() {
        return null;
    }

    @Override
    public void registerContextCalculator(ContextCalculator<Subject> calculator) {

    }

}
