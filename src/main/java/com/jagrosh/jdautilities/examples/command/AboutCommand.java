package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import java.awt.Color;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

public class AboutCommand extends Command
{
    private final Color color;
    private final String description;
    private final String[] features;
    private boolean isAuthor = true;
    private String replacementCharacter = "";

    public AboutCommand(Color color, String description, String[] features, Permission[] ignored)
    {
        this.color = color;
        this.description = description;
        this.features = features;
        this.name = "about";
        this.help = "shows information about the bot";
        this.guildOnly = false;
    }

    public void setIsAuthor(boolean isAuthor)
    {
        this.isAuthor = isAuthor;
    }

    public void setReplacementCharacter(String replacementCharacter)
    {
        this.replacementCharacter = replacementCharacter;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        EmbedBuilder eb = new EmbedBuilder()
                .setColor(color)
                .setTitle(event.getSelfUser().getName())
                .setDescription(description + "\n\n" + String.join("\n", features));
        if(isAuthor)
            eb.setFooter(replacementCharacter + " " + event.getAuthor().getName(), event.getAuthor().getEffectiveAvatarUrl());
        event.reply(eb.build());
    }
}
