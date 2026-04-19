package com.jagrosh.jdautilities.command;

import net.dv8tion.jda.api.entities.Guild;

public interface GuildSettingsManager<T>
{
    T getSettings(Guild guild);

    default void init()
    {
    }

    default void shutdown()
    {
    }
}
