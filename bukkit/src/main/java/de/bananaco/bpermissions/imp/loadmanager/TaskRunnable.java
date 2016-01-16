package de.bananaco.bpermissions.imp.loadmanager;

public interface TaskRunnable extends Runnable {

    static enum TaskType {
        SAVE,
        PLAYER_SETUP,
        LOAD,
        SERVER
    }

    public TaskType getType();
}
