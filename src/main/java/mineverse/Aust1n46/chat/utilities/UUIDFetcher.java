package mineverse.Aust1n46.chat.utilities;

import com.google.common.collect.ImmutableList;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.proxy.VentureChatProxySource;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

public class UUIDFetcher implements Callable<Map<String, UUID>> {
    private static final double PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;

    public UUIDFetcher(final List<String> names, final boolean rateLimiting) {
        this.names = ImmutableList.copyOf(names);
        this.rateLimiting = rateLimiting;
    }

    public UUIDFetcher(final List<String> names) {
        this(names, true);
    }

    private static void writeBody(final HttpURLConnection connection, final String body) throws Exception {
        final OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        final URL url = URI.create(PROFILE_URL).toURL();
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(final String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    public static byte[] toBytes(final UUID uuid) {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(final byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        final ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        final long mostSignificant = byteBuffer.getLong();
        final long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static UUID getUUIDOf(final String name) throws Exception {
        return new UUIDFetcher(Collections.singletonList(name)).call().get(name);
    }

    /**
     * Returns whether the passed UUID is a v3 UUID. Offline UUIDs are v3, online are v4.
     *
     * @param uuid the UUID to check
     * @return whether the UUID is a v3 UUID & thus is offline
     */
    public static boolean uuidIsOffline(final UUID uuid) {
        return uuid.version() == 3;
    }

    public static boolean shouldSkipOfflineUUID(final UUID uuid) {
        return (uuidIsOffline(uuid) && !MineverseChat.getInstance().getConfig().getBoolean("offline_server_acknowledgement", false));
    }

    public static boolean shouldSkipOfflineUUIDProxy(final UUID uuid, final VentureChatProxySource source) {
        return (uuidIsOffline(uuid) && !source.isOfflineServerAcknowledgementSet());
    }

    public static void checkOfflineUUIDWarning(final UUID uuid) {
        if (shouldSkipOfflineUUID(uuid)) {
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - Detected Offline UUID!"));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - If you are using BungeeCord, make sure you have properly setup IP Forwarding."));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - https://www.spigotmc.org/wiki/bungeecord-ip-forwarding/"));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - You can access this wiki page from the log file or just Google it."));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - If you're running a \"cracked\" server, player data might not be stored properly, and thus, you are on your own."));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - If you run your server in offline mode, you will probably lose your player data when switching to online mode!"));
            Bukkit.getConsoleSender().sendMessage(Format.FormatStringAll("&8[&eVentureChat&8]&c - No player data will be saved in offline mode unless you set the \"cracked\" server acknowledgement in the config!"));
        }
    }

    public static void checkOfflineUUIDWarningProxy(final UUID uuid, final VentureChatProxySource source) {
        if (shouldSkipOfflineUUIDProxy(uuid, source)) {
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - Detected Offline UUID!");
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - If you are using BungeeCord, make sure you have properly setup IP Forwarding.");
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - https://www.spigotmc.org/wiki/bungeecord-ip-forwarding/");
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - You can access this wiki page from the log file or just Google it.");
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - If you're running a \"cracked\" server, player data might not be stored properly, and thus, you are on your own.");
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - If you run your server in offline mode, you will probably lose your player data when switching to online mode!");
            source.sendConsoleMessage("&8[&eVentureChat&8]&c - No player data will be saved in offline mode unless you set the \"cracked\" server acknowledgement in the config!");
        }
    }

    public Map<String, UUID> call() throws Exception {
        final Map<String, UUID> uuidMap = new HashMap<String, UUID>();
        final int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
        for (int i = 0; i < requests; i++) {
            final HttpURLConnection connection = createConnection();
            final String body = JSONArray.toJSONString(names.subList(i * 100, Math.min((i + 1) * 100, names.size())));
            writeBody(connection, body);
            final JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
            for (final Object profile : array) {
                final JSONObject jsonProfile = (JSONObject) profile;
                final String id = (String) jsonProfile.get("id");
                final String name = (String) jsonProfile.get("name");
                final UUID uuid = UUIDFetcher.getUUID(id);
                uuidMap.put(name, uuid);
            }
            if (rateLimiting && i != requests - 1) {
                Thread.sleep(100L);
            }
        }
        return uuidMap;
    }
}
