package com.jagrosh.jdautilities.command;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

class SimpleCommandClient extends ListenerAdapter implements CommandClient
{
    private final String ownerId;
    private final String[] coOwnerIds;
    private final String prefix;
    private final String altPrefix;
    private final String success;
    private final String warning;
    private final String error;
    private final String helpWord;
    private final String serverInvite;
    private final ScheduledExecutorService scheduleExecutor;
    private final GuildSettingsManager settingsManager;
    private final List<Command> commands;
    private final OffsetDateTime startTime = OffsetDateTime.now();
    private final Map<String, OffsetDateTime> cooldowns = new HashMap<>();
    private final Map<String, Integer> commandUses = new HashMap<>();
    private CommandListener listener;

    SimpleCommandClient(String ownerId, String[] coOwnerIds, String prefix, String altPrefix, String success, String warning,
                        String error, String helpWord, String serverInvite, CommandListener listener,
                        ScheduledExecutorService scheduleExecutor, GuildSettingsManager settingsManager, List<Command> commands)
    {
        this.ownerId = ownerId;
        this.coOwnerIds = coOwnerIds == null ? new String[0] : coOwnerIds;
        this.prefix = prefix;
        this.altPrefix = altPrefix;
        this.success = success;
        this.warning = warning;
        this.error = error;
        this.helpWord = helpWord;
        this.serverInvite = serverInvite;
        this.listener = listener;
        this.scheduleExecutor = scheduleExecutor;
        this.settingsManager = settingsManager;
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if(event.getAuthor().isBot() || event.isWebhookMessage())
            return;
        String raw = event.getMessage().getContentRaw();
        String usedPrefix = resolvePrefix(event, raw);
        if(usedPrefix == null)
            return;
        String remaining = raw.substring(usedPrefix.length()).trim();
        if(remaining.isEmpty())
            return;

        String[] parts = remaining.split("\\s+", 2);
        if(helpWord != null && helpWord.equalsIgnoreCase(parts[0]))
        {
            sendHelp(new CommandEvent(event, parts.length > 1 ? parts[1] : "", this));
            return;
        }
        Command command = findCommand(parts[0], commands);
        if(command == null)
            return;
        String args = parts.length > 1 ? parts[1] : "";
        while(!args.isEmpty() && command.getChildren() != null && command.getChildren().length > 0)
        {
            String[] childParts = args.split("\\s+", 2);
            Command child = findCommand(childParts[0], java.util.Arrays.asList(command.getChildren()));
            if(child == null)
                break;
            command = child;
            args = childParts.length > 1 ? childParts[1] : "";
        }
        commandUses.merge(command.getName().toLowerCase(Locale.ROOT), 1, Integer::sum);
        command.run(new CommandEvent(event, args, this));
    }

    private String resolvePrefix(MessageReceivedEvent event, String raw)
    {
        if("@mention".equals(prefix))
        {
            String mention1 = event.getJDA().getSelfUser().getAsMention() + " ";
            String mention2 = "<@!" + event.getJDA().getSelfUser().getId() + "> ";
            if(raw.startsWith(mention1)) return mention1;
            if(raw.startsWith(mention2)) return mention2;
        }
        else if(prefix != null && raw.startsWith(prefix))
            return prefix;

        if(altPrefix != null && raw.startsWith(altPrefix))
            return altPrefix;

        if(event.isFromGuild() && settingsManager != null)
        {
            Object settings = settingsManager.getSettings(event.getGuild());
            if(settings instanceof GuildSettingsProvider)
                for(String guildPrefix : ((GuildSettingsProvider) settings).getPrefixes())
                    if(guildPrefix != null && raw.startsWith(guildPrefix))
                        return guildPrefix;
        }
        return null;
    }

    private Command findCommand(String invoke, List<Command> commandList)
    {
        for(Command command : commandList)
            if(command.isCommandFor(invoke))
                return command;
        return null;
    }

    private Command findCommandRecursive(String invoke, List<Command> commandList)
    {
        Command command = findCommand(invoke, commandList);
        if(command != null)
            return command;
        for(Command parent : commandList)
        {
            if(parent.getChildren() == null || parent.getChildren().length == 0)
                continue;
            Command child = findCommandRecursive(invoke, java.util.Arrays.asList(parent.getChildren()));
            if(child != null)
                return child;
        }
        return null;
    }

