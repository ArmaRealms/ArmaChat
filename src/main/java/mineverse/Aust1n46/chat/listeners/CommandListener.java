package mineverse.Aust1n46.chat.listeners;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.alias.Alias;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.database.Database;
import mineverse.Aust1n46.chat.gui.GuiSlot;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import mineverse.Aust1n46.chat.versions.VersionHandler;
import org.bukkit.Bukkit;
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

import java.io.FileNotFoundException;

public class CommandListener implements Listener {
    private final MineverseChat plugin = MineverseChat.getInstance();

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) throws FileNotFoundException {
        if (event.getPlayer() == null) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Event.getPlayer() returned null in PlayerCommandPreprocessEvent"));
            return;
        }
        final ConfigurationSection cs = plugin.getConfig().getConfigurationSection("commandspy");
        final Boolean wec = cs.getBoolean("worldeditcommands", true);
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
            return;
        }

        final String message = event.getMessage();

        if (Database.isEnabled()) {
            Database.writeVentureChat(mcp.getUUID().toString(), mcp.getName(), "Local", "Command_Component", event.getMessage().replace("'", "''"), "Command");
        }

        for (final Alias a : Alias.getAliases()) {
            if (message.toLowerCase().substring(1).split(" ")[0].equals(a.name().toLowerCase())) {
                for (String s : a.components()) {
                    if (!mcp.getPlayer().hasPermission(a.permission()) && a.hasPermission()) {
                        mcp.getPlayer().sendMessage(ChatColor.RED + "You do not have permission for this alias.");
                        event.setCancelled(true);
                        return;
                    }
                    int num = 1;
                    if (message.length() < a.name().length() + 2 || a.arguments() == 0)
                        num = 0;
                    int arg = 0;
                    if (message.substring(a.name().length() + 1 + num).length() == 0)
                        arg = 1;
                    final String[] args = message.substring(a.name().length() + 1 + num).split(" ");
                    String send = "";
                    if (args.length - arg < a.arguments()) {
                        String keyword = "arguments.";
                        if (a.arguments() == 1)
                            keyword = "argument.";
                        mcp.getPlayer().sendMessage(ChatColor.RED + "Invalid arguments for this alias, enter at least " + a.arguments() + " " + keyword);
                        event.setCancelled(true);
                        return;
                    }
                    for (int b = 0; b < args.length; b++) {
                        send += " " + args[b];
                    }
                    if (send.length() > 0)
                        send = send.substring(1);
                    s = Format.FormatStringAll(s);
                    if (mcp.getPlayer().hasPermission("venturechat.color.legacy")) {
                        send = Format.FormatStringLegacyColor(send);
                    }
                    if (mcp.getPlayer().hasPermission("venturechat.color")) {
                        send = Format.FormatStringColor(send);
                    }
                    if (mcp.getPlayer().hasPermission("venturechat.format")) {
                        send = Format.FormatString(send);
                    }
                    if (s.startsWith("Command:")) {
                        mcp.getPlayer().chat(s.substring(9).replace("$", send));
                        event.setCancelled(true);
                    }
                    if (s.startsWith("Message:")) {
                        mcp.getPlayer().sendMessage(s.substring(9).replace("$", send));
                        event.setCancelled(true);
                    }
                    if (s.startsWith("Broadcast:")) {
                        Format.broadcastToServer(s.substring(11).replace("$", send));
                        event.setCancelled(true);
                    }
                }
            }
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
        final ChatChannel channel = ChatChannel.getChannel(ChatColor.stripColor(skullMeta.getLore().get(0)).replace("Channel: ", ""));
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
