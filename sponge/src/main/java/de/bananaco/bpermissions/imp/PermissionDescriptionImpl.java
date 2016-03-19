package de.bananaco.bpermissions.imp;

import com.google.common.base.Preconditions;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ryan on 19/03/2016.
 */
public class PermissionDescriptionImpl implements PermissionDescription {
    private String id;
    private Text description;
    private final PluginContainer owner;

    public PermissionDescriptionImpl(String id, Text description, PluginContainer owner) {
        this.id = id;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Text getDescription() {
        return description;
    }

    @Override
    public Map<Subject, Boolean> getAssignedSubjects(String type) {
        return null;
    }

    @Override
    public PluginContainer getOwner() {
        return owner;
    }

    static class Builder implements PermissionDescription.Builder {
        private final PluginContainer owner;
        private final bPermissionsService service;
        private String id;
        private Text description;
        private Map<String, Integer> ranks = new HashMap<>();

        public Builder(PluginContainer owner, bPermissionsService service) {
            this.owner = owner;
            this.service = service;
        }

        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        @Override
        public Builder description(Text text) {
            this.description = text;
            return this;
        }

        @Override
        public PermissionDescription.Builder assign(String role, boolean value) {
            return null;
        }


        @Override
        public PermissionDescriptionImpl register() throws IllegalStateException {
            Preconditions.checkNotNull(id, "id");
            Preconditions.checkNotNull(description, "description");

            final PermissionDescriptionImpl ret = new PermissionDescriptionImpl(this.id, this.description, this.owner);
            service.registerDescription(ret);
            return ret;
        }
    }
}
