package de.bananaco.bpermissions.imp;

import com.google.inject.Inject;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.imp.commands.*;
import de.bananaco.bpermissions.imp.service.bPermissionsService;
import de.bananaco.bpermissions.util.Debugger;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;


/**
 * bPermissions plugin
 */
@Plugin(id = "de.bananaco.bpermissions", name = "bPermissions", version = "EARLY-ALPHA-SPONGE", description = "Permissions manager for Bukkit and Sponge")
public class Permissions {
    @Inject
    private Logger log;
    @Inject
    private ServiceManager services;
    @Inject
    private Game game;

    private MainThread mt;
    private WorldManager wm;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;

    private bPermissionsService bPermsService;

    public static Permissions instance = null;
    private HashMap<String, Commands> commands;
    public PlayerHandler handler;

    @Listener
    public void enable(GamePreInitializationEvent event) throws Exception {
        log.info("Loading bPermissions...");

        if (!privateConfigDir.toFile().exists()) {
            privateConfigDir.toFile().mkdirs();
        }

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

        // Load the default Map for Commands
        commands = new HashMap<String, Commands>();

        CommandSpec worldSpec = CommandSpec.builder()
                .description(Text.of("Selects a world to use permission commands on"))
                .permission("bPermissions.admin")
                .executor(new WorldCmdHandler(commands))
                .arguments(GenericArguments.optional(GenericArguments.world(Text.of("world"))))
                .build();

        CommandElement userOrString = GenericArguments.optional(
            GenericArguments.firstParsing(
                GenericArguments.onlyOne(new UserCommandElement(Text.of("player"))),
                GenericArguments.onlyOne(new ArgsCommandHandler(Text.of("action")))
            )
        );

        CommandElement groupOrString = GenericArguments.optional(
            GenericArguments.firstParsing(
                GenericArguments.onlyOne(new GroupCommandElement(Text.of("group"), commands)),
                GenericArguments.onlyOne(new ArgsCommandHandler(Text.of("action")))
            )
        );

        CommandSpec userSpec = CommandSpec.builder()
                .description(Text.of("Longform commands used when modifying users"))
                .permission("bPermissions.admin")
                .executor(new UserCmdHandler(commands))
                .arguments(userOrString)
                .build();

        CommandSpec groupSpec = CommandSpec.builder()
                .description(Text.of("Longform commands used when modifying groups"))
                .permission("bPermissions.admin")
                .executor(new GroupCmdHandler(commands))
                .arguments(groupOrString)
                .build();

        CommandSpec execSpec = CommandSpec.builder()
                .description(Text.of("Shortform /exec command - great for scripts!"))
                .permission("bPermissions.admin")
                .executor(new ExecCmdHandler(commands))
                .arguments(GenericArguments.onlyOne(new ArgsCommandHandler(Text.of("args"))))
                .build();

        CommandSpec permsSpec = CommandSpec.builder()
                .description(Text.of("Miscellaneous management commands"))
                .permission("bPermissions.admin")
                .executor(new PermsCommandHandler(commands))
                .arguments(new StringListCommandElement(Text.of("option"), PermsCommandHandler.options, true))
                .build();

        Sponge.getCommandManager().register(this, worldSpec, "world", "w");
        Sponge.getCommandManager().register(this, userSpec, "user", "u");
        Sponge.getCommandManager().register(this, groupSpec, "group", "g");
        Sponge.getCommandManager().register(this, execSpec, "exec", "e");
        Sponge.getCommandManager().register(this, permsSpec, "permissions", "p", "bp", "perms");

        // create the default world
        FileWorld defaultWorld = new FileWorld("global", this, new File(getFolder() + "/global/"));
        wm.setDefaultWorld(defaultWorld);
        defaultWorld.load();

        HashMap<String, String> mirrors = new HashMap<String, String>();

        Sponge.getEventManager().registerListeners(this, new WorldLoader(this, mirrors));
        handler = new PlayerHandler(this);
        Sponge.getEventManager().registerListeners(this, handler);

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

    public Game getGame() {
        return game;
    }
}
