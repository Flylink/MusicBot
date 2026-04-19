package com.jagrosh.jdautilities.examples.command;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class PingCommand extends Command
{
    public PingCommand()
    {
        this.name = "ping";
        this.help = "shows the bot latency";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.replySuccess("Pong!");
    }
}
