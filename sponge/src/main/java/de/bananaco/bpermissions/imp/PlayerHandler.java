package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.imp.service.bPermissionsSubjectData;
import de.bananaco.bpermissions.util.Debugger;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.option.OptionSubject;
import org.spongepowered.api.service.permission.option.OptionSubjectData;
import org.spongepowered.api.text.Text;

import java.util.*;

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
        MainThread mt = MainThread.getInstance();
        if (!mt.getStarted()) {
            event.setMessage(Text.of("Unable to join server."), Text.of("bPermissions not enabled"));
            return; // don't touch the bPerms
        }

        final String uuid = event.getProfile().getUniqueId().toString();
        for (final de.bananaco.bpermissions.api.World world : wm.getAllWorlds()) {
            world.loadIfExists(uuid, CalculableType.USER);

            User user = (User) world.get(uuid, CalculableType.USER);
            try {
                user.calculateMappedPermissions();
                user.calculateEffectiveMeta();
            } catch (RecursiveGroupException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void playerLoader(final ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();

        setupPlayer(player, true);
    }

    @Listener
    public void playerUpdater(DisplaceEntityEvent.Teleport event) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        if (!event.getFromTransform().getExtent().equals(event.getToTransform().getExtent())) {
            Player player = (Player) event.getTargetEntity();
            setupPlayer(player, event.getToTransform().getExtent().getName(), true);
        }
    }

    public void setupPlayer(Player player, boolean recalculate) {
        if (!permissions.getGame().getPluginManager().isLoaded("de.bananaco.bpermissions")) {
            return;
        }

        setupPlayer(player, player.getWorld().getName(), recalculate);
    }

    public void setupPlayer(Player player, String world, boolean recalculate) {
        // Grab the pre-calculated effectivePermissions from the User object
        // Then whack it onto the player
        Map<String, Boolean> perms = ApiLayer.getEffectivePermissions(
                world,
                CalculableType.USER,
                player.getUniqueId().toString(),
                recalculate
        );

        // lets give this some context
        Context worldContext = new Context(Context.WORLD_KEY, world);
        Set<Context> contexts = new HashSet<>();
        //contexts.add(worldContext);

        // and get the players subject
        OptionSubjectData subject = (OptionSubjectData) player.getSubjectData();

        if (subject instanceof bPermissionsSubjectData) {
            bPermissionsSubjectData bPermsSubject = ((bPermissionsSubjectData) subject);
            bPermsSubject.setWorld(world);
        }

        if (perms.keySet().size() > 0) {
            String testPerm = (String) perms.keySet().toArray()[0];
            Debugger.log("Does player have permission? " + testPerm + " is " + player.hasPermission(testPerm));
        }

        Subject testSubject = player.getContainingCollection().get(player.getIdentifier());
        if (testSubject instanceof OptionSubject) {
            Debugger.log("Player prefix is: " + ((OptionSubject) testSubject).getOption("prefix").orElse(""));
        } else {
            Debugger.log("Player isn't an option subject");
        }

        // we're done!
    }
}