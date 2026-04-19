package com.jagrosh.jdautilities.command;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandEvent
{
    private final MessageReceivedEvent event;
    private final String args;
    private final CommandClient client;

    public CommandEvent(MessageReceivedEvent event, String args, CommandClient client)
    {
        this.event = event;
        this.args = args == null ? "" : args;
        this.client = client;
    }

    public String getArgs(){ return args; }
    public MessageReceivedEvent getEvent(){ return event; }
    public CommandClient getClient(){ return client; }

    public void reply(String text){ getChannel().sendMessage(text).queue(); }
    public void reply(String text, Consumer<Message> success){ getChannel().sendMessage(text).queue(success); }
    public void reply(String text, Consumer<Message> success, Consumer<Throwable> failure){ getChannel().sendMessage(text).queue(success, failure); }
    public void reply(MessageEmbed embed){ getChannel().sendMessageEmbeds(embed).queue(); }
    public void reply(MessageEmbed embed, Consumer<Message> success){ getChannel().sendMessageEmbeds(embed).queue(success); }
    public void reply(Message message){ getChannel().sendMessage(message).queue(); }
    public void reply(Message message, Consumer<Message> success){ getChannel().sendMessage(message).queue(success); }
    public void reply(File file, String filename){ getChannel().sendFile(file, filename).queue(); }
    public void reply(String content, File file, String filename){ getChannel().sendFile(file, filename).content(content).queue(); }
    public void replyFormatted(String format, Object... args){ reply(String.format(format, args)); }
    public void replyOrAlternate(MessageEmbed embed, String alternate){ reply(embed); }
    public void replyOrAlternate(String content, File file, String filename, String alternate){ reply(content, file, filename); }
    public void replyInDm(String text){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(text).queue()); }
    public void replyInDm(String text, Consumer<Message> success){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(text).queue(success)); }
    public void replyInDm(MessageEmbed embed){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue()); }
    public void replyInDm(Message message){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(message).queue()); }
    public void replySuccess(String text){ reply(client.getSuccess() + " " + text); }
    public void replySuccess(String text, Consumer<Message> success){ reply(client.getSuccess() + " " + text, success); }
    public void replyWarning(String text){ reply(client.getWarning() + " " + text); }
    public void replyWarning(String text, Consumer<Message> success){ reply(client.getWarning() + " " + text, success); }
    public void replyError(String text){ reply(client.getError() + " " + text); }
    public void replyError(String text, Consumer<Message> success){ reply(client.getError() + " " + text, success); }
    public void async(Runnable runnable){ client.getScheduleExecutor().submit(runnable); }
    public static ArrayList<String> splitMessage(String input){ return new ArrayList<>(Arrays.asList(input)); }

    public SelfUser getSelfUser(){ return event.getJDA().getSelfUser(); }
    public Member getSelfMember(){ return event.isFromGuild() ? event.getGuild().getSelfMember() : null; }
    public boolean isOwner(){ return getAuthor().getId().equals(client.getOwnerId()); }
    public User getAuthor(){ return event.getAuthor(); }
    public MessageChannel getChannel(){ return event.getChannel(); }
    public ChannelType getChannelType(){ return event.getChannelType(); }
    public Guild getGuild(){ return event.isFromGuild() ? event.getGuild() : null; }
    public JDA getJDA(){ return event.getJDA(); }
    public Member getMember(){ return event.getMember(); }
    public Message getMessage(){ return event.getMessage(); }
    public PrivateChannel getPrivateChannel(){ return event.isFromType(ChannelType.PRIVATE) ? event.getPrivateChannel() : null; }
    public long getResponseNumber(){ return event.getResponseNumber(); }
    public TextChannel getTextChannel(){ return event.isFromType(ChannelType.TEXT) ? event.getTextChannel() : null; }
    public boolean isFromType(ChannelType type){ return event.isFromType(type); }
}
