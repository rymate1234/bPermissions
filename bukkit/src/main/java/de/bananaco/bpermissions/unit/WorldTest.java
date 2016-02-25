package de.bananaco.bpermissions.unit;

import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.World;

import java.util.UUID;

public class WorldTest extends World {

    public WorldTest(String world) {
        super(world);
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public boolean save() {
        return true;
    }

    @Override
    public boolean loadOne(String name, CalculableType type) {
        return true;
    }

    //@Override
    //public boolean saveOne(String name, CalculableType type) {
    //    return true;
    //}

    @Override
    public boolean storeContains(String name, CalculableType type) {
        return false;
    }

    @Override
    public String getDefaultGroup() {
        return "default";
    }

    @Override
    public boolean setupPlayer(String player) {
        return false;
    }

    @Override
    public UUID getUUID(String player) {
        return null;
    }

    @Override
    public void setDefaultGroup(String group) {
        // TODO Auto-generated method stub
    }
}