    private void sendHelp(CommandEvent event)
    {
        String args = event.getArgs().trim();
        if(args.isEmpty())
        {
            StringBuilder builder = new StringBuilder(warning).append(" Commands:");
            for(Command command : commands)
            {
                if(command.hidden)
                    continue;
                builder.append("\n`").append(prefix).append(command.getName());
                if(command.getArguments() != null && !command.getArguments().isEmpty())
                    builder.append(" ").append(command.getArguments());
                builder.append("` - ").append(command.getHelp() == null ? "no description" : command.getHelp());
            }
            event.reply(builder.toString());
            return;
        }

        Command command = findCommandRecursive(args.split("\\s+")[0], commands);
        if(command == null || command.hidden)
        {
            event.replyError("No command found matching `" + args + "`.");
            return;
        }

        StringBuilder builder = new StringBuilder(success).append(" `").append(prefix).append(command.getName());
        if(command.getArguments() != null && !command.getArguments().isEmpty())
            builder.append(" ").append(command.getArguments());
        builder.append("`");
        if(command.getHelp() != null && !command.getHelp().isEmpty())
            builder.append("\n").append(command.getHelp());
        if(command.getAliases() != null && command.getAliases().length > 0)
            builder.append("\nAliases: ").append(String.join(", ", command.getAliases()));
        if(command.getChildren() != null && command.getChildren().length > 0)
        {
            builder.append("\nSubcommands:");
            for(Command child : command.getChildren())
            {
                if(child.hidden)
                    continue;
                builder.append("\n - ").append(child.getName());
                if(child.getArguments() != null && !child.getArguments().isEmpty())
                    builder.append(" ").append(child.getArguments());
            }
        }
        event.reply(builder.toString());
    }

    @Override public String getPrefix(){ return prefix; }
    @Override public String getAltPrefix(){ return altPrefix; }
    @Override public String getTextualPrefix(){ return prefix; }
    @Override public void addCommand(Command command){ commands.add(command); }
    @Override public void addCommand(Command command, int index){ commands.add(index, command); }
    @Override public void removeCommand(String name){ commands.removeIf(c -> c.isCommandFor(name)); }
    @Override public void addAnnotatedModule(Object module){ }
    @Override public void addAnnotatedModule(Object module, java.util.function.Function<Command, Integer> ordering){ }
    @Override public void setListener(CommandListener listener){ this.listener = listener; }
    @Override public CommandListener getListener(){ return listener; }
    @Override public List<Command> getCommands(){ return commands; }
    @Override public OffsetDateTime getStartTime(){ return startTime; }
    @Override public OffsetDateTime getCooldown(String key){ return cooldowns.get(key); }
    @Override public int getRemainingCooldown(String key){ return 0; }
    @Override public void applyCooldown(String key, int seconds){ cooldowns.put(key, OffsetDateTime.now().plusSeconds(seconds)); }
    @Override public void cleanCooldowns(){ }
    @Override public int getCommandUses(Command command){ return getCommandUses(command.getName()); }
    @Override public int getCommandUses(String name){ return commandUses.getOrDefault(name.toLowerCase(Locale.ROOT), 0); }
    @Override public String getOwnerId(){ return ownerId; }
    @Override public long getOwnerIdLong(){ return ownerId == null ? 0L : Long.parseLong(ownerId); }
    @Override public String[] getCoOwnerIds(){ return coOwnerIds; }
    @Override public long[] getCoOwnerIdsLong(){ return java.util.Arrays.stream(coOwnerIds).mapToLong(Long::parseLong).toArray(); }
    @Override public String getSuccess(){ return success; }
    @Override public String getWarning(){ return warning; }
    @Override public String getError(){ return error; }
    @Override public ScheduledExecutorService getScheduleExecutor(){ return scheduleExecutor; }
    @Override public String getServerInvite(){ return serverInvite; }
    @Override public int getTotalGuilds(){ return 0; }
    @Override public String getHelpWord(){ return helpWord; }
    @Override public boolean usesLinkedDeletion(){ return false; }
    @Override public <S> S getSettingsFor(Guild guild){ return settingsManager == null ? null : (S) settingsManager.getSettings(guild); }
    @Override public <M extends GuildSettingsManager> M getSettingsManager(){ return (M) settingsManager; }
    @Override public void shutdown(){ scheduleExecutor.shutdownNow(); }
}
