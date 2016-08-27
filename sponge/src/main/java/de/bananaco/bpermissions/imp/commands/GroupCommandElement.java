package de.bananaco.bpermissions.imp.commands;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.World;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;

import static de.bananaco.bpermissions.imp.commands.BaseCmdHandler.getName;

/**
 * Created by Ryan on 27/08/2016.
 */
public class GroupCommandElement extends CommandElement {
    private final HashMap<String, Commands> commands;

    public GroupCommandElement(@Nullable Text key, HashMap<String, Commands> cmds) {
        super(key);
        this.commands = cmds;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        if (args.getAll().size() > 1) throw args.createError(Text.of("Too many arguments!"));

        Optional<String> optionalStr = args.nextIfPresent();
        String toReturn;
        if (!optionalStr.isPresent()) return args.createError(Text.of("You need to specify a group!"));
        else toReturn = optionalStr.get();

        return toReturn;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        Commands cmd = getCommands(src);

        World world = cmd.getWorld();
        Set<Calculable> groupsMap = world.getAll(CalculableType.GROUP);

        List<String> completion = new ArrayList<String>();

        Optional<String> optionalStr = args.nextIfPresent();
        String toCheck;
        if (!optionalStr.isPresent()) return completion;
        else toCheck = optionalStr.get();

        for (Calculable group : groupsMap) {
            String groupName = group.getNameLowerCase();
            if (groupName.startsWith(toCheck)) {
                completion.add(group.getName());
            }
        }

        return completion;
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of("<name>");
    }

    public Commands getCommands(CommandSource src) {
        if (!commands.containsKey(getName(src))) {
            commands.put(getName(src), new Commands());
        }

        Commands cmd = commands.get(getName(src));

        if (cmd.getWorld() == null) {
            cmd.setDefaultWorld(src);
        }
        return cmd;
    }

}
