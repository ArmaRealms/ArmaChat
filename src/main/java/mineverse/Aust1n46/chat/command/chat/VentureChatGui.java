package mineverse.Aust1n46.chat.command.chat;

import me.clip.placeholderapi.PlaceholderAPI;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.channel.ChatChannel;
import mineverse.Aust1n46.chat.gui.GuiSlot;
import mineverse.Aust1n46.chat.localization.LocalizedMessage;
import mineverse.Aust1n46.chat.utilities.Format;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class VentureChatGui extends Command {
    private final MineverseChat plugin = MineverseChat.getInstance();

    public VentureChatGui() {
        super("venturechatgui");
    }

    @Override
    public boolean execute(final @NonNull CommandSender sender, final @NonNull String command, final String @NonNull [] args) {
        if (!(sender instanceof Player)) {
            Bukkit.getServer().getConsoleSender().sendMessage(LocalizedMessage.COMMAND_MUST_BE_RUN_BY_PLAYER.toString());
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(LocalizedMessage.COMMAND_INVALID_ARGUMENTS.toString().replace("{command}", "/venturechatgui").replace("{args}", "[player] [channel] [hashcode]"));
            return true;
        }
        final MineverseChatPlayer mcp = MineverseChatAPI.getOnlineMineverseChatPlayer((Player) sender);
        if (mcp.getPlayer().hasPermission("venturechat.gui")) {
            final MineverseChatPlayer target = MineverseChatAPI.getMineverseChatPlayer(args[0]);
            if (target == null && !args[0].equals("Discord")) {
                mcp.getPlayer().sendMessage(LocalizedMessage.PLAYER_OFFLINE.toString().replace("{args}", args[0]));
                return true;
            }
            if (ChatChannel.isChannel(args[1])) {
                final ChatChannel channel = ChatChannel.getChannel(args[1]);
                final int hash;
                try {
                    hash = Integer.parseInt(args[2]);
                } catch (final Exception e) {
                    sender.sendMessage(LocalizedMessage.INVALID_HASH.toString());
                    return true;
                }
                if (args[0].equals("Discord")) {
                    this.openInventoryDiscord(mcp, channel, hash);
                    return true;
                }
                if (target != null) {
                    this.openInventory(mcp, target, channel, hash);
                }
                return true;
            }
            mcp.getPlayer().sendMessage(LocalizedMessage.INVALID_CHANNEL.toString().replace("{args}", args[1]));
            return true;
        }
        mcp.getPlayer().sendMessage(LocalizedMessage.COMMAND_NO_PERMISSION.toString());
        return true;
    }

    @SuppressWarnings("deprecation")
    private void openInventory(final MineverseChatPlayer mcp, final MineverseChatPlayer target, final ChatChannel channel, final int hash) {
        final Inventory inv = Bukkit.createInventory(null, this.getSlots(), "VentureChat: " + target.getName() + " GUI");
        final ItemStack close = new ItemStack(Material.BARRIER);
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        final ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "Close GUI");
        close.setItemMeta(closeMeta);

        final SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(target.getName());
        skullMeta.setDisplayName(ChatColor.AQUA + target.getName());
        final List<String> skullLore = new ArrayList<String>();
        skullLore.add(ChatColor.GRAY + "Channel: " + channel.getColor() + channel.getName());
        skullLore.add(ChatColor.GRAY + "Hash: " + channel.getColor() + hash);
        skullMeta.setLore(skullLore);
        skull.setItemMeta(skullMeta);
        skull.setDurability((short) 3);
        inv.setItem(0, skull);

        for (final GuiSlot g : GuiSlot.getGuiSlots()) {
            if (!g.hasPermission() || mcp.getPlayer().hasPermission(g.permission())) {
                if (this.checkSlot(g.slot())) {
                    MineverseChat.getInstance().getServer().getConsoleSender()
                            .sendMessage(Format.FormatStringAll("&cGUI: " + g.name() + " has invalid slot: " + g.slot() + "!"));
                    continue;
                }
                final ItemStack gStack = new ItemStack(g.icon());
                gStack.setDurability((short) g.durability());
                final ItemMeta gMeta = gStack.getItemMeta();
                String displayName = g.text().replace("{player_name}", target.getName()).replace("{channel}", channel.getName()).replace("{hash}", hash + "");
                if (target.isOnline()) {
                    displayName = PlaceholderAPI.setBracketPlaceholders(target.getPlayer(), displayName);
                }
                gMeta.setDisplayName(Format.FormatStringAll(displayName));
                final List<String> gLore = new ArrayList<String>();
                gMeta.setLore(gLore);
                gStack.setItemMeta(gMeta);
                inv.setItem(g.slot(), gStack);
            }
        }

        inv.setItem(8, close);
        mcp.getPlayer().openInventory(inv);
    }

    @SuppressWarnings("deprecation")
    private void openInventoryDiscord(final MineverseChatPlayer mcp, final ChatChannel channel, final int hash) {
        final Inventory inv = Bukkit.createInventory(null, this.getSlots(), "VentureChat: Discord_Message GUI");
        final ItemStack close = new ItemStack(Material.BARRIER);
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);

        final ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "Close GUI");
        close.setItemMeta(closeMeta);

        final SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner("Scarsz");
        skullMeta.setDisplayName(ChatColor.AQUA + "Discord_Message");
        final List<String> skullLore = new ArrayList<>();
        skullLore.add(ChatColor.GRAY + "Channel: " + channel.getColor() + channel.getName());
        skullLore.add(ChatColor.GRAY + "Hash: " + channel.getColor() + hash);
        skullMeta.setLore(skullLore);
        skull.setItemMeta(skullMeta);
        skull.setDurability((short) 3);
        inv.setItem(0, skull);

        for (final GuiSlot g : GuiSlot.getGuiSlots()) {
            if (!g.hasPermission() || mcp.getPlayer().hasPermission(g.permission())) {
                if (this.checkSlot(g.slot())) {
                    MineverseChat.getInstance().getServer().getConsoleSender()
                            .sendMessage(Format.FormatStringAll("&cGUI: " + g.name() + " has invalid slot: " + g.slot() + "!"));
                    continue;
                }
                final ItemStack gStack = new ItemStack(g.icon());
                gStack.setDurability((short) g.durability());
                final ItemMeta gMeta = gStack.getItemMeta();
                final String displayName = g.text().replace("{player_name}", "Discord_Message").replace("{channel}", channel.getName()).replace("{hash}", hash + "");
                gMeta.setDisplayName(Format.FormatStringAll(displayName));
                final List<String> gLore = new ArrayList<>();
                gMeta.setLore(gLore);
                gStack.setItemMeta(gMeta);
                inv.setItem(g.slot(), gStack);
            }
        }

        inv.setItem(8, close);
        mcp.getPlayer().openInventory(inv);
    }

    private boolean checkSlot(final int slot) {
        return slot == 0 || slot == 8;
    }

    private int getSlots() {
        final int rows = plugin.getConfig().getInt("guirows", 1);
        if (rows == 2)
            return 18;
        if (rows == 3)
            return 27;
        if (rows == 4)
            return 36;
        if (rows == 5)
            return 45;
        if (rows == 6)
            return 54;
        return 9;
    }
}
