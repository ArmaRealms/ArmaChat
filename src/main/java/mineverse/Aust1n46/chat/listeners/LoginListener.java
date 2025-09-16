package mineverse.Aust1n46.chat.listeners;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.database.PlayerData;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.utilities.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Manages player login and logout events.
 *
 * @author Aust1n46
 */
public class LoginListener implements Listener {
    private final MineverseChat plugin = MineverseChat.getInstance();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(final @NotNull PlayerJoinEvent event) throws Exception {
        MineverseChatPlayer mcp = MineverseChatAPI.getMineverseChatPlayer(event.getPlayer());
        final Player player = event.getPlayer();
        final String name = player.getName();
        if (mcp == null) {
            final UUID uuid = player.getUniqueId();
            mcp = new MineverseChatPlayer(uuid, name);
            MineverseChatAPI.addMineverseChatPlayerToMap(mcp);
            MineverseChatAPI.addNameToMap(mcp);
        }
        UUIDFetcher.checkOfflineUUIDWarning(mcp.getUUID());
        //check for name change
        if (!mcp.getName().equals(name)) {
            handleNameChange(mcp, event.getPlayer());
        }
        mcp.setOnline(true);
        mcp.setHasPlayed(false);
        MineverseChatAPI.addMineverseChatOnlinePlayerToMap(mcp);
        mcp.setJsonFormat();
        for (final ChatChannel ch : ChatChannel.getAutojoinList()) {
            if (ch.hasPermission()) {
                if (mcp.getPlayer().hasPermission(ch.getPermission())) {
                    mcp.addListening(ch.getName());
                }
            } else {
                mcp.addListening(ch.getName());
            }
        }

        if (MineverseChat.isConnectedToProxy()) {
            final MineverseChatPlayer sync = mcp;
            plugin.getServer().getScheduler()
                    .runTaskLaterAsynchronously(plugin, () -> MineverseChat.synchronize(sync, false), 20L);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(final @NotNull PlayerQuitEvent playerQuitEvent) {
        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(playerQuitEvent.getPlayer());
        PlayerData.savePlayerData(mcp);
        mcp.clearMessages();
        mcp.setOnline(false);
        MineverseChatAPI.removeMineverseChatOnlinePlayerToMap(mcp);
    }

    void handleNameChange(final @NotNull MineverseChatPlayer mcp, final @NotNull Player eventPlayerInstance) {
        Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&e - Detected Name Change. Old Name:&c " + mcp.getName() + " &eNew Name:&c " + eventPlayerInstance.getName()));
        MineverseChatAPI.removeNameFromMap(mcp.getName());
        mcp.setName(eventPlayerInstance.getName());
        MineverseChatAPI.addNameToMap(mcp);
    }

}
