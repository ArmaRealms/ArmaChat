package mineverse.Aust1n46.chat.api;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API class for looking up wrapped {@link MineverseChatPlayer} objects from
 * {@link Player}, {@link UUID}, or {@link String} usernames.
 *
 * @author Aust1n46
 */
public final class MineverseChatAPI {
    private static final Map<UUID, MineverseChatPlayer> playerMap = new HashMap<>();
    private static final Map<String, UUID> namesMap = new HashMap<>();
    private static final Map<UUID, MineverseChatPlayer> onlinePlayerMap = new HashMap<>();
    private static final List<String> networkPlayerNames = new ArrayList<>();

    private static final Map<UUID, SynchronizedMineverseChatPlayer> proxyPlayerMap = new HashMap<>();

    public static List<String> getNetworkPlayerNames() {
        return networkPlayerNames;
    }

    public static void clearNetworkPlayerNames() {
        networkPlayerNames.clear();
    }

    public static void addNetworkPlayerName(final String name) {
        networkPlayerNames.add(name);
    }

    public static void addSynchronizedMineverseChatPlayerToMap(final SynchronizedMineverseChatPlayer smcp) {
        proxyPlayerMap.put(smcp.getUUID(), smcp);
    }

    public static void clearProxyPlayerMap() {
        proxyPlayerMap.clear();
    }

    public static Collection<SynchronizedMineverseChatPlayer> getSynchronizedMineverseChatPlayers() {
        return proxyPlayerMap.values();
    }

    public static void addNameToMap(final MineverseChatPlayer mcp) {
        namesMap.put(mcp.getName(), mcp.getUUID());
    }

    public static void removeNameFromMap(final String name) {
        namesMap.remove(name);
    }

    public static void clearNameMap() {
        namesMap.clear();
    }

    @SuppressWarnings("deprecation")
    public static void addMineverseChatPlayerToMap(final MineverseChatPlayer mcp) {
        playerMap.put(mcp.getUUID(), mcp);
        MineverseChat.players.add(mcp);
    }

    @SuppressWarnings("deprecation")
    public static void clearMineverseChatPlayerMap() {
        playerMap.clear();
        MineverseChat.players.clear();
    }

    public static Collection<MineverseChatPlayer> getMineverseChatPlayers() {
        return playerMap.values();
    }

    @SuppressWarnings("deprecation")
    public static void addMineverseChatOnlinePlayerToMap(final MineverseChatPlayer mcp) {
        onlinePlayerMap.put(mcp.getUUID(), mcp);
        MineverseChat.onlinePlayers.add(mcp);
    }

    @SuppressWarnings("deprecation")
    public static void removeMineverseChatOnlinePlayerToMap(final MineverseChatPlayer mcp) {
        onlinePlayerMap.remove(mcp.getUUID());
        MineverseChat.onlinePlayers.remove(mcp);
    }

    @SuppressWarnings("deprecation")
    public static void clearOnlineMineverseChatPlayerMap() {
        onlinePlayerMap.clear();
        MineverseChat.onlinePlayers.clear();
    }

    public static Collection<MineverseChatPlayer> getOnlineMineverseChatPlayers() {
        return onlinePlayerMap.values();
    }

    /**
     * Get a MineverseChatPlayer wrapper from a Bukkit Player instance.
     *
     * @param player {@link Player} object.
     * @return {@link MineverseChatPlayer}
     */
    public static MineverseChatPlayer getMineverseChatPlayer(final Player player) {
        return getMineverseChatPlayer(player.getUniqueId());
    }

    /**
     * Get a MineverseChatPlayer wrapper from a UUID.
     *
     * @param uuid {@link UUID}.
     * @return {@link MineverseChatPlayer}
     */
    public static MineverseChatPlayer getMineverseChatPlayer(final UUID uuid) {
        return playerMap.get(uuid);
    }

    /**
     * Get a MineverseChatPlayer wrapper from a user name.
     *
     * @param name {@link String}.
     * @return {@link MineverseChatPlayer}
     */
    public static MineverseChatPlayer getMineverseChatPlayer(final String name) {
        return getMineverseChatPlayer(namesMap.get(name));
    }

    /**
     * Get a MineverseChatPlayer wrapper from a Bukkit Player instance. Only checks
     * current online players. Much more efficient!
     *
     * @param player {@link Player} object.
     * @return {@link MineverseChatPlayer}
     */
    public static MineverseChatPlayer getOnlineMineverseChatPlayer(final Player player) {
        return getOnlineMineverseChatPlayer(player.getUniqueId());
    }

    /**
     * Get a MineverseChatPlayer wrapper from a UUID. Only checks current online
     * players. Much more efficient!
     *
     * @param uuid {@link UUID}.
     * @return {@link MineverseChatPlayer}
     */
    public static MineverseChatPlayer getOnlineMineverseChatPlayer(final UUID uuid) {
        return onlinePlayerMap.get(uuid);
    }

    /**
     * Get a MineverseChatPlayer wrapper from a user name. Only checks current
     * online players. Much more efficient!
     *
     * @param name {@link String}.
     * @return {@link MineverseChatPlayer}
     */
    public static MineverseChatPlayer getOnlineMineverseChatPlayer(final String name) {
        return getOnlineMineverseChatPlayer(namesMap.get(name));
    }

    /**
     * Get a SynchronizedMineverseChatPlayer from a UUID.
     *
     * @param uuid {@link UUID}
     * @return {@link SynchronizedMineverseChatPlayer}
     */
    public static SynchronizedMineverseChatPlayer getSynchronizedMineverseChatPlayer(final UUID uuid) {
        return proxyPlayerMap.get(uuid);
    }
}
