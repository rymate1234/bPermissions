package de.bananaco.bpermissions.imp.commands;

import de.bananaco.bpermissions.api.Calculable;
import de.bananaco.bpermissions.api.CalculableType;
import de.bananaco.bpermissions.api.World;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static de.bananaco.bpermissions.imp.commands.BaseCmdHandler.getName;

/**
 * Created by Ryan on 27/08/2016.
 */
public class UserCommandElement extends CommandElement {

    public UserCommandElement(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        Optional<String> optionalStr = args.nextIfPresent();

        String name;
        if (!optionalStr.isPresent()) return args.createError(Text.of("You need to specify a user!"));
        else name = optionalStr.get();

        Server server = Sponge.getGame().getServer();
        GameProfile user;

        String[] components = name.split("-");
        try {
            if (components.length == 5) {
                user = server.getGameProfileManager().get(UUID.fromString(name)).get();
            } else {
                user = server.getGameProfileManager().get(name).get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        return user;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        Optional<String> optionalStr = args.nextIfPresent();
        String toCheck;
        if (!optionalStr.isPresent()) return Collections.emptyList();
        else toCheck = optionalStr.get();

        return Sponge.getGame().getServer().getOnlinePlayers().stream()
                .filter(player -> player.getName().toLowerCase().startsWith(toCheck.toLowerCase()))
                .map(input -> input == null ? null : input.getName())
                .collect(Collectors.toList());
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of("<name/uuid>");
    }


}
