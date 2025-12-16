package mineverse.Aust1n46.chat.api;

import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.command.mute.MuteContainer;
import mineverse.Aust1n46.chat.json.JsonFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Wrapper for {@link Player}
 *
 * @author Aust1n46
 */
public class MineverseChatPlayer {
    private final UUID uuid;
    private final Set<UUID> ignores;
    private final Set<String> listening;
    private final HashMap<String, MuteContainer> mutes;
    private final Set<String> blockedCommands;
    private final HashMap<ChatChannel, Long> cooldowns;
    private final HashMap<ChatChannel, List<Long>> spam;
    private String name;
    private ChatChannel currentChannel;
    private boolean host;
    private UUID party;
    private boolean filter;
    private boolean notifications;
    private boolean online;
    private Player player;
    private boolean hasPlayed;
    private UUID conversation;
    private boolean spy;
    private boolean commandSpy;
    private boolean quickChat;
    private ChatChannel quickChannel;
    private UUID replyPlayer;
    private boolean partyChat;
    private boolean modified;
    private String jsonFormat;
    private boolean editing;
    private int editHash;
    private boolean rangedSpy;
    private boolean messageToggle;
    private boolean bungeeToggle;

    @Deprecated
    public MineverseChatPlayer(final UUID uuid, final String name, final ChatChannel currentChannel, final Set<UUID> ignores, final Set<String> listening, final HashMap<String, MuteContainer> mutes, final Set<String> blockedCommands, final boolean host, final UUID party, final boolean filter, final boolean notifications, final String nickname, final String jsonFormat, final boolean spy, final boolean commandSpy, final boolean rangedSpy, final boolean messageToggle, final boolean bungeeToggle) {
        this(uuid, name, currentChannel, ignores, listening, mutes, blockedCommands, host, party, filter, notifications, jsonFormat, spy, commandSpy, rangedSpy, messageToggle, bungeeToggle);
    }

    public MineverseChatPlayer(final UUID uuid, final String name, final ChatChannel currentChannel, final Set<UUID> ignores, final Set<String> listening, final HashMap<String, MuteContainer> mutes, final Set<String> blockedCommands, final boolean host, final UUID party, final boolean filter, final boolean notifications, final String jsonFormat, final boolean spy, final boolean commandSpy, final boolean rangedSpy, final boolean messageToggle, final boolean bungeeToggle) {
        this.uuid = uuid;
        this.name = name;
        this.currentChannel = currentChannel;
        this.ignores = ignores;
        this.listening = listening;
        this.mutes = mutes;
        this.blockedCommands = blockedCommands;
        this.host = host;
        this.party = party;
        this.filter = filter;
        this.notifications = notifications;
        this.online = false;
        this.player = null;
        this.hasPlayed = false;
        this.conversation = null;
        this.spy = spy;
        this.rangedSpy = rangedSpy;
        this.commandSpy = commandSpy;
        this.quickChat = false;
        this.quickChannel = null;
        this.replyPlayer = null;
        this.partyChat = false;
        this.modified = false;
        this.jsonFormat = jsonFormat;
        this.cooldowns = new HashMap<>();
        this.spam = new HashMap<>();
        this.messageToggle = messageToggle;
        this.bungeeToggle = bungeeToggle;
    }

    public MineverseChatPlayer(final UUID uuid, final String name) {
        this.uuid = uuid;
        this.name = name;
        this.currentChannel = ChatChannel.getDefaultChannel();
        this.ignores = new HashSet<UUID>();
        this.listening = new HashSet<String>();
        listening.add(currentChannel.getName());
        this.mutes = new HashMap<String, MuteContainer>();
        this.blockedCommands = new HashSet<String>();
        this.host = false;
        this.party = null;
        this.filter = true;
        this.notifications = true;
        this.online = false;
        this.player = null;
        this.hasPlayed = false;
        this.conversation = null;
        this.spy = false;
        this.rangedSpy = false;
        this.commandSpy = false;
        this.quickChat = false;
        this.quickChannel = null;
        this.replyPlayer = null;
        this.partyChat = false;
        this.modified = false;
        this.jsonFormat = "Default";
        this.cooldowns = new HashMap<>();
        this.spam = new HashMap<>();
        this.messageToggle = true;
        this.bungeeToggle = true;
    }

