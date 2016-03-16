package de.bananaco.bpermissions.imp;

import com.google.common.reflect.TypeToken;
import de.bananaco.bpermissions.api.*;
import de.bananaco.bpermissions.util.Debugger;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.spongepowered.api.entity.living.player.Player;

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

    public FileWorld(String world, Permissions permissions, File root) {
        super(world);

        this.permissions = permissions;
        if (wm.isUseGlobalUsers())
            this.ufile = new File(new File("./bPermissions/global/"), "users." + format);
        else
            this.ufile = new File(root, "users." + format);

        this.gfile = new File(root, "groups." + format);

        this.usersArray = new String[0];
        this.groupsArray = new String[0];
        this.root = root;
    }

    @Override
    public boolean load() {
        try {
            loadUnsafe();
        } catch (Exception e) {
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
            ubuilder.setFile(ufile);
            YAMLConfigurationLoader.Builder gbuilder = YAMLConfigurationLoader.builder();
            gbuilder.setFile(gfile);

            YAMLConfigurationLoader uloader = ubuilder.build();
            uconfig = uloader.load();

            YAMLConfigurationLoader gloader = gbuilder.build();
            gconfig = gloader.load();
        } else {
            // assume HOCON
            HoconConfigurationLoader.Builder ubuilder = HoconConfigurationLoader.builder();
            ubuilder.setFile(ufile);
            HoconConfigurationLoader.Builder gbuilder = HoconConfigurationLoader.builder();
            gbuilder.setFile(gfile);

            uconfig = ubuilder.build().load();
            gconfig = gbuilder.build().load();
        }

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

            //for (Player player : permissions.game.getServer().getOnlinePlayers()) {
            for (String name : usersArray) {
                //String name = player.getUniqueId().toString();
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

        Set<Calculable> test = getAll(CalculableType.GROUP);
        System.out.println(test.size());
    }

    @Override
    public boolean save() {
        // test save as HOCON
        // File ufile = new File(root, "users.hocon");
        // File gfile = new File(root, "groups.hocon");

        HoconConfigurationLoader.Builder ubuilder = HoconConfigurationLoader.builder();
        ubuilder.setFile(ufile);
        HoconConfigurationLoader.Builder gbuilder = HoconConfigurationLoader.builder();
        ubuilder.setFile(gfile);
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
        return false;
    }

    @Override
    public boolean storeContains(String name, CalculableType type) {
        return false;
    }

    @Override
    public void setDefaultGroup(String group) {

    }

    @Override
    public String getDefaultGroup() {
        return null;
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
