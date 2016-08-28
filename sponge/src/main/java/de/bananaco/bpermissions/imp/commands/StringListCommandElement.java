package de.bananaco.bpermissions.imp.commands;

import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.StartsWithPredicate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.spongepowered.api.util.SpongeApiTranslationHelper.t;

public class StringListCommandElement extends CommandElement {
    private final List<String> choices;
    private final boolean choicesInUsage;

    public StringListCommandElement(Text key, List<String> choices, boolean choicesInUsage) {
        super(key);
        this.choices = choices;
        this.choicesInUsage = choicesInUsage;
    }

    @Override
    public Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        boolean check = this.choices.contains(args.peek());
        if (!check) {
            throw args.createError(t("Argument was not a valid choice. Valid choices: %s", this.choices.toString()));
        }
        return args.next();
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        final String prefix = args.nextIfPresent().orElse("");
        return this.choices.stream().filter(new StartsWithPredicate(prefix)).collect(GuavaCollectors.toImmutableList());
    }

    @Override
    public Text getUsage(CommandSource commander) {
        if (this.choicesInUsage) {
            final Text.Builder build = Text.builder();
            build.append(CommandMessageFormatting.LT_TEXT);
            for (Iterator<String> it = this.choices.iterator(); it.hasNext();) {
                build.append(Text.of(it.next()));
                if (it.hasNext()) {
                    build.append(CommandMessageFormatting.PIPE_TEXT);
                }
            }
            build.append(CommandMessageFormatting.GT_TEXT);
            return build.build();
        } else {
            return super.getUsage(commander);
        }
    }
}