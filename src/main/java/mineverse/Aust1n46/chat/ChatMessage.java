package mineverse.Aust1n46.chat;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.kyori.adventure.text.Component;

//This class is used to create ChatMessage objects, which are used to store information about previous text components
//that were sent to the player.  This is a main component in making the message remover work.
public class ChatMessage {
    private final String message;
    private final String coloredMessage;
    private Object component; // Can be WrappedChatComponent (ProtocolLib) or Component (PacketEvents/Adventure)
    private int hash;

    public ChatMessage(WrappedChatComponent component, String message, String coloredMessage, int hash) {
        this.component = component;
        this.message = message;
        this.coloredMessage = coloredMessage;
        this.hash = hash;
    }

    public ChatMessage(Component component, String message, String coloredMessage, int hash) {
        this.component = component;
        this.message = message;
        this.coloredMessage = coloredMessage;
        this.hash = hash;
    }

    public Object getComponentObject() {
        return this.component;
    }

    public WrappedChatComponent getComponent() {
        if (component instanceof WrappedChatComponent) {
            return (WrappedChatComponent) this.component;
        }
        return null;
    }

    public void setComponent(WrappedChatComponent component) {
        this.component = component;
    }

    public void setComponent(Component component) {
        this.component = component;
    }

    public String getMessage() {
        return this.message;
    }

    public String getColoredMessage() {
        return this.coloredMessage;
    }

    public int getHash() {
        return this.hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }
}