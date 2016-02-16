package de.bananaco.bpermissions.imp.loadmanager;

import de.bananaco.bpermissions.api.RecursiveGroupException;
import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.imp.Permissions;
import de.bananaco.bpermissions.imp.SuperPermissionHandler;
import de.bananaco.bpermissions.util.Debugger;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SetupThread extends Thread {
    private final World world;
    private final SuperPermissionHandler handler;

    public SetupThread(World w, Permissions permissions) {
        this.world = w;
        this.setName("SetupThread - " + world.getName());
        this.handler = permissions.handler;
    }
    // a list of players that need setting up

    private List<Player> players = new ArrayList<Player>();
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
            if (hasPlayers()) {
                List tasks = null;

                if (getPlayers().size() > 0) {
                    tasks = getPlayers();
                }

                if (tasks != null && tasks.size() > 0) {
                    Player player = (Player) tasks.get(0);
                    tasks.remove(0);
                    setupPlayer(player);
                }
            } else {
                sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPlayer(Player player) {
        final UUID name = player.getUniqueId();
        long start, finish, time;
        start = System.currentTimeMillis();
        try {
            User user = world.getUser(name);
            synchronized (user) {
                user.setDirty(true);
                user.calculateEffectivePermissions();
                user.calculateEffectiveMeta();
            }

            synchronized (player) {
                handler.setupPlayer(player);
            }
        } catch (RecursiveGroupException e) {
            e.printStackTrace();
        }
        finish = System.currentTimeMillis();
        time = finish - start;
        Debugger.log("Setting up user took: " + time + "ms.");
    }

    /**
     * Internal method, concurrent modification exception prevention
     *
     * @return List<Player>
     */
    private List<Player> getPlayers() {
        return players;
    }

    // from the interface
    public boolean hasPlayers() {
        return players.size() > 0;
    }

    public boolean isRunning() {
        return running;
    }

    public void clearPlayers() {
        players.clear();
    }

    public void setRunning(final boolean running) {
        clearPlayers();
        if (!running) {
            this.running = false;
        }
    }

    public boolean getStarted() {
        return started;
    }

    public void setStarted(final boolean started) {
        this.started = started;
    }

    public void schedule(Player p) {
        // stop tasks from building up
        if (Collections.frequency(getPlayers(), p) < 2) {
            getPlayers().add(p);
        }
    }

}
