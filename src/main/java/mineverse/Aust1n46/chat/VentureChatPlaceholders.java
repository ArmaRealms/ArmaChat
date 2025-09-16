package mineverse.Aust1n46.chat;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VentureChatPlaceholders extends PlaceholderExpansion {
    @Override
    public String onPlaceholderRequest(Player p, @NotNull String identifier) {
        if (p == null) {
            return null;
        }
        MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(p);
        if (mcp == null) {
            return "";
        }
        if (identifier.equalsIgnoreCase("nickname")) {
            return mcp.hasNickname() ? mcp.getNickname() : mcp.getName();
        }
        if (identifier.equalsIgnoreCase("something_else_you_think_of")) {
            return "value for that identifier *";
        }
        if (identifier.startsWith("channel_")) {
            ChatChannel currentChannel = mcp.isQuickChat() ? mcp.getQuickChannel() : mcp.getCurrentChannel();
            if (currentChannel == null) {
                return "";
            }
            switch (identifier) {
                case "channel_name":
                    return currentChannel.getName();
                case "channel_alias":
                    return currentChannel.getAlias();
                case "channel_color":
                    return currentChannel.getColor();
                case "channel_chatcolor":
                    return currentChannel.getChatColor();
                case "channel_is_bungee":
                    return currentChannel.getBungee() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
                case "channel_cooldown":
                    return currentChannel.getCooldown() + "";
                case "channel_distance":
                    return currentChannel.getDistance() + "";
                case "channel_prefix":
                    return currentChannel.getPrefix();
            }
        }
        if (identifier.equalsIgnoreCase("notifications")) {
            return mcp.hasNotifications() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }
        if (identifier.equalsIgnoreCase("message_toggle")) {
            return mcp.getMessageToggle() ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
        }
        return null;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Aust1n46";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "venturechat";
    }

    @Override
    public @NotNull String getVersion() {
        return MineverseChat.getInstance().getDescription().getVersion();
    }
}
