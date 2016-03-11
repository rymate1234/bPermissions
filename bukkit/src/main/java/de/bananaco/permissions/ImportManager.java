package de.bananaco.permissions;

import java.io.File;
import java.util.*;

import com.evilmidget38.UUIDFetcher;
import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.util.Debugger;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import de.bananaco.bpermissions.imp.loadmanager.MainThread;
import de.bananaco.bpermissions.imp.loadmanager.TaskRunnable;

public class ImportManager {

    private WorldManager wm = WorldManager.getInstance();
    private final JavaPlugin plugin;

    public ImportManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean pexImport() {
        if (MainThread.getInstance() == null) {
            Debugger.log("MainThread cancelled");
            return false;
        }
        try {
            // load async
            MainThread.getInstance().schedule(new TaskRunnable() {
                public void run() {
                    try {
                        importPEX();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public TaskRunnable.TaskType getType() {
                    return TaskRunnable.TaskType.LOAD;
                }
            });
            // If it loaded correctly cancel the error
        } catch (Exception e) {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "PEX import failed! Check the server log!");
            e.printStackTrace();
        }
        return true;
    }

    protected synchronized void importPEX() throws Exception {
        File file = new File("plugins/PermissionsEx/permissions.yml");
        // No point doing anything if the file doesn't exist
        if (!file.exists()) {
            System.err.println("File not exist");
            return;
        }
        boolean autosave = wm.getAutoSave();

        YamlConfiguration perm = new YamlConfiguration();
        perm.load(file);

        World world = plugin.getServer().getWorlds().get(0);
        de.bananaco.bpermissions.api.World wd = wm.getWorld(world.getName());
        wm.setAutoSave(false);

        ConfigurationSection users = perm.getConfigurationSection("users");
        ConfigurationSection groups = perm.getConfigurationSection("groups");

        if (groups.getKeys(false) != null && groups.getKeys(false).size() > 0) {
            System.out.println("Importing groups....");
            for (String group : groups.getKeys(false)) {
                if (groups.getBoolean(group + ".default")) {
                    wd.setDefaultGroup(group);
                    System.out.println("DEFAULT GROUP DETECTED: " + group);
                }
                List<String> g = groups.getStringList(group + ".inheritance");
                List<String> p = groups.getStringList(group + ".permissions");
                Group u = (Group) wd.get(group, CalculableType.GROUP);
                // Remove the existing groups
                u.getGroupsAsString().clear();
                // Add all the groups
                if (g != null && g.size() > 0) {
                    for (String gr : g) {
                        Calculable grp = wd.get(gr, CalculableType.GROUP);
                        u.addGroup(grp.getNameLowerCase());
                    }
                }

                if (p != null && p.size() > 0) {
                    for (String pr : p) {
                        if (pr.startsWith("-")) {
                            u.addPermission(pr.replace("-", ""), false);
                        } else {
                            u.addPermission(pr, true);
                        }
                    }
                }
                String prefix = groups.getString(group + ".prefix");
                if (prefix != null) {
                    u.setValue("prefix", prefix);
                }
                String suffix = groups.getString(group + ".suffix");
                if (suffix != null) {
                    u.setValue("suffix", suffix);
                }
                String priority = groups.getString(group + ".options.rank");
                if (priority != null) {
                    u.setValue("priority", priority);
                }
                u.calculateGroups();
            }
            System.out.println("Groups loaded!");
        }

        if (users.getKeys(false) != null && users.getKeys(false).size() > 0) {
            System.out.println("Importing users....");
            for (String user : users.getKeys(false)) {
                List<String> g = users.getStringList(user + ".group");
                List<String> p = users.getStringList(user + ".permissions");
                User u = (User) wd.get(user, CalculableType.USER);
                // Remove the existing groups
                u.getGroupsAsString().clear();
                // Add all the groups
                if (g != null && g.size() > 0) {
                    for (String gr : g) {
                        Calculable grp = wd.get(gr, CalculableType.GROUP);
                        if (grp != null) u.addGroup(grp.getNameLowerCase());
                    }
                }
                if (p != null && p.size() > 0) {
                    for (String pr : p) {
                        if (pr.startsWith("-")) {
                            u.addPermission(pr.replace("-", ""), false);
                        } else {
                            u.addPermission(pr, true);
                        }
                    }
                }
                String prefix = users.getString(user + ".prefix");
                if (prefix != null) {
                    u.setValue("prefix", prefix);
                }
                String suffix = users.getString(user + ".suffix");
                if (suffix != null) {
                    u.setValue("suffix", suffix);
                }
                u.calculateGroups();
            }
            System.out.println("Users loaded!");
        }

        wm.setAutoSave(autosave);
        wm.saveAll();
    }

    public void importYML() throws Exception {
        for (World world : plugin.getServer().getWorlds()) {
            de.bananaco.bpermissions.api.World wd = wm.getWorld(world.getName());
            File perms = new File("plugins/bPermissions/worlds/"
                    + world.getName() + ".yml");
            if (perms.exists()) {
                System.out.println("Importing world: " + world.getName());
                YamlConfiguration pConfig = new YamlConfiguration();//new Configuration(perms);
                pConfig.load(perms);
                // Here we grab the different bits and bobs
                ConfigurationSection users = pConfig.getConfigurationSection("players");
                ConfigurationSection groups = pConfig.getConfigurationSection("groups");

                // Load users
                if (users != null && users.getKeys(false) != null && users.getKeys(false).size() > 0) {
                    Set<String> u = users.getKeys(false);
                    for (String usr : u) {
                        System.out.println("Importing user: " + usr);
                        List<String> g = users.getStringList(usr);
                        // Clear the groups in their list firstly
                        wd.getUser(usr).getGroupsAsString().clear();
                        // Another NPE Fix
                        if (g != null && g.size() > 0) {
                            for (String group : g) {
                                wd.getUser(usr).addGroup(group);
                            }
                        }
                    }
                }
                // Load groups
                if (groups != null && groups.getKeys(false) != null && groups.getKeys(false).size() > 0) {
                    Set<String> g = groups.getKeys(false);
                    for (String grp : g) {
                        System.out.println("Importing group: " + grp);
                        List<String> p = groups.getStringList(grp);
                        if (p != null && p.size() > 0) {
                            for (String perm : p) {
                                wd.getGroup(grp).getPermissions().add(Permission.loadFromString(perm));
                            }
                        }
                    }
                }
            }
            // Forgot to save after importing!
            wd.save();
        }
        wm.cleanup();
    }

    public void importGroupManager() throws Exception {
        for (World world : plugin.getServer().getWorlds()) {
            de.bananaco.bpermissions.api.World wd = wm.getWorld(world.getName());

            File users = new File("plugins/GroupManager/worlds/" + world.getName()
                    + "/users.yml");
            File groups = new File("plugins/GroupManager/worlds/" + world.getName()
                    + "/groups.yml");

            if (users.exists() && groups.exists()) {
                System.out.println("Importing world: " + world.getName());

                YamlConfiguration uConfig = new YamlConfiguration();
                YamlConfiguration gConfig = new YamlConfiguration();
                try {
                    uConfig.load(users);
                    gConfig.load(groups);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ConfigurationSection usConfig = uConfig.getConfigurationSection("users");
                ConfigurationSection grConfig = gConfig.getConfigurationSection("groups");

                Set<String> usersList = null;
                if (usConfig != null) {
                    usersList = usConfig.getKeys(false);
                }
                Set<String> groupsList = null;
                if (grConfig != null) {
                    groupsList = grConfig.getKeys(false);
                }

                if (usersList != null) {
                    for (String player : usersList) {
                        System.out.println("Importing user: " + player);
                        User user = wd.getUser(player);
                        try {
                            List<String> p = uConfig.getStringList("users." + player + ".permissions");
                            List<String> i = uConfig.getStringList("users." + player + ".subgroups");
                            i.add(uConfig.getString("users." + player + ".group"));

                            String prefix = uConfig.getString("users." + player + ".info." + "prefix");
                            String suffix = uConfig.getString("users." + player + ".info." + "suffix");

                            if (p != null) {
                                user.getPermissions().addAll(Permission.loadFromString(p));
                            }
                            if (i != null) {
                                user.getGroupsAsString().clear();
                                user.getGroupsAsString().addAll(i);
                            }
                            if (prefix != null) {
                                user.setValue("prefix", prefix);
                            }
                            if (suffix != null) {
                                user.setValue("suffix", suffix);
                            }
                        } catch (Exception e) {
                            System.err.println("Error importing user: " + player);
                        }
                    }
                }

                if (groupsList != null) {
                    for (String group : groupsList) {
                        System.out.println("Importing group: " + group);
                        Group gr = wd.getGroup(group);
                        try {
                            List<String> p = gConfig.getStringList("groups." + group + ".permissions");
                            List<String> i = gConfig.getStringList("groups." + group + ".inheritance");

                            String prefix = gConfig.getString("groups." + group + ".info." + "prefix");
                            String suffix = gConfig.getString("groups." + group + ".info." + "suffix");

                            if (gConfig.getBoolean("groups." + group + ".default")) {
                                wd.setDefaultGroup(group);
                                System.out.println("DEFAULT GROUP DETECTED: " + group);
                            }
                            if (p != null) {
                                gr.getPermissions().addAll(Permission.loadFromString(p));
                            }
                            if (i != null) {
                                List<String> fp = new ArrayList<String>();
                                for (int j = 0; j < i.size(); j++) {
                                    String fpp = i.get(j);
                                    if (fpp.startsWith("g:")) {
                                        // do nothing
                                    } else {
                                        fp.add(fpp);
                                    }
                                }
                                i.clear();
                                i.addAll(fp);
                                gr.getGroupsAsString().addAll(i);
                            }
                            if (prefix != null) {
                                gr.setValue("prefix", prefix);
                            }
                            if (suffix != null) {
                                gr.setValue("suffix", suffix);
                            }
                        } catch (Exception e) {
                            System.err.println("Error importing group: " + group);
                        }
                    }
                }
                wd.save();
            }
        }
        wm.cleanup();
    }

    public void importPermissions3() throws Exception {

    }

    public void importUuid() throws Exception {
        String GROUPS = "groups";
        String PERMISSIONS = "permissions";
        String META = "meta";
        String USERS = "users";
        List<World> worlds = plugin.getServer().getWorlds();

        for (int i = 0; i < worlds.size(); i++) {
            Bukkit.getLogger().info("------ Processing world " + (i + 1) + " of " + worlds.size() + " ------");
            World world = worlds.get(i);
            de.bananaco.bpermissions.api.World wd = wm.getWorld(world.getName());

            Bukkit.getLogger().info("Processed world " + (i + 1) + " of " + worlds.size() + ". Loading and converting.");

            File ufile = new File("plugins/bPermissions/" + world.getName().toLowerCase() + "/users.yml");
            if (!ufile.exists()) {
                Bukkit.getLogger().info("Users yml not found for world " + world.getName());
                return;
            }

            YamlConfiguration uconfig = new de.bananaco.bpermissions.imp.YamlConfiguration();

            long t = System.currentTimeMillis();
            uconfig.load(ufile);
            long f = System.currentTimeMillis();
            Debugger.log("Loading files took " + (f - t) + "ms");

            /*
             * Load the users
             */
            ConfigurationSection usersConfig = uconfig.getConfigurationSection(USERS);
            if (usersConfig != null) {
                Bukkit.getLogger().info("Converting world: " + world.getName());
                Set<String> names = usersConfig.getKeys(false);
                List<String> usersList = new ArrayList<>();
                usersList.addAll(names);
                UUIDFetcher fetcher = new UUIDFetcher(usersList);

                Map<String, UUID> result = null;

                try {
                    result = fetcher.call();
                } catch (Exception e) {
                    plugin.getLogger().warning("Exception while running UUIDFetcher");
                    e.printStackTrace();
                    return;
                }

                Map<String, UUID> players = new HashMap<>();

                // convert names to lower case...
                for (String username : result.keySet()) {
                    players.put(username.toLowerCase(), result.get(username));
                }

                int size = names.size();
                int total = 1;
                for (String name : names) {
                    UUID uniqueId = players.get(name);
                    plugin.getLogger().info("Converting user " + uniqueId + " - " + total + " of " + size);
                    if (uniqueId == null) {
                        plugin.getLogger().warning("UUID for " + name + " not found! Using alternative method to fetch UUID");
                        plugin.getLogger().warning("Did he already change his username?");
                        uniqueId = Bukkit.getServer().getOfflinePlayer(name).getUniqueId();
                    }
                    List<String> nPerm = usersConfig.getStringList(name + "."
                            + PERMISSIONS);
                    List<String> nGroup = usersConfig.getStringList(name + "."
                            + GROUPS);
                    Set<Permission> perms = Permission.loadFromString(nPerm);
                    // remove the old user
                    User user = new User(name, nGroup, perms, world.getName(), wd);
                    wd.remove(user);

                    // Create the new user!
                    User uuidUser = new User(uniqueId.toString(), nGroup, perms, world.getName(), wd);
                    // MetaData
                    ConfigurationSection meta = usersConfig
                            .getConfigurationSection(name + "." + META);
                    if (meta != null) {
                        Set<String> keys = meta.getKeys(false);
                        if (keys != null && keys.size() > 0) {
                            for (String key : keys) {
                                uuidUser.setValue(key, meta.get(key).toString());
                            }
                        }
                    }
                    wd.add(uuidUser);
                    total++;
                }

                Bukkit.getLogger().info("Converted world: " + world.getName());
            } else {
                Bukkit.getLogger().info("Users yml of world is empty: " + world.getName());
                Debugger.log("Empty ConfigurationSection:" + USERS + ":" + ufile.getPath());
            }

            wd.save();
            wm.cleanup();

            Bukkit.getLogger().info("------ World " + (i + 1) + " of " + worlds.size() + " complete ------");

        }

        Bukkit.getLogger().info("Conversion complete!");
    }
}
