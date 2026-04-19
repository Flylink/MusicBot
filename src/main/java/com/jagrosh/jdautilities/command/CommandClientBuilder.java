package com.jagrosh.jdautilities.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class CommandClientBuilder
{
    private final List<Command> commands = new ArrayList<>();
    private String ownerId;
    private String[] coOwnerIds = new String[0];
    private String prefix;
    private String altPrefix;
    private String success = "";
    private String warning = "";
    private String error = "";
    private String helpWord = "help";
    private String serverInvite;
    private CommandListener listener;
    private ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor();
    private GuildSettingsManager settingsManager;

    public CommandClient build()
    {
        return new SimpleCommandClient(ownerId, coOwnerIds, prefix, altPrefix, success, warning, error, helpWord,
                serverInvite, listener, scheduleExecutor, settingsManager, commands);
    }

    public CommandClientBuilder setOwnerId(String ownerId){ this.ownerId = ownerId; return this; }
    public CommandClientBuilder setCoOwnerIds(String... coOwnerIds){ this.coOwnerIds = coOwnerIds; return this; }
    public CommandClientBuilder setPrefix(String prefix){ this.prefix = prefix; return this; }
    public CommandClientBuilder setAlternativePrefix(String altPrefix){ this.altPrefix = altPrefix; return this; }
    public CommandClientBuilder useHelpBuilder(boolean ignored){ return this; }
    public CommandClientBuilder setHelpConsumer(java.util.function.Consumer<CommandEvent> ignored){ return this; }
    public CommandClientBuilder setHelpWord(String helpWord){ this.helpWord = helpWord; return this; }
    public CommandClientBuilder setServerInvite(String serverInvite){ this.serverInvite = serverInvite; return this; }
    public CommandClientBuilder setEmojis(String success, String warning, String error){ this.success = success; this.warning = warning; this.error = error; return this; }
    public CommandClientBuilder setActivity(Activity ignored){ return this; }
    public CommandClientBuilder useDefaultGame(){ return this; }
    public CommandClientBuilder setStatus(OnlineStatus ignored){ return this; }
    public CommandClientBuilder addCommand(Command command){ this.commands.add(command); return this; }
    public CommandClientBuilder addCommands(Command... commands){ for(Command command : commands) this.commands.add(command); return this; }
    public CommandClientBuilder addAnnotatedModule(Object ignored){ return this; }
    public CommandClientBuilder addAnnotatedModules(Object... ignored){ return this; }
    public CommandClientBuilder setAnnotatedCompiler(Object ignored){ return this; }
    public CommandClientBuilder setCarbonitexKey(String ignored){ return this; }
    public CommandClientBuilder setDiscordBotsKey(String ignored){ return this; }
    public CommandClientBuilder setDiscordBotListKey(String ignored){ return this; }
    public CommandClientBuilder setListener(CommandListener listener){ this.listener = listener; return this; }
    public CommandClientBuilder setScheduleExecutor(ScheduledExecutorService scheduleExecutor){ this.scheduleExecutor = scheduleExecutor; return this; }
    public CommandClientBuilder setShutdownAutomatically(boolean ignored){ return this; }
    public CommandClientBuilder setLinkedCacheSize(int ignored){ return this; }
    public CommandClientBuilder setGuildSettingsManager(GuildSettingsManager settingsManager){ this.settingsManager = settingsManager; return this; }
}
