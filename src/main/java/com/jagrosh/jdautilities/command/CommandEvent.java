package com.jagrosh.jdautilities.command;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

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
    public void reply(MessageCreateData message){ getChannel().sendMessage(message).queue(); }
    public void reply(MessageCreateData message, Consumer<Message> success){ getChannel().sendMessage(message).queue(success); }
    public void reply(Message message){ getChannel().sendMessage(new MessageCreateBuilder().applyMessage(message).build()).queue(); }
    public void reply(Message message, Consumer<Message> success){ getChannel().sendMessage(new MessageCreateBuilder().applyMessage(message).build()).queue(success); }
    public void reply(File file, String filename){ getChannel().sendFiles(FileUpload.fromData(file, filename)).queue(); }
    public void reply(String content, File file, String filename){ getChannel().sendMessage(content).addFiles(FileUpload.fromData(file, filename)).queue(); }
    public void replyFormatted(String format, Object... args){ reply(String.format(format, args)); }
    public void replyOrAlternate(MessageEmbed embed, String alternate){ reply(embed); }
    public void replyOrAlternate(String content, File file, String filename, String alternate){ reply(content, file, filename); }
    public void replyInDm(String text){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(text).queue()); }
    public void replyInDm(String text, Consumer<Message> success){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(text).queue(success)); }
    public void replyInDm(MessageEmbed embed){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue()); }
    public void replyInDm(Message message){ getAuthor().openPrivateChannel().queue(pc -> pc.sendMessage(new MessageCreateBuilder().applyMessage(message).build()).queue()); }
    public void replySuccess(String text){ reply(client.getSuccess() + " " + text); }
    public void replySuccess(String text, Consumer<Message> success){ reply(client.getSuccess() + " " + text, success); }
    public void replyWarning(String text){ reply(client.getWarning() + " " + text); }
    public void replyWarning(String text, Consumer<Message> success){ reply(client.getWarning() + " " + text, success); }
    public void replyError(String text){ reply(client.getError() + " " + text); }
    public void replyError(String text, Consumer<Message> success){ reply(client.getError() + " " + text, success); }
    public void async(Runnable runnable){ client.getScheduleExecutor().submit(runnable); }
    public static ArrayList<String> splitMessage(String input)
    {
        ArrayList<String> parts = new ArrayList<>();
        if(input == null || input.isEmpty())
        {
            parts.add("");
            return parts;
        }

        String remaining = input;
        while(remaining.length() > 2000)
        {
            int split = remaining.lastIndexOf('\n', 2000);
            if(split <= 0)
                split = 2000;
            parts.add(remaining.substring(0, split));
            remaining = remaining.substring(split).stripLeading();
        }
        if(!remaining.isEmpty())
            parts.add(remaining);
        return parts;
    }

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
    public PrivateChannel getPrivateChannel(){ return event.isFromType(ChannelType.PRIVATE) ? event.getChannel().asPrivateChannel() : null; }
    public long getResponseNumber(){ return event.getResponseNumber(); }
    public TextChannel getTextChannel(){ return event.isFromType(ChannelType.TEXT) ? event.getChannel().asTextChannel() : null; }
    public boolean isFromType(ChannelType type){ return event.isFromType(type); }
}
