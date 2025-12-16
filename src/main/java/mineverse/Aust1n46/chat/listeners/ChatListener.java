package mineverse.Aust1n46.chat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.VentureChatEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.command.mute.MuteContainer;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Set;

//This class listens to chat through the chat event and handles the bulk of the chat channels and formatting.
public class ChatListener implements Listener {
    private final GsonComponentSerializer serializer = GsonComponentSerializer.builder().build();
    private final boolean essentialsDiscordHook = Bukkit.getPluginManager().isPluginEnabled("EssentialsDiscord");
    private final MineverseChat plugin = MineverseChat.getInstance();

    // this event isn't always asynchronous even though the event's name starts with "Async"
    // blame md_5 for that one
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncPlayerChatEvent(@NotNull final AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> handleTrueAsyncPlayerChatEvent(event));
    }

    public void handleTrueAsyncPlayerChatEvent(@NotNull final AsyncPlayerChatEvent event) {
        final boolean bungee;
        String chat = event.getMessage();
        String format;
        final Set<Player> recipients = event.getRecipients();
        int recipientCount = recipients.size(); // Don't count vanished players
        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(event.getPlayer());
        if (mcp == null) {
            return;
        }
        ChatChannel eventChannel = mcp.getCurrentChannel();

        final Player mcpPlayer = mcp.getPlayer();
        if (mcp.isEditing()) {
            mcpPlayer.sendMessage(Format.FormatStringAll(chat));
            mcp.setEditing(false);
            return;
        }

        if (mcp.isQuickChat()) {
            eventChannel = mcp.getQuickChannel();
        }

        if (mcp.hasConversation() && !mcp.isQuickChat()) {
            final MineverseChatPlayer tp = MineverseChatAPI.getMineverseChatPlayer(mcp.getConversation());
            if (!tp.isOnline()) {
                mcpPlayer.sendMessage(ChatColor.RED + tp.getName() + " is not available.");
                if (!mcpPlayer.hasPermission("venturechat.spy.override")) {
                    for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                        if (p.getName().equals(mcp.getName())) {
                            continue;
                        }
                        if (p.isSpy()) {
                            p.getPlayer().sendMessage(LocalizedMessage.EXIT_PRIVATE_CONVERSATION_SPY.toString()
                                    .replace("{player_sender}", mcp.getName())
                                    .replace("{player_receiver}", tp.getName()));
                        }
                    }
                }
                mcp.setConversation(null);
            } else {
                if (tp.getIgnores().contains(mcp.getUUID())) {
                    mcpPlayer.sendMessage(LocalizedMessage.IGNORING_MESSAGE.toString()
                            .replace("{player}", tp.getName()));
                    event.setCancelled(true);
                    return;
                }
                if (!tp.getMessageToggle()) {
                    mcpPlayer.sendMessage(LocalizedMessage.BLOCKING_MESSAGE.toString()
                            .replace("{player}", tp.getName()));
                    event.setCancelled(true);
                    return;
                }
                String filtered = chat;
                String echo;
                String send;
                String spy;
                if (mcp.hasFilter()) {
                    filtered = Format.FilterChat(filtered);
                }
                if (mcpPlayer.hasPermission("venturechat.color.legacy")) {
                    filtered = Format.FormatStringLegacyColor(filtered);
                }
                if (mcpPlayer.hasPermission("venturechat.color")) {
                    filtered = Format.FormatStringColor(filtered);
                }
                if (mcpPlayer.hasPermission("venturechat.format")) {
                    filtered = Format.FormatString(filtered);
                }
                filtered = " " + filtered;

                send = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcpPlayer, plugin.getConfig().getString("tellformatfrom").replaceAll("sender_", "")));
                echo = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcpPlayer, plugin.getConfig().getString("tellformatto").replaceAll("sender_", "")));
                spy = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcpPlayer, plugin.getConfig().getString("tellformatspy").replaceAll("sender_", "")));

                send = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(tp.getPlayer(), send.replaceAll("receiver_", ""))) + filtered;
                echo = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(tp.getPlayer(), echo.replaceAll("receiver_", ""))) + filtered;
                spy = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(tp.getPlayer(), spy.replaceAll("receiver_", ""))) + filtered;

                if (!mcpPlayer.hasPermission("venturechat.spy.override")) {
                    for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                        if (p.getName().equals(mcp.getName()) || p.getName().equals(tp.getName())) {
                            continue;
                        }
                        if (p.isSpy()) {
                            p.getPlayer().sendMessage(spy);
                        }
                    }
                }
                tp.getPlayer().sendMessage(send);
                mcpPlayer.sendMessage(echo);
                if (tp.hasNotifications()) {
                    Format.playMessageSound(tp);
                }
                mcp.setReplyPlayer(tp.getUUID());
                tp.setReplyPlayer(mcp.getUUID());
                if (Database.isEnabled()) {
                    Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", "Messaging_Component", chat.replace("'", "''"), "Chat");
                }
            }
            return;
        }

        if (mcp.isPartyChat() && !mcp.isQuickChat()) {
            if (mcp.hasParty()) {
                return;
            }
            mcpPlayer.sendMessage(ChatColor.RED + "You are not in a party.");
            return;
        }

        Location locreceip;
        final Location locsender = mcpPlayer.getLocation();
        Location diff;
        final Boolean filterthis;
        mcp.addListening(eventChannel.getName());
        if (mcp.isMuted(eventChannel.getName())) {
            final MuteContainer muteContainer = mcp.getMute(eventChannel.getName());
            if (muteContainer.hasDuration()) {
                final long dateTimeMillis = System.currentTimeMillis();
                final long muteTimeMillis = muteContainer.getDuration();
                long remainingMuteTime = muteTimeMillis - dateTimeMillis;
                if (remainingMuteTime < 1000) {
                    remainingMuteTime = 1000;
                }
                final String timeString = Format.parseTimeStringFromMillis(remainingMuteTime);
                if (muteContainer.hasReason()) {
                    mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_MUTED_TIMED_REASON.toString()
                                    .replace("{channel_color}", eventChannel.getColor())
                                    .replace("{channel_name}", eventChannel.getName())
                                    .replace("{time}", timeString)
                                    .replace("{reason}", muteContainer.getReason()));
                } else {
                    mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_MUTED_TIMED.toString()
                                    .replace("{channel_color}", eventChannel.getColor())
                                    .replace("{channel_name}", eventChannel.getName())
                                    .replace("{time}", timeString));
                }
            } else {
                if (muteContainer.hasReason()) {
                    mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_MUTED_REASON.toString()
                                    .replace("{channel_color}", eventChannel.getColor())
                                    .replace("{channel_name}", eventChannel.getName())
                                    .replace("{reason}", muteContainer.getReason()));
                } else {
                    mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_MUTED.toString()
                                    .replace("{channel_color}", eventChannel.getColor())
                                    .replace("{channel_name}", eventChannel.getName()));
                }
            }
            mcp.setQuickChat(false);
            return;
        }
        Double chDistance = (double) 0;
        final String curColor;
        if (eventChannel.hasPermission() && !mcpPlayer.hasPermission(eventChannel.getPermission())) {
            mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_NO_PERMISSION.toString());
            mcp.setQuickChat(false);
            mcp.removeListening(eventChannel.getName());
            mcp.setCurrentChannel(ChatChannel.getDefaultChannel());
            return;
        }
        if (eventChannel.hasSpeakPermission() && !mcpPlayer.hasPermission(eventChannel.getSpeakPermission())) {
            mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_NO_SPEAK_PERMISSIONS.toString());
            mcp.setQuickChat(false);
            return;
        }
        curColor = eventChannel.getChatColor();
        bungee = eventChannel.getBungee();

        final long dateTimeSeconds = System.currentTimeMillis() / Format.MILLISECONDS_PER_SECOND;

        int chCooldown = 0;
        if (eventChannel.hasCooldown()) {
            chCooldown = eventChannel.getCooldown();
        }
        try {
            if (mcp.hasCooldown(eventChannel)) {
                final long cooldownTime = mcp.getCooldowns().get(eventChannel);
                if (dateTimeSeconds < cooldownTime) {
                    final long remainingCooldownTime = cooldownTime - dateTimeSeconds;
                    final String cooldownString = Format.parseTimeStringFromMillis(remainingCooldownTime * Format.MILLISECONDS_PER_SECOND);
                    mcpPlayer.sendMessage(LocalizedMessage.CHANNEL_COOLDOWN.toString()
                            .replace("{cooldown}", cooldownString));
                    mcp.setQuickChat(false);
                    return;
                }
            }
            if (eventChannel.hasCooldown() && !mcpPlayer.hasPermission("venturechat.cooldown.bypass")) {
                mcp.addCooldown(eventChannel, dateTimeSeconds + chCooldown);
            }

        } catch (final NumberFormatException e) {
            plugin.getLogger().warning("Invalid cooldown value for channel " + eventChannel.getName() + ". Please check your configuration.");
        }

        if (mcp.hasSpam(eventChannel) && plugin.getConfig().getConfigurationSection("antispam").getBoolean("enabled")
                && !mcpPlayer.hasPermission("venturechat.spam.bypass")) {
            final long spamcount = mcp.getSpam().get(eventChannel).get(0);
            final long spamtime = mcp.getSpam().get(eventChannel).get(1);
            long spamtimeconfig = plugin.getConfig().getConfigurationSection("antispam").getLong("spamnumber");
            final String mutedForTime = plugin.getConfig().getConfigurationSection("antispam").getString("mutetime", "0");
            final long dateTime = System.currentTimeMillis();
            if (dateTimeSeconds < spamtime
                    + plugin.getConfig().getConfigurationSection("antispam").getLong("spamtime")) {
                if (spamcount + 1 >= spamtimeconfig) {
                    final long time = Format.parseTimeStringToMillis(mutedForTime);
                    if (time > 0) {
                        mcp.addMute(eventChannel.getName(), dateTime + time, LocalizedMessage.SPAM_MUTE_REASON_TEXT.toString());
                        final String timeString = Format.parseTimeStringFromMillis(time);
                        mcpPlayer
                                .sendMessage(LocalizedMessage.MUTE_PLAYER_PLAYER_TIME_REASON.toString()
                                        .replace("{channel_color}", eventChannel.getColor())
                                        .replace("{channel_name}", eventChannel.getName())
                                        .replace("{time}", timeString)
                                        .replace("{reason}", LocalizedMessage.SPAM_MUTE_REASON_TEXT.toString()));
                    } else {
                        mcp.addMute(eventChannel.getName(), LocalizedMessage.SPAM_MUTE_REASON_TEXT.toString());
                        mcpPlayer
                                .sendMessage(LocalizedMessage.MUTE_PLAYER_PLAYER_REASON.toString()
                                        .replace("{channel_color}", eventChannel.getColor())
                                        .replace("{channel_name}", eventChannel.getName())
                                        .replace("{reason}", LocalizedMessage.SPAM_MUTE_REASON_TEXT.toString()));
                    }
                    if (eventChannel.getBungee()) {
                        MineverseChat.synchronize(mcp, true);
                    }
                    mcp.getSpam().get(eventChannel).set(0, 0L);
                    mcp.setQuickChat(false);
                    return;
                } else {
                    if (spamtimeconfig % 2 != 0) {
                        spamtimeconfig++;
                    }
                    if (spamcount + 1 == spamtimeconfig / 2) {
                        mcpPlayer.sendMessage(LocalizedMessage.SPAM_WARNING.toString());
                    }
                    mcp.getSpam().get(eventChannel).set(0, spamcount + 1);
                }
            } else {
                mcp.getSpam().get(eventChannel).set(0, 1L);
                mcp.getSpam().get(eventChannel).set(1, dateTimeSeconds);
            }
        } else {
            mcp.addSpam(eventChannel);
            mcp.getSpam().get(eventChannel).add(0, 1L);
            mcp.getSpam().get(eventChannel).add(1, dateTimeSeconds);
        }

        if (eventChannel.hasDistance()) {
            chDistance = eventChannel.getDistance();
        }

        format = Format.FormatStringAll(eventChannel.getFormat());

        filterthis = eventChannel.isFiltered();
        if (filterthis && mcp.hasFilter()) {
            chat = Format.FilterChat(chat);
        }

        for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
            if (p.getPlayer() != mcpPlayer) {
                if (!p.isListening(eventChannel.getName())) {
                    recipients.remove(p.getPlayer());
                    recipientCount--;
                    continue;
                }
                if (plugin.getConfig().getBoolean("ignorechat", false) && p.getIgnores().contains(mcp.getUUID())) {
                    recipients.remove(p.getPlayer());
                    recipientCount--;
                    continue;
                }

                if (chDistance > 0 && !bungee && !p.getRangedSpy()) {
                    locreceip = p.getPlayer().getLocation();
                    if (locreceip.getWorld() == mcpPlayer.getWorld()) {
                        diff = locreceip.subtract(locsender);
                        if (Math.abs(diff.getX()) > chDistance || Math.abs(diff.getZ()) > chDistance || Math.abs(diff.getY()) > chDistance) {
                            recipients.remove(p.getPlayer());
                            recipientCount--;
                            continue;
                        }
                        if (!mcpPlayer.canSee(p.getPlayer())) {
                            recipientCount--;
                            continue;
                        }
                    } else {
                        recipients.remove(p.getPlayer());
                        recipientCount--;
                        continue;
                    }
                }
                if (!mcpPlayer.canSee(p.getPlayer())) {
                    recipientCount--;
                }
            }
        }

        if (mcpPlayer.hasPermission("venturechat.color.legacy")) {
            chat = Format.FormatStringLegacyColor(chat);
        }
        if (mcpPlayer.hasPermission("venturechat.color")) {
            chat = Format.FormatStringColor(chat);
        }
        if (mcpPlayer.hasPermission("venturechat.format")) {
            chat = Format.FormatString(chat);
        }
        if (!mcp.isQuickChat()) {
            chat = " " + chat;
        }
        if (curColor.equalsIgnoreCase("None")) {
            // Format the placeholders and their color codes to determine the last color code to use for the chat message color
            chat = Format.getLastCode(Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcpPlayer, format))) + chat;
        } else {
            chat = curColor + chat;
        }

        final String globalJSON = Format.convertToJson(mcp, format, chat);
        format = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(mcpPlayer, Format.FormatStringAll(format)));
        final String message = Format.stripColor(format + chat); // UTF-8 encoding issues.
        final int hash = message.hashCode();

        //Create VentureChatEvent
        final VentureChatEvent ventureChatEvent = new VentureChatEvent(mcp, mcp.getName(), mcp.getNickname(), MineverseChat.getVaultPermission().getPrimaryGroup(mcpPlayer), eventChannel, recipients, recipientCount, format, chat, globalJSON, hash, bungee);
        //Fire event and wait for other plugin listeners to act on it
        Bukkit.getServer().getPluginManager().callEvent(ventureChatEvent);
        //Call method to send the processed chat
        handleVentureChatEvent(ventureChatEvent);
        // Reset quick chat flag
        mcp.setQuickChat(false);
    }

    public void handleVentureChatEvent(@NotNull final VentureChatEvent event) {
        final MineverseChatPlayer mcp = event.getMineverseChatPlayer();
        final ChatChannel channel = event.getChannel();
        final Set<Player> recipients = event.getRecipients();
        final int recipientCount = event.getRecipientCount();
        final String format = event.getFormat();
        final String chat = event.getChat();
        final String consoleChat = event.getConsoleChat();
        final String globalJSON = event.getGlobalJSON();
        final int hash = event.getHash();
        final boolean bungee = event.isBungee();

        final Player mcpPlayer = mcp.getPlayer();
        if (essentialsDiscordHook && channel.isDefaultchannel()) {
            final DiscordService discordService = Bukkit.getServicesManager().load(DiscordService.class);
            if (discordService != null) {
                discordService.sendChatMessage(mcpPlayer, chat);
            }
        }

        if (!bungee) {
            if (Database.isEnabled()) {
                Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", channel.getName(), chat.replace("'", "''"), "Chat");
            }

            if (recipientCount == 1 && !plugin.getConfig().getString("emptychannelalert", "&6No one is listening to you.").equals("")) {
                mcpPlayer.sendMessage(Format.FormatStringAll(plugin.getConfig().getString("emptychannelalert", "&6No one is listening to you.")));
            }

            final Component message = serializer.deserialize(globalJSON);
            for (final Player p : recipients) {
                p.sendMessage(message);
            }

            Bukkit.getConsoleSender().sendMessage(consoleChat);
        } else {
            final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(byteOutStream);
            try {
                out.writeUTF("Chat");
                out.writeUTF(channel.getName());
                out.writeUTF(mcp.getName());
                out.writeUTF(mcp.getUUID().toString());
                out.writeBoolean(mcp.getBungeeToggle());
                out.writeInt(hash);
                out.writeUTF(format);
                out.writeUTF(chat);
                if (plugin.getConfig().getString("loglevel", "info").equals("debug")) {
                    System.out.println(out.size() + " size bytes without json");
                }
                out.writeUTF(globalJSON);
                if (plugin.getConfig().getString("loglevel", "info").equals("debug")) {
                    System.out.println(out.size() + " bytes size with json");
                }
                out.writeUTF(MineverseChat.getVaultPermission().getPrimaryGroup(mcpPlayer));
                out.writeUTF(mcp.getNickname());
                mcpPlayer.sendPluginMessage(plugin, MineverseChat.PLUGIN_MESSAGING_CHANNEL, byteOutStream.toByteArray());
                out.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }
}
