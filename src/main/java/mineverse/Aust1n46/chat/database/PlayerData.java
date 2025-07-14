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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Class for reading and writing player data.
 *
 * @author Aust1n46
 */
public class PlayerData {
    private static final MineverseChat plugin = MineverseChat.getInstance();
    private static final String PLAYER_DATA_DIRECTORY_PATH = plugin.getDataFolder().getAbsolutePath() + "/PlayerData";

    public static void loadPlayerData() {
        try {
            final File playerDataDirectory = new File(PLAYER_DATA_DIRECTORY_PATH);
            if (!playerDataDirectory.exists()) {
                //noinspection ResultOfMethodCallIgnored
                playerDataDirectory.mkdirs();
            }
            try (final Stream<Path> paths = Files.walk(Paths.get(PLAYER_DATA_DIRECTORY_PATH))) {
                paths.filter(Files::isRegularFile)
                        .forEach(PlayerData::readPlayerDataFile);
            }
        } catch (final IOException e) {
            plugin.getLogger().warning("Could not load player data files. Please check the directory: " + PLAYER_DATA_DIRECTORY_PATH);
        }
    }

    /**
     * Loads the player data file for a specific player. Corrupt/invalid data files are skipped and deleted.
     *
     * @param path the path to the player data file
     */
    private static void readPlayerDataFile(final @NotNull Path path) {
        final MineverseChatPlayer mcp;
        final File playerDataFile = path.toFile();
        if (!playerDataFile.exists()) return;

        try {
            final FileConfiguration playerDataFileYamlConfiguration = YamlConfiguration.loadConfiguration(playerDataFile);
            final String uuidString = playerDataFile.getName().replace(".yml", "");
            final UUID uuid = UUID.fromString(uuidString);
            if (UUIDFetcher.shouldSkipOfflineUUID(uuid)) {
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Skipping Offline UUID: " + uuid));
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - File will be skipped and deleted."));
                playerDataFile.delete();
                return;
            }
            final String name = playerDataFileYamlConfiguration.getString("name");
            final String currentChannelName = playerDataFileYamlConfiguration.getString("current", "local");
            final ChatChannel currentChannel = ChatChannel.isChannel(currentChannelName) ? ChatChannel.getChannel(currentChannelName) : ChatChannel.getDefaultChannel();
            final Set<UUID> ignores = new HashSet<>();
            final StringTokenizer i = new StringTokenizer(playerDataFileYamlConfiguration.getString("ignores"), ",");
            while (i.hasMoreTokens()) {
                ignores.add(UUID.fromString(i.nextToken()));
            }
            final Set<String> listening = new HashSet<>();
            final StringTokenizer l = new StringTokenizer(playerDataFileYamlConfiguration.getString("listen"), ",");
            while (l.hasMoreTokens()) {
                final String channel = l.nextToken();
                if (ChatChannel.isChannel(channel)) {
                    listening.add(channel);
                }
            }
            final HashMap<String, MuteContainer> mutes = new HashMap<String, MuteContainer>();
            final ConfigurationSection muteSection = playerDataFileYamlConfiguration.getConfigurationSection("mutes");
            for (final String channelName : muteSection.getKeys(false)) {
                final ConfigurationSection channelSection = muteSection.getConfigurationSection(channelName);
                mutes.put(channelName, new MuteContainer(channelName, channelSection.getLong("time"), channelSection.getString("reason")));
            }

            final Set<String> blockedCommands = new HashSet<>();
            final StringTokenizer b = new StringTokenizer(playerDataFileYamlConfiguration.getString("blockedcommands"), ",");
            while (b.hasMoreTokens()) {
                blockedCommands.add(b.nextToken());
            }
            final boolean host = playerDataFileYamlConfiguration.getBoolean("host");
            final UUID party = playerDataFileYamlConfiguration.getString("party").length() > 0 ? UUID.fromString(playerDataFileYamlConfiguration.getString("party")) : null;
            final boolean filter = playerDataFileYamlConfiguration.getBoolean("filter");
            final boolean notifications = playerDataFileYamlConfiguration.getBoolean("notifications");
            final String jsonFormat = "Default";
            final boolean spy = playerDataFileYamlConfiguration.getBoolean("spy", false);
            final boolean commandSpy = playerDataFileYamlConfiguration.getBoolean("commandspy", false);
            final boolean rangedSpy = playerDataFileYamlConfiguration.getBoolean("rangedspy", false);
            final boolean messageToggle = playerDataFileYamlConfiguration.getBoolean("messagetoggle", true);
            final boolean bungeeToggle = playerDataFileYamlConfiguration.getBoolean("bungeetoggle", true);
            mcp = new MineverseChatPlayer(uuid, name, currentChannel, ignores, listening, mutes, blockedCommands, host, party, filter, notifications, jsonFormat, spy, commandSpy, rangedSpy, messageToggle, bungeeToggle);
        } catch (final Exception e) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Error Loading Data File: " + playerDataFile.getName()));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - File will be skipped and deleted."));
            playerDataFile.delete();
            return;
        }

        MineverseChatAPI.addMineverseChatPlayerToMap(mcp);
        MineverseChatAPI.addNameToMap(mcp);
    }

    public static void savePlayerData(final MineverseChatPlayer mcp) {
        if (mcp == null || UUIDFetcher.shouldSkipOfflineUUID(mcp.getUUID()) || (!mcp.isOnline() && !mcp.wasModified())) {
            return;
        }
        try {
            final File playerDataFile = new File(PLAYER_DATA_DIRECTORY_PATH, mcp.getUUID() + ".yml");
            final FileConfiguration playerDataFileYamlConfiguration = YamlConfiguration.loadConfiguration(playerDataFile);
            if (!playerDataFile.exists()) {
                playerDataFileYamlConfiguration.save(playerDataFile);
            }

            playerDataFileYamlConfiguration.set("name", mcp.getName());
            playerDataFileYamlConfiguration.set("current", mcp.getCurrentChannel().getName());
            final StringBuilder ignores = new StringBuilder();
            for (final UUID s : mcp.getIgnores()) {
                ignores.append(s.toString()).append(",");
            }
            playerDataFileYamlConfiguration.set("ignores", ignores.toString());
            StringBuilder listening = new StringBuilder();
            for (final String channel : mcp.getListening()) {
                final ChatChannel c = ChatChannel.getChannel(channel);
                listening.append(c.getName()).append(",");
            }
            final StringBuilder blockedCommands = new StringBuilder();
            for (final String s : mcp.getBlockedCommands()) {
                blockedCommands.append(s).append(",");
            }
            if (!listening.isEmpty()) {
                listening = new StringBuilder(listening.substring(0, listening.length() - 1));
            }
            playerDataFileYamlConfiguration.set("listen", listening.toString());

            final ConfigurationSection muteSection = playerDataFileYamlConfiguration.createSection("mutes");
            for (final MuteContainer mute : mcp.getMutes()) {
                final ConfigurationSection channelSection = muteSection.createSection(mute.getChannel());
                channelSection.set("time", mute.getDuration());
                channelSection.set("reason", mute.getReason());
            }

            playerDataFileYamlConfiguration.set("blockedcommands", blockedCommands.toString());
            playerDataFileYamlConfiguration.set("host", mcp.isHost());
            playerDataFileYamlConfiguration.set("party", mcp.hasParty() ? mcp.getParty().toString() : "");
            playerDataFileYamlConfiguration.set("filter", mcp.hasFilter());
            playerDataFileYamlConfiguration.set("notifications", mcp.hasNotifications());
            playerDataFileYamlConfiguration.set("spy", mcp.isSpy());
            playerDataFileYamlConfiguration.set("commandspy", mcp.hasCommandSpy());
            playerDataFileYamlConfiguration.set("rangedspy", mcp.getRangedSpy());
            playerDataFileYamlConfiguration.set("messagetoggle", mcp.getMessageToggle());
            playerDataFileYamlConfiguration.set("bungeetoggle", mcp.getBungeeToggle());
            final Calendar currentDate = Calendar.getInstance();
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");
            final String dateNow = formatter.format(currentDate.getTime());
            playerDataFileYamlConfiguration.set("date", dateNow);
            mcp.setModified(false);

            playerDataFileYamlConfiguration.save(playerDataFile);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void savePlayerData() {
        for (final MineverseChatPlayer p : MineverseChatAPI.getMineverseChatPlayers()) {
            savePlayerData(p);
        }
    }
}
