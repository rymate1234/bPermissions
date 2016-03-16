package de.bananaco.bpermissions.imp;

import com.google.common.reflect.TypeToken;
import com.typesafe.config.ConfigRenderOptions;
import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.util.Debugger;
import de.bananaco.bpermissions.util.loadmanager.MainThread;
import de.bananaco.bpermissions.util.loadmanager.TaskRunnable;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.entity.living.player.Player;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This is the file based loader for sponge
 *
 * Supports both HOCON and YAML depending on config
 *
 * Created by rymate1234 on 15/03/2016.
 */
public class FileWorld extends World {
    protected static final String GROUPS = "groups";
    protected static final String PERMISSIONS = "permissions";
    protected static final String USERNAME = "username";
    protected static final String META = "meta";
    protected static final String USERS = "users";
    private final File root;

    private ConfigurationNode uconfig;
    private ConfigurationNode gconfig;

    private final File ufile;
    private final File gfile;
    protected final Permissions permissions;
    protected final WorldManager wm = WorldManager.getInstance();

    protected final String format = wm.getFileFormat().toLowerCase();

    // If there's an error loading the files, don't save them as it overrides them!
    protected boolean error = false;
    // Only save if flagged true
    protected boolean save = false;

    private String[] usersArray;
    private String[] groupsArray;

    TypeToken<String> stringToken = TypeToken.of(String.class);
    private AbstractConfigurationLoader uloader;
    private AbstractConfigurationLoader gloader;

    public FileWorld(String world, Permissions permissions, File root) {
        super(world);

        this.permissions = permissions;
        if (wm.isUseGlobalUsers())
            this.ufile = new File(new File(permissions.getFolder() + "/global/"), "users." + format);
        else
            this.ufile = new File(root, "users." + format);

        this.gfile = new File(root, "groups." + format);

        this.usersArray = new String[0];
        this.groupsArray = new String[0];
        this.root = root;
    }

