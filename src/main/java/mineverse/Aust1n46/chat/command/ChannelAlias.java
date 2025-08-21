package mineverse.Aust1n46.chat.command;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.api.events.ChannelJoinEvent;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChannelAlias extends Command {

    public ChannelAlias() {
        super("channelalias");
    }

    @Override
    public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizedMessage.COMMAND_MUST_BE_RUN_BY_PLAYER.toString());
            return true;
        }

        MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(player);
        for (ChatChannel channel : ChatChannel.getChatChannels()) {
            if (commandLabel.toLowerCase().equals(channel.getAlias())) {
                if (args.length == 0) {
                    new ChannelJoinEvent(player, channel, LocalizedMessage.SET_CHANNEL.toString()
                            .replace("{channel_color}", channel.getColor())
                            .replace("{channel_name}", channel.getName()))
                            .callEvent();
                    return true;
                } else {
                    mcp.setQuickChat(true);
                    mcp.setQuickChannel(channel);
                    mcp.addListening(channel.getName());
                    if (channel.getBungee()) {
                        MineverseChat.synchronize(mcp, true);
                    }
                    StringBuilder msg = new StringBuilder();
                    for (String arg : args) {
                        if (!arg.isEmpty()) {
                            msg.append(" ").append(arg);
                        }
                    }
                    mcp.getPlayer().chat(msg.toString());
                }
                return true;
            }
        }
        return true;
    }
}
