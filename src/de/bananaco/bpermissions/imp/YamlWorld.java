package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.imp.loadmanager.MainThread;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Here is the main YamlWorld class This loads from the default users.yml and
 * groups.yml on first creation. Isn't it pretty?
 */
public class YamlWorld extends World {

    protected static final String GROUPS = "groups";
    protected static final String PERMISSIONS = "permissions";
    protected static final String USERNAME = "username";
    protected static final String META = "meta";
    protected static final String USERS = "users";
    protected YamlConfiguration uconfig = null;//new YamlConfiguration();
    protected YamlConfiguration gconfig = null;//new YamlConfiguration();
    private final File ufile;
    private final File gfile;
    protected final Permissions permissions;
    protected final WorldManager wm = WorldManager.getInstance();
    // If there's an error loading the files, don't save them as it overrides them!
    protected boolean error = false;
    // Only save if flagged true
    protected boolean save = false;

    public YamlWorld(String world, Permissions permissions, File root) {
        super(world);
        this.permissions = permissions;
        if (wm.isUseGlobalUsers())
            this.ufile = new File(new File("plugins/bPermissions/global/"), "users.yml");
        else
            this.ufile = new File(root, "users.yml");

        this.gfile = new File(root, "groups.yml");
    }

    @Override
    public String getDefaultGroup() {
        if (gconfig != null) {
            return gconfig.getString("default", "default");
        }
        return "default";
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        uconfig = new YamlConfiguration();
        gconfig = new YamlConfiguration();
        try {
            saveUnsafe(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load() {
        if (MainThread.getInstance() == null) {
            Debugger.log("MainThread cancelled");
            return false;
        }
        try {
            // load async
            new BukkitRunnable() {
                public void run() {
                    try {
                        //clear();
                        loadUnsafe();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(permissions);

            // If it loaded correctly cancel the error
            error = false;
        } catch (Exception e) {
            error = true;
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Permissions for world:" + getName() + " did not load correctly! Please consult server.log");
            e.printStackTrace();
        }
        return true;
    }

    protected synchronized void loadUnsafe() throws Exception {
        boolean autoSave = wm.getAutoSave();
        wm.setAutoSave(false);
        if (!ufile.exists()) {
            if (ufile.getParentFile() != null) {
                ufile.getParentFile().mkdirs();
            }
            ufile.createNewFile();
            gfile.createNewFile();
        }
        uconfig = new YamlConfiguration();
        gconfig = new YamlConfiguration();

        YamlConfiguration uconfig = this.uconfig;
        YamlConfiguration gconfig = this.gconfig;

        long t = System.currentTimeMillis();
        uconfig.load(ufile);
        gconfig.load(gfile);

        /*
         * Load the users
         */
        ConfigurationSection usersConfig = uconfig.getConfigurationSection(USERS);
        if (usersConfig != null) {
            Set<String> names = usersConfig.getKeys(false);
            // for (String name : names) {
            // experiment - only load online users
            for (Player player : this.permissions.getServer().getOnlinePlayers()) {
                String name = player.getUniqueId().toString();
                List<String> nPerm = usersConfig.getStringList(name + "." + PERMISSIONS);
                List<String> nGroup = usersConfig.getStringList(name + "." + GROUPS);
                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new user
                User user = new User(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationSection meta = usersConfig.getConfigurationSection(name + "." + META);
                if (meta != null) {
                    Set<String> keys = meta.getKeys(false);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            user.setValue(key, meta.get(key).toString());
                        }
                    }
                }
                // Upload to API
                remove(user);
                add(user);
            }
        } else {
            Debugger.log("Empty ConfigurationSection:" + USERS + ":" + ufile.getPath());
        }
        /*
         * Load the groups
         */
        ConfigurationSection groupsConfig = gconfig.getConfigurationSection(GROUPS);
        if (groupsConfig != null) {
            Set<String> names = groupsConfig.getKeys(false);
            for (String name : names) {
                List<String> nPerm = groupsConfig.getStringList(name + "." + PERMISSIONS);
                List<String> nGroup = groupsConfig.getStringList(name + "." + GROUPS);

                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new group
                Group group = new Group(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationSection meta = groupsConfig
                        .getConfigurationSection(name + "." + META);
                if (meta != null) {
                    Set<String> keys = meta.getKeys(false);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            group.setValue(key, meta.get(key).toString());
                        }
                    }
                }
                // Upload to API
                remove(group);
                add(group);
            }
        } else {
            Debugger.log("Empty ConfigurationSection:" + GROUPS + ":" + gfile.getPath());
        }

        long f = System.currentTimeMillis();
        Debugger.log("Loading files for " + getName() + " took " + (f - t) + "ms");

        Debugger.log(this.getAll(CalculableType.USER).size() + " users loaded.");
        Debugger.log(this.getAll(CalculableType.GROUP).size() + " groups loaded.");

        for (Player player : this.permissions.getServer().getOnlinePlayers()) {
            String name = player.getUniqueId().toString();
            String world = player.getWorld().getName();
            if (wm.getWorld(world) == this) {
                getUser(name).calculateEffectivePermissions();
                getUser(name).calculateEffectiveMeta();
                setupPlayer(name);
            }
        }

        Bukkit.getLogger().info("[bPermissions] Permissions for world " + super.getName() + " has loaded!");

        wm.setAutoSave(autoSave);
    }

    public boolean save() {
        if (MainThread.getInstance() == null) {
            Debugger.log("MainThread cancelled");
            return false;
        }
        if (error) {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Permissions for world:" + this.getName() + " did not load correctly, please consult server.log.");
            return false;
        }
        save = true;
        // async again
        try {
            new BukkitRunnable() {
                public void run() {
                    try {
                        saveUnsafe(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(permissions);

            save = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    protected void saveUnsafe(boolean sort) throws Exception {
        if (!ufile.exists()) {
            ufile.getParentFile().mkdirs();
            ufile.createNewFile();
            gfile.createNewFile();
        }

        YamlConfiguration usaveconfig = uconfig; //new YamlConfiguration();
        YamlConfiguration gsaveconfig = gconfig; //new YamlConfiguration();

        //usaveconfig.setDefaults(this.uconfig);
        //gsaveconfig.setDefaults(this.gconfig);

        String def = getDefaultGroup();
        gsaveconfig.set("default", def);

        long t = System.currentTimeMillis();

        if (!wm.isUseGlobalUsers() || getName().equalsIgnoreCase("global")) {
            Set<Calculable> usr = getAll(CalculableType.USER);
            Debugger.log(usr.size() + " users saved.");
            // Sort them :D
            List<Calculable> users = new ArrayList<Calculable>(usr);
            if (sort) {
                MetaData.sort(users);
            }

            for (Calculable user : users) {
                String name = user.getName();

                // don't save users with default settings
                if (user.getMeta().size() == 0
                        && user.getPermissions().size() == 0
                        && (user.getGroupsAsString().size() == 0
                        || (user.getGroupsAsString().size() == 1
                        && user.getGroupsAsString().iterator().next().equals(getDefaultGroup())))) {
                    continue;
                }

                usaveconfig.set(USERS + "." + name + "." + PERMISSIONS, user.serialisePermissions());
                usaveconfig.set(USERS + "." + name + "." + USERNAME, Bukkit.getOfflinePlayer(UUID.fromString(name)).getName());
                usaveconfig.set(USERS + "." + name + "." + GROUPS, user.serialiseGroups());
                // MetaData
                Map<String, String> meta = user.getMeta();
                if (meta.size() > 0) {
                    for (String key : meta.keySet()) {
                        usaveconfig.set(USERS + "." + name + "." + META + "." + key, meta.get(key));
                    }
                }
            }
        }

        Set<Calculable> grp = getAll(CalculableType.GROUP);
        Debugger.log(grp.size() + " groups saved.");

        // Sort them :D
        @SuppressWarnings({"rawtypes", "unchecked"})
        List<Group> groups = new ArrayList(grp);
        if (sort) {
            MetaData.sort(groups);
        }

        for (Calculable group : groups) {
            String name = group.getName();
            gsaveconfig.set(GROUPS + "." + name + "." + PERMISSIONS, group.serialisePermissions());
            gsaveconfig.set(GROUPS + "." + name + "." + GROUPS, group.serialiseGroups());
            // MetaData
            Map<String, String> meta = group.getMeta();
            if (meta.size() > 0) {
                for (String key : meta.keySet()) {
                    gsaveconfig.set(GROUPS + "." + name + "." + META + "." + key, meta.get(key));
                }
            }
        }


        if (!wm.isUseGlobalUsers() || getName().equalsIgnoreCase("global"))
            usaveconfig.save(ufile);

        gsaveconfig.save(gfile);
        long f = System.currentTimeMillis();
        Debugger.log("Saving files for " + getName() + " took " + (f - t) + "ms");
    }

    public boolean loadOne(String name, CalculableType type) {
        if (!storeContains(name, type))
            return false;

        if (type == CalculableType.USER) {
            /*
             * Load as a user
             */
            ConfigurationSection usersConfig = uconfig.getConfigurationSection(USERS);
            if (usersConfig != null) {
                List<String> nPerm = usersConfig.getStringList(name + "." + PERMISSIONS);
                List<String> nGroup = usersConfig.getStringList(name + "." + GROUPS);
                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new user
                User user = new User(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationSection meta = usersConfig.getConfigurationSection(name + "." + META);
                if (meta != null) {
                    Set<String> keys = meta.getKeys(false);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            user.setValue(key, meta.get(key).toString());
                        }
                    }
                }
                // Upload to API
                remove(user);
                add(user);
            } else {
                Debugger.log("Empty ConfigurationSection:" + USERS + ":" + ufile.getPath());
                return false;
            }

            if (Bukkit.getPlayer(UUID.fromString(name)) != null) {
                try {
                    getUser(name).calculateEffectivePermissions();
                    getUser(name).calculateEffectiveMeta();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setupPlayer(name);
            }
        } else if (type == CalculableType.GROUP) {
            /*
             * Load a group
             */
            ConfigurationSection groupsConfig = gconfig.getConfigurationSection(GROUPS);
            if (groupsConfig != null) {
                List<String> nPerm = groupsConfig.getStringList(name + "." + PERMISSIONS);
                List<String> nGroup = groupsConfig.getStringList(name + "." + GROUPS);

                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new group
                Group group = new Group(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationSection meta = groupsConfig.getConfigurationSection(name + "." + META);
                if (meta != null) {
                    Set<String> keys = meta.getKeys(false);
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys) {
                            group.setValue(key, meta.get(key).toString());
                        }
                    }
                }
                // Upload to API
                remove(group);
                add(group);
            } else {
                Debugger.log("Empty ConfigurationSection:" + GROUPS + ":" + gfile.getPath());
            }
        }
        return true;
    }

    public boolean saveOne(String name, CalculableType type) {
        return false;
    }

    @Override
    public boolean storeContains(String name, CalculableType type) {
        if (type == CalculableType.USER) {
            ConfigurationSection usersConfig = uconfig.getConfigurationSection(USERS);
            if (usersConfig != null) {
                return usersConfig.getKeys(false).contains(name);
            }
            return false;
        } else if (type == CalculableType.GROUP) {
            ConfigurationSection groupsConfig = gconfig.getConfigurationSection(GROUPS);
            if (groupsConfig != null) {
                return groupsConfig.getKeys(false).contains(name);
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean setupAll() {
       Collection<Player> players = (Collection<Player>) Bukkit.getOnlinePlayers();
        for (Player player : players) {
            setupPlayer(player.getUniqueId().toString());
        }
        // return true for success
        return true;
    }

    @Override
    public boolean isOnline(User user) {
        return Bukkit.getPlayer(user.getName()) != null;
    }

    @Override
    public boolean setupPlayer(String player) {
        permissions.handler.setupPlayer(Bukkit.getPlayer(UUID.fromString(player)));
        return true;
    }

    @Override
    public void setDefaultGroup(String group) {
        gconfig.set("default", group);
        try {
            gconfig.save(gfile);
        } catch (IOException e) {
        }
    }
}
