package mineverse.Aust1n46.chat.hooks.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import mineverse.Aust1n46.chat.ChatMessage;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

/**
 * PacketEvents listener for legacy chat packets (pre-1.19).
 */
public class PacketEventsLegacyChat implements PacketListener {
    
    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        // Only handle chat packets
        if (event.getPacketType() != PacketType.Play.Server.CHAT) {
            return;
        }
        
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(player);
        if (mcp == null) {
            return;
        }
        
        try {
            WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(event);
            Component component = packet.getMessage();
            
            if (component == null) {
                return;
            }
            
            // Convert component to plain text and colored text
            String message = PlainTextComponentSerializer.plainText().serialize(component);
            String coloredMessage = LegacyComponentSerializer.legacySection().serialize(component);
            String json = GsonComponentSerializer.gson().serialize(component);
            
            int hash = message.hashCode();
            mcp.addMessage(new ChatMessage(component, message, coloredMessage, hash));
        } catch (Exception e) {
            // Silently ignore any errors in packet processing
        }
    }
}
