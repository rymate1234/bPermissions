package de.bananaco.bpermissions.imp.commands;

import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.imp.FileWorld;
import de.bananaco.bpermissions.imp.Permissions;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.*;
import java.util.concurrent.Callable;

import static org.spongepowered.api.Sponge.getScheduler;

/**
 * Created by Ryan on 28/08/2016.
 */
public class PermsCommandHandler extends BaseCmdHandler {
    public static List<String> options = Arrays.<String>asList(
            "save", "reload", "backup", "convert", "version"
    );

    public PermsCommandHandler(HashMap<String, Commands> commands) {
        super(commands);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args, Commands commands) {
        Optional<String> optional = args.<String>getOne("option");
        String option = optional.orElse("version");

        if (option.equalsIgnoreCase("save")) {
            sendMessage(src, "Saving files...");
            WorldManager.getInstance().saveAll();
            sendMessage(src, "Saved all files");
        } else if (option.equalsIgnoreCase("reload")) {
            // just reload perms files for now

            Scheduler scheduler = Sponge.getScheduler();
            Task.Builder taskBuilder = scheduler.createTaskBuilder();

            MainThread.getInstance().schedule(new TaskRunnable() {
                public void run() {
                    for (final World world : WorldManager.getInstance().getAllWorlds()) {
                        try {
                            world.setFiles();
                            ((FileWorld) world).loadUnsafe();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        taskBuilder.execute(() -> world.setupAll())
                            .name("bPermissions - Reload world " + world.getName()).submit(Permissions.instance);
                    }
                }

                public TaskRunnable.TaskType getType() {
                    return TaskRunnable.TaskType.LOAD;
                }
            });


            sendMessage(src, "Reloaded bPermissions!");
        }


        return CommandResult.success();
    }
}
