package mineverse.Aust1n46.chat.alias;

import mineverse.Aust1n46.chat.MineverseChat;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Alias {
    private static final MineverseChat plugin = MineverseChat.getInstance();
    private static List<Alias> aliases;

    private final String name;
    private final int arguments;
    private final List<String> components;
    private final String permission;

    public Alias(String name, int arguments, List<String> components, String permission) {
        this.name = name;
        this.arguments = arguments;
        this.components = components;
        this.permission = "venturechat." + permission;
    }

    public static void initialize() {
        aliases = new ArrayList<Alias>();
        ConfigurationSection cs = plugin.getConfig().getConfigurationSection("alias");
        for (String key : cs.getKeys(false)) {
            String name = key;
            int arguments = cs.getInt(key + ".arguments", 0);
            List<String> components = cs.getStringList(key + ".components");
            String permissions = cs.getString(key + ".permissions", "None");
            aliases.add(new Alias(name, arguments, components, permissions));
        }
    }

    public static List<Alias> getAliases() {
        return aliases;
    }

    public String getName() {
        return name;
    }

    public int getArguments() {
        return arguments;
    }

    public List<String> getComponents() {
        return components;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return !permission.equalsIgnoreCase("venturechat.none");
    }
}
