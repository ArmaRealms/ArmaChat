package mineverse.Aust1n46.chat.hooks;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.hooks.listeners.PacketEventsLegacyChat;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PacketHook implementation using PacketEvents.
 */
public class PacketEventsHook implements PacketHook {
    
    private final MineverseChat plugin;
    private PacketEventsLegacyChat packetListener;
    
    public PacketEventsHook(MineverseChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getName() {
        return "PacketEvents";
    }
    
    @Override
    public void initialize() {
        try {
            if (VersionHandler.isUnder_1_19()) {
                packetListener = new PacketEventsLegacyChat();
                PacketEvents.getAPI().getEventManager().registerListener(packetListener, PacketListenerPriority.MONITOR);
                plugin.getLogger().info("PacketEvents hook initialized successfully");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize PacketEvents hook: " + e.getMessage());
        }
    }
    
    @Override
    public void shutdown() {
        if (packetListener != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        }
    }
    
    @Override
    public boolean sendChatPacket(@NotNull Player player, @NotNull String json) {
        try {
            Component component = GsonComponentSerializer.gson().deserialize(json);
            
            if (VersionHandler.isUnder_1_19()) {
                // For versions 1.7 - 1.18
                WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(component);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            } else {
                // For versions 1.19+
                WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(component, false);
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send chat packet via PacketEvents: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Class.forName("com.github.retrooper.packetevents.PacketEvents");
            return PacketEvents.getAPI() != null && PacketEvents.getAPI().isLoaded();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
