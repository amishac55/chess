package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import requests.CreateGameRequest;
import requests.JoinGameRequest;
import requests.LoginRequest;
import requests.RegisterRequest;
import responses.CreateGameResponse;
import responses.GetGameResponse;
import responses.ListGamesResponse;
import server.Server;
import utils.PlayerColor;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static final ServerFacade serverFacade = ServerFacade.getInstance();

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void setup() {
        // Clear database before each test
        try {
            serverFacade.executeRequest("/db", ServerFacade.HttpMethod.DELETE, null, null);
        } catch (ResponseException e) {
            fail("Failed to clear database: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void createGame_success() throws ResponseException {
        registerTestUser();
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResponse response = serverFacade.createGame(request);

        assertNotNull(response);
        assertNotNull(response.gameID());
        assertTrue(response.gameID() > 0);
    }

    @Test
    public void joinGame_success() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();
        JoinGameRequest request = new JoinGameRequest(PlayerColor.WHITE, gameId);

        Boolean result = serverFacade.joinGame(request);
        assertTrue(result);
    }

    @Test
    public void observeGame_success() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();

        Boolean result = serverFacade.observeGame(gameId);
        assertTrue(result);
    }

    @Test
    public void listGames_success() throws ResponseException {
        registerTestUser();
        createTestGame();
        createTestGame();
        ListGamesResponse response = serverFacade.listGames();

        assertNotNull(response);
        assertEquals(2, response.games().size());
    }

    @Test
    public void getGame_success() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();
        GetGameResponse response = serverFacade.getGame(gameId);

        assertNotNull(response);
        assertNotNull(response.gameData());
        assertEquals(gameId, response.gameData().getGameID());
    }

    @Test
    public void login_success() throws ResponseException {
        // Setup
        RegisterRequest registerRequest = new RegisterRequest("testUser", "password", "test@email.com");
        serverFacade.register(registerRequest);
        serverFacade.logout(); // Clear auth token
        LoginRequest loginRequest = new LoginRequest("testUser", "password");

        // Execute
        serverFacade.login(loginRequest);

        // Verify
        assertNotNull(serverFacade.getAuthToken());
    }

    @Test
    public void register_success() throws ResponseException {
        // Setup
        RegisterRequest request = new RegisterRequest("newUser", "password", "new@email.com");

        // Execute
        serverFacade.register(request);

        // Verify
        assertNotNull(serverFacade.getAuthToken());
    }

    @Test
    public void logout_success() throws ResponseException {
        // Setup
        registerTestUser();
        assertNotNull(serverFacade.getAuthToken());

        // Execute
        serverFacade.logout();

        // Verify
        assertNull(serverFacade.getAuthToken());
    }

    // Negative Tests
    @Test
    public void createGame_unauthorized() {
        // Setup - don't login
        CreateGameRequest request = new CreateGameRequest("Test Game");

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.createGame(request));
    }

    @Test
    public void joinGame_invalidGame() throws ResponseException {
        // Setup
        registerTestUser();
        JoinGameRequest request = new JoinGameRequest(PlayerColor.WHITE, 999);

        // Execute
        Boolean result = serverFacade.joinGame(request);

        // Verify
        assertFalse(result);
    }

    @Test
    public void observeGame_invalidGame() throws ResponseException {
        // Setup
        registerTestUser();

        // Execute
        Boolean result = serverFacade.observeGame(999);

        // Verify
        assertFalse(result);
    }

    @Test
    public void getGame_invalidGame() throws ResponseException {
        // Setup
        registerTestUser();

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.getGame(999));
    }

    @Test
    public void login_invalidCredentials() {
        // Setup
        LoginRequest request = new LoginRequest("wrongUser", "wrongPass");

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.login(request));
    }

    @Test
    public void register_duplicateUser() throws ResponseException {
        // Setup
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@email.com");
        serverFacade.register(request);
        serverFacade.logout();

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.register(request));
    }

    @Test
    public void logout_unauthorized() {
        // Setup - don't login

        // Execute & Verify
        assertThrows(ResponseException.class, () -> serverFacade.logout());
    }

    // Helper Methods
    private void registerTestUser() throws ResponseException {
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@email.com");
        serverFacade.register(request);
    }

    private Integer createTestGame() throws ResponseException {
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResponse response = serverFacade.createGame(request);
        return response.gameID();
    }
}
