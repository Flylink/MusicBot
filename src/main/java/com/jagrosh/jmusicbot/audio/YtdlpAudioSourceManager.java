/*
 * Copyright 2026
 */
package com.jagrosh.jmusicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class YtdlpAudioSourceManager implements AudioSourceManager
{
    private static final Logger LOG = LoggerFactory.getLogger(YtdlpAudioSourceManager.class);

    private final String executable;
    private final String cookiesFromBrowser;
    private final String cookiesFile;
    private final HttpAudioSourceManager httpSourceManager;

    public YtdlpAudioSourceManager(String executable, String cookiesFromBrowser, String cookiesFile)
    {
        this.executable = executable == null || executable.isBlank() ? "yt-dlp" : executable;
        this.cookiesFromBrowser = cookiesFromBrowser == null ? "" : cookiesFromBrowser.trim();
        this.cookiesFile = cookiesFile == null ? "" : cookiesFile.trim();
        this.httpSourceManager = new HttpAudioSourceManager();
    }

    @Override
    public String getSourceName()
    {
        return "ytdlp";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference)
    {
        String identifier = reference.identifier;
        if(identifier == null)
            return null;

        if(!identifier.startsWith("http://") && !identifier.startsWith("https://") && !identifier.startsWith("ytsearch:"))
            return null;

        if(!isYoutubeReference(identifier))
            return null;

        ResolvedAudio resolved = resolve(identifier);
        if(resolved == null || resolved.streamUrl == null || resolved.streamUrl.isBlank())
            return null;

        AudioItem item = httpSourceManager.loadItem(manager, new AudioReference(resolved.streamUrl, resolved.title));
        if(item == null)
            throw new FriendlyException("yt-dlp resolved the URL, but lavaplayer could not play the resulting stream.", SUSPICIOUS, null);

        return item;
    }

    private boolean isYoutubeReference(String identifier)
    {
        if(identifier.startsWith("ytsearch:"))
            return true;

        return identifier.contains("youtube.com/") || identifier.contains("youtu.be/");
    }

    private ResolvedAudio resolve(String identifier)
    {
        List<String> command = new ArrayList<>();
        command.add(executable);
        command.add("--ignore-config");
        command.add("--no-playlist");
        command.add("--no-warnings");
        command.add("--dump-single-json");
        command.add("--extractor-args");
        command.add("youtube:player_client=tv,ios,android,web");
        if(!cookiesFile.isBlank())
        {
            command.add("--cookies");
            command.add(cookiesFile);
        }
        else if(!cookiesFromBrowser.isBlank())
        {
            command.add("--cookies-from-browser");
            command.add(cookiesFromBrowser);
        }
        command.add(identifier);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        try
        {
            Process process = pb.start();
            String output;
            try(InputStream input = process.getInputStream())
            {
                output = readAll(input);
            }

            int exitCode = process.waitFor();
            if(exitCode != 0)
            {
                LOG.warn("yt-dlp failed with exit code {}. Command: {} Output: {}", exitCode, String.join(" ", command), output);
                throw new FriendlyException("yt-dlp failed to resolve this YouTube item.", COMMON,
                        new IllegalStateException("yt-dlp exit code " + exitCode + ": " + output));
            }

            JSONObject json = new JSONObject(output);
            String streamUrl = json.optString("url", null);
            if((streamUrl == null || streamUrl.isBlank()) && json.has("requested_downloads"))
            {
                streamUrl = json.getJSONArray("requested_downloads")
                        .optJSONObject(0)
                        .optString("url", null);
            }
            String title = json.optString("title", null);
            if(streamUrl == null || streamUrl.isBlank())
            {
                throw new FriendlyException("yt-dlp did not return a direct media URL.", SUSPICIOUS,
                        new IllegalStateException(output));
            }

            LOG.info("Resolved YouTube item through yt-dlp{}: {}",
                    !cookiesFile.isBlank() ? " with cookies file"
                            : cookiesFromBrowser.isBlank() ? "" : " with browser cookies",
                    title == null ? identifier : title);
            return new ResolvedAudio(streamUrl, title);
        }
        catch(IOException ex)
        {
            throw new FriendlyException("Failed to execute yt-dlp. Make sure yt-dlp is installed and available in PATH.", COMMON, ex);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new FriendlyException("yt-dlp execution was interrupted.", SUSPICIOUS, ex);
        }
    }

    private String readAll(InputStream input) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while((read = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track)
    {
        return httpSourceManager.isTrackEncodable(track);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException
    {
        httpSourceManager.encodeTrack(track, output);
    }

    @Override
    public AudioTrack decodeTrack(com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo trackInfo, DataInput input) throws IOException
    {
        return httpSourceManager.decodeTrack(trackInfo, input);
    }

    @Override
    public void shutdown()
    {
        httpSourceManager.shutdown();
    }

    private static class ResolvedAudio
    {
        private final String streamUrl;
        private final String title;

        private ResolvedAudio(String streamUrl, String title)
        {
            this.streamUrl = streamUrl;
            this.title = title;
        }
    }
}
