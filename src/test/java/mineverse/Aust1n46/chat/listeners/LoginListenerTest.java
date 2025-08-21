package mineverse.Aust1n46.chat.listeners;

import mineverse.Aust1n46.chat.MineverseChat;
import mineverse.Aust1n46.chat.api.MineverseChatAPI;
import mineverse.Aust1n46.chat.api.MineverseChatPlayer;
import mineverse.Aust1n46.chat.database.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class LoginListenerTest {
    private static ServerMock server;

    private static MockedStatic<MineverseChat> mockedMineverseChat;
    private static MockedStatic<PlayerData> mockedPlayerData;
    private static MockedStatic<MineverseChatAPI> mockedMineverseChatAPI;

    private MineverseChatPlayer mockMCP;
    private Player mockPlayer;
    private LoginListener testLoginListener;
    private PlayerQuitEvent mockPlayerQuitEvent;

    @BeforeClass
    public static void init() {
        server = MockBukkit.mock();

        final MineverseChat plugin = MockBukkit.load(MineverseChat.class);

        mockedMineverseChat = Mockito.mockStatic(MineverseChat.class);
        mockedMineverseChat.when(MineverseChat::getInstance).thenReturn(plugin);
        mockedPlayerData = Mockito.mockStatic(PlayerData.class);
        mockedMineverseChatAPI = Mockito.mockStatic(MineverseChatAPI.class);
    }

    @AfterClass
    public static void close() {
        if (mockedMineverseChat != null) mockedMineverseChat.close();
        if (mockedPlayerData != null) mockedPlayerData.close();
        if (mockedMineverseChatAPI != null) mockedMineverseChatAPI.close();
        MockBukkit.unmock();
    }

    @Before
    public void setUp() {
        mockPlayer = server.addPlayer("NewName");

        mockMCP = Mockito.mock(MineverseChatPlayer.class);
        mockPlayerQuitEvent = Mockito.mock(PlayerQuitEvent.class);
        Mockito.when(mockPlayerQuitEvent.getPlayer()).thenReturn(mockPlayer);

        Mockito.when(MineverseChatAPI.getMineverseChatPlayer(Mockito.any(Player.class))).thenReturn(mockMCP);
        Mockito.when(MineverseChatAPI.getOnlineMineverseChatPlayer(Mockito.any(Player.class))).thenReturn(mockMCP);

        testLoginListener = new LoginListener();
    }


    @After
    public void tearDown() {
        // nada crítico aqui
    }

    @Test
    public void testLoginWithNameChange() {
        Mockito.when(mockMCP.getName()).thenReturn("OldName");
        testLoginListener.handleNameChange(mockMCP, mockPlayer);
        Mockito.verify(mockMCP).setName("NewName");
    }

    @Test
    public void testPlayerQuit() {
        testLoginListener.onPlayerQuit(mockPlayerQuitEvent);
        Mockito.verify(mockMCP, Mockito.times(1)).clearMessages();
        Mockito.verify(mockMCP, Mockito.times(1)).setOnline(false);
    }
}
