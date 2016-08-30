package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.User;
import de.bananaco.bpermissions.api.World;

import java.util.List;

/**
 * Created by Ryan on 30/06/2016.
 */
public class ReplaceGroupPromotion extends BasePromotionTrack {
    @Override
    public void promote(String player, String track, String world) {
        List<String> groups = trackmap.get(track.toLowerCase());
        if (world == null) {
            for (World w : wm.getAllWorlds()) {
                promoteUser(w, player, groups);
            }
        } else {
            World w = wm.getWorld(world);
            promoteUser(w, player, groups);
        }
    }

    private void promoteUser(World w, String player, List<String> groups) {
        User user = w.getUser(player);
        boolean promoted = false;
        // If they don't have the group, set it to their group
        for (int i = 0; i < groups.size() && !promoted; i++) {
            if (user.getGroupsAsString().contains(groups.get(i))) {
                if (i + 1 == groups.size()) {
                    break;
                }

                String oldGroup = groups.get(i);
                String newGroup = groups.get(i + 1);
                // Replace the old group
                user.replaceGroup(oldGroup, newGroup);
                // We've promoted successfully
                promoted = true;
                w.save();
            }
        }
    }

    @Override
    public void demote(String player, String track, String world) {
        List<String> groups = trackmap.get(track.toLowerCase());
        if (world == null) {
            for (World w : wm.getAllWorlds()) {
                demoteUser(w, player, groups);
            }
        } else {
            World w = wm.getWorld(world);
            demoteUser(w, player, groups);
        }
    }

    private void demoteUser(World w, String player, List<String> groups) {
        User user = w.getUser(player);
        boolean demoted = false;
        // If they don't have the group, set it to their group
        for (int i = groups.size() - 1; i > 0 && !demoted; i--) {
            if (user.getGroupsAsString().contains(groups.get(i))) {
                if (i - 1 == -1) {
                    break;
                }

                String oldGroup = groups.get(i);
                String newGroup = groups.get(i - 1);
                // Replace the old group
                user.replaceGroup(oldGroup, newGroup);

                // We've demoted successfully
                demoted = true;
                w.save();
            }
        }
    }


}
