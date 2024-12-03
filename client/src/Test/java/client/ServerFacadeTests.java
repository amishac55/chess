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

import static client.TestUtils.createTestGame;
import static client.TestUtils.registerTestUser;
import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static final ServerFacade SERVER_FACADE = ServerFacade.getInstance();

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
            SERVER_FACADE.executeRequest("/db", ServerFacade.HttpMethod.DELETE, null, null);
        } catch (ResponseException e) {
            fail("Failed to clear database: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void createGameSuccess() throws ResponseException {
        registerTestUser();
        CreateGameRequest request = new CreateGameRequest("Test Game");
        CreateGameResponse response = SERVER_FACADE.createGame(request);

        assertNotNull(response);
        assertNotNull(response.gameID());
        assertTrue(response.gameID() > 0);
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();
        JoinGameRequest request = new JoinGameRequest(PlayerColor.WHITE, gameId);

        Boolean result = SERVER_FACADE.joinGame(request);
        assertTrue(result);
    }

    @Test
    public void observeGameSuccess() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();

        Boolean result = SERVER_FACADE.observeGame(gameId);
        assertTrue(result);
    }

    @Test
    public void listGamesSuccess() throws ResponseException {
        registerTestUser();
        createTestGame();
        createTestGame();
        ListGamesResponse response = SERVER_FACADE.listGames();

        assertNotNull(response);
        assertEquals(2, response.games().size());
    }

    @Test
    public void getGameSuccess() throws ResponseException {
        registerTestUser();
        Integer gameId = createTestGame();
        GetGameResponse response = SERVER_FACADE.getGame(gameId);

        assertNotNull(response);
        assertNotNull(response.gameData());
        assertEquals(gameId, response.gameData().getGameID());
    }

    @Test
    public void loginSuccess() throws ResponseException {
        // Setup
        RegisterRequest registerRequest = new RegisterRequest("testUser", "password", "test@email.com");
        SERVER_FACADE.register(registerRequest);
        SERVER_FACADE.logout(); // Clear auth token
        LoginRequest loginRequest = new LoginRequest("testUser", "password");

        // Execute
        SERVER_FACADE.login(loginRequest);

        // Verify
        assertNotNull(SERVER_FACADE.getAuthToken());
    }

    @Test
    public void registerSuccess() throws ResponseException {
        // Setup
        RegisterRequest request = new RegisterRequest("newUser", "password", "new@email.com");

        // Execute
        SERVER_FACADE.register(request);

        // Verify
        assertNotNull(SERVER_FACADE.getAuthToken());
    }

    @Test
    public void logoutSuccess() throws ResponseException {
        // Setup
        registerTestUser();
        assertNotNull(SERVER_FACADE.getAuthToken());

        // Execute
        SERVER_FACADE.logout();

        // Verify
        assertNull(SERVER_FACADE.getAuthToken());
    }

    // Negative Tests
    @Test
    public void createGameUnauthorized() {
        // Setup - don't login
        CreateGameRequest request = new CreateGameRequest("Test Game");

        // Execute & Verify
        assertThrows(ResponseException.class, () -> SERVER_FACADE.createGame(request));
    }

    @Test
    public void joinGameInvalidGame() throws ResponseException {
        // Setup
        registerTestUser();
        JoinGameRequest request = new JoinGameRequest(PlayerColor.WHITE, 999);

        // Execute
        Boolean result = SERVER_FACADE.joinGame(request);

        // Verify
        assertFalse(result);
    }

    @Test
    public void observeGameInvalidGame() throws ResponseException {
        // Setup
        registerTestUser();

        // Execute
        Boolean result = SERVER_FACADE.observeGame(999);

        // Verify
        assertFalse(result);
    }

    @Test
    public void getGameInvalidGame() throws ResponseException {
        // Setup
        registerTestUser();

        // Execute & Verify
        assertThrows(ResponseException.class, () -> SERVER_FACADE.getGame(999));
    }

    @Test
    public void loginInvalidCredentials() {
        // Setup
        LoginRequest request = new LoginRequest("wrongUser", "wrongPass");

        // Execute & Verify
        assertThrows(ResponseException.class, () -> SERVER_FACADE.login(request));
    }

    @Test
    public void registerDuplicateUser() throws ResponseException {
        // Setup
        RegisterRequest request = new RegisterRequest("testUser", "password", "test@email.com");
        SERVER_FACADE.register(request);
        SERVER_FACADE.logout();

        // Execute & Verify
        assertThrows(ResponseException.class, () -> SERVER_FACADE.register(request));
    }

    @Test
    public void logoutUnauthorized() {
        // Setup - don't login

        // Execute & Verify
        assertThrows(ResponseException.class, () -> SERVER_FACADE.logout());
    }
}
