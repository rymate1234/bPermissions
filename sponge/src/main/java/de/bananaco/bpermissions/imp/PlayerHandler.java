package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.RecursiveGroupException;
import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.util.HashMap;

/**
 * Created by rymate1234 on 16/03/2016.
 */
public class PlayerHandler {
    private WorldManager wm = WorldManager.getInstance();

    private final Permissions permissions;

    public PlayerHandler(Permissions permissions) {
        this.permissions = permissions;
    }


    // load the players data into the API
    @Listener
    public void playerLoader(final ClientConnectionEvent.Auth event) {
        final String uuid = event.getProfile().getUniqueId().toString();
        for (final de.bananaco.bpermissions.api.World world : wm.getAllWorlds()) {
            TaskRunnable r = new TaskRunnable() {
                @Override
                public TaskType getType() {
                    return TaskType.SERVER;
                }

                public void run() {
                    world.loadIfExists(uuid, CalculableType.USER);

                    User user = (User) world.get(uuid, CalculableType.USER);
                    try {
                        user.calculateMappedPermissions();
                        user.calculateEffectiveMeta();
                    } catch (RecursiveGroupException e) {
                        e.printStackTrace();
                    }
                }
            };
            MainThread.getInstance().schedule(r);
        }
    }

    @Listener
    public void playerLoader(final ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();


    }
}