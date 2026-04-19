package com.jagrosh.jdautilities.command;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import net.dv8tion.jda.api.entities.Guild;

public interface CommandClient
{
    String getPrefix();
    String getAltPrefix();
    String getTextualPrefix();
    void addCommand(Command command);
    void addCommand(Command command, int index);
    void removeCommand(String name);
    void addAnnotatedModule(Object module);
    void addAnnotatedModule(Object module, java.util.function.Function<Command, Integer> ordering);
    void setListener(CommandListener listener);
    CommandListener getListener();
    List<Command> getCommands();
    OffsetDateTime getStartTime();
    OffsetDateTime getCooldown(String key);
    int getRemainingCooldown(String key);
    void applyCooldown(String key, int seconds);
    void cleanCooldowns();
    int getCommandUses(Command command);
    int getCommandUses(String name);
    String getOwnerId();
    long getOwnerIdLong();
    String[] getCoOwnerIds();
    long[] getCoOwnerIdsLong();
    String getSuccess();
    String getWarning();
    String getError();
    ScheduledExecutorService getScheduleExecutor();
    String getServerInvite();
    int getTotalGuilds();
    String getHelpWord();
    boolean usesLinkedDeletion();
    <S> S getSettingsFor(Guild guild);
    <M extends GuildSettingsManager> M getSettingsManager();
    void shutdown();
}
