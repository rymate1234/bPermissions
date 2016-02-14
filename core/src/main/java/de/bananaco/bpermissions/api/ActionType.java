package de.bananaco.bpermissions.api;

/**
 * Some predefined strings for action types.
 *
 * Created by rymate1234 on 14/02/2016.
 */
public enum ActionType {

    ADD_GROUP("addgroup"),
    REMOVE_GROUP("rmgroup"),
    SET_GROUP("setgroup"),
    ADD_PERMISSION("addperm"),
    REMOVE_PERMISSION("rmperm"),
    ADD_META("addmeta"),
    REMOVE_META("rmmeta");

    private final String name;

    ActionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
