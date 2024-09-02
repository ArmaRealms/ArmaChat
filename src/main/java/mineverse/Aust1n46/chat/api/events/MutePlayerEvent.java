package mineverse.Aust1n46.chat.api.events;

import mineverse.Aust1n46.chat.channel.ChatChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

//This class is a custom event that is part of the plugins API.  It is called when a player executes the mute command.
public class MutePlayerEvent extends PlayerEvent implements Cancellable {    //unimplemented
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private boolean cancelled;
    private ChatChannel channel;
    private int time;

    public MutePlayerEvent(Player player, ChatChannel channel, int time) {
        super(player, !Bukkit.isPrimaryThread());
        this.channel = channel;
        this.time = time;
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

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}