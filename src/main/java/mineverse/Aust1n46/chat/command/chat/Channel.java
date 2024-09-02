package mineverse.Aust1n46.chat.command.chat;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.ChannelJoinEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Channel extends Command implements Listener {
    public Channel() {
        super("channel");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String command, String[] args) {
        if (!(sender instanceof Player player)) {
            Bukkit.getServer().getConsoleSender().sendMessage(LocalizedMessage.COMMAND_MUST_BE_RUN_BY_PLAYER.toString());
            return true;
        }

        if (args.length > 0) {
            if (!ChatChannel.isChannel(args[0])) {
                player.sendMessage(LocalizedMessage.INVALID_CHANNEL.toString()
                        .replace("{args}", args[0]));
                return true;
            }

            ChatChannel channel = ChatChannel.getChannel(args[0]);
            new ChannelJoinEvent(player, channel, LocalizedMessage.SET_CHANNEL.toString()
                    .replace("{channel_color}", channel.getColor())
                    .replace("{channel_name}", channel.getName()))
                    .callEvent();
            return true;
        }

        player.sendMessage(LocalizedMessage.COMMAND_INVALID_ARGUMENTS.toString()
                .replace("{command}", "/channel")
                .replace("{args}", "[channel]"));
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChannelJoin(@NotNull ChannelJoinEvent event) {
        if (event.isCancelled()) return;

        ChatChannel channel = event.getChannel();
        Player player = event.getPlayer();
        MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(player);
        if (channel.hasPermission() && !player.hasPermission(channel.getPermission())) {
            mcp.removeListening(channel.getName());
            player.sendMessage(LocalizedMessage.CHANNEL_NO_PERMISSION.toString());
            return;
        }

        if (mcp.hasConversation()) {
            for (MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                if (p.isSpy()) {
                    p.getPlayer().sendMessage(LocalizedMessage.EXIT_PRIVATE_CONVERSATION_SPY.toString()
                            .replace("{player_sender}", mcp.getName())
                            .replace("{player_receiver}", MineverseChatAPI.getMineverseChatPlayer(mcp.getConversation()).getName()));
                }
            }

            player.sendMessage(LocalizedMessage.EXIT_PRIVATE_CONVERSATION.toString()
                    .replace("{player_receiver}", MineverseChatAPI.getMineverseChatPlayer(mcp.getConversation()).getName()));
            mcp.setConversation(null);
        }

        mcp.addListening(channel.getName());
        mcp.setCurrentChannel(channel);
        player.sendMessage(event.getMessage());

        if (channel.getBungee()) {
            MineverseChat.synchronize(mcp, true);
        }
    }
}
