package de.bananaco.bpermissions.api;

public class CalculableChange {
    private Calculable calculable;
    private ChangeType type;
    private String world;
    private String group;
    private String replacedGroup;
    private String permission;
    private String metaKey;
    private String metaValue;

    /**
     * Gets the Calculable associated with this change
     *
     * @return the Calculable that was changed
     */
    public Calculable getCalculable() {
        return calculable;
    }

    void setCalculable(Calculable calculableChanged) {
        this.calculable = calculableChanged;
    }

    /**
     * Gets the type of change that occurred
     *
     * @return a ChangeType
     */
    public ChangeType getType() {
        return type;
    }

    void setType(ChangeType type) {
        this.type = type;
    }

    /**
     * Gets the world this change is associated with
     *
     * @return the world as a string
     */
    public String getWorld() {
        return world;
    }

    void setWorld(String world) {
        this.world = world;
    }

    /**
     * Gets the group that was added or removed from a Calculable
     *
     * If this is a ChangeType.REPLACE_GROUP, this is the new
     *
     * @return a group name
     */
    public String getGroup() {
        return group;
    }

    void setGroup(String group) {
        this.group = group;
    }

    /**
     * Gets the group that was replaced as part of a ChangeType.REPLACE_GROUP
     *
     * @return a group name
     */
    public String getReplacedGroup() {
        return replacedGroup;
    }

    void setReplacedGroup(String replacedGroup) {
        this.replacedGroup = replacedGroup;
    }

    /**
     * Gets the permission that was added or removed from a Calculable
     *
     * @return a permission string
     */
    public String getPermission() {
        return permission;
    }

    void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * Gets a meta key that was added or removed from a calculable
     *
     * @return the meta key
     */
    public String getMetaKey() {
        return metaKey;
    }

    void setMetaKey(String metaKey) {
        this.metaKey = metaKey;
    }

    /**
     * Gets a meta value that was added or removed from a calculable
     *
     * @return the meta value
     */
    public String getMetaValue() {
        return metaValue;
    }

    void setMetaValue(String metaValue) {
        this.metaValue = metaValue;
    }
}