    @Override
    public boolean load() {
        if (MainThread.getInstance() == null) {
            Debugger.log("MainThread cancelled");
            return false;
        }

        try {
            // load async
            TaskRunnable loadTask = new TaskRunnable() {
                @Override
                public TaskType getType() {
                    return TaskType.LOAD;
                }

                public void run() {
                    try {
                        //clear();
                        loadUnsafe();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            MainThread.getInstance().schedule(loadTask);

            // If it loaded correctly cancel the error
            error = false;
        } catch (Exception e) {
            error = true;
            permissions.getLog().error("Permissions for world:" + getName() + " did not load correctly!");
            e.printStackTrace();
        }
        return true;
    }

    protected synchronized void loadUnsafe() throws IOException, ObjectMappingException {
        if (!ufile.exists()) {
            if (ufile.getParentFile() != null) {
                ufile.getParentFile().mkdirs();
            }
            try {
                ufile.createNewFile();
                gfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (wm.getFileFormat().equalsIgnoreCase("YML")) {
            // YAML format
            YAMLConfigurationLoader.Builder ubuilder = YAMLConfigurationLoader.builder();
            ubuilder.setFlowStyle(DumperOptions.FlowStyle.BLOCK).setFile(ufile).setIndent(2);
            YAMLConfigurationLoader.Builder gbuilder = YAMLConfigurationLoader.builder().setIndent(2);
            gbuilder.setFlowStyle(DumperOptions.FlowStyle.BLOCK).setFile(gfile).setIndent(2);

            uloader = ubuilder.build();
            gloader = gbuilder.build();
        } else if (wm.getFileFormat().equalsIgnoreCase("HOCON")) {
            // assume HOCON
            HoconConfigurationLoader.Builder ubuilder = HoconConfigurationLoader.builder();
            ubuilder.setFile(ufile).setRenderOptions(ConfigRenderOptions.defaults());
            HoconConfigurationLoader.Builder gbuilder = HoconConfigurationLoader.builder();
            gbuilder.setFile(gfile).setRenderOptions(ConfigRenderOptions.defaults());

            uloader = ubuilder.build();
            gloader = gbuilder.build();
        } else {
            permissions.getLog().error("The file format " + wm.getFileFormat() + " is not supported!");
            permissions.getLog().error("Please use either HOCON or YML as the file format.");
            return;
        }

        uconfig = uloader.load();
        gconfig = gloader.load();

        // load groups
        ConfigurationNode groupsConfig = gconfig.getNode(GROUPS);
        Map<String, ConfigurationNode> gchildren = (Map<String, ConfigurationNode>) groupsConfig.getValue();
        if (gchildren != null) {
            List<String> names = new ArrayList<String>();
            for (String name : gchildren.keySet()) {
                names.add(name);
            }
            groupsArray = names.toArray(new String[names.size()]);

            //for (Player player : permissions.game.getServer().getOnlinePlayers()) {
            for (String name : groupsArray) {
                //String name = player.getUniqueId().toString();
                ConfigurationNode permsNode = groupsConfig.getNode(name, PERMISSIONS);
                List<String> nPerm = permsNode.getList(stringToken);
                List<String> nGroup = groupsConfig.getNode(name, GROUPS).getList(stringToken);
                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new user
                Group group = new Group(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationNode meta = groupsConfig.getNode(name, META);
                if (meta != null) {
                    Map<String, String> keys = (Map<String, String>) meta.getValue();
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys.keySet()) {
                            group.setValue(key, keys.get(key));
                        }
                    }
                }
                // Upload to API
                remove(group);
                add(group);
                group.setLoaded();
            }
        } else {
            Debugger.log("Empty ConfigurationSection:" + GROUPS + ":" + gfile.getPath());
        }


        // load users
        ConfigurationNode usersConfig = uconfig.getNode(USERS);
        Map<String, ConfigurationNode> uchildren = (Map<String, ConfigurationNode>) usersConfig.getValue();
        if (uchildren != null) {
            List<String> names = new ArrayList<String>();
            for (String name : uchildren.keySet()) {
                names.add(name);
            }
            usersArray = names.toArray(new String[names.size()]);

            for (Player player : permissions.game.getServer().getOnlinePlayers()) {
                String name = player.getUniqueId().toString();
                ConfigurationNode permsNode = usersConfig.getNode(name, PERMISSIONS);
                List<String> nPerm = permsNode.getList(stringToken);
                List<String> nGroup = usersConfig.getNode(name, GROUPS).getList(stringToken);
                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new user
                User user = new User(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationNode meta = usersConfig.getNode(name, META);
                if (meta != null) {
                    Map<String, String> keys = (Map<String, String>) meta.getValue();
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys.keySet()) {
                            user.setValue(key, keys.get(key));
                        }
                    }
                }
                // Upload to API
                remove(user);
                add(user);
                user.setLoaded();

                try {
                    user.calculateMappedPermissions();
                    user.calculateEffectiveMeta();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Debugger.log("Empty ConfigurationSection:" + USERS + ":" + ufile.getPath());
        }

        permissions.getLog().info("Permissions for world " + super.getName() + " has loaded!");
    }

    @Override
    public boolean save() {
        if (MainThread.getInstance() == null) {
            Debugger.log("MainThread cancelled");
            return false;
        }

        try {
            // load async
            TaskRunnable saveTask = new TaskRunnable() {
                @Override
                public TaskType getType() {
                    return TaskType.SAVE;
                }

                public void run() {
                    try {
                        //clear();
                        saveUnsafe(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            MainThread.getInstance().schedule(saveTask);

            // If it loaded correctly cancel the error
            error = false;
        } catch (Exception e) {
            error = true;
            permissions.getLog().error("Permissions for world:" + getName() + " did not save correctly!");
            e.printStackTrace();
        }
        return true;
    }

    protected synchronized void saveUnsafe(boolean sort) throws IOException {
        boolean autoSave = wm.getAutoSave();
        wm.setAutoSave(false);
        if (!ufile.exists()) {
            if (ufile.getParentFile() != null) {
                ufile.getParentFile().mkdirs();
            }
            ufile.createNewFile();
            gfile.createNewFile();
        }

        ConfigurationNode usaveconfig = uconfig;
        ConfigurationNode gsaveconfig = gconfig;

        String def = getDefaultGroup();
        gsaveconfig.getNode("default").setValue(def);

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
                usaveconfig.getNode(USERS + "." + name).setValue(null);

                // don't save users with default settings
                if (user.getMeta().size() == 0
                        && user.getPermissions().size() == 0
                        && (user.getGroupsAsString().size() == 0
                        || (user.getGroupsAsString().size() == 1
                        && user.getGroupsAsString().iterator().next().equals(getDefaultGroup())))) {
                    continue;
                }

                if (isUUID(name) && permissions.game.getServer().getPlayer(UUID.fromString(name)).get() != null) {
                    String player = permissions.game.getServer().getPlayer(UUID.fromString(name)).get().getName();
                    // save their username
                    usaveconfig.getNode(USERS + "." + name + "." + USERNAME).setValue(player);

                    usaveconfig.getNode(USERS + "." + name + "." + PERMISSIONS).setValue(user.serialisePermissions());
                    usaveconfig.getNode(USERS + "." + name + "." + GROUPS).setValue(user.serialiseGroups());
                    usaveconfig.getNode(USERS + "." + name + "." + META).setValue(user.getMeta());
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
            if (name.equals("")) continue;

            name = name.replace(".", "");

            usaveconfig.getNode(USERS + "." + name + "." + PERMISSIONS).setValue(group.serialisePermissions());
            usaveconfig.getNode(USERS + "." + name + "." + GROUPS).setValue(group.serialiseGroups());
            usaveconfig.getNode(USERS + "." + name + "." + META).setValue(group.getMeta());
        }

        if (!wm.isUseGlobalUsers() || getName().equalsIgnoreCase("global"))
            uloader.save(usaveconfig);

        gloader.save(gsaveconfig);
    }

    public boolean convertToHocon() {
        // convert files to the hocon format
        File ufile = new File(root, "users.hocon");
        File gfile = new File(root, "groups.hocon");

        if (!ufile.exists()) {
            if (ufile.getParentFile() != null) {
                ufile.getParentFile().mkdirs();
            }
            try {
                ufile.createNewFile();
                gfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HoconConfigurationLoader.Builder ubuilder = HoconConfigurationLoader.builder();
        ubuilder.setFile(ufile);
        HoconConfigurationLoader.Builder gbuilder = HoconConfigurationLoader.builder();
        gbuilder.setFile(gfile);

        try {
            ubuilder.build().save(uconfig);
            gbuilder.build().save(gconfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean convertToYml() {
        // convert files to the yml format
        File ufile = new File(root, "users.yml");
        File gfile = new File(root, "groups.yml");

        if (!ufile.exists()) {
            if (ufile.getParentFile() != null) {
                ufile.getParentFile().mkdirs();
            }
            try {
                ufile.createNewFile();
                gfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        YAMLConfigurationLoader.Builder ubuilder = YAMLConfigurationLoader.builder();
        ubuilder.setFlowStyle(DumperOptions.FlowStyle.BLOCK).setFile(ufile).setIndent(2);
        YAMLConfigurationLoader.Builder gbuilder = YAMLConfigurationLoader.builder().setIndent(2);
        gbuilder.setFlowStyle(DumperOptions.FlowStyle.BLOCK).setFile(gfile).setIndent(2);

        try {
            ubuilder.build().save(uconfig);
            gbuilder.build().save(gconfig);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean loadOne(String name, CalculableType type) {
        long t = System.currentTimeMillis();

        if (contains(name, type))
            return true;

        if (!storeContains(name, type))
            return false;

        if (type == CalculableType.USER) {
            ConfigurationNode usersConfig = uconfig.getNode(USERS);
            Map<String, ConfigurationNode> uchildren = (Map<String, ConfigurationNode>) usersConfig.getValue();
            if (uchildren != null) {
                ConfigurationNode permsNode = usersConfig.getNode(name, PERMISSIONS);
                List<String> nPerm;
                List<String> nGroup;
                try {
                    nPerm = permsNode.getList(stringToken);
                    nGroup = usersConfig.getNode(name, GROUPS).getList(stringToken);
                } catch (ObjectMappingException e) {
                    e.printStackTrace();
                    return false;
                }
                Set<Permission> perms = Permission.loadFromString(nPerm);
                // Create the new user
                User user = new User(name, nGroup, perms, getName(), this);
                // MetaData
                ConfigurationNode meta = usersConfig.getNode(name, META);
                if (meta != null) {
                    Map<String, String> keys = (Map<String, String>) meta.getValue();
                    if (keys != null && keys.size() > 0) {
                        for (String key : keys.keySet()) {
                            user.setValue(key, keys.get(key));
                        }
                    }
                }
                // Upload to API
                remove(user);
                add(user);
                user.setLoaded();

                try {
                    user.calculateMappedPermissions();
                    user.calculateEffectiveMeta();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (isOnline(user)) {
                    setupPlayer(name);
                }
            } else {
                Debugger.log("Empty ConfigurationSection:" + USERS + ":" + ufile.getPath());
            }
        } else if (type == CalculableType.GROUP) {
            return false;
        }

        return true;
    }

    @Override
    public boolean storeContains(String name, CalculableType type) {
        return false;
    }

    @Override
    public void setDefaultGroup(String group) {
        gconfig.getNode("default").setValue(group);
    }

    @Override
    public String getDefaultGroup() {
        if (gconfig != null) {
            return gconfig.getNode("default").getString("default");
        }
        return "default";
    }


    @Override
    public boolean setupPlayer(String player) {
        return false;
    }

    @Override
    public UUID getUUID(String player) {
        return null;
    }
}