    @Deprecated
    public String getNickname() {
        return this.online ? this.player.getDisplayName() : "";
    }

    @Deprecated
    public void setNickname(final String nick) {
    }

    @Deprecated
    public boolean hasNickname() {
        return false;
    }

    public boolean getBungeeToggle() {
        return this.bungeeToggle;
    }

    public void setBungeeToggle(final boolean bungeeToggle) {
        this.bungeeToggle = bungeeToggle;
    }

    public boolean getMessageToggle() {
        return this.messageToggle;
    }

    public void setMessageToggle(final boolean messageToggle) {
        this.messageToggle = messageToggle;
    }

    public boolean getRangedSpy() {
        if (isOnline()) {
            if (!getPlayer().hasPermission("venturechat.rangedspy")) {
                setRangedSpy(false);
                return false;
            }
        }
        return this.rangedSpy;
    }

    public void setRangedSpy(final boolean rangedSpy) {
        this.rangedSpy = rangedSpy;
    }

    public boolean isEditing() {
        return this.editing;
    }

    public void setEditing(final boolean editing) {
        this.editing = editing;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public ChatChannel getCurrentChannel() {
        return this.currentChannel;
    }

    public boolean setCurrentChannel(final ChatChannel channel) {
        if (channel != null) {
            this.currentChannel = channel;
            return true;
        }
        return false;
    }

    public Set<UUID> getIgnores() {
        return this.ignores;
    }

    public void addIgnore(final UUID ignore) {
        this.ignores.add(ignore);
    }

    public void removeIgnore(final UUID ignore) {
        this.ignores.remove(ignore);
    }

    public Set<String> getListening() {
        return this.listening;
    }

    public boolean isListening(final String channel) {
        if (this.isOnline()) {
            if (ChatChannel.isChannel(channel)) {
                final ChatChannel chatChannel = ChatChannel.getChannel(channel);
                if (chatChannel.hasPermission()) {
                    if (!this.getPlayer().hasPermission(chatChannel.getPermission())) {
                        if (this.getCurrentChannel().equals(chatChannel)) {
                            this.setCurrentChannel(ChatChannel.getDefaultChannel());
                        }
                        this.removeListening(channel);
                        return false;
                    }
                }
            }
        }
        return this.listening.contains(channel);
    }

    public boolean addListening(final String channel) {
        if (channel != null) {
            this.listening.add(channel);
            return true;
        }
        return false;
    }

    public boolean removeListening(final String channel) {
        if (channel != null) {
            this.listening.remove(channel);
            return true;
        }
        return false;
    }

    public void clearListening() {
        this.listening.clear();
    }

    public Collection<MuteContainer> getMutes() {
        return this.mutes.values();
    }

    public MuteContainer getMute(final String channel) {
        return mutes.get(channel);
    }

    public boolean addMute(final String channel) {
        return addMute(channel, 0, "");
    }

    public boolean addMute(final String channel, final long time) {
        return addMute(channel, time, "");
    }

    public boolean addMute(final String channel, final String reason) {
        return addMute(channel, 0, reason);
    }

    public boolean addMute(final String channel, final long time, final String reason) {
        if (channel != null && time >= 0) {
            mutes.put(channel, new MuteContainer(channel, time, reason));
            return true;
        }
        return false;
    }

    public boolean removeMute(final String channel) {
        if (channel != null) {
            mutes.remove(channel);
            return true;
        }
        return false;
    }

    public boolean isMuted(final String channel) {
        return channel != null && this.mutes.containsKey(channel);
    }

    public Set<String> getBlockedCommands() {
        return this.blockedCommands;
    }

    public void addBlockedCommand(final String command) {
        this.blockedCommands.add(command);
    }

    public void removeBlockedCommand(final String command) {
        this.blockedCommands.remove(command);
    }

    public boolean isBlockedCommand(final String command) {
        return this.blockedCommands.contains(command);
    }

    public boolean isHost() {
        return this.host;
    }

    public void setHost(final boolean host) {
        this.host = host;
    }

    public UUID getParty() {
        return this.party;
    }

    public void setParty(final UUID party) {
        this.party = party;
    }

    public boolean hasParty() {
        return this.party != null;
    }

    public boolean hasFilter() {
        return this.filter;
    }

    public void setFilter(final boolean filter) {
        this.filter = filter;
    }

    public boolean hasNotifications() {
        return this.notifications;
    }

    public void setNotifications(final boolean notifications) {
        this.notifications = notifications;
    }

    public boolean isOnline() {
        return this.online;
    }

    public void setOnline(final boolean online) {
        this.online = online;
        if (this.online) {
            this.player = Bukkit.getPlayer(name);
        } else {
            this.player = null;
        }
    }

    public Player getPlayer() {
        return this.online ? this.player : null;
    }

    public boolean hasPlayed() {
        return this.hasPlayed;
    }

    public void setHasPlayed(final boolean played) {
        this.hasPlayed = played;
    }

    public UUID getConversation() {
        return this.conversation;
    }

    public void setConversation(final UUID conversation) {
        this.conversation = conversation;
    }

    public boolean hasConversation() {
        return this.conversation != null;
    }

    public boolean isSpy() {
        if (this.isOnline()) {
            if (!this.getPlayer().hasPermission("venturechat.spy")) {
                this.setSpy(false);
                return false;
            }
        }
        return this.spy;
    }

    public void setSpy(final boolean spy) {
        this.spy = spy;
    }

    public boolean hasCommandSpy() {
        if (this.isOnline()) {
            if (!this.getPlayer().hasPermission("venturechat.commandspy")) {
                this.setCommandSpy(false);
                return false;
            }
        }
        return this.commandSpy;
    }

    public void setCommandSpy(final boolean commandSpy) {
        this.commandSpy = commandSpy;
    }

    public boolean isQuickChat() {
        return this.quickChat;
    }

    public void setQuickChat(final boolean quickChat) {
        this.quickChat = quickChat;
    }

    public ChatChannel getQuickChannel() {
        return this.quickChannel;
    }

    public boolean setQuickChannel(final ChatChannel channel) {
        if (channel != null) {
            this.quickChannel = channel;
            return true;
        }
        return false;
    }

    @Deprecated
    /**
     * Not needed and never resets to it's original null value after being set once.
     * @return
     */
    public boolean hasQuickChannel() {
        return this.quickChannel != null;
    }

    public UUID getReplyPlayer() {
        return this.replyPlayer;
    }

    public void setReplyPlayer(final UUID replyPlayer) {
        this.replyPlayer = replyPlayer;
    }

    public boolean hasReplyPlayer() {
        return this.replyPlayer != null;
    }

    public boolean isPartyChat() {
        return this.partyChat;
    }

    public void setPartyChat(final boolean partyChat) {
        this.partyChat = partyChat;
    }

    public HashMap<ChatChannel, Long> getCooldowns() {
        return this.cooldowns;
    }

    public boolean addCooldown(final ChatChannel channel, final long time) {
        if (channel != null && time > 0) {
            cooldowns.put(channel, time);
            return true;
        }
        return false;
    }

    public boolean removeCooldown(final ChatChannel channel) {
        if (channel != null) {
            cooldowns.remove(channel);
            return true;
        }
        return false;
    }

    public boolean hasCooldown(final ChatChannel channel) {
        return channel != null && this.cooldowns != null && this.cooldowns.containsKey(channel);
    }

    public HashMap<ChatChannel, List<Long>> getSpam() {
        return this.spam;
    }

    public boolean hasSpam(final ChatChannel channel) {
        return channel != null && this.spam != null && this.spam.containsKey(channel);
    }

    public boolean addSpam(final ChatChannel channel) {
        if (channel != null) {
            spam.put(channel, new ArrayList<Long>());
            return true;
        }
        return false;
    }

    public void setModified(final boolean modified) {
        this.modified = modified;
    }

    public boolean wasModified() {
        return this.modified;
    }

    public String getJsonFormat() {
        return this.jsonFormat;
    }

    public void setJsonFormat() {
        this.jsonFormat = "Default";
        for (final JsonFormat j : JsonFormat.getJsonFormats()) {
            if (this.getPlayer().hasPermission("venturechat.json." + j.name())) {
                if (JsonFormat.getJsonFormat(this.getJsonFormat()).priority() > j.priority()) {
                    this.jsonFormat = j.name();
                }
            }
        }
    }
}
