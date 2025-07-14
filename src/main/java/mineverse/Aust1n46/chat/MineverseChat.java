package mineverse.Aust1n46.chat;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.alias.Alias;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.channel.ChatChannelInfo;
import mineverse.Aust1n46.chat.command.VentureCommandExecutor;
import mineverse.Aust1n46.chat.command.chat.Channel;
import mineverse.Aust1n46.chat.command.mute.MuteContainer;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.database.PlayerData;
import mineverse.Aust1n46.chat.gui.GuiSlot;
import mineverse.Aust1n46.chat.json.JsonFormat;
import mineverse.Aust1n46.chat.listeners.ChatListener;
import mineverse.Aust1n46.chat.listeners.CommandListener;
import mineverse.Aust1n46.chat.listeners.LoginListener;
import mineverse.Aust1n46.chat.listeners.PacketListenerLegacyChat;
import mineverse.Aust1n46.chat.localization.Localization;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * VentureChat Minecraft plugin for servers running Spigot or Paper software.
 *
 * @author Aust1n46
 */
public class MineverseChat extends JavaPlugin implements PluginMessageListener {
    // Plugin Messaging Channel
    public static final String PLUGIN_MESSAGING_CHANNEL = "venturechat:data";

    // Event constants
    public static final boolean ASYNC = true;
    public static final boolean SYNC = false;

    public static final int LINE_LENGTH = 40;

    // DiscordSRV backwards compatibility
    @Deprecated
    public static ChatChannelInfo ccInfo;

    @Deprecated
    public static Set<MineverseChatPlayer> players = new HashSet<>();
    @Deprecated
    public static Set<MineverseChatPlayer> onlinePlayers = new HashSet<>();

    // Vault
    private static Permission permission = null;
    private static Chat chat = null;

    // TODO: This won't be so poorly done in the 4.0.0 branch I promise...
    public static boolean isConnectedToProxy() {
        try {
            final MineverseChat plugin = MineverseChat.getInstance();
            return (plugin.getServer().spigot().getConfig().getBoolean("settings.bungeecord")
                    || plugin.getServer().spigot().getPaperConfig().getBoolean("settings.velocity-support.enabled")
                    || plugin.getServer().spigot().getPaperConfig().getBoolean("proxies.velocity.enabled"));
        } catch (final NoSuchMethodError ignored) {
        } // Thrown if server isn't Paper.
        return false;
    }

    public static MineverseChat getInstance() {
        return getPlugin(MineverseChat.class);
    }

    public static void initializeConfigReaders() {
        Localization.initialize();
        Alias.initialize();
        JsonFormat.initialize();
        GuiSlot.initialize();
        ChatChannel.initialize();
    }

    public static Chat getVaultChat() {
        return chat;
    }

    public static Permission getVaultPermission() {
        return permission;
    }

