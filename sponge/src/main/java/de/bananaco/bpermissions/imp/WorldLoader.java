package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.bpermissions.util.Debugger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.Map;

/**
 * Created by Ryan on 16/03/2016.
 */
public class WorldLoader {
    private WorldManager wm = WorldManager.getInstance();
    private Map<String, String> mirrors;
    private Permissions permissions;

    protected WorldLoader(Permissions permissions, Map<String, String> mirrors) {
        this.mirrors = mirrors;
        this.permissions = permissions;
        for (World world : permissions.game.getServer().getWorlds()) {
            createWorld(world);
        }
    }

    @Listener
    public void onWorldInit(LoadWorldEvent event) {
        createWorld(event.getTargetWorld());
    }

    private void createWorld(World targetWorld) {
        // TODO this is probably going to be an issue
        String world = targetWorld.getName().toLowerCase();

        if (!mirrors.containsKey(world)) {
            Debugger.log("Loading world: " + targetWorld.getName());
            wm.createWorld(world, new FileWorld(world, permissions, new File("./bPermissions/" + world + "/")));
        }
    }

}
