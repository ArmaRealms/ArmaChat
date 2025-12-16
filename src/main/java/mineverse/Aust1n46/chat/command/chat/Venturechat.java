package mineverse.Aust1n46.chat.command.chat;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.localization.InternalMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Venturechat extends Command {
    private final MineverseChat plugin = MineverseChat.getInstance();

    public Venturechat() {
        super("venturechat");
    }

    @Override
    public boolean execute(final CommandSender sender, final String command, final String[] args) {
        sender.sendMessage(InternalMessage.VENTURECHAT_VERSION.toString()
                .replace("{version}", plugin.getDescription().getVersion()));
        sender.sendMessage(InternalMessage.VENTURECHAT_AUTHOR.toString());
        return true;
    }
}
