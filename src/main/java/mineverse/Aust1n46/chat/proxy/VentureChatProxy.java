package mineverse.Aust1n46.chat.proxy;

import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.SynchronizedMineverseChatPlayer;
import mineverse.Aust1n46.chat.command.mute.MuteContainer;
import mineverse.Aust1n46.chat.database.TemporaryDataInstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class VentureChatProxy {
    public static String PLUGIN_MESSAGING_CHANNEL_NAMESPACE = "venturechat";
    public static String PLUGIN_MESSAGING_CHANNEL_NAME = "data";
    public static String PLUGIN_MESSAGING_CHANNEL_STRING = "venturechat:data";

    public static void onPluginMessage(final byte[] data, final String serverName, final VentureChatProxySource source) {
        final ByteArrayInputStream instream = new ByteArrayInputStream(data);
        final DataInputStream in = new DataInputStream(instream);
        try {
            final String subchannel = in.readUTF();
            //System.out.println(subchannel);
            final ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(outstream);
            if (subchannel.equals("Chat")) {
                final String chatchannel = in.readUTF();
                final String senderName = in.readUTF();
                final String senderUUID = in.readUTF();
                final boolean bungeeToggle = in.readBoolean();
                final int hash = in.readInt();
                final String format = in.readUTF();
                final String chat = in.readUTF();
                final String json = in.readUTF();
                final String primaryGroup = in.readUTF();
                final String nickname = in.readUTF();
                out.writeUTF("Chat");
                out.writeUTF(serverName);
                out.writeUTF(chatchannel);
                out.writeUTF(senderName);
                out.writeUTF(senderUUID);
                out.writeInt(hash);
                out.writeUTF(format);
                out.writeUTF(chat);
                out.writeUTF(json);
                out.writeUTF(primaryGroup);
                out.writeUTF(nickname);
                for (final VentureChatProxyServer send : source.getServers()) {
                    if (!send.empty()) {
                        if (!bungeeToggle && !send.name().equalsIgnoreCase(serverName)) {
                            continue;
                        }
                        source.sendPluginMessage(send.name(), outstream.toByteArray());
                    }
                }
            }
            if (subchannel.equals("DiscordSRV")) {
                final String chatchannel = in.readUTF();
                final String message = in.readUTF();
                out.writeUTF("DiscordSRV");
                out.writeUTF(chatchannel);
                out.writeUTF(message);
                source.getServers().forEach(send -> {
                    if (!send.empty()) {
                        source.sendPluginMessage(send.name(), outstream.toByteArray());
                    }
                });
            }
            if (subchannel.equals("Chwho")) {
                final String identifier = in.readUTF();
                if (identifier.equals("Get")) {
                    final String server = serverName;
                    final String sender = in.readUTF();
                    final String channel = in.readUTF();
                    final SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(sender));
                    if (smcp == null) {
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c Synchronized player instance is null!  This shouldn't be!");
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c You probably have an issue with your player data saving and/or your login data sync!");
                        return;
                    }
                    smcp.clearMessagePackets();
                    smcp.clearMessageData();
                    out.writeUTF("Chwho");
                    out.writeUTF("Get");
                    out.writeUTF(server);
                    out.writeUTF(sender);
                    out.writeUTF(channel);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            source.sendPluginMessage(send.name(), outstream.toByteArray());
                        }
                    });
                }
                if (identifier.equals("Receive")) {
                    final String server = in.readUTF();
                    final String sender = in.readUTF();
                    final String channel = in.readUTF();
                    final SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(sender));
                    if (smcp == null) {
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c Synchronized player instance is null!  This shouldn't be!");
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c You probably have an issue with your player data saving and/or your login data sync!");
                        return;
                    }
                    smcp.incrementMessagePackets();
                    final int players = in.readInt();
                    for (int a = 0; a < players; a++) {
                        smcp.addData(in.readUTF());
                    }
                    final AtomicInteger servers = new AtomicInteger(0);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            servers.incrementAndGet();
                        }
                    });
                    if (smcp.getMessagePackets() >= servers.get()) {
                        smcp.clearMessagePackets();
                        out.writeUTF("Chwho");
                        out.writeUTF("Receive");
                        out.writeUTF(sender);
                        out.writeUTF(channel);
                        out.writeInt(smcp.getMessageData().size());
                        for (final String s : smcp.getMessageData()) {
                            out.writeUTF(s);
                        }
                        smcp.clearMessageData();
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
            }
            if (subchannel.equals("RemoveMessage")) {
                final String hash = in.readUTF();
                out.writeUTF("RemoveMessage");
                out.writeUTF(hash);
                source.getServers().forEach(send -> {
                    if (!send.empty()) {
                        source.sendPluginMessage(send.name(), outstream.toByteArray());
                    }
                });
            }
            if (subchannel.equals("Ignore")) {
                final String identifier = in.readUTF();
                if (identifier.equals("Send")) {
                    final String server = serverName;
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    final SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(sender));
                    if (smcp == null) {
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c Synchronized player instance is null!  This shouldn't be!");
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c You probably have an issue with your player data saving and/or your login data sync!");
                        return;
                    }
                    smcp.clearMessagePackets();
                    out.writeUTF("Ignore");
                    out.writeUTF("Send");
                    out.writeUTF(server);
                    out.writeUTF(player);
                    out.writeUTF(sender);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            source.sendPluginMessage(send.name(), outstream.toByteArray());
                        }
                    });
                }
                if (identifier.equals("Offline")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    final SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(sender));
                    if (smcp == null) {
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c Synchronized player instance is null!  This shouldn't be!");
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c You probably have an issue with your player data saving and/or your login data sync!");
                        return;
                    }
                    smcp.incrementMessagePackets();
                    final AtomicInteger servers = new AtomicInteger(0);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            servers.incrementAndGet();
                        }
                    });
                    if (smcp.getMessagePackets() >= servers.get()) {
                        smcp.clearMessagePackets();
                        out.writeUTF("Ignore");
                        out.writeUTF("Offline");
                        out.writeUTF(player);
                        out.writeUTF(sender);
                        if (!source.getServer(server).empty()) {
                            source.sendPluginMessage(server, outstream.toByteArray());
                        }
                    }
                }
                if (identifier.equals("Echo")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String receiverName = in.readUTF();
                    final String sender = in.readUTF();
                    out.writeUTF("Ignore");
                    out.writeUTF("Echo");
                    out.writeUTF(player);
                    out.writeUTF(receiverName);
                    out.writeUTF(sender);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
                if (identifier.equals("Bypass")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    out.writeUTF("Ignore");
                    out.writeUTF("Bypass");
                    out.writeUTF(player);
                    out.writeUTF(sender);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
            }
            if (subchannel.equals("Mute")) {
                final String identifier = in.readUTF();
                if (identifier.equals("Send")) {
                    final String server = serverName;
                    final String senderIdentifier = in.readUTF();
                    final String playerToMute = in.readUTF();
                    final String channelName = in.readUTF();
                    final long time = in.readLong();
                    final String reason = in.readUTF();
                    final UUID temporaryDataInstanceUUID = TemporaryDataInstance.createTemporaryDataInstance();
                    out.writeUTF("Mute");
                    out.writeUTF("Send");
                    out.writeUTF(server);
                    out.writeUTF(senderIdentifier);
                    out.writeUTF(temporaryDataInstanceUUID.toString());
                    out.writeUTF(playerToMute);
                    out.writeUTF(channelName);
                    out.writeLong(time);
                    out.writeUTF(reason);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            source.sendPluginMessage(send.name(), outstream.toByteArray());
                        }
                    });
                }
                if (identifier.equals("Valid")) {
                    final String server = in.readUTF();
                    final String senderIdentifier = in.readUTF();
                    final String playerToMute = in.readUTF();
                    final String channelName = in.readUTF();
                    final long time = in.readLong();
                    final String reason = in.readUTF();
                    out.writeUTF("Mute");
                    out.writeUTF("Valid");
                    out.writeUTF(senderIdentifier);
                    out.writeUTF(playerToMute);
                    out.writeUTF(channelName);
                    out.writeLong(time);
                    out.writeUTF(reason);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
                if (identifier.equals("Offline")) {
                    final String server = in.readUTF();
                    final UUID temporaryDataInstanceUUID = UUID.fromString(in.readUTF());
                    final String senderIdentifier = in.readUTF();
                    final String playerToMute = in.readUTF();
                    final TemporaryDataInstance temporaryDataInstance = TemporaryDataInstance.getTemporaryDataInstance(temporaryDataInstanceUUID);
                    temporaryDataInstance.incrementMessagePackets();
                    final AtomicInteger servers = new AtomicInteger(0);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            servers.incrementAndGet();
                        }
                    });
                    if (temporaryDataInstance.getMessagePackets() >= servers.get()) {
                        temporaryDataInstance.destroyInstance();
                        out.writeUTF("Mute");
                        out.writeUTF("Offline");
                        out.writeUTF(senderIdentifier);
                        out.writeUTF(playerToMute);
                        if (!source.getServer(server).empty()) {
                            source.sendPluginMessage(server, outstream.toByteArray());
                        }
                    }
                }
                if (identifier.equals("AlreadyMuted")) {
                    final String server = in.readUTF();
                    final String senderIdentifier = in.readUTF();
                    final String playerToMute = in.readUTF();
                    final String channelName = in.readUTF();
                    out.writeUTF("Mute");
                    out.writeUTF("AlreadyMuted");
                    out.writeUTF(senderIdentifier);
                    out.writeUTF(playerToMute);
                    out.writeUTF(channelName);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
            }
            if (subchannel.equals("Unmute")) {
                final String identifier = in.readUTF();
                if (identifier.equals("Send")) {
                    final String server = serverName;
                    final String senderIdentifier = in.readUTF();
                    final String playerToUnmute = in.readUTF();
                    final String channelName = in.readUTF();
                    final UUID temporaryDataInstanceUUID = TemporaryDataInstance.createTemporaryDataInstance();
                    out.writeUTF("Unmute");
                    out.writeUTF("Send");
                    out.writeUTF(server);
                    out.writeUTF(senderIdentifier);
                    out.writeUTF(temporaryDataInstanceUUID.toString());
                    out.writeUTF(playerToUnmute);
                    out.writeUTF(channelName);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            source.sendPluginMessage(send.name(), outstream.toByteArray());
                        }
                    });
                }
                if (identifier.equals("Valid")) {
                    final String server = in.readUTF();
                    final String senderIdentifier = in.readUTF();
                    final String playerToUnmute = in.readUTF();
                    final String channelName = in.readUTF();
                    out.writeUTF("Unmute");
                    out.writeUTF("Valid");
                    out.writeUTF(senderIdentifier);
                    out.writeUTF(playerToUnmute);
                    out.writeUTF(channelName);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
                if (identifier.equals("Offline")) {
                    final String server = in.readUTF();
                    final UUID temporaryDataInstanceUUID = UUID.fromString(in.readUTF());
                    final String senderIdentifier = in.readUTF();
                    final String playerToUnmute = in.readUTF();
                    final TemporaryDataInstance temporaryDataInstance = TemporaryDataInstance.getTemporaryDataInstance(temporaryDataInstanceUUID);
                    temporaryDataInstance.incrementMessagePackets();
                    final AtomicInteger servers = new AtomicInteger(0);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            servers.incrementAndGet();
                        }
                    });
                    if (temporaryDataInstance.getMessagePackets() >= servers.get()) {
                        temporaryDataInstance.destroyInstance();
                        out.writeUTF("Unmute");
                        out.writeUTF("Offline");
                        out.writeUTF(senderIdentifier);
                        out.writeUTF(playerToUnmute);
                        if (!source.getServer(server).empty()) {
                            source.sendPluginMessage(server, outstream.toByteArray());
                        }
                    }
                }
                if (identifier.equals("NotMuted")) {
                    final String server = in.readUTF();
                    final String senderIdentifier = in.readUTF();
                    final String playerToUnmute = in.readUTF();
                    final String channelName = in.readUTF();
                    out.writeUTF("Unmute");
                    out.writeUTF("NotMuted");
                    out.writeUTF(senderIdentifier);
                    out.writeUTF(playerToUnmute);
                    out.writeUTF(channelName);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
            }
            if (subchannel.equals("Message")) {
                final String identifier = in.readUTF();
                if (identifier.equals("Send")) {
                    final String server = serverName;
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    final String sName = in.readUTF();
                    final String send = in.readUTF();
                    final String echo = in.readUTF();
                    final String spy = in.readUTF();
                    final String msg = in.readUTF();
                    final SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(sender));
                    if (smcp == null) {
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c Synchronized player instance is null!  This shouldn't be!");
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c You probably have an issue with your player data saving and/or your login data sync!");
                        return;
                    }
                    smcp.clearMessagePackets();
                    out.writeUTF("Message");
                    out.writeUTF("Send");
                    out.writeUTF(server);
                    out.writeUTF(player);
                    out.writeUTF(sender);
                    out.writeUTF(sName);
                    out.writeUTF(send);
                    out.writeUTF(echo);
                    out.writeUTF(spy);
                    out.writeUTF(msg);
                    source.getServers().forEach(serv -> {
                        if (!serv.empty()) {
                            source.sendPluginMessage(serv.name(), outstream.toByteArray());
                        }
                    });
                }
                if (identifier.equals("Offline")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    final SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(sender));
                    if (smcp == null) {
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c Synchronized player instance is null!  This shouldn't be!");
                        source.sendConsoleMessage("&8[&eVentureChat&8]&c You probably have an issue with your player data saving and/or your login data sync!");
                        return;
                    }
                    smcp.incrementMessagePackets();
                    final AtomicInteger servers = new AtomicInteger(0);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            servers.incrementAndGet();
                        }
                    });
                    if (smcp.getMessagePackets() >= servers.get()) {
                        smcp.clearMessagePackets();
                        out.writeUTF("Message");
                        out.writeUTF("Offline");
                        out.writeUTF(player);
                        out.writeUTF(sender);
                        if (!source.getServer(server).empty()) {
                            source.sendPluginMessage(server, outstream.toByteArray());
                        }
                    }
                }
                if (identifier.equals("Ignore")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    out.writeUTF("Message");
                    out.writeUTF("Ignore");
                    out.writeUTF(player);
                    out.writeUTF(sender);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
                if (identifier.equals("Blocked")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String sender = in.readUTF();
                    out.writeUTF("Message");
                    out.writeUTF("Blocked");
                    out.writeUTF(player);
                    out.writeUTF(sender);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
                if (identifier.equals("Echo")) {
                    final String server = in.readUTF();
                    final String player = in.readUTF();
                    final String receiverUUID = in.readUTF();
                    final String sender = in.readUTF();
                    final String sName = in.readUTF();
                    final String echo = in.readUTF();
                    final String spy = in.readUTF();
                    out.writeUTF("Message");
                    out.writeUTF("Echo");
                    out.writeUTF(player);
                    out.writeUTF(receiverUUID);
                    out.writeUTF(sender);
                    out.writeUTF(echo);
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                    outstream.reset();
                    out = new DataOutputStream(outstream);
                    out.writeUTF("Message");
                    out.writeUTF("Spy");
                    out.writeUTF(player);
                    out.writeUTF(sName);
                    out.writeUTF(spy);
                    source.getServers().forEach(send -> {
                        if (!send.empty()) {
                            source.sendPluginMessage(send.name(), outstream.toByteArray());
                        }
                    });
                }
            }
            if (subchannel.equals("Sync")) {
                //System.out.println("Sync received...");
                final String identifier = in.readUTF();
                if (identifier.equals("Receive")) {
                    //System.out.println("Sending update...");
                    final String server = serverName;
                    final UUID uuid = UUID.fromString(in.readUTF());
                    SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(uuid);
                    if (smcp == null) {
                        smcp = new SynchronizedMineverseChatPlayer(uuid);
                        MineverseChatAPI.addSynchronizedMineverseChatPlayerToMap(smcp);
                    }
                    out.writeUTF("Sync");
                    out.writeUTF(uuid.toString());
                    final int channelCount = smcp.getListening().size();
                    //System.out.println(channelCount);
                    out.write(channelCount);
                    for (final String channel : smcp.getListening()) {
                        out.writeUTF(channel);
                    }
                    final int muteCount = smcp.getMutes().size();
                    //System.out.println(muteCount);
                    out.write(muteCount);
                    for (final MuteContainer muteContainer : smcp.getMutes()) {
                        out.writeUTF(muteContainer.getChannel());
                        out.writeLong(muteContainer.getDuration());
                        out.writeUTF(muteContainer.getReason());
                    }
                    //System.out.println(smcp.isSpy() + " spy value");
                    //System.out.println(out.size() + " size before");
                    out.writeBoolean(smcp.isSpy());
                    out.writeBoolean(smcp.getMessageToggle());
                    //System.out.println(out.size() + " size after");
                    final int ignoreCount = smcp.getIgnores().size();
                    //System.out.println(ignoreCount + " ignore size");
                    out.write(ignoreCount);
                    for (final UUID ignore : smcp.getIgnores()) {
                        out.writeUTF(ignore.toString());
                    }
                    if (!source.getServer(server).empty()) {
                        source.sendPluginMessage(server, outstream.toByteArray());
                    }
                }
                if (identifier.equals("Update")) {
                    final UUID uuid = UUID.fromString(in.readUTF());
                    SynchronizedMineverseChatPlayer smcp = MineverseChatAPI.getSynchronizedMineverseChatPlayer(uuid);
                    if (smcp == null) {
                        smcp = new SynchronizedMineverseChatPlayer(uuid);
                        MineverseChatAPI.addSynchronizedMineverseChatPlayerToMap(smcp);
                    }
                    smcp.getListening().clear();
                    smcp.clearMutes();
                    smcp.getIgnores().clear();
                    final int sizeL = in.read();
                    //System.out.println(sizeL + " listening");
                    for (int a = 0; a < sizeL; a++) {
                        smcp.addListening(in.readUTF());
                    }
                    final int sizeM = in.read();
                    for (int b = 0; b < sizeM; b++) {
                        final String mute = in.readUTF();
                        final long muteTime = in.readLong();
                        final String muteReason = in.readUTF();
                        //System.out.println(mute);
                        smcp.addMute(mute, muteTime, muteReason);
                    }
                    final int sizeI = in.read();
                    for (int c = 0; c < sizeI; c++) {
                        final String ignore = in.readUTF();
                        //System.out.println(mute);
                        smcp.addIgnore(MineverseChatAPI.getSynchronizedMineverseChatPlayer(UUID.fromString(ignore)));
                    }
                    smcp.setSpy(in.readBoolean());
                    smcp.setMessageToggle(in.readBoolean());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
