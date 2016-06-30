package de.bananaco.bpermissions.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.bananaco.bpermissions.util.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.permissions.interfaces.PromotionTrack;

public class SingleGroupPromotion extends BasePromotionTrack {
    public int getIndex(String data, List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (data.equalsIgnoreCase(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void promote(String player, String track, String world) {
        List<String> groups = trackmap.get(track.toLowerCase());
        if (world == null) {
            for (World w : wm.getAllWorlds()) {
                User user = w.getUser(player);
                // If they don't have the group, set it to their group
                int index = 0;
                for (int i = 0; i < groups.size(); i++) {
                    Debugger.log("hasGroup?" + groups.get(i) + " " + user.hasGroup(groups.get(i)));
                    if (user.hasGroup(groups.get(i))) {
                        int current = getIndex(groups.get(i), groups);
                        if (current >= index) {
                            index = current + 1;
                        }
                    }
                }
                if (index < groups.size()) {
                    Debugger.log("index: " + index + " group: " + groups.get(index));
                    user.getGroupsAsString().clear();
                    user.addGroup(groups.get(index));
                    w.save();
                }
            }
        } else {
            User user = wm.getWorld(world).getUser(player);
            // If they don't have the group, set it to their group
            int index = 0;
            for (int i = 0; i < groups.size(); i++) {
                Debugger.log("hasGroup?" + groups.get(i) + " " + user.hasGroup(groups.get(i)));
                if (user.hasGroup(groups.get(i))) {
                    int current = getIndex(groups.get(i), groups);
                    if (current >= index) {
                        index = current + 1;
                    }
                }
            }

            if (index < groups.size()) {
                Debugger.log("index: " + index + " group: " + groups.get(index));
                user.getGroupsAsString().clear();
                user.addGroup(groups.get(index));
                wm.getWorld(world).save();
            }
        }
    }

    @Override
    public void demote(String player, String track, String world) {
        List<String> groups = trackmap.get(track.toLowerCase());
        if (world == null) {
            for (World w : wm.getAllWorlds()) {
                User user = w.getUser(player);
                // If they don't have the group, set it to their group
                int index = 0;
                for (int i = 0; i < groups.size(); i++) {
                    Debugger.log("hasGroup?" + groups.get(i) + " " + user.hasGroup(groups.get(i)));
                    if (user.hasGroup(groups.get(i))) {
                        int current = getIndex(groups.get(i), groups);
                        if (current > index) {
                            index = current - 1;
                        }
                    }
                }
                if (index >= 0) {
                    Debugger.log("index: " + index + " group: " + groups.get(index));
                    user.getGroupsAsString().clear();
                    user.addGroup(groups.get(index));
                    wm.getWorld(world).save();
                }
            }
        } else {
            User user = wm.getWorld(world).getUser(player);
            // If they don't have the group, set it to their group
            int index = 0;
            for (int i = 0; i < groups.size(); i++) {
                Debugger.log("hasGroup?" + groups.get(i) + " " + user.hasGroup(groups.get(i)));
                if (user.hasGroup(groups.get(i))) {
                    int current = getIndex(groups.get(i), groups);
                    if (current > index) {
                        index = current - 1;
                    }
                }
            }
            if (index >= 0) {
                Debugger.log("index: " + index + " group: " + groups.get(index));
                user.getGroupsAsString().clear();
                user.addGroup(groups.get(index));
                wm.getWorld(world).save();
            }
        }
    }
}