    public static void synchronize(final MineverseChatPlayer mcp, final boolean changes) {
        // System.out.println("Sync started...");
        final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(outstream);
        try {
            out.writeUTF("Sync");
            if (!changes) {
                out.writeUTF("Receive");
                // System.out.println(mcp.getPlayer().getServer().getServerName());
                // out.writeUTF(mcp.getPlayer().getServer().getServerName());
                out.writeUTF(mcp.getUUID().toString());
                Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (!mcp.isOnline() || mcp.hasPlayed()) {
                            return;
                        }
                        synchronize(mcp, false);
                    }
                }, 20L); // one second delay before running again
            } else {
                out.writeUTF("Update");
                out.writeUTF(mcp.getUUID().toString());
                // out.writeUTF("Channels");
                int channelCount = 0;
                for (final String c : mcp.getListening()) {
                    final ChatChannel channel = ChatChannel.getChannel(c);
                    if (channel.getBungee()) {
                        channelCount++;
                    }
                }
                out.write(channelCount);
                for (final String c : mcp.getListening()) {
                    final ChatChannel channel = ChatChannel.getChannel(c);
                    if (channel.getBungee()) {
                        out.writeUTF(channel.getName());
                    }
                }
                // out.writeUTF("Mutes");
                int muteCount = 0;
                for (final MuteContainer mute : mcp.getMutes()) {
                    final ChatChannel channel = ChatChannel.getChannel(mute.getChannel());
                    if (channel.getBungee()) {
                        muteCount++;
                    }
                }
                // System.out.println(muteCount + " mutes");
                out.write(muteCount);
                for (final MuteContainer mute : mcp.getMutes()) {
                    final ChatChannel channel = ChatChannel.getChannel(mute.getChannel());
                    if (channel.getBungee()) {
                        out.writeUTF(channel.getName());
                        out.writeLong(mute.getDuration());
                        out.writeUTF(mute.getReason());
                    }
                }
                int ignoreCount = 0;
                for (@SuppressWarnings("unused") final
                UUID c : mcp.getIgnores()) {
                    ignoreCount++;
                }
                out.write(ignoreCount);
                for (final UUID c : mcp.getIgnores()) {
                    out.writeUTF(c.toString());
                }
                out.writeBoolean(mcp.isSpy());
                out.writeBoolean(mcp.getMessageToggle());
            }
            sendPluginMessage(outstream);
            // System.out.println("Sync start bottom...");
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendPluginMessage(final ByteArrayOutputStream byteOutStream) {
        if (!MineverseChatAPI.getOnlineMineverseChatPlayers().isEmpty()) {
            MineverseChatAPI.getOnlineMineverseChatPlayers().iterator().next().getPlayer().sendPluginMessage(getInstance(), PLUGIN_MESSAGING_CHANNEL, byteOutStream.toByteArray());
        }
    }

    public static void sendDiscordSRVPluginMessage(final String chatChannel, final String message) {
        if (MineverseChatAPI.getOnlineMineverseChatPlayers().isEmpty()) {
            return;
        }
        final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
        final DataOutputStream out = new DataOutputStream(byteOutStream);
        try {
            out.writeUTF("DiscordSRV");
            out.writeUTF(chatChannel);
            out.writeUTF(message);
            sendPluginMessage(byteOutStream);
            out.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        ccInfo = new ChatChannelInfo();

        try {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Initializing..."));
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            final File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Config not found! Generating file."));
                saveDefaultConfig();
            } else {
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Config found! Loading file."));
            }
            saveResource("example_config_always_up_to_date!.yml", true);
        } catch (final Exception ex) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - &cCould not load configuration! Something unexpected went wrong!"));
        }

        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Checking for Vault..."));

        if (!setupPermissions() || !setupChat()) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - &cCould not find Vault and/or a Vault compatible permissions plugin!"));
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        initializeConfigReaders();

        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Loading player data"));
        PlayerData.loadPlayerData();

        Bukkit.getScheduler().runTaskAsynchronously(this, Database::initializeMySQL);

        VentureCommandExecutor.initialize();

        registerListeners();
        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Registering Listeners"));
        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Attaching to Executors"));

        if (MineverseChat.isConnectedToProxy()) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Establishing BungeeCord"));
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, PLUGIN_MESSAGING_CHANNEL);
            Bukkit.getMessenger().registerIncomingPluginChannel(this, PLUGIN_MESSAGING_CHANNEL, this);
        }

        final PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.isPluginEnabled("Towny")) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling Towny Formatting"));
        }
        if (pluginManager.isPluginEnabled("Jobs")) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling Jobs Formatting"));
        }
        if (pluginManager.isPluginEnabled("Factions")) {
            final String version = pluginManager.getPlugin("Factions").getDescription().getVersion();
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling Factions Formatting version " + version));
        }
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabling PlaceholderAPI Hook"));
        }

        new VentureChatPlaceholders().register();

        startRepeatingTasks();

        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Enabled Successfully"));
    }

    @Override
    public void onDisable() {
        PlayerData.savePlayerData();
        MineverseChatAPI.clearMineverseChatPlayerMap();
        MineverseChatAPI.clearNameMap();
        MineverseChatAPI.clearOnlineMineverseChatPlayerMap();
        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Disabling..."));
        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Disabled Successfully"));
    }

    private void startRepeatingTasks() {
        final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.runTaskTimerAsynchronously(this, () -> {
            PlayerData.savePlayerData();
            if (getConfig().getString("loglevel", "info").equals("debug")) {
                Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Saving Player Data"));
            }
        }, 0L, getConfig().getInt("saveinterval") * 1200L); //one minute * save interval

        scheduler.runTaskTimerAsynchronously(this, () -> {
            for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                final long currentTimeMillis = System.currentTimeMillis();
                final Iterator<MuteContainer> iterator = p.getMutes().iterator();
                while (iterator.hasNext()) {
                    final MuteContainer mute = iterator.next();
                    if (ChatChannel.isChannel(mute.getChannel())) {
                        final ChatChannel channel = ChatChannel.getChannel(mute.getChannel());
                        final long timemark = mute.getDuration();
                        if (timemark == 0) {
                            continue;
                        }
                        if (getConfig().getString("loglevel", "info").equals("debug")) {
                            getLogger().info(currentTimeMillis + " " + timemark);
                        }
                        if (currentTimeMillis >= timemark) {
                            iterator.remove();
                            p.getPlayer().sendMessage(LocalizedMessage.UNMUTE_PLAYER_PLAYER.toString()
                                    .replace("{player}", p.getName())
                                    .replace("{channel_color}", channel.getColor())
                                    .replace("{channel_name}", mute.getChannel()));
                            if (channel.getBungee()) {
                                synchronize(p, true);
                            }
                        }
                    }
                }
            }
            if (getConfig().getString("loglevel", "info").equals("trace")) {
                Bukkit.getConsoleSender()
                        .sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Updating Player Mutes"));
            }
        }, 0L, 60L); // three second interval
    }

    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new Channel(), this);
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new CommandListener(), this);
        pluginManager.registerEvents(new LoginListener(), this);
        if (VersionHandler.isUnder_1_19()) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListenerLegacyChat());
        }
    }

    private boolean setupPermissions() {
        final RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        final RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] inputStream) {
        if (!MineverseChat.isConnectedToProxy()) {
            return;
        }
        if (!channel.equals(PLUGIN_MESSAGING_CHANNEL)) {
            return;
        }
        try {
            final DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(inputStream));
            if (getConfig().getString("loglevel", "info").equals("debug")) {
                System.out.println(msgin.available() + " size on receiving end");
            }
            final String subchannel = msgin.readUTF();
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(stream);
            if (subchannel.equals("Chat")) {
                final String server = msgin.readUTF();
                final String chatchannel = msgin.readUTF();
                final String senderName = msgin.readUTF();
                final UUID senderUUID = UUID.fromString(msgin.readUTF());
                final int hash = msgin.readInt();
                final String format = msgin.readUTF();
                final String chat = msgin.readUTF();
                final String consoleChat = format + chat;
                final String globalJSON = msgin.readUTF();
                final String primaryGroup = msgin.readUTF();
                final String nickname = msgin.readUTF();

                if (!ChatChannel.isChannel(chatchannel)) {
                    return;
                }
                final ChatChannel chatChannelObject = ChatChannel.getChannel(chatchannel);

                if (!chatChannelObject.getBungee()) {
                    return;
                }

                final Set<Player> recipients = new HashSet<>();
                for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                    if (p.isListening(chatChannelObject.getName())) {
                        recipients.add(p.getPlayer());
                    }
                }

                Bukkit.getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
                    @Override
                    public void run() {
                        //Create VentureChatEvent
                        final VentureChatEvent ventureChatEvent = new VentureChatEvent(null, senderName, nickname, primaryGroup, chatChannelObject, recipients, recipients.size(), format, chat, globalJSON, hash, false);
                        //Fire event and wait for other plugin listeners to act on it
                        Bukkit.getServer().getPluginManager().callEvent(ventureChatEvent);
                    }
                });

                Bukkit.getConsoleSender().sendMessage(consoleChat);

                if (Database.isEnabled()) {
                    Database.writeVentureChat(senderUUID.toString(), senderName, server, chatchannel, chat.replace("'", "''"), "Chat");
                }

                for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                    if (p.isListening(chatChannelObject.getName())) {
                        if (!p.getBungeeToggle() && MineverseChatAPI.getOnlineMineverseChatPlayer(senderName) == null) {
                            continue;
                        }

                        final String json = Format.formatModerationGUI(globalJSON, p.getPlayer(), senderName, chatchannel, hash);
                        final PacketContainer packet = Format.createPacketPlayOutChat(json);

                        if (getConfig().getBoolean("ignorechat", false)) {
                            if (!p.getIgnores().contains(senderUUID)) {
                                // System.out.println("Chat sent");
                                Format.sendPacketPlayOutChat(p.getPlayer(), packet);
                            }
                            continue;
                        }
                        Format.sendPacketPlayOutChat(p.getPlayer(), packet);
                    }
                }
            }
            if (subchannel.equals("DiscordSRV")) {
                final String chatChannel = msgin.readUTF();
                final String message = msgin.readUTF();
                if (!ChatChannel.isChannel(chatChannel)) {
                    return;
                }
                final ChatChannel chatChannelObj = ChatChannel.getChannel(chatChannel);
                if (!chatChannelObj.getBungee()) {
                    return;
                }

                final String json = Format.convertPlainTextToJson(message, true);
                final int hash = (message.replaceAll("([�]([a-z0-9]))", "")).hashCode();

                for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                    if (p.isListening(chatChannelObj.getName())) {
                        final String finalJSON = Format.formatModerationGUI(json, p.getPlayer(), "Discord", chatChannelObj.getName(), hash);
                        final PacketContainer packet = Format.createPacketPlayOutChat(finalJSON);
                        Format.sendPacketPlayOutChat(p.getPlayer(), packet);
                    }
                }
            }
            if (subchannel.equals("PlayerNames")) {
                MineverseChatAPI.clearNetworkPlayerNames();
                final int playerCount = msgin.readInt();
                for (int a = 0; a < playerCount; a++) {
                    MineverseChatAPI.addNetworkPlayerName(msgin.readUTF());
                }
            }
            if (subchannel.equals("Chwho")) {
                final String identifier = msgin.readUTF();
                if (identifier.equals("Get")) {
                    final String server = msgin.readUTF();
                    final String sender = msgin.readUTF();
                    final String chatchannel = msgin.readUTF();
                    final List<String> listening = new ArrayList<>();
                    if (ChatChannel.isChannel(chatchannel)) {
                        for (final MineverseChatPlayer mcp : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                            if (mcp.isListening(chatchannel)) {
                                String entry = "&f" + mcp.getName();
                                if (mcp.isMuted(chatchannel)) {
                                    entry = "&c" + mcp.getName();
                                }
                                listening.add(entry);
                            }
                        }
                    }
                    out.writeUTF("Chwho");
                    out.writeUTF("Receive");
                    out.writeUTF(server);
                    out.writeUTF(sender);
                    out.writeUTF(chatchannel);
                    out.writeInt(listening.size());
                    for (final String s : listening) {
                        out.writeUTF(s);
                    }
                    sendPluginMessage(stream);
                }
                if (identifier.equals("Receive")) {
                    final String sender = msgin.readUTF();
                    final String stringchannel = msgin.readUTF();
                    final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(UUID.fromString(sender));
                    final ChatChannel chatchannel = ChatChannel.getChannel(stringchannel);
                    StringBuilder playerList = new StringBuilder();
                    final int size = msgin.readInt();
                    for (int a = 0; a < size; a++) {
                        playerList.append(msgin.readUTF()).append(ChatColor.WHITE).append(", ");
                    }
                    if (playerList.length() > 2) {
                        playerList = new StringBuilder(playerList.substring(0, playerList.length() - 2));
                    }
                    mcp.getPlayer().sendMessage(LocalizedMessage.CHANNEL_PLAYER_LIST_HEADER.toString()
                            .replace("{channel_color}", chatchannel.getColor())
                            .replace("{channel_name}", chatchannel.getName()));
                    mcp.getPlayer().sendMessage(Format.FormatStringAll(playerList.toString()));
                }
            }
            if (subchannel.equals("RemoveMessage")) {
                final String hash = msgin.readUTF();
                getServer().dispatchCommand(this.getServer().getConsoleSender(), "removemessage " + hash);
            }
            if (subchannel.equals("Sync")) {
                if (getConfig().getString("loglevel", "info").equals("debug")) {
                    Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Received update..."));
                }
                final String uuid = msgin.readUTF();
                final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(UUID.fromString(uuid));
                if (p == null || p.hasPlayed()) {
                    return;
                }
                for (final Object ch : p.getListening().toArray()) {
                    final String c = ch.toString();
                    final ChatChannel cha = ChatChannel.getChannel(c);
                    if (cha.getBungee()) {
                        p.removeListening(c);
                    }
                }
                final int size = msgin.read();
                for (int a = 0; a < size; a++) {
                    final String ch = msgin.readUTF();
                    if (ChatChannel.isChannel(ch)) {
                        final ChatChannel cha = ChatChannel.getChannel(ch);
                        if (!cha.hasPermission() || p.getPlayer().hasPermission(cha.getPermission())) {
                            p.addListening(ch);
                        }
                    }
                }
                p.getMutes().removeIf(mute -> ChatChannel.getChannel(mute.getChannel()).getBungee());
                final int sizeB = msgin.read();
                // System.out.println(sizeB + " mute size");
                for (int b = 0; b < sizeB; b++) {
                    final String ch = msgin.readUTF();
                    final long muteTime = msgin.readLong();
                    final String muteReason = msgin.readUTF();
                    // System.out.println(ch);
                    if (ChatChannel.isChannel(ch)) {
                        p.addMute(ch, muteTime, muteReason);
                    }
                }
                // System.out.println(msgin.available() + " available before");
                p.setSpy(msgin.readBoolean());
                p.setMessageToggle(msgin.readBoolean());
                // System.out.println(msgin.available() + " available after");
                for (final Object o : p.getIgnores().toArray()) {
                    p.removeIgnore((UUID) o);
                }
                final int sizeC = msgin.read();
                // System.out.println(sizeC + " ignore size");
                for (int c = 0; c < sizeC; c++) {
                    final String i = msgin.readUTF();
                    // System.out.println(i);
                    p.addIgnore(UUID.fromString(i));
                }
                if (!p.hasPlayed()) {
                    boolean isThereABungeeChannel = false;
                    for (final ChatChannel ch : ChatChannel.getAutojoinList()) {
                        if ((!ch.hasPermission() || p.getPlayer().hasPermission(ch.getPermission())) && !p.isListening(ch.getName())) {
                            p.addListening(ch.getName());
                            if (ch.getBungee()) {
                                isThereABungeeChannel = true;
                            }
                        }
                    }
                    p.setHasPlayed(true);
                    // Only run a sync update if the player joined a BungeeCord channel
                    if (isThereABungeeChannel) {
                        synchronize(p, true);
                    }
                }
            }
            if (subchannel.equals("Ignore")) {
                final String identifier = msgin.readUTF();
                if (identifier.equals("Send")) {
                    final String server = msgin.readUTF();
                    final String receiver = msgin.readUTF();
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(receiver);
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    if (!getConfig().getBoolean("bungeecordmessaging", true) || p == null || !p.isOnline()) {
                        out.writeUTF("Ignore");
                        out.writeUTF("Offline");
                        out.writeUTF(server);
                        out.writeUTF(receiver);
                        out.writeUTF(sender.toString());
                        sendPluginMessage(stream);
                        return;
                    }
                    if (p.getPlayer().hasPermission("venturechat.ignore.bypass")) {
                        out.writeUTF("Ignore");
                        out.writeUTF("Bypass");
                        out.writeUTF(server);
                        out.writeUTF(receiver);
                        out.writeUTF(sender.toString());
                        sendPluginMessage(stream);
                        return;
                    }
                    out.writeUTF("Ignore");
                    out.writeUTF("Echo");
                    out.writeUTF(server);
                    out.writeUTF(p.getUUID().toString());
                    out.writeUTF(receiver);
                    out.writeUTF(sender.toString());
                    sendPluginMessage(stream);
                    return;
                }
                if (identifier.equals("Offline")) {
                    final String receiver = msgin.readUTF();
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                    p.getPlayer().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString()
                            .replace("{args}", receiver));
                }
                if (identifier.equals("Echo")) {
                    final UUID receiver = UUID.fromString(msgin.readUTF());
                    final String receiverName = msgin.readUTF();
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);

                    if (p.getIgnores().contains(receiver)) {
                        p.getPlayer().sendMessage(LocalizedMessage.IGNORE_PLAYER_OFF.toString()
                                .replace("{player}", receiverName));
                        p.removeIgnore(receiver);
                        synchronize(p, true);
                        return;
                    }

                    p.addIgnore(receiver);
                    p.getPlayer().sendMessage(LocalizedMessage.IGNORE_PLAYER_ON.toString()
                            .replace("{player}", receiverName));
                    synchronize(p, true);
                }
                if (identifier.equals("Bypass")) {
                    final String receiver = msgin.readUTF();
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                    p.getPlayer().sendMessage(LocalizedMessage.IGNORE_PLAYER_CANT.toString().replace("{player}", receiver));
                }
            }
            if (subchannel.equals("Mute")) {
                final String identifier = msgin.readUTF();
                switch (identifier) {
                    case "Send" -> {
                        final String server = msgin.readUTF();
                        final String senderIdentifier = msgin.readUTF();
                        final String temporaryDataInstanceUUIDString = msgin.readUTF();
                        final String playerToMute = msgin.readUTF();
                        final String channelName = msgin.readUTF();
                        final long time = msgin.readLong();
                        final String reason = msgin.readUTF();
                        final MineverseChatPlayer playerToMuteMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(playerToMute);
                        if (playerToMuteMCP == null) {
                            out.writeUTF("Mute");
                            out.writeUTF("Offline");
                            out.writeUTF(server);
                            out.writeUTF(temporaryDataInstanceUUIDString);
                            out.writeUTF(senderIdentifier);
                            out.writeUTF(playerToMute);
                            sendPluginMessage(stream);
                            return;
                        }
                        if (!ChatChannel.isChannel(channelName)) {
                            return;
                        }
                        final ChatChannel chatChannelObj = ChatChannel.getChannel(channelName);
                        if (playerToMuteMCP.isMuted(chatChannelObj.getName())) {
                            out.writeUTF("Mute");
                            out.writeUTF("AlreadyMuted");
                            out.writeUTF(server);
                            out.writeUTF(senderIdentifier);
                            out.writeUTF(playerToMute);
                            out.writeUTF(channelName);
                            sendPluginMessage(stream);
                            return;
                        }
                        if (time > 0) {
                            final long datetime = System.currentTimeMillis();
                            if (reason.isEmpty()) {
                                playerToMuteMCP.addMute(chatChannelObj.getName(), datetime + time);
                                final String timeString = Format.parseTimeStringFromMillis(time);
                                playerToMuteMCP.getPlayer()
                                        .sendMessage(LocalizedMessage.MUTE_PLAYER_PLAYER_TIME.toString()
                                                .replace("{channel_color}", chatChannelObj.getColor())
                                                .replace("{channel_name}", chatChannelObj.getName())
                                                .replace("{time}", timeString));
                            } else {
                                playerToMuteMCP.addMute(chatChannelObj.getName(), datetime + time, reason);
                                final String timeString = Format.parseTimeStringFromMillis(time);
                                playerToMuteMCP.getPlayer()
                                        .sendMessage(LocalizedMessage.MUTE_PLAYER_PLAYER_TIME_REASON.toString()
                                                .replace("{channel_color}", chatChannelObj.getColor())
                                                .replace("{channel_name}", chatChannelObj.getName())
                                                .replace("{time}", timeString)
                                                .replace("{reason}", reason));
                            }
                        } else {
                            if (reason.isEmpty()) {
                                playerToMuteMCP.addMute(chatChannelObj.getName());
                                playerToMuteMCP.getPlayer()
                                        .sendMessage(LocalizedMessage.MUTE_PLAYER_PLAYER.toString()
                                                .replace("{channel_color}", chatChannelObj.getColor())
                                                .replace("{channel_name}", chatChannelObj.getName()));
                            } else {
                                playerToMuteMCP.addMute(chatChannelObj.getName(), reason);
                                playerToMuteMCP.getPlayer()
                                        .sendMessage(LocalizedMessage.MUTE_PLAYER_PLAYER_REASON.toString()
                                                .replace("{channel_color}", chatChannelObj.getColor())
                                                .replace("{channel_name}", chatChannelObj.getName())
                                                .replace("{reason}", reason));
                            }
                        }
                        synchronize(playerToMuteMCP, true);
                        out.writeUTF("Mute");
                        out.writeUTF("Valid");
                        out.writeUTF(server);
                        out.writeUTF(senderIdentifier);
                        out.writeUTF(playerToMute);
                        out.writeUTF(channelName);
                        out.writeLong(time);
                        out.writeUTF(reason);
                        sendPluginMessage(stream);
                        return;
                    }
                    case "Valid" -> {
                        final String senderIdentifier = msgin.readUTF();
                        final String playerToMute = msgin.readUTF();
                        final String channelName = msgin.readUTF();
                        final long time = msgin.readLong();
                        final String reason = msgin.readUTF();
                        if (!ChatChannel.isChannel(channelName)) {
                            return;
                        }
                        final ChatChannel chatChannelObj = ChatChannel.getChannel(channelName);
                        if (time > 0) {
                            final String timeString = Format.parseTimeStringFromMillis(time);
                            if (reason.isEmpty()) {
                                if (senderIdentifier.equals("VentureChat:Console")) {
                                    Bukkit.getConsoleSender().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER_TIME.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName())
                                            .replace("{time}", timeString));
                                } else {
                                    final UUID sender = UUID.fromString(senderIdentifier);
                                    final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                                    senderMCP.getPlayer().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER_TIME.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName())
                                            .replace("{time}", timeString));
                                }
                            } else {
                                if (senderIdentifier.equals("VentureChat:Console")) {
                                    Bukkit.getConsoleSender().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER_TIME_REASON.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName())
                                            .replace("{time}", timeString)
                                            .replace("{reason}", reason));
                                } else {
                                    final UUID sender = UUID.fromString(senderIdentifier);
                                    final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                                    senderMCP.getPlayer().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER_TIME_REASON.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName())
                                            .replace("{time}", timeString)
                                            .replace("{reason}", reason));
                                }
                            }
                        } else {
                            if (reason.isEmpty()) {
                                if (senderIdentifier.equals("VentureChat:Console")) {
                                    Bukkit.getConsoleSender().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName()));
                                } else {
                                    final UUID sender = UUID.fromString(senderIdentifier);
                                    final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                                    senderMCP.getPlayer().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName()));
                                }
                            } else {
                                if (senderIdentifier.equals("VentureChat:Console")) {
                                    Bukkit.getConsoleSender().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER_REASON.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName())
                                            .replace("{reason}", reason));
                                } else {
                                    final UUID sender = UUID.fromString(senderIdentifier);
                                    final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                                    senderMCP.getPlayer().sendMessage(LocalizedMessage.MUTE_PLAYER_SENDER_REASON.toString()
                                            .replace("{player}", playerToMute)
                                            .replace("{channel_color}", chatChannelObj.getColor())
                                            .replace("{channel_name}", chatChannelObj.getName())
                                            .replace("{reason}", reason));
                                }
                            }
                        }
                        return;
                    }
                    case "Offline" -> {
                        final String senderIdentifier = msgin.readUTF();
                        final String playerToMute = msgin.readUTF();
                        if (senderIdentifier.equals("VentureChat:Console")) {
                            Bukkit.getConsoleSender().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString()
                                    .replace("{args}", playerToMute));
                            return;
                        }
                        final UUID sender = UUID.fromString(senderIdentifier);
                        final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                        senderMCP.getPlayer().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString()
                                .replace("{args}", playerToMute));
                        return;
                    }
                    case "AlreadyMuted" -> {
                        final String senderIdentifier = msgin.readUTF();
                        final String playerToMute = msgin.readUTF();
                        final String channelName = msgin.readUTF();
                        if (!ChatChannel.isChannel(channelName)) {
                            return;
                        }
                        final ChatChannel chatChannelObj = ChatChannel.getChannel(channelName);
                        if (senderIdentifier.equals("VentureChat:Console")) {
                            Bukkit.getConsoleSender().sendMessage(LocalizedMessage.PLAYER_ALREADY_MUTED.toString()
                                    .replace("{player}", playerToMute).replace("{channel_color}", chatChannelObj.getColor())
                                    .replace("{channel_name}", chatChannelObj.getName()));
                            return;
                        }
                        final UUID sender = UUID.fromString(senderIdentifier);
                        final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                        senderMCP.getPlayer().sendMessage(LocalizedMessage.PLAYER_ALREADY_MUTED.toString()
                                .replace("{player}", playerToMute).replace("{channel_color}", chatChannelObj.getColor())
                                .replace("{channel_name}", chatChannelObj.getName()));
                        return;
                    }
                }
            }
            if (subchannel.equals("Unmute")) {
                final String identifier = msgin.readUTF();
                switch (identifier) {
                    case "Send" -> {
                        final String server = msgin.readUTF();
                        final String senderIdentifier = msgin.readUTF();
                        final String temporaryDataInstanceUUIDString = msgin.readUTF();
                        final String playerToUnmute = msgin.readUTF();
                        final String channelName = msgin.readUTF();
                        final MineverseChatPlayer playerToUnmuteMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(playerToUnmute);
                        if (playerToUnmuteMCP == null) {
                            out.writeUTF("Unmute");
                            out.writeUTF("Offline");
                            out.writeUTF(server);
                            out.writeUTF(temporaryDataInstanceUUIDString);
                            out.writeUTF(senderIdentifier);
                            out.writeUTF(playerToUnmute);
                            sendPluginMessage(stream);
                            return;
                        }
                        if (!ChatChannel.isChannel(channelName)) {
                            return;
                        }
                        final ChatChannel chatChannelObj = ChatChannel.getChannel(channelName);
                        if (!playerToUnmuteMCP.isMuted(chatChannelObj.getName())) {
                            out.writeUTF("Unmute");
                            out.writeUTF("NotMuted");
                            out.writeUTF(server);
                            out.writeUTF(senderIdentifier);
                            out.writeUTF(playerToUnmute);
                            out.writeUTF(channelName);
                            sendPluginMessage(stream);
                            return;
                        }
                        playerToUnmuteMCP.removeMute(chatChannelObj.getName());
                        playerToUnmuteMCP.getPlayer().sendMessage(LocalizedMessage.UNMUTE_PLAYER_PLAYER.toString()
                                .replace("{player}", player.getName()).replace("{channel_color}", chatChannelObj.getColor())
                                .replace("{channel_name}", chatChannelObj.getName()));
                        synchronize(playerToUnmuteMCP, true);
                        out.writeUTF("Unmute");
                        out.writeUTF("Valid");
                        out.writeUTF(server);
                        out.writeUTF(senderIdentifier);
                        out.writeUTF(playerToUnmute);
                        out.writeUTF(channelName);
                        sendPluginMessage(stream);
                        return;
                    }
                    case "Valid" -> {
                        final String senderIdentifier = msgin.readUTF();
                        final String playerToUnmute = msgin.readUTF();
                        final String channelName = msgin.readUTF();
                        if (!ChatChannel.isChannel(channelName)) {
                            return;
                        }
                        final ChatChannel chatChannelObj = ChatChannel.getChannel(channelName);
                        if (senderIdentifier.equals("VentureChat:Console")) {
                            Bukkit.getConsoleSender().sendMessage(LocalizedMessage.UNMUTE_PLAYER_SENDER.toString()
                                    .replace("{player}", playerToUnmute)
                                    .replace("{channel_color}", chatChannelObj.getColor())
                                    .replace("{channel_name}", chatChannelObj.getName()));
                        } else {
                            final UUID sender = UUID.fromString(senderIdentifier);
                            final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                            senderMCP.getPlayer().sendMessage(LocalizedMessage.UNMUTE_PLAYER_SENDER.toString()
                                    .replace("{player}", playerToUnmute)
                                    .replace("{channel_color}", chatChannelObj.getColor())
                                    .replace("{channel_name}", chatChannelObj.getName()));
                        }
                        return;
                    }
                    case "Offline" -> {
                        final String senderIdentifier = msgin.readUTF();
                        final String playerToUnmute = msgin.readUTF();
                        if (senderIdentifier.equals("VentureChat:Console")) {
                            Bukkit.getConsoleSender().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString()
                                    .replace("{args}", playerToUnmute));
                            return;
                        }
                        final UUID sender = UUID.fromString(senderIdentifier);
                        final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                        senderMCP.getPlayer().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString()
                                .replace("{args}", playerToUnmute));
                        return;
                    }
                    case "NotMuted" -> {
                        final String senderIdentifier = msgin.readUTF();
                        final String playerToUnmute = msgin.readUTF();
                        final String channelName = msgin.readUTF();
                        if (!ChatChannel.isChannel(channelName)) {
                            return;
                        }
                        final ChatChannel chatChannelObj = ChatChannel.getChannel(channelName);
                        if (senderIdentifier.equals("VentureChat:Console")) {
                            Bukkit.getConsoleSender().sendMessage(LocalizedMessage.PLAYER_NOT_MUTED.toString()
                                    .replace("{player}", playerToUnmute).replace("{channel_color}", chatChannelObj.getColor())
                                    .replace("{channel_name}", chatChannelObj.getName()));
                            return;
                        }
                        final UUID sender = UUID.fromString(senderIdentifier);
                        final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                        senderMCP.getPlayer().sendMessage(LocalizedMessage.PLAYER_NOT_MUTED.toString()
                                .replace("{player}", playerToUnmute).replace("{channel_color}", chatChannelObj.getColor())
                                .replace("{channel_name}", chatChannelObj.getName()));
                        return;
                    }
                }
            }
            if (subchannel.equals("Message")) {
                final String identifier = msgin.readUTF();
                if (identifier.equals("Send")) {
                    final String server = msgin.readUTF();
                    final String receiver = msgin.readUTF();
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(receiver);
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final String sName = msgin.readUTF();
                    final String send = msgin.readUTF();
                    final String echo = msgin.readUTF();
                    final String spy = msgin.readUTF();
                    final String msg = msgin.readUTF();
                    if (!getConfig().getBoolean("bungeecordmessaging", true) || p == null) {
                        out.writeUTF("Message");
                        out.writeUTF("Offline");
                        out.writeUTF(server);
                        out.writeUTF(receiver);
                        out.writeUTF(sender.toString());
                        sendPluginMessage(stream);
                        return;
                    }
                    if (p.getIgnores().contains(sender)) {
                        out.writeUTF("Message");
                        out.writeUTF("Ignore");
                        out.writeUTF(server);
                        out.writeUTF(receiver);
                        out.writeUTF(sender.toString());
                        sendPluginMessage(stream);
                        return;
                    }
                    if (!p.getMessageToggle()) {
                        out.writeUTF("Message");
                        out.writeUTF("Blocked");
                        out.writeUTF(server);
                        out.writeUTF(receiver);
                        out.writeUTF(sender.toString());
                        sendPluginMessage(stream);
                        return;
                    }
                    p.getPlayer().sendMessage(Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(p.getPlayer(), send.replaceAll("receiver_", ""))) + msg);
                    if (p.hasNotifications()) {
                        Format.playMessageSound(p);
                    }
                    if (MineverseChatAPI.getMineverseChatPlayer(sender) == null) {
                        final MineverseChatPlayer senderMCP = new MineverseChatPlayer(sender, sName);
                        MineverseChatAPI.addMineverseChatPlayerToMap(senderMCP);
                        MineverseChatAPI.addNameToMap(senderMCP);
                    }
                    p.setReplyPlayer(sender);
                    out.writeUTF("Message");
                    out.writeUTF("Echo");
                    out.writeUTF(server);
                    out.writeUTF(receiver);
                    out.writeUTF(p.getUUID().toString());
                    out.writeUTF(sender.toString());
                    out.writeUTF(sName);
                    out.writeUTF(Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(p.getPlayer(), echo.replaceAll("receiver_", ""))) + msg);
                    out.writeUTF(Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(p.getPlayer(), spy.replaceAll("receiver_", ""))) + msg);
                    sendPluginMessage(stream);
                    return;
                }
                if (identifier.equals("Offline")) {
                    final String receiver = msgin.readUTF();
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                    p.getPlayer().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString()
                            .replace("{args}", receiver));
                    p.setReplyPlayer(null);
                }
                if (identifier.equals("Ignore")) {
                    final String receiver = msgin.readUTF();
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                    p.getPlayer().sendMessage(LocalizedMessage.IGNORING_MESSAGE.toString()
                            .replace("{player}", receiver));
                }
                if (identifier.equals("Blocked")) {
                    final String receiver = msgin.readUTF();
                    final UUID sender = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer p = MineverseChatAPI.getOnlineMineverseChatPlayer(sender);
                    p.getPlayer().sendMessage(LocalizedMessage.BLOCKING_MESSAGE.toString()
                            .replace("{player}", receiver));
                }
                if (identifier.equals("Echo")) {
                    final String receiverName = msgin.readUTF();
                    final UUID receiverUUID = UUID.fromString(msgin.readUTF());
                    final UUID senderUUID = UUID.fromString(msgin.readUTF());
                    final MineverseChatPlayer senderMCP = MineverseChatAPI.getOnlineMineverseChatPlayer(senderUUID);
                    final String echo = msgin.readUTF();
                    if (MineverseChatAPI.getMineverseChatPlayer(receiverUUID) == null) {
                        final MineverseChatPlayer receiverMCP = new MineverseChatPlayer(receiverUUID, receiverName);
                        MineverseChatAPI.addMineverseChatPlayerToMap(receiverMCP);
                        MineverseChatAPI.addNameToMap(receiverMCP);
                    }
                    senderMCP.setReplyPlayer(receiverUUID);
                    senderMCP.getPlayer().sendMessage(echo);
                }
                if (identifier.equals("Spy")) {
                    final String receiverName = msgin.readUTF();
                    final String senderName = msgin.readUTF();
                    final String spy = msgin.readUTF();
                    if (!spy.startsWith("VentureChat:NoSpy")) {
                        for (final MineverseChatPlayer pl : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                            if (pl.isSpy() && !pl.getName().equals(senderName) && !pl.getName().equals(receiverName)) {
                                pl.getPlayer().sendMessage(spy);
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
