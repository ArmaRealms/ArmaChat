package mineverse.Aust1n46.chat.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.listeners.PacketListenerLegacyChat;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PacketHook implementation using ProtocolLib.
 */
public class ProtocolLibHook implements PacketHook {
    
    private final MineverseChat plugin;
    private ProtocolManager protocolManager;
    private PacketListenerLegacyChat packetListener;
    
    public ProtocolLibHook(MineverseChat plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getName() {
        return "ProtocolLib";
    }
    
    @Override
    public void initialize() {
        try {
            protocolManager = ProtocolLibrary.getProtocolManager();
            if (VersionHandler.isUnder_1_19()) {
                packetListener = new PacketListenerLegacyChat();
                protocolManager.addPacketListener(packetListener);
                plugin.getLogger().info("ProtocolLib hook initialized successfully");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize ProtocolLib hook: " + e.getMessage());
        }
    }
    
    @Override
    public void shutdown() {
        if (protocolManager != null && packetListener != null) {
            protocolManager.removePacketListener(packetListener);
        }
    }
    
    @Override
    public boolean sendChatPacket(@NotNull Player player, @NotNull String json) {
        if (protocolManager == null) {
            return false;
        }
        
        try {
            PacketContainer packet = createChatPacket(json);
            protocolManager.sendServerPacket(player, packet);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send chat packet via ProtocolLib: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Class.forName("com.comphenix.protocol.ProtocolLibrary");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private PacketContainer createChatPacket(String json) {
        WrappedChatComponent component = WrappedChatComponent.fromJson(json);
        PacketContainer container;
        
        if (VersionHandler.is1_7()) {
            container = new PacketContainer(PacketType.Play.Server.CHAT);
            container.getModifier().writeDefaults();
            container.getStrings().write(0, component.getJson());
            container.getBooleans().write(0, false);
        } else if (VersionHandler.isUnder_1_19()) { // 1.8 -> 1.18
            container = new PacketContainer(PacketType.Play.Server.CHAT);
            container.getModifier().writeDefaults();
            container.getChatComponents().write(0, component);
        } else { // 1.19+
            container = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
            container.getStrings().write(0, component.getJson());
            container.getIntegers().write(0, 1);
        }
        
        return container;
    }
}
