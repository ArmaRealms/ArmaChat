package mineverse.Aust1n46.chat.api.events;

import mineverse.Aust1n46.chat.channel.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player attempts to join a valid channel
 */
public class ChannelJoinEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private ChatChannel channel;
    private String message;

    public ChannelJoinEvent(Player player, ChatChannel channel, String message) {
        super(player, !Bukkit.isPrimaryThread());
        this.channel = channel;
        this.message = message;
        this.cancelled = false;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public ChatChannel getChannel() {
        return this.channel;
    }

    public void setChannel(ChatChannel channel) {
        this.channel = channel;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}