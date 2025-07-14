package mineverse.Aust1n46.chat.alias;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public record Alias(String name, int arguments, List<String> components, String permission) {
    private static final MineverseChat plugin = MineverseChat.getInstance();
    private static List<Alias> aliases;

    public Alias(final String name, final int arguments, final List<String> components, final String permission) {
        this.name = name;
        this.arguments = arguments;
        this.components = components;
        this.permission = "venturechat." + permission;
    }

    public static void initialize() {
        aliases = new ArrayList<>();
        final ConfigurationSection cs = plugin.getConfig().getConfigurationSection("alias");
        if (cs == null) {
            plugin.getLogger().warning("No aliases found in the configuration file.");
            return;
        }
        for (final String key : cs.getKeys(false)) {
            final int arguments = cs.getInt(key + ".arguments", 0);
            final List<String> components = cs.getStringList(key + ".components");
            final String permissions = cs.getString(key + ".permissions", "None");
            aliases.add(new Alias(key, arguments, components, permissions));
        }
    }

    public static List<Alias> getAliases() {
        return aliases;
    }

    public boolean hasPermission() {
        return !permission.equalsIgnoreCase("venturechat.none");
    }
}
