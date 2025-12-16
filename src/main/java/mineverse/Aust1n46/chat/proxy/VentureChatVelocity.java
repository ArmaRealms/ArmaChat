package mineverse.Aust1n46.chat.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent.ForwardResult;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import mineverse.Aust1n46.chat.database.ProxyPlayerData;
import mineverse.Aust1n46.chat.utilities.Format;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * VentureChat Minecraft plugin for Velocity.
 *
 * @author Aust1n46
 */
public class VentureChatVelocity implements VentureChatProxySource {
    private static Configuration velocityConfig;
    private final ProxyServer proxyServer;
    private final ChannelIdentifier channelIdentifier = MinecraftChannelIdentifier.create(VentureChatProxy.PLUGIN_MESSAGING_CHANNEL_NAMESPACE, VentureChatProxy.PLUGIN_MESSAGING_CHANNEL_NAME);
    private final Logger logger;
    @Inject
    @DataDirectory
    private Path dataPath;
    private File velocityPlayerDataDirectory;

    @Inject
    public VentureChatVelocity(final ProxyServer server, final Logger logger) {
        this.proxyServer = server;
        this.logger = logger;
    }

    public static Configuration getVelocityConfig() {
        return velocityConfig;
    }

    @Subscribe
    public void onInitialize(final ProxyInitializeEvent event) {
        proxyServer.getChannelRegistrar().register(channelIdentifier);

        final File dataFolder = dataPath.toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }
        final File config = new File(dataFolder, "velocityconfig.yml");
        try {
            if (!config.exists()) {
                Files.copy(getClass().getClassLoader().getResourceAsStream("velocityconfig.yml"), config.toPath());
            }
            velocityConfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(dataFolder, "velocityconfig.yml"));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        velocityPlayerDataDirectory = new File(dataPath.toAbsolutePath() + "/PlayerData");
        ProxyPlayerData.loadProxyPlayerData(velocityPlayerDataDirectory, this);
    }

    @Subscribe
    public void onShutdown(final ProxyShutdownEvent event) {
        ProxyPlayerData.saveProxyPlayerData(velocityPlayerDataDirectory, this);
    }

    @Subscribe
    public void onPlayerJoin(final ServerPostConnectEvent event) {
        updatePlayerNames();
    }

    @Subscribe
    public void onPlayerQuit(final DisconnectEvent event) {
        // Delay sending plugin message to make sure disconnecting player is truly disconnected.
        proxyServer.getScheduler().buildTask(this, () -> {
                    updatePlayerNames();
                })
                .delay(1, TimeUnit.SECONDS)
                .schedule();
    }

    private void updatePlayerNames() {
        try {
            final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            final DataOutputStream out = new DataOutputStream(outstream);
            out.writeUTF("PlayerNames");
            out.writeInt(proxyServer.getPlayerCount());
            for (final Player player : proxyServer.getAllPlayers()) {
                out.writeUTF(player.getUsername());
            }
            getServers().forEach(send -> {
                if (!send.empty()) {
                    sendPluginMessage(send.name(), outstream.toByteArray());
                }
            });
        } catch (final IllegalStateException e) {
            sendConsoleMessage("Velocity being finicky with DisconnectEvent.");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        final String channelIdentifierId = event.getIdentifier().getId();
        if (!channelIdentifierId.equals(VentureChatProxy.PLUGIN_MESSAGING_CHANNEL_STRING) && !channelIdentifierId.contains("viaversion:")) {
            return;
        }
        // Critical to prevent client from sending or receiving messages
        event.setResult(ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }
        final String serverName = ((ServerConnection) event.getSource()).getServerInfo().getName();
        VentureChatProxy.onPluginMessage(event.getData(), serverName, this);
    }

    @Override
    public void sendPluginMessage(final String serverName, final byte[] data) {
        final Optional<RegisteredServer> server = proxyServer.getServer(serverName);
        if (server.isPresent()) {
            server.get().sendPluginMessage(channelIdentifier, data);
        }
    }

    @Override
    public List<VentureChatProxyServer> getServers() {
        return proxyServer.getAllServers().stream().map(velocityServer -> new VentureChatProxyServer(velocityServer.getServerInfo().getName(), velocityServer.getPlayersConnected().isEmpty())).collect(Collectors.toList());
    }

    @Override
    public VentureChatProxyServer getServer(final String serverName) {
        final RegisteredServer server = proxyServer.getServer(serverName).get();
        return new VentureChatProxyServer(serverName, server.getPlayersConnected().isEmpty());
    }

    @Override
    public void sendConsoleMessage(final String message) {
        logger.info(Format.stripColor(message));
    }

    @Override
    public boolean isOfflineServerAcknowledgementSet() {
        return velocityConfig.getBoolean("offline_server_acknowledgement");
    }
}
