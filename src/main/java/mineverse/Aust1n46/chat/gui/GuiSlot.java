package mineverse.Aust1n46.chat.gui;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public record GuiSlot(String name, Material icon, int durability, String text, String permission, String command,
                      int slot) {
    private static final MineverseChat plugin = MineverseChat.getInstance();
    private static final List<GuiSlot> guiSlots = new ArrayList<>();

    public GuiSlot(final String name, final String icon, final int durability, final String text, final String permission, final String command, final int slot) {
        this(name, Material.valueOf(icon.toUpperCase()), durability, text, "venturechat." + permission, command, slot);
    }

    public static void initialize() {
        guiSlots.clear();
        final ConfigurationSection cs = plugin.getConfig().getConfigurationSection("venturegui");
        if (cs == null) {
            return;
        }
        for (final String key : cs.getKeys(false)) {
            final String icon = cs.getString(key + ".icon");
            final int durability = cs.getInt(key + ".durability");
            final String text = cs.getString(key + ".text");
            final String permission = cs.getString(key + ".permission");
            final String command = cs.getString(key + ".command");
            final int slot = cs.getInt(key + ".slot");
            if (icon == null || text == null || permission == null || command == null) {
                continue;
            }
            guiSlots.add(new GuiSlot(key, icon, durability, text, permission, command, slot));
        }
    }

    public static List<GuiSlot> getGuiSlots() {
        return guiSlots;
    }

    public boolean hasPermission() {
        return !permission.equalsIgnoreCase("venturechat.none");
    }
}
