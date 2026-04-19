package com.jagrosh.jdautilities.commons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public class FinderUtil
{
    public static List<TextChannel> findTextChannels(String query, Guild guild)
    {
        return find(query, guild.getTextChannels());
    }

    public static List<VoiceChannel> findVoiceChannels(String query, Guild guild)
    {
        return find(query, guild.getVoiceChannels());
    }

    public static List<Role> findRoles(String query, Guild guild)
    {
        return find(query, guild.getRoles());
    }

    public static List<Member> findMembers(String query, Guild guild)
    {
        return find(query, guild.getMembers());
    }

    private static <T> List<T> find(String query, List<T> values)
    {
        String lowered = query.toLowerCase(Locale.ROOT).trim();
        List<T> exact = new ArrayList<>();
        List<T> partial = new ArrayList<>();
        for(T value : values)
        {
            String name = extractName(value).toLowerCase(Locale.ROOT);
            String id = extractId(value);
            if(id.equals(query) || name.equals(lowered))
                exact.add(value);
            else if(name.contains(lowered))
                partial.add(value);
        }
        return exact.isEmpty() ? partial : exact;
    }

    private static String extractName(Object value)
    {
        if(value instanceof TextChannel) return ((TextChannel) value).getName();
        if(value instanceof VoiceChannel) return ((VoiceChannel) value).getName();
        if(value instanceof Role) return ((Role) value).getName();
        if(value instanceof Member) return ((Member) value).getEffectiveName();
        return value.toString();
    }

    private static String extractId(Object value)
    {
        if(value instanceof net.dv8tion.jda.api.entities.ISnowflake) return ((net.dv8tion.jda.api.entities.ISnowflake) value).getId();
        return "";
    }
}
