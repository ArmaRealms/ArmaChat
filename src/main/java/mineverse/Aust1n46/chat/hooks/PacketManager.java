package mineverse.Aust1n46.chat.hooks;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for packet hook implementations.
 * Automatically detects and initializes the best available packet handling library.
 */
public class PacketManager {
    
    private static PacketHook activeHook = null;
    private static final MineverseChat plugin = MineverseChat.getInstance();
    
    private PacketManager() {
        // Utility class
    }
    
    /**
     * Initializes the packet manager by detecting and loading the best available packet hook.
     * Priority: ProtocolLib > PacketEvents
     */
    public static void initialize() {
        // Try ProtocolLib first (existing implementation)
        PacketHook protocolLibHook = new ProtocolLibHook(plugin);
        if (protocolLibHook.isAvailable()) {
            activeHook = protocolLibHook;
            activeHook.initialize();
            plugin.getLogger().info("Using ProtocolLib for packet handling");
            return;
        }
        
        // Try PacketEvents as fallback
        PacketHook packetEventsHook = new PacketEventsHook(plugin);
        if (packetEventsHook.isAvailable()) {
            activeHook = packetEventsHook;
            activeHook.initialize();
            plugin.getLogger().info("Using PacketEvents for packet handling");
            return;
        }
        
        // No packet hook available
        plugin.getLogger().warning("No packet handling library found! Please install ProtocolLib or PacketEvents.");
        plugin.getLogger().warning("Some features may not work correctly.");
    }
    
    /**
     * Shuts down the active packet hook.
     */
    public static void shutdown() {
        if (activeHook != null) {
            activeHook.shutdown();
            activeHook = null;
        }
    }
    
    /**
     * Sends a chat packet to a player.
     * 
     * @param player The player to send the packet to
     * @param json The JSON string representing the chat message
     * @return true if the packet was sent successfully
     */
    public static boolean sendChatPacket(@NotNull Player player, @NotNull String json) {
        if (activeHook == null) {
            return false;
        }
        return activeHook.sendChatPacket(player, json);
    }
    
    /**
     * Gets the active packet hook.
     * 
     * @return The active packet hook, or null if none is available
     */
    @Nullable
    public static PacketHook getActiveHook() {
        return activeHook;
    }
    
    /**
     * Checks if a packet hook is available.
     * 
     * @return true if a packet hook is active
     */
    public static boolean isAvailable() {
        return activeHook != null;
    }
    
    /**
     * Gets the name of the active packet hook.
     * 
     * @return The hook name, or "None" if no hook is available
     */
    @NotNull
    public static String getActiveHookName() {
        return activeHook != null ? activeHook.getName() : "None";
    }
}
