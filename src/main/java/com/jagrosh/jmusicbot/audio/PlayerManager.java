/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.audio;

import com.dunctebot.sourcemanagers.DuncteBotSources;
import com.jagrosh.jmusicbot.Bot;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerRegistry;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.getyarn.GetyarnAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.AndroidMusic;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.MWeb;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.Tv;
import dev.lavalink.youtube.clients.TvHtml5Simply;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.WebEmbedded;
import dev.lavalink.youtube.clients.skeleton.Client;
import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class PlayerManager extends DefaultAudioPlayerManager
{
    private static final Logger LOG = LoggerFactory.getLogger(PlayerManager.class);
    private final Bot bot;
    
    public PlayerManager(Bot bot)
    {
        this.bot = bot;
    }
    
    public void init()
    {
        TransformativeAudioSourceManager.createTransforms(bot.getConfig().getTransforms()).forEach(t -> registerSourceManager(t));
        registerSourceManager(new YtdlpAudioSourceManager(
                bot.getConfig().getYoutubeYtdlpPath(),
                bot.getConfig().getYoutubeYtdlpCookiesFromBrowser(),
                bot.getConfig().getYoutubeYtdlpCookiesFile()));

        String poToken = bot.getConfig().getYoutubePoToken();
        String visitorData = bot.getConfig().getYoutubeVisitorData();
        boolean hasPoToken = poToken != null && !poToken.isBlank() && visitorData != null && !visitorData.isBlank();
        if(hasPoToken)
        {
            Web.setPoTokenAndVisitorData(poToken, visitorData);
            WebEmbedded.setPoTokenAndVisitorData(poToken, visitorData);
            LOG.info("Configured YouTube poToken support for WEB clients");
        }

        Client[] youtubeClients = hasPoToken
                ? new Client[] {
                    new Web(),
                    new WebEmbedded(),
                    new Music(),
                    new AndroidVr(),
                    new AndroidMusic(),
                    new MWeb(),
                    new TvHtml5Simply(),
                    new Tv()
                }
                : new Client[] {
                    new Music(),
                    new AndroidVr(),
                    new AndroidMusic(),
                    new MWeb(),
                    new Web(),
                    new WebEmbedded(),
                    new TvHtml5Simply(),
                    new Tv()
                };

        YoutubeAudioSourceManager yt = new YoutubeAudioSourceManager(true, youtubeClients);

        String refreshToken = bot.getConfig().getYoutubeOauthRefreshToken();
        if(hasPoToken)
        {
            LOG.info("Skipping YouTube OAuth because poToken is configured");
        }
        else if(refreshToken != null && !refreshToken.isBlank())
            yt.useOauth2(refreshToken, true);
        else if(bot.getConfig().useYoutubeOauthAutoInit())
            yt.useOauth2(null, false);

        LOG.info("Configured YouTube clients: {}",
                java.util.Arrays.stream(youtubeClients)
                        .map(Client::getIdentifier)
                        .reduce((left, right) -> left + ", " + right)
                        .orElse("<none>"));
        yt.setPlaylistPageCount(bot.getConfig().getMaxYTPlaylistPages());
        registerSourceManager(yt);

        registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        registerSourceManager(new BandcampAudioSourceManager());
        registerSourceManager(new VimeoAudioSourceManager());
        registerSourceManager(new TwitchStreamAudioSourceManager());
        registerSourceManager(new BeamAudioSourceManager());
        registerSourceManager(new GetyarnAudioSourceManager());
        registerSourceManager(new NicoAudioSourceManager());
        registerSourceManager(new HttpAudioSourceManager(MediaContainerRegistry.DEFAULT_REGISTRY));

        AudioSourceManagers.registerLocalSource(this);

        DuncteBotSources.registerAll(this, "en-US");

        getConfiguration().setFilterHotSwapEnabled(true);
        setFrameBufferDuration(1000);
    }
    
    public Bot getBot()
    {
        return bot;
    }
    
    public boolean hasHandler(Guild guild)
    {
        return guild.getAudioManager().getSendingHandler()!=null;
    }
    
    public AudioHandler setUpHandler(Guild guild)
    {
        AudioHandler handler;
        if(guild.getAudioManager().getSendingHandler()==null)
        {
            AudioPlayer player = createPlayer();
            player.setVolume(bot.getSettingsManager().getSettings(guild).getVolume());
            handler = new AudioHandler(this, guild, player);
            player.addListener(handler);
            guild.getAudioManager().setSendingHandler(handler);
        }
        else
            handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
        return handler;
    }
}
