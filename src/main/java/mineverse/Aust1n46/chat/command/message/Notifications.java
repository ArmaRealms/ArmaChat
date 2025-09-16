package mineverse.Aust1n46.chat.command.message;

import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Notifications extends Command {
    public Notifications() {
        super("notifications");
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String command, final String[] args) {
        if (!(sender instanceof final Player senderPlayer)) {
            Bukkit.getServer().getConsoleSender().sendMessage(LocalizedMessage.COMMAND_MUST_BE_RUN_BY_PLAYER.toString());
            return true;
        }

        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(senderPlayer);
        if (!mcp.hasNotifications()) {
            mcp.setNotifications(true);
            mcp.getPlayer().sendMessage(LocalizedMessage.NOTIFICATIONS_ON.toString());
            return true;
        }

        mcp.setNotifications(false);
        mcp.getPlayer().sendMessage(LocalizedMessage.NOTIFICATIONS_OFF.toString());
        return true;
    }
}
