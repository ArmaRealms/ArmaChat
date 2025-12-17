package mineverse.Aust1n46.chat.json;

import mineverse.Aust1n46.chat.ClickAction;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.utilities.Format;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public record JsonFormat(String name, int priority, List<JsonAttribute> jsonAttributes) {
    private static final MineverseChat plugin = MineverseChat.getInstance();
    private static HashMap<String, JsonFormat> jsonFormats;

    public static void initialize() {
        jsonFormats = new HashMap<>();
        final ConfigurationSection jsonFormatSection = plugin.getConfig().getConfigurationSection("jsonformatting");
        for (final String jsonFormat : jsonFormatSection.getKeys(false)) {
            final int priority = jsonFormatSection.getInt(jsonFormat + ".priority", 0);
            final List<JsonAttribute> jsonAttributes = new ArrayList<>();
            final ConfigurationSection jsonAttributeSection = jsonFormatSection.getConfigurationSection(jsonFormat + ".json_attributes");
            if (jsonAttributeSection != null) {
                for (final String attribute : jsonAttributeSection.getKeys(false)) {
                    final List<String> hoverText = jsonAttributeSection.getStringList(attribute + ".hover_text");
                    final String clickActionText = jsonAttributeSection.getString(attribute + ".click_action", "none");
                    try {
                        final ClickAction clickAction = ClickAction.valueOf(clickActionText.toUpperCase());
                        final String clickText = jsonAttributeSection.getString(attribute + ".click_text", "");
                        jsonAttributes.add(new JsonAttribute(attribute, hoverText, clickAction, clickText));
                    } catch (final IllegalArgumentException | NullPointerException exception) {
                        plugin.getServer().getConsoleSender()
                                .sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Illegal click_action: " + clickActionText + " in jsonFormat: " + jsonFormat));
                    }
                }
            }
            jsonFormats.put(jsonFormat.toLowerCase(), new JsonFormat(jsonFormat, priority, jsonAttributes));
        }
    }

    @Contract(pure = true)
    public static @NotNull Collection<JsonFormat> getJsonFormats() {
        return jsonFormats.values();
    }

    public static JsonFormat getJsonFormat(@NotNull final String name) {
        return jsonFormats.get(name.toLowerCase());
    }
}
