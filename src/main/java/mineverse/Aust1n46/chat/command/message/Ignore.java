package mineverse.Aust1n46.chat.command.message;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Ignore extends Command {
    private final MineverseChat plugin = MineverseChat.getInstance();

    public Ignore() {
        super("ignore");
    }

    @Override
    public boolean execute(@NotNull final CommandSender sender, @NotNull final String command, final String[] args) {
        if (!(sender instanceof final Player senderPlayer)) {
            plugin.getServer().getConsoleSender().sendMessage(LocalizedMessage.COMMAND_MUST_BE_RUN_BY_PLAYER.toString());
            return true;
        }
        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(senderPlayer);
        if (args.length == 0) {
            mcp.getPlayer().sendMessage(LocalizedMessage.COMMAND_INVALID_ARGUMENTS_IGNORE.toString());
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            final StringBuilder ignoreList = new StringBuilder();
            for (final UUID ignore : mcp.getIgnores()) {
                final MineverseChatPlayer i = MineverseChatAPI.getMineverseChatPlayer(ignore);
                String iName = ignore.toString();
                if (i != null) {
                    iName = i.getName();
                }
                ignoreList.append(ChatColor.RED)
                        .append(iName)
                        .append(ChatColor.WHITE)
                        .append(", ");
            }
            mcp.getPlayer().sendMessage(LocalizedMessage.IGNORE_LIST_HEADER.toString());
            if (!ignoreList.isEmpty()) {
                mcp.getPlayer().sendMessage(ignoreList.substring(0, ignoreList.length() - 2));
            }
            return true;
        }
        if (mcp.getName().equalsIgnoreCase(args[0])) {
            mcp.getPlayer().sendMessage(LocalizedMessage.IGNORE_YOURSELF.toString());
            return true;
        }
        if (plugin.getConfig().getBoolean("bungeecordmessaging", true)) {
            final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(byteOutStream);
            try {
                out.writeUTF("Ignore");
                out.writeUTF("Send");
                out.writeUTF(args[0]);
                out.writeUTF(mcp.getUUID().toString());
                mcp.getPlayer().sendPluginMessage(plugin, MineverseChat.PLUGIN_MESSAGING_CHANNEL, byteOutStream.toByteArray());
                out.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        final MineverseChatPlayer player = MineverseChatAPI.getOnlineMineverseChatPlayer(args[0]);
        if (player == null) {
            mcp.getPlayer().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString().replace("{args}", args[0]));
            return true;
        }
        if (mcp.getIgnores().contains(player.getUUID())) {
            mcp.getPlayer().sendMessage(LocalizedMessage.IGNORE_PLAYER_OFF.toString().replace("{player}", player.getName()));
            mcp.removeIgnore(player.getUUID());
            MineverseChat.synchronize(mcp, true);
            return true;
        }
        if (player.getPlayer().hasPermission("venturechat.ignore.bypass")) {
            mcp.getPlayer().sendMessage(LocalizedMessage.IGNORE_PLAYER_CANT.toString().replace("{player}", player.getName()));
            return true;
        }
        mcp.getPlayer().sendMessage(LocalizedMessage.IGNORE_PLAYER_ON.toString().replace("{player}", player.getName()));
        mcp.addIgnore(player.getUUID());
        MineverseChat.synchronize(mcp, true);
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull final CommandSender sender, @NotNull final String label, final String[] args) {
        if (plugin.getConfig().getBoolean("bungeecordmessaging", true)) {
            final List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[args.length - 1], MineverseChatAPI.getNetworkPlayerNames(), completions);
            Collections.sort(completions);
            return completions;
        }
        return super.tabComplete(sender, label, args);
    }
}
