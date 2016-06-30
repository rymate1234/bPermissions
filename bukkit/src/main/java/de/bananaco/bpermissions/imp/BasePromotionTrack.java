package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.WorldManager;
import de.bananaco.permissions.interfaces.PromotionTrack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.*;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.io.File;
import java.util.*;

public class BasePromotionTrack implements PromotionTrack {
    final File tracks = new File("plugins/bPermissions/tracks.yml");
    final WorldManager wm = WorldManager.getInstance();
    org.bukkit.configuration.file.YamlConfiguration config = new org.bukkit.configuration.file.YamlConfiguration();
    Map<String, List<String>> trackmap = new HashMap<String, List<String>>();

    public void load() {
        try {
            // Tidy up
            config = new org.bukkit.configuration.file.YamlConfiguration();
            trackmap.clear();
            // Then do your basic if exists checks
            if (!tracks.exists()) {
                tracks.getParentFile().mkdirs();
                tracks.createNewFile();
            }
            config.load(tracks);
            if (config.getKeys(false) == null
                    || config.getKeys(false).size() == 0) {
                List<String> defTrack = new ArrayList<String>();
                defTrack.add("default");
                defTrack.add("moderator");
                defTrack.add("admin");
                config.set("default", defTrack);
                config.save(tracks);
            } else {
                Set<String> keys = config.getKeys(false);
                Map<String, Boolean> children = new HashMap<String, Boolean>();
                if (keys != null && keys.size() > 0) {
                    for (String key : keys) {
                        children.put("tracks." + key.toLowerCase(), true);
                        List<String> groups = config.getStringList(key);
                        if (groups != null && groups.size() > 0) {
                            trackmap.put(key.toLowerCase(), groups);
                        }
                    }
                }
                Permission perm = new Permission("tracks.*", PermissionDefault.OP, children);

                Permission permCheck = Bukkit.getServer().getPluginManager().getPermission("tracks.*");
                if (permCheck != null) Bukkit.getServer().getPluginManager().removePermission(permCheck);

                Bukkit.getPluginManager().addPermission(perm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void promote(String player, String track, String world) {

    }

    @Override
    public void demote(String player, String track, String world) {

    }

    @Override
    public boolean containsTrack(String track) {
        return trackmap.containsKey(track.toLowerCase());
    }

    @Override
    public List<String> getGroups(String track) {
        return config.getStringList(track);
    }
}
