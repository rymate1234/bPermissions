package de.bananaco.bpermissions.imp;

import com.google.inject.Inject;

import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.util.Debugger;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;

import org.slf4j.Logger;
import org.spongepowered.api.Game;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.PermissionService;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;


/**
 * bPermissions plugin
 */
@Plugin(id = "bpermissions", name = "bPermissions", version = "EARLY-ALPHA-SPONGE", description = "Permissions manager for Bukkit and Sponge")
public class Permissions {
    @Inject private Logger log;
    @Inject private ServiceManager services;
    @Inject protected Game game;

    private MainThread mt;
    private WorldManager wm;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    bPermissionsService bPermsService;

    protected static Permissions instance = null;


    @Listener
    public void enable(GamePreInitializationEvent event) throws Exception {
        log.info("Loading bPermissions...");

        bPermsService = new bPermissionsService();

        // start the main thread
        mt = MainThread.getInstance();
        mt.start();

        // grab the WorldManager
        wm = WorldManager.getInstance();

        // testing variables
        // TODO: CONFIG FILE
        wm.setFileFormat("YML");
        wm.setAutoSave(true);
        wm.setUseGlobalUsers(false);
        Debugger.setDebug(true);

        // create the default world
        FileWorld defaultWorld = new FileWorld("global", this, new File(getFolder() + "/global/"));
        wm.setDefaultWorld(defaultWorld);
        defaultWorld.load();

        HashMap<String, String> mirrors = new HashMap<String, String>();

        Sponge.getEventManager().registerListeners(this, new WorldLoader(this, mirrors));
        Sponge.getEventManager().registerListeners(this, new PlayerHandler(this));

        // Register bPermissions as the permissions service, assuming there isn't one already
        if (!services.isRegistered(PermissionService.class)) {
            services.setProvider(this, PermissionService.class, bPermsService);
        } else {
            throw new Exception("There's already a permissions plugin installed!");
        }
        instance = this;
    }

    public Logger getLog() {
        return log;
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

    public Path getFolder() {
        return privateConfigDir;
    }
}
