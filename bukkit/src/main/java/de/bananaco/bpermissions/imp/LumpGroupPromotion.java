package de.bananaco.bpermissions.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.permissions.interfaces.PromotionTrack;

public class LumpGroupPromotion extends BasePromotionTrack {
    @Override
    public void promote(String player, String track, String world) {
        List<String> groups = trackmap.get(track.toLowerCase());
        if (world == null) {
            for (World w : wm.getAllWorlds()) {
                User user = w.getUser(player);
                // If they don't have the group, set it to their group
                for (int i = 0; i < groups.size(); i++) {
                    user.addGroup(groups.get(i));
                }
                w.save();
            }
        } else {
            User user = wm.getWorld(world).getUser(player);
            // If they don't have the group, set it to their group
            for (int i = 0; i < groups.size(); i++) {
                user.addGroup(groups.get(i));
            }
            wm.getWorld(world).save();
        }
    }

    @Override
    public void demote(String player, String track, String world) {
        List<String> groups = trackmap.get(track.toLowerCase());
        if (world == null) {
            for (World w : wm.getAllWorlds()) {
                User user = w.getUser(player);
                // Remove all the groups!
                for (int i = groups.size() - 1; i >= 0; i--) {
                    user.removeGroup(groups.get(i));
                }
                // Add the default group if they have no groups
                if (user.getGroupsAsString().size() == 0) {
                    user.addGroup(wm.getWorld(world).getDefaultGroup());
                }
                w.save();
            }
        } else {
            User user = wm.getWorld(world).getUser(player);
            // Remove all the groups!
            for (int i = groups.size() - 1; i >= 0; i--) {
                user.removeGroup(groups.get(i));
            }
            // Add the default group if they have no groups
            if (user.getGroupsAsString().size() == 0) {
                user.addGroup(wm.getWorld(world).getDefaultGroup());
            }
            wm.getWorld(world).save();
        }
    }


}
