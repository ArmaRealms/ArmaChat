package mineverse.Aust1n46.chat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.gui.GuiSlot;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Objects;

public class CommandListener implements Listener {
    private final MineverseChat plugin = MineverseChat.getInstance();

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final ConfigurationSection cs = plugin.getConfig().getConfigurationSection("commandspy");
        if (cs == null) {
            plugin.getLogger().warning("No commandspy configuration found in the configuration file.");
            return;
        }
        final boolean wec = cs.getBoolean("worldeditcommands", true);
        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer(event.getPlayer());
        if (!mcp.getPlayer().hasPermission("venturechat.commandspy.override")) {
            for (final MineverseChatPlayer p : MineverseChatAPI.getOnlineMineverseChatPlayers()) {
                if (p.hasCommandSpy()) {
                    if (wec) {
                        p.getPlayer().sendMessage(Format.FormatStringAll(cs.getString("format").replace("{player}", mcp.getName()).replace("{command}", event.getMessage())));
                    } else {
                        if (!(event.getMessage().toLowerCase().startsWith("//"))) {
                            p.getPlayer().sendMessage(Format.FormatStringAll(cs.getString("format").replace("{player}", mcp.getName()).replace("{command}", event.getMessage())));
                        } else {
                            if (!(event.getMessage().toLowerCase().startsWith("//"))) {
                                p.getPlayer().sendMessage(ChatColor.GOLD + mcp.getName() + ": " + event.getMessage());
                            }
                        }
                    }
                }
            }
        }

        final String[] blocked = event.getMessage().split(" ");
        if (mcp.getBlockedCommands().contains(blocked[0])) {
            mcp.getPlayer().sendMessage(LocalizedMessage.BLOCKED_COMMAND.toString().replace("{command}", event.getMessage()));
            event.setCancelled(true);
        }
    }

    // old 1.8 command map
    @EventHandler
    public void onServerCommand(final ServerCommandEvent event) {
        if (Database.isEnabled()) {
            Database.writeVentureChat("N/A", "Console", "Local", "Command_Component", event.getCommand().replace("'", "''"), "Command");
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOW)
    public void InventoryClick(final InventoryClickEvent e) {
        final ItemStack item = e.getCurrentItem();
        if (item == null || !e.getView().getTitle().contains("VentureChat")) {
            return;
        }
        e.setCancelled(true);
        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer((Player) e.getWhoClicked());
        final String playerName = e.getView().getTitle().replace(" GUI", "").replace("VentureChat: ", "");
        final MineverseChatPlayer target = MineverseChatAPI.getMineverseChatPlayer(playerName);
        final ItemStack skull = e.getInventory().getItem(0);
        final SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        final ChatChannel channel = ChatChannel.getChannel(ChatColor.stripColor(Objects.requireNonNull(skullMeta.getLore()).getFirst()).replace("Channel: ", ""));
        final int hash = Integer.parseInt(ChatColor.stripColor(skullMeta.getLore().get(1).replace("Hash: ", "")));
        if (VersionHandler.is1_7()) {
            if (item.getType() == Material.BEDROCK) {
                mcp.getPlayer().closeInventory();
            }
        } else {
            if (item.getType() == Material.BARRIER) {
                mcp.getPlayer().closeInventory();
            }
        }
        for (final GuiSlot g : GuiSlot.getGuiSlots()) {
            if (g.getIcon() == item.getType() && g.getDurability() == item.getDurability() && g.getSlot() == e.getSlot()) {
                String command = g.getCommand().replace("{channel}", channel.getName()).replace("{hash}", hash + "");
                if (target != null) {
                    command = command.replace("{player_name}", target.getName());
                    if (target.isOnline()) {
                        command = Format.FormatStringAll(PlaceholderAPI.setBracketPlaceholders(target.getPlayer(), command));
                    }
                } else {
                    command = command.replace("{player_name}", "Discord_Message");
                }
                mcp.getPlayer().chat(command);
            }
        }
    }
}
