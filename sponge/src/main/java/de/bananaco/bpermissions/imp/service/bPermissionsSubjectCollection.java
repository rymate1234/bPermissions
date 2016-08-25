package de.bananaco.bpermissions.imp.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.bananaco.bpermissions.api.CalculableType;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Ryan on 18/03/2016.
 */
public class bPermissionsSubjectCollection implements SubjectCollection {
    private final String type;
    private final bPermissionsService service;

    private final LoadingCache<String, bPermissionsSubject> subjects = CacheBuilder.newBuilder().build(new CacheLoader<String, bPermissionsSubject>() {
        @Override
        public bPermissionsSubject load(String identifier) throws Exception {
            return new bPermissionsSubject(identifier, bPermissionsSubjectCollection.this);
        }
    });

    public bPermissionsSubjectCollection(String type, bPermissionsService service) {
        this.type = type;
        this.service = service;
    }

    @Override
    public String getIdentifier() {
        return type;
    }

    public CalculableType getType() {
        if (type.equalsIgnoreCase(PermissionService.SUBJECTS_GROUP)) return CalculableType.GROUP;
        else return CalculableType.USER;
    }

    @Override
    public Subject get(String identifier) {
        try {
            return subjects.get(identifier);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void add(bPermissionsSubject s) {
        subjects.put(s.getIdentifier(), s);
    }

    @Override
    public boolean hasRegistered(String identifier) {
        return false;
    }

    @Override
    public Iterable<Subject> getAllSubjects() {
        return null;
    }

    @Override
    public Map<Subject, Boolean> getAllWithPermission(String permission) {
        return null;
    }

    @Override
    public Map<Subject, Boolean> getAllWithPermission(Set<Context> contexts, String permission) {
        return null;
    }
}
