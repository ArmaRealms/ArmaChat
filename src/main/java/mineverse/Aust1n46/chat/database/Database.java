package mineverse.Aust1n46.chat.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.utilities.Format;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Initializes and handles writing to the chat logging database.
 */
public class Database {
    private static HikariDataSource dataSource = null;

    public static void initializeMySQL() {
        try {
            final ConfigurationSection mysqlConfig = MineverseChat.getInstance().getConfig().getConfigurationSection("mysql");
            if (mysqlConfig.getBoolean("enabled", false)) {
                final String host = mysqlConfig.getString("host");
                final int port = mysqlConfig.getInt("port");
                final String database = mysqlConfig.getString("database");
                final String user = mysqlConfig.getString("user");
                final String password = mysqlConfig.getString("password");

                final HikariConfig config = new HikariConfig();
                final String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?autoReconnect=true&useSSL=false", host, port, database);
                config.setJdbcUrl(jdbcUrl);
                config.setUsername(user);
                config.setPassword(password);
                config.addDataSourceProperty("cachePrepStmts", "true");
                config.addDataSourceProperty("prepStmtCacheSize", "250");
                config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                dataSource = new HikariDataSource(config);
                final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS VentureChat "
                        + "(ID SERIAL PRIMARY KEY, ChatTime TEXT, UUID TEXT, Name TEXT, "
                        + "Server TEXT, Channel TEXT, Text TEXT, Type TEXT)";
                final Connection conn = dataSource.getConnection();
                final PreparedStatement statement = conn.prepareStatement(SQL_CREATE_TABLE);
                statement.executeUpdate();
            }
        } catch (final Exception exception) {
            Bukkit.getConsoleSender().sendMessage(
                    Format.FormatStringAll("&8[&eVentureChat&8]&c - Database could not be loaded. Is it running?"));
        }
    }

    public static boolean isEnabled() {
        return dataSource != null;
    }

    public static void writeVentureChat(final String uuid, final String name, final String server, final String channel, final String text,
                                        final String type) {
        final MineverseChat plugin = MineverseChat.getInstance();
        final Calendar currentDate = Calendar.getInstance();
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String date = formatter.format(currentDate.getTime());
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (final Connection conn = dataSource.getConnection();
                 final PreparedStatement statement = conn.prepareStatement(
                         "INSERT INTO VentureChat " + "(ChatTime, UUID, Name, Server, Channel, Text, Type) "
                                 + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                statement.setString(1, date);
                statement.setString(2, uuid);
                statement.setString(3, name);
                statement.setString(4, server);
                statement.setString(5, channel);
                statement.setString(6, text);
                statement.setString(7, type);
                statement.executeUpdate();
            } catch (final SQLException error) {
                error.printStackTrace();
            }
        });
    }
}
