package de.bananaco.bpermissions.imp.loadmanager;

import java.util.ArrayList;
import java.util.List;

import de.bananaco.bpermissions.imp.loadmanager.TaskRunnable.TaskType;
import de.bananaco.bpermissions.util.Debugger;

public class MainThread extends Thread implements TaskThread {

    private static MainThread thread = new MainThread();

    public static MainThread getInstance() {
        return thread;
    }
    // a list of tasks
    private List<Runnable> load = new ArrayList<Runnable>();
    private List<Runnable> save = new ArrayList<Runnable>();
    private List<Runnable> server = new ArrayList<Runnable>();
    private List<Runnable> playerSetup = new ArrayList<Runnable>();
    private boolean running = true;
    private boolean started = false;

    @Override
    public void run() {
        while (running) {
            check();
        }
    }

    /**
     * Internal method, check scheduler
     */
    private synchronized void check() {
        try {
            if (hasTasks()) {
                TaskRunnable run = null;
                List tasks = null;
                if (getTasks(TaskType.LOAD).size() > 0) {
                    tasks = getTasks(TaskType.LOAD);
                } else if (getTasks(TaskType.PLAYER_SETUP).size() > 0) {
                    tasks = getTasks(TaskType.PLAYER_SETUP);
                } else if (getTasks(TaskType.SAVE).size() > 0) {
                    tasks = getTasks(TaskType.SAVE);
                } else if (getTasks(TaskType.SERVER).size() > 0) {
                    tasks = getTasks(TaskType.SERVER);
                }

                if (tasks != null && tasks.size() > 0) {
                    run = (TaskRunnable) tasks.get(0);
                    tasks.remove(0);
                    TaskRunnable r = run;

                    r.run();
                }
            } else {
                sleep(10L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Internal method, concurrent modification exception prevention
     *
     * @return List<Runnable>
     */
    private List<Runnable> getTasks(TaskType type) {
        if (type == TaskType.LOAD) {
            return load;
        }
        if (type == TaskType.SAVE) {
            return save;
        }
        if (type == TaskType.PLAYER_SETUP) {
            return playerSetup;
        }
        if (type == TaskType.SERVER) {
            return server;
        }
        return new ArrayList<Runnable>();
    }

    // from the interface
    public boolean hasTasks() {
        return load.size() + save.size() + server.size() + playerSetup.size() > 0;
    }

    public boolean isRunning() {
        return running;
    }

    public void clearTasks() {
        load.clear();
        save.clear();
        server.clear();
        playerSetup.clear();
    }

    public void setRunning(final boolean running) {
        clearTasks();
        if (!running) {
            thread.running = false;
            thread = null;
        }
    }

    public boolean getStarted() {
        return started;
    }

    public void setStarted(final boolean started) {
        TaskRunnable r = new TaskRunnable() {
            public void run() {
                thread.started = started;
                Debugger.log("Set started: " + started);
            }

            public TaskType getType() {
                return TaskType.SERVER;
            }
        };
        schedule(r);
    }

    public void schedule(TaskRunnable r) {
        // stop tasks from building up
        if (tasksCount() < 10) {
            getTasks(r.getType()).add(r);
        }
    }

    public int tasksCount() {
        return load.size() + save.size() + server.size() + playerSetup.size();
    }
}
