package de.bananaco.bpermissions.imp;

import com.google.inject.Inject;

import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;

import org.slf4j.Logger;
import org.spongepowered.api.Game;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;


/**
 * bPermissions plugin
 */
@Plugin(id = "de.bananaco.bpermissions-sponge", name = "bPermissions", version = "EARLY-ALPHA-SPONGE",
        description = "Permissions manager for Bukkit and Sponge")
public class Permissions {
    @Inject private Logger log;

    @Inject private Game game;

    private MainThread mt;
    private WorldManager wm;

    @Listener
    public void enable(GameInitializationEvent  event) {
        log.info("Loading bPermissions...");

        // start the main thread
        mt = MainThread.getInstance();
        mt.start();

        // grab the WorldManager
        wm = WorldManager.getInstance();
    }


    @Listener
    public void disable(GameStoppedServerEvent event) {
        log.info("Waiting 30s to finish tasks...");
        // try to finish previous tasks first
        for (int i = 0; i < 31; i++) {
            if (mt.hasTasks()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i == 30) {
                    log.info("Tasks not finished - disabling anyway.");
                    log.info("Tasks remaining: " + mt.tasksCount());

                    mt.clearTasks();
                }
            } else {
                log.info("All tasks finished after " + i + " seconds!");
                i = 31;
            }
        }

        log.info("Saving worlds...");

        //save all worlds
        for (World world : wm.getAllWorlds()) {
            world.save();
        }

        // then disable
        mt.schedule(new TaskRunnable() {
            public void run() {
                mt.setRunning(false);
                log.info("bPermissions Disabled!");
            }

            public TaskRunnable.TaskType getType() {
                return TaskRunnable.TaskType.SERVER;
            }
        });

        while (mt.hasTasks()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
