package com.jagrosh.jdautilities.command;

import java.util.function.Predicate;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public abstract class Command
{
    protected String name;
    protected String help;
    protected Category category;
    protected String arguments;
    protected boolean guildOnly;
    protected String requiredRole;
    protected boolean ownerCommand;
    protected int cooldown;
    protected Permission[] userPermissions;
    protected Permission[] botPermissions;
    protected String[] aliases = new String[0];
    protected Command[] children = new Command[0];
    protected boolean hidden;

    protected abstract void execute(CommandEvent event);

    public final void run(CommandEvent event)
    {
        if(guildOnly && event.getGuild() == null)
        {
            event.replyError("This command cannot be used in direct messages.");
            return;
        }
        if(ownerCommand && !event.isOwner())
        {
            event.replyError("Only the bot owner can use that command.");
            return;
        }
        if(category != null && !category.test(event))
        {
            event.replyError("You do not have permission to use that command.");
            return;
        }
        if(event.getGuild() != null && userPermissions != null && userPermissions.length > 0 && !event.getMember().hasPermission(userPermissions))
        {
            event.replyError("You do not have permission to use that command.");
            return;
        }
        if(event.getGuild() != null && botPermissions != null && botPermissions.length > 0 && !event.getSelfMember().hasPermission(botPermissions))
        {
            event.replyError("I do not have permission to do that here.");
            return;
        }
        execute(event);
    }

    public boolean isCommandFor(String input)
    {
        if(input == null)
            return false;
        if(name != null && name.equalsIgnoreCase(input))
            return true;
        if(aliases != null)
            for(String alias : aliases)
                if(alias.equalsIgnoreCase(input))
                    return true;
        return false;
    }

    public boolean isAllowed(TextChannel ignored)
    {
        return true;
    }

    public String getName()
    {
        return name;
    }

    public String getHelp()
    {
        return help;
    }

    public Category getCategory()
    {
        return category;
    }

    public String getArguments()
    {
        return arguments;
    }

    public String[] getAliases()
    {
        return aliases;
    }

    public Command[] getChildren()
    {
        return children;
    }

    public static class Category
    {
        private final String name;
        private final Predicate<CommandEvent> predicate;

        public Category(String name)
        {
            this(name, event -> true);
        }

        public Category(String name, Predicate<CommandEvent> predicate)
        {
            this.name = name;
            this.predicate = predicate;
        }

        public String getName()
        {
            return name;
        }

        public boolean test(CommandEvent event)
        {
            return predicate == null || predicate.test(event);
        }
    }
}
