package mineverse.Aust1n46.chat.hooks;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for packet handling implementations.
 * Supports ProtocolLib, PacketEvents, or NMS with Paper UserDev.
 */
public interface PacketHook {
    
    /**
     * Gets the name of this packet hook implementation.
     * 
     * @return The hook name (e.g., "ProtocolLib", "PacketEvents", "NMS")
     */
    @NotNull String getName();
    
    /**
     * Initializes the packet hook and registers packet listeners.
     * Called during plugin startup.
     */
    void initialize();
    
    /**
     * Cleans up resources and unregisters packet listeners.
     * Called during plugin shutdown.
     */
    void shutdown();
    
    /**
     * Sends a chat packet to a player.
     * 
     * @param player The player to send the packet to
     * @param json The JSON string representing the chat message
     * @return true if the packet was sent successfully
     */
    boolean sendChatPacket(@NotNull Player player, @NotNull String json);
    
    /**
     * Checks if this hook is available and can be used.
     * 
     * @return true if the hook's dependencies are present
     */
    boolean isAvailable();
}
