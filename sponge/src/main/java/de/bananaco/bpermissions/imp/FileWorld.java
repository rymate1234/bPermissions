package de.bananaco.bpermissions.imp;

import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.World;
import de.bananaco.bpermissions.api.WorldManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

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
            ubuilder.setFile(gfile);
            try {
                uconfig = ubuilder.build().load();
                gconfig = gbuilder.build().load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // assume HOCON
            HoconConfigurationLoader.Builder ubuilder = HoconConfigurationLoader.builder();
            ubuilder.setFile(ufile);
            HoconConfigurationLoader.Builder gbuilder = HoconConfigurationLoader.builder();
            ubuilder.setFile(gfile);
            try {
                uconfig = ubuilder.build().load();
                gconfig = gbuilder.build().load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean save() {
        // test save as HOCON
        File ufile = new File(root, "users.hocon");
        File gfile = new File(root, "groups.hocon");

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
