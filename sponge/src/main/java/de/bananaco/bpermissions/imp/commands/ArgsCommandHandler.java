package de.bananaco.bpermissions.imp.commands;

import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.*;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Easy way to get a String[] of args similar to bukkit
 */

public class ArgsCommandHandler extends CommandElement {
    public ArgsCommandHandler(Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String[] argsArray = args.getAll().toArray(new String[args.getAll().size()]);

        while (args.hasNext()) { // Consume remaining args
            args.next();
        }

        return argsArray;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Collections.emptyList();
    }

    @Override
    public Text getUsage(CommandSource src) {
        return Text.of(CommandMessageFormatting.LT_TEXT, getKey(), CommandMessageFormatting.ELLIPSIS_TEXT, CommandMessageFormatting.GT_TEXT);
    }
}
