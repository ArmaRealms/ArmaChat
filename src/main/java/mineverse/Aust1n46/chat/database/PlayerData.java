package mineverse.Aust1n46.chat.database;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.command.mute.MuteContainer;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.utilities.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Class for reading and writing player data.
 *
 * @author Aust1n46
 */
public final class PlayerData {

    private PlayerData() {
    }

    private static MineverseChat plugin() {
        return MineverseChat.getInstance();
    }

    private static @NotNull Path playerDataDir() {
        return plugin().getDataFolder().toPath().resolve("PlayerData");
    }

    public static void loadPlayerData() {
        try {
            final Path dir = playerDataDir();
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }
            try (final Stream<Path> paths = Files.walk(dir)) {
                paths.filter(Files::isRegularFile)
                        .forEach(PlayerData::readPlayerDataFile);
            }
        } catch (final IOException e) {
            plugin().getLogger().warning("Could not load player data files. Please check the directory: " + playerDataDir());
        }
    }

    /**
     * Loads the player data file for a specific player. Corrupt/invalid data files are skipped and deleted.
     *
     * @param path the path to the player data file
     */
    private static void readPlayerDataFile(final @NotNull Path path) {
        final File playerDataFile = path.toFile();
        if (!playerDataFile.exists()) return;

        final MineverseChatPlayer mcp;
        try {
            final FileConfiguration cfg = YamlConfiguration.loadConfiguration(playerDataFile);
            final String uuidString = playerDataFile.getName().replace(".yml", "");
            final UUID uuid = UUID.fromString(uuidString);

            if (UUIDFetcher.shouldSkipOfflineUUID(uuid)) {
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Skipping Offline UUID: " + uuid));
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - File will be skipped and deleted."));
                //noinspection ResultOfMethodCallIgnored
                playerDataFile.delete();
                return;
            }

            final String name = Optional.ofNullable(cfg.getString("name")).orElse("");
            final String currentChannelName = cfg.getString("current", "local");
            final ChatChannel currentChannel = ChatChannel.isChannel(currentChannelName)
                    ? ChatChannel.getChannel(currentChannelName)
                    : ChatChannel.getDefaultChannel();

            final Set<UUID> ignores = new HashSet<>();
            final String ignoresStr = Optional.ofNullable(cfg.getString("ignores")).orElse("");
            final StringTokenizer i = new StringTokenizer(ignoresStr, ",");
            while (i.hasMoreTokens()) {
                final String token = i.nextToken().trim();
                if (!token.isEmpty()) {
                    try {
                        ignores.add(UUID.fromString(token));
                    } catch (final IllegalArgumentException ignored) {
                    }
                }
            }

            final Set<String> listening = new HashSet<>();
            final String listenStr = Optional.ofNullable(cfg.getString("listen")).orElse("");
            final StringTokenizer l = new StringTokenizer(listenStr, ",");
            while (l.hasMoreTokens()) {
                final String channel = l.nextToken().trim();
                if (!channel.isEmpty() && ChatChannel.isChannel(channel)) {
                    listening.add(channel);
                }
            }

            final Map<String, MuteContainer> mutes = new HashMap<>();
            final ConfigurationSection muteSection = cfg.getConfigurationSection("mutes");
            if (muteSection != null) {
                for (final String channelName : muteSection.getKeys(false)) {
                    final ConfigurationSection channelSection = muteSection.getConfigurationSection(channelName);
                    if (channelSection != null) {
                        mutes.put(
                                channelName,
                                new MuteContainer(
                                        channelName,
                                        channelSection.getLong("time"),
                                        channelSection.getString("reason")
                                )
                        );
                    }
                }
            }

            final Set<String> blockedCommands = new HashSet<>();
            final String blockedStr = Optional.ofNullable(cfg.getString("blockedcommands")).orElse("");
            final StringTokenizer b = new StringTokenizer(blockedStr, ",");
            while (b.hasMoreTokens()) {
                final String cmd = b.nextToken().trim();
                if (!cmd.isEmpty()) blockedCommands.add(cmd);
            }

            final boolean host = cfg.getBoolean("host");
            final String partyStr = Optional.ofNullable(cfg.getString("party")).orElse("");
            final UUID party = !partyStr.isEmpty() ? safeUUID(partyStr) : null;
            final boolean filter = cfg.getBoolean("filter");
            final boolean notifications = cfg.getBoolean("notifications");
            final String jsonFormat = "Default";
            final boolean spy = cfg.getBoolean("spy", false);
            final boolean commandSpy = cfg.getBoolean("commandspy", false);
            final boolean rangedSpy = cfg.getBoolean("rangedspy", false);
            final boolean messageToggle = cfg.getBoolean("messagetoggle", true);
            final boolean bungeeToggle = cfg.getBoolean("bungeetoggle", true);

            mcp = new MineverseChatPlayer(
                    uuid, name, currentChannel, ignores, listening, new HashMap<>(mutes),
                    blockedCommands, host, party, filter, notifications, jsonFormat, spy, commandSpy,
                    rangedSpy, messageToggle, bungeeToggle
            );

        } catch (final Exception e) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Error Loading Data File: " + playerDataFile.getName()));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - File will be skipped and deleted."));
            //noinspection ResultOfMethodCallIgnored
            playerDataFile.delete();
            return;
        }

        MineverseChatAPI.addMineverseChatPlayerToMap(mcp);
        MineverseChatAPI.addNameToMap(mcp);
    }

    public static void savePlayerData(final MineverseChatPlayer mcp) {
        if (mcp == null
                || UUIDFetcher.shouldSkipOfflineUUID(mcp.getUUID())
                || (!mcp.isOnline() && !mcp.wasModified())) {
            return;
        }
        try {
            final Path dir = playerDataDir();
            if (Files.notExists(dir)) {
                Files.createDirectories(dir);
            }

            final File playerDataFile = dir.resolve(mcp.getUUID() + ".yml").toFile();
            final FileConfiguration cfg = YamlConfiguration.loadConfiguration(playerDataFile);
            if (!playerDataFile.exists()) {
                cfg.save(playerDataFile);
            }

            cfg.set("name", mcp.getName());
            cfg.set("current", mcp.getCurrentChannel().getName());

            final StringBuilder ignores = new StringBuilder();
            for (final UUID s : mcp.getIgnores()) {
                ignores.append(s).append(",");
            }
            cfg.set("ignores", ignores.toString());

            StringBuilder listening = new StringBuilder();
            for (final String channel : mcp.getListening()) {
                final ChatChannel c = ChatChannel.getChannel(channel);
                listening.append(c.getName()).append(",");
            }
            if (!listening.isEmpty()) {
                listening = new StringBuilder(listening.substring(0, listening.length() - 1));
            }
            cfg.set("listen", listening.toString());

            final ConfigurationSection muteSection = cfg.createSection("mutes");
            for (final MuteContainer mute : mcp.getMutes()) {
                final ConfigurationSection channelSection = muteSection.createSection(mute.getChannel());
                channelSection.set("time", mute.getDuration());
                channelSection.set("reason", mute.getReason());
            }

            final StringBuilder blockedCommands = new StringBuilder();
            for (final String s : mcp.getBlockedCommands()) {
                blockedCommands.append(s).append(",");
            }
            cfg.set("blockedcommands", blockedCommands.toString());

            cfg.set("host", mcp.isHost());
            cfg.set("party", mcp.hasParty() ? mcp.getParty().toString() : "");
            cfg.set("filter", mcp.hasFilter());
            cfg.set("notifications", mcp.hasNotifications());
            cfg.set("spy", mcp.isSpy());
            cfg.set("commandspy", mcp.hasCommandSpy());
            cfg.set("rangedspy", mcp.getRangedSpy());
            cfg.set("messagetoggle", mcp.getMessageToggle());
            cfg.set("bungeetoggle", mcp.getBungeeToggle());

            final String dateNow = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
            cfg.set("date", dateNow);

            mcp.setModified(false);
            cfg.save(playerDataFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerData() {
        for (final MineverseChatPlayer p : MineverseChatAPI.getMineverseChatPlayers()) {
            savePlayerData(p);
        }
    }

    private static UUID safeUUID(final String s) {
        try {
            return UUID.fromString(s);
        } catch (final IllegalArgumentException ex) {
            return null;
        }
    }
}